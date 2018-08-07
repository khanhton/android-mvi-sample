package com.kanawish.sample.mvi.util

import io.reactivex.Maybe
import io.reactivex.Observable

fun <T, R> Observable<T>.firstOptional(mapper: (T) -> R?): Maybe<R> {
    return firstElement().flatMap { t ->
        mapper(t)?.let { r -> Maybe.just(r) } ?: Maybe.empty()
    }
}