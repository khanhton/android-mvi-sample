package com.kanawish.sample.mvi.model

import android.annotation.SuppressLint
import com.kanawish.sample.mvi.intent.blockIntent
import com.kanawish.sample.mvi.intent.editorIntent
import com.kanawish.sample.mvi.model.TaskEditorState.Deleting
import com.kanawish.sample.mvi.model.TaskEditorState.Saving
import javax.inject.Inject
import javax.inject.Singleton


@SuppressLint("CheckResult")
@Singleton class TaskEditorStore @Inject constructor(private val tasksModelStore: TasksModelStore) :
        ModelStore<TaskEditorState>(TaskEditorState.Editing(Task())) {

    init {
        // When extra processing is needed, we subscribe to our own state and react accordingly.
        this.modelState().subscribe { state ->
            when (state) {
                is Saving -> handleSaving(state)
                is Deleting -> handleDeleting(state)
            }
        }
    }

    /**
     * When we are done saving an edited task, we signal back to the Editor
     */
    private fun handleSaving(state: Saving) {
        tasksModelStore.process(blockIntent {
            tasks.toMutableList().let { newList ->

                newList.find { state.task.id == it.id }?.let {
                    newList[newList.indexOf(it)] = state.task
                } ?: newList.add(state.task)

                copy(tasks = newList)
            }
        })

        process(editorIntent<Saving> { success() })
    }

    private fun handleDeleting(state: Deleting) {
        tasksModelStore.process(blockIntent {
            copy(tasks = tasks.toMutableList().apply {
                find { it.id == state.taskId }?.also { remove(it) }
            })
        })
    }
}

/**
 * State transitions only concern state. They're not responsible for asynchronous jobs or
 * communication with external components.
 */
sealed class TaskEditorState {

    object Closed : TaskEditorState() {
        fun newTask() = Editing(Task())
        fun editTask(task: Task) = Editing(task)
    }

    data class Editing(val task: Task) : TaskEditorState() {
        fun edit(block: Task.() -> Task) = copy(task = task.block())
        fun save() = Saving(task)
        fun delete() = Deleting(task.id)
    }

    data class Saving(val task: Task) : TaskEditorState() {
        fun success() = Closed
    }

    data class Deleting(val taskId: String) : TaskEditorState() {
        fun success() = Closed
    }
}

/*
    Edit State Machine

    @startuml
    [*] --> CLOSED
    CLOSED --> EDITING : edit

    EDITING --> SAVING : save
    EDITING --> CLOSED : cancel

    SAVING --> EDITING : error
    SAVING --> CLOSED : success
    @enduml
 */
