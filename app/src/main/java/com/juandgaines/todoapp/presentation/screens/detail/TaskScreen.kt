package com.juandgaines.todoapp.presentation.screens.detail

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.juandgaines.todoapp.R
import com.juandgaines.todoapp.domain.Category
import com.juandgaines.todoapp.presentation.screens.detail.providers.TaskScreenStatePreviewProvider
import com.juandgaines.todoapp.ui.theme.TodoAppTheme

@Composable
fun TaskScreenRoot(
        navigateBack: () -> Boolean,
        viewModel: TaskViewModel
)
{
    val state = viewModel.state
    val events = viewModel.events

    val context = LocalContext.current

    LaunchedEffect(true) {
        events.collect { event ->
            when(event)
            {
                TaskEvent.TaskCreated ->
                {
                    Toast.makeText(
                        context,
                        context.getString(R.string.task_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateBack()
                }
            }
        }
    }

    TaskScreen(
        state = state,
        onActionTask = { action ->
            when(action)
            {
                is ActionTask.Back -> {
                    navigateBack()
                }
                else -> {
                    viewModel.onAction(action)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
        modifier: Modifier = Modifier,
        state: TaskScreenState,
        onActionTask: (ActionTask) -> Unit
)
{
    var isExpanded by remember { mutableStateOf(false) }
    var isDescriptionFocused by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.task),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable {
                            onActionTask(ActionTask.Back)
                        }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .imePadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.done),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(8.dp)
                )
                Checkbox(
                    checked = state.isTaskDone,
                    onCheckedChange = {
                        onActionTask(
                            ActionTask.ChangeTaskDone(
                                isTaskDone = it
                            )
                        )
                    }
                )
                Spacer(
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        isExpanded = true
                    }
                ) {
                    Text(
                        text = state.category?.toString() ?: stringResource(id = R.string.category),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(
                                    8.dp
                                )
                            )
                            .padding(8.dp)
                    )

                    Box(
                        modifier = Modifier.padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )

                        DropdownMenu(
                            expanded = isExpanded,
                            onDismissRequest = { isExpanded = false },
                            modifier = Modifier.background(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        ) {
                            Column {
                                Category.entries.forEach { category ->
                                    Text(
                                        text = category.toString(),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .clickable {
                                                isExpanded = false
                                                onActionTask(
                                                    ActionTask.ChangeTaskCategory(
                                                        category = category)
                                                )
                                            }
                                    )
                                }
                            }
                        }
                    }

                }
            }

            BasicTextField(
                state = state.taskName,
                textStyle = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                ),
                lineLimits = TextFieldLineLimits.SingleLine,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                decorator = { innerBox ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        if(state.taskName.text.toString().isEmpty())
                        {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                text = stringResource(id = R.string.task_name),
                                color = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.5f
                                ),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        else
                        {
                            innerBox()
                        }
                    }
                }
            )

            BasicTextField(
                state = state.taskDescription,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
                lineLimits = if(isDescriptionFocused){
                    TextFieldLineLimits.MultiLine(
                        minHeightInLines = 1,
                        maxHeightInLines = 5
                    )
                }else{
                    TextFieldLineLimits.Default
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .onFocusChanged {
                        isDescriptionFocused = it.isFocused
                    },
                decorator = { innerBox ->
                    Column {
                        if(state.taskDescription.text.toString().isBlank() && !isDescriptionFocused)
                        {
                            Text(
                                text = stringResource(R.string.task_description),
                                color = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.5f
                                ),
                            )
                        }
                        else
                        {
                            innerBox()
                        }
                    }
                }
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Button(
                enabled = state.canSaveTask,
                onClick = {
                    onActionTask(ActionTask.SaveTask)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(46.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.save),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@Composable
@Preview
fun TaskScreenLightPreview(
        @PreviewParameter(TaskScreenStatePreviewProvider::class) state: TaskScreenState
)
{
    TodoAppTheme {
        TaskScreen(
            state = state,
            onActionTask = {}
        )
    }
}

@Composable
@Preview(
    uiMode = UI_MODE_NIGHT_YES
)
fun TaskScreenDarkPreview(
        @PreviewParameter(TaskScreenStatePreviewProvider::class) state: TaskScreenState
)
{
    TodoAppTheme {
        TaskScreen(
            state = state,
            onActionTask = {}
        )
    }
}


