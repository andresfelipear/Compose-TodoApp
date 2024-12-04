package com.juandgaines.todoapp.data

import com.juandgaines.todoapp.domain.Task
import com.juandgaines.todoapp.domain.TaskLocalDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RoomTaskLocalDataSource @Inject constructor(
    private val taskDao: TaskDao,
    private val dispatcherIO: CoroutineDispatcher
) : TaskLocalDataSource {
    override val taskFlow: Flow<List<Task>>
        get() = taskDao.getAllTasks().map{
            it.map{ taskEntity -> taskEntity.toTask()  }
        }

    override suspend fun addTask(task: Task) = withContext(dispatcherIO)
    {
        taskDao.upsertTask(TaskEntity.fromTask(task))
    }

    override suspend fun updateTask(task: Task) = withContext(dispatcherIO)
    {
        taskDao.upsertTask(TaskEntity.fromTask(task))
    }

    override suspend fun removeTask(task: Task) = withContext(dispatcherIO)
    {
        taskDao.deleteTaskById(task.id)
    }

    override suspend fun deleteAllTasks() = withContext(dispatcherIO)
    {
        taskDao.deleteAllTasks()
    }

    override suspend fun getTaskById(id: String): Task? = withContext(dispatcherIO)
    {
        taskDao.getTaskById(id)?.toTask()
    }

}