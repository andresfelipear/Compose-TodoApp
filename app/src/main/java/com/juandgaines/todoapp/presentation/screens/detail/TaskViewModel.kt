package com.juandgaines.todoapp.presentation.screens.detail

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.juandgaines.todoapp.domain.Task
import com.juandgaines.todoapp.domain.TaskLocalDataSource
import com.juandgaines.todoapp.navigation.TaskScreenDestination
import com.juandgaines.todoapp.presentation.screens.detail.ActionTask.ChangeTaskCategory
import com.juandgaines.todoapp.presentation.screens.detail.ActionTask.ChangeTaskDone
import com.juandgaines.todoapp.presentation.screens.detail.ActionTask.SaveTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
        private val savedStateHandle: SavedStateHandle,
        private val taskLocalDataSource: TaskLocalDataSource
): ViewModel()
{
    var state by mutableStateOf(TaskScreenState())
        private set

    private val eventsChannel = Channel<TaskEvent>()
    val events = eventsChannel.receiveAsFlow()

    private val canSaveTask = snapshotFlow { state.taskName.text.toString() }
    private val taskData = savedStateHandle.toRoute<TaskScreenDestination>()

    private var editedTask: Task? = null
    init{

        taskData.taskId?.let{
            viewModelScope.launch {
                val task = taskLocalDataSource.getTaskById(it)
                editedTask = task

                state = state.copy(
                    taskName = TextFieldState(task?.title?:""),
                    taskDescription = TextFieldState(task?.description?:""),
                    isTaskDone = task?.isCompleted?:false,
                    category = task?.category
                )
            }
        }
        canSaveTask.onEach{
            state = state.copy(canSaveTask = it.isNotBlank())
        }.launchIn(viewModelScope)
    }

    fun onAction(actionTask: ActionTask){
        viewModelScope.launch {
            when(actionTask){
                is ChangeTaskCategory -> {
                    state = state.copy(category = actionTask.category)
                }
                is ChangeTaskDone -> {
                    state = state.copy(isTaskDone = actionTask.isTaskDone)
                    print(state.isTaskDone)
                }
                SaveTask -> {
                    editedTask?.let{
                        taskLocalDataSource.updateTask(
                            task = it.copy(
                                id = it.id,
                                title = state.taskName.text.toString(),
                                description = state.taskDescription.text.toString(),
                                category = state.category,
                                isCompleted = state.isTaskDone
                            )
                        )
                    }?:run{
                        val task = Task(
                            id = UUID.randomUUID().toString(),
                            title = state.taskName.text.toString(),
                            description = state.taskDescription.text.toString(),
                            category = state.category,
                            isCompleted = state.isTaskDone
                        )

                        taskLocalDataSource.addTask(task)
                    }
                    eventsChannel.send(TaskEvent.TaskCreated)
                }
                else -> Unit
            }
        }
    }
}