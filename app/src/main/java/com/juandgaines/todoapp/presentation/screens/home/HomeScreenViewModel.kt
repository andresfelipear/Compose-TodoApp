package com.juandgaines.todoapp.presentation.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juandgaines.todoapp.domain.TaskLocalDataSource
import com.juandgaines.todoapp.presentation.screens.home.HomeScreenAction.OnDeleteAllTasks
import com.juandgaines.todoapp.presentation.screens.home.HomeScreenAction.OnDeleteTask
import com.juandgaines.todoapp.presentation.screens.home.HomeScreenAction.OnToggleTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val taskLocalDataSource: TaskLocalDataSource
): ViewModel()
{

    var state by mutableStateOf(HomeDataState())

    private val eventsChannel = Channel<HomeScreenEvent>()
    val events = eventsChannel.receiveAsFlow()

    init
    {
        state = state.copy(
            date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd yyyy")),
        )
        taskLocalDataSource.taskFlow
            .onEach {
                val completedTask = it
                    .filter { task -> task.isCompleted}
                    .sortedByDescending { task ->
                        task.date
                    }
                val pendingTask = it
                    .filter { task -> !task.isCompleted}
                    .sortedByDescending { task->
                        task.date
                    }

                state = state.copy(
                    summary = pendingTask.size.toString(),
                    completedTask = completedTask,
                    pendingTask = pendingTask
                )
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: HomeScreenAction){
        viewModelScope.launch {
            when(action)
            {
                OnDeleteAllTasks -> {
                    taskLocalDataSource.deleteAllTasks()
                    eventsChannel.send(HomeScreenEvent.DeletedAllTasks)
                }
                is OnDeleteTask -> {
                    taskLocalDataSource.removeTask(action.task)
                    eventsChannel.send(HomeScreenEvent.DeletedTask)
                }
                is OnToggleTask -> {
                    val updatedTask = action.task.copy(isCompleted = !action.task.isCompleted)
                    taskLocalDataSource.updateTask(updatedTask)
                    eventsChannel.send(HomeScreenEvent.UpdatedTask)
                }
                else -> Unit
            }
        }
    }
}