package com.kanawish.sample.mvi.intent

import com.kanawish.sample.mvi.model.FilterType
import com.kanawish.sample.mvi.model.Task
import com.kanawish.sample.mvi.model.TasksModelState
import com.kanawish.sample.mvi.util.copyAdd
import com.kanawish.sample.mvi.view.tasks.TasksViewEvent
import io.reactivex.Observable

fun Observable<TasksViewEvent>.toIntent(): Observable<Intent<TasksModelState>> {
    return map { event ->
        when (event) {
            TasksViewEvent.ClearCompletedClick -> blockIntent<TasksModelState> {
                copy( tasks = tasks.filter { !it.completed }.toList() )
            }
            TasksViewEvent.FilterTypeClick -> blockIntent {
                copy( filter = when(filter) {
                    FilterType.ANY -> FilterType.ACTIVE
                    FilterType.ACTIVE -> FilterType.COMPLETE
                    FilterType.COMPLETE -> FilterType.ANY
                })
            }
            TasksViewEvent.RefreshTasksSwipe,
            TasksViewEvent.RefreshTasksClick -> blockIntent {
                this // TODO: Implement network fetches
            }
            is TasksViewEvent.CompleteTaskClick -> blockIntent {
                tasks.toMutableList().let {
                    it[tasks.indexOf(event.task)] = event.task.copy(completed = event.checked)
                    copy(tasks = it)
                }
            }
        }
    }
}

fun buildAddTaskIntent(task:Task) : Intent<TasksModelState> {
    return blockIntent {
        copy(tasks = tasks.copyAdd(task))
    }
}

