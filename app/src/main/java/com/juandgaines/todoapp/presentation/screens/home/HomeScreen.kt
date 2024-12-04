@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.juandgaines.todoapp.presentation.screens.home

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.juandgaines.todoapp.R
import com.juandgaines.todoapp.presentation.screens.home.providers.HomeScreenPreviewProvider
import com.juandgaines.todoapp.ui.theme.TodoAppTheme

@Composable
fun HomeScreenRoot(
        navigateToTaskScreen: (String?) -> Unit,
        viewModel: HomeScreenViewModel
)
{
    val state = viewModel.state
    val events = viewModel.events

    val context = LocalContext.current

    LaunchedEffect(true){
        events.collect{ event ->
            when(event){
                HomeScreenEvent.DeletedAllTasks -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.all_tasks_deleted),
                        Toast.LENGTH_SHORT
                    )
                }
                HomeScreenEvent.DeletedTask -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.task_deleted),
                        Toast.LENGTH_SHORT
                    )
                }
                HomeScreenEvent.UpdatedTask -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.task_updated),
                        Toast.LENGTH_SHORT
                    )
                }
            }
        }
    }

    HomeScreen(
        state = state,
        onAction = { action ->
            when(action)
            {
                is HomeScreenAction.OnClickedTask -> {
                    navigateToTaskScreen(action.taskId)
                }
                is HomeScreenAction.OnAddTask -> {
                    navigateToTaskScreen(null)
                }
                else -> viewModel.onAction(action)
            }
        }
    )

}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
        modifier: Modifier = Modifier,
        state: HomeDataState,
        onAction:(HomeScreenAction)->Unit,
        navigateToTaskScreen: (String?) -> Unit = {}
)
{
    var isMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier.padding(8.dp).clickable {
                            isMenuExpanded = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Add Task",
                            tint = MaterialTheme.colorScheme.onSurface
                        )

                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }
                        ){
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(id = R.string.delete_all)
                                    )
                                },
                                onClick = {
                                    isMenuExpanded = false
                                    onAction(HomeScreenAction.OnDeleteAllTasks)
                                }
                            )
                        }
                    }
                }
            )
        },
        content = { paddingValues ->

            if(state.pendingTask.isEmpty() && state.completedTask.isEmpty()){
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text = stringResource(id = R.string.no_tasks),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }else
            {
                LazyColumn(
                    modifier = Modifier.padding( paddingValues = paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ){
                    item {
                        SummaryInfo(
                            date = state.date,
                            taskSummary = state.summary,
                            completedTask = state.completedTask.size,
                            totalTask = state.completedTask.size + state.pendingTask.size
                        )
                    }

                    stickyHeader {
                        SectionTitle(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surface
                                )
                                .fillParentMaxWidth(),
                            title = stringResource(id = R.string.pending_task)
                        )
                    }

                    items(
                        state.pendingTask,
                        key = {
                                task -> task.id
                        }
                    ) { task ->
                        TaskItem(
                            modifier = Modifier.clip(
                                RoundedCornerShape(
                                    8.dp
                                )
                            ),
                            task = task,
                            onClickItem = {
                                onAction(HomeScreenAction.OnClickedTask(task.id))
                            },
                            onDeleteItem = {
                                onAction(HomeScreenAction.OnDeleteTask(task))
                            },
                            onToggleCompletion = {
                                onAction(HomeScreenAction.OnToggleTask(task))
                            }
                        )
                    }

                    stickyHeader {
                        SectionTitle(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surface
                                ),
                            title = stringResource(id = R.string.completed_task)
                        )
                    }

                    items(
                        state.completedTask,
                        key = {
                                task -> task.id
                        }
                    ) { task ->
                        TaskItem(
                            modifier = Modifier.clip(
                                RoundedCornerShape(
                                    8.dp
                                )
                            ),
                            task = task,
                            onClickItem = {
                                onAction(HomeScreenAction.OnClickedTask(task.id))
                            },
                            onDeleteItem = {
                                onAction(HomeScreenAction.OnDeleteTask(task))
                            },
                            onToggleCompletion = {
                                onAction(HomeScreenAction.OnToggleTask(task))
                            }
                        )
                    }
                }

            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onAction(
                        HomeScreenAction.OnAddTask
                    )
                },
                content = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Task",
                    )
                }
            )
        }
    )
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun HomeScreenPreviewDark(
        @PreviewParameter(HomeScreenPreviewProvider::class) state: HomeDataState)
{
    TodoAppTheme {
        HomeScreen(
            state = HomeDataState(
                date = state.date,
                summary = state.summary,
                completedTask = state.completedTask,
                pendingTask = state.pendingTask
            ),
            onAction = { }
        )
    }
}

@Preview()
@Composable
fun HomeScreenPreviewLight(
        @PreviewParameter(HomeScreenPreviewProvider::class) state: HomeDataState)
{
    TodoAppTheme {
        HomeScreen(
            state = HomeDataState(
                date = state.date,
                summary = state.summary,
                completedTask = state.completedTask,
                pendingTask = state.pendingTask
            ),
            onAction = { }
        )
    }
}
