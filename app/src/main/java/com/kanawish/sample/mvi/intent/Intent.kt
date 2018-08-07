package com.kanawish.sample.mvi.intent

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

interface Intent<S> {
    fun reducers(): Observable<Reducer<S>>
}


typealias Reducer<S> = (S) -> S

/**
 * Creates a single-reducer Intent. Reducers can return null to
 * signal an illegal operation, and we'll throw an illegal state exception.
 */
fun <S> reducerIntent(reducer: (S) -> S?): Intent<S> {
    return object : Intent<S> {
        override fun reducers(): Observable<Reducer<S>> {
            return Observable.just { old ->
                reducer(old)
                        ?: throw IllegalStateException("Reducer encountered an inconsistent State.")
            }
        }
    }
}

/**
 * Creates a single-reducer Intent from a block. The block can return null to
 * signal an illegal operation, and we'll throw an illegal state exception.
 */
fun <S> blockIntent(block: S.() -> S?): Intent<S> {
    return object : Intent<S> {
        override fun reducers(): Observable<Reducer<S>> {
            return Observable.just { old ->
                old.block()
                        ?: throw IllegalStateException("Reducer encountered an inconsistent State.")
            }
        }
    }
}

fun <S> intervalBlocksIntent(period: Long, vararg blocks: S.() -> S): Intent<S> {
    return object : Intent<S> {
        override fun reducers(): Observable<Reducer<S>> {
            return Observable.fromArray(*blocks)
                    .map { block -> { old: S -> old.block() } }
                    .zipWith(
                            Observable.interval(period, TimeUnit.SECONDS),
                            BiFunction { b, _ -> b }
                    )
        }
    }
}

/**
 * checkedIntent function creates a single-reducer intent. We use this to guard against
 * incoherent incoming ViewEvents.
 */
inline fun <reified S : T, reified T> checkedIntent(crossinline block: S.() -> T?, crossinline fallback: () -> T): Intent<T> {
    return object : Intent<T> {
        override fun reducers(): Observable<Reducer<T>> {
            return Observable.just { old ->
                (old as? S)?.block()
                        ?: fallback()
            }
        }
    }
}