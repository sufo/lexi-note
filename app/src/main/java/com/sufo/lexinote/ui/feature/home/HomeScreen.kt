package com.sufo.lexinote.ui.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.sufo.lexinote.R
import com.sufo.lexinote.data.local.db.entity.Notebook
import com.sufo.lexinote.data.repo.NotebookWithStats
import com.sufo.lexinote.ui.icons.CustomIcons
import com.sufo.lexinote.ui.navigation.NavigationService
import com.sufo.lexinote.ui.navigation.Screen
import com.sufo.lexinote.ui.theme.LexiNoteTheme
import com.sufo.lexinote.utils.Serialization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    nav: NavigationService,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val notebooksWithStats by viewModel.notebooksWithStats.collectAsState(initial = emptyList())
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val selectedNotebook by viewModel.selectedNotebook.collectAsState()
    val showAddEditDialog by viewModel.showAddEditDialog.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                        .height(48.dp)
                        .background(
                            Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { nav.navigate(Screen.SearchWord.route) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Search dictionary or my words...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(notebooksWithStats, key = { it.notebook.id as Any }) { item ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    NotebookCard(
                        notebookWithStats = item,
                        onCardClick = { viewModel.toWordList(item.notebook.id!!, item.notebook.name) },
                        onReviewClick = { viewModel.onStartReviewNotebookClicked(item.notebook.id!!) },
                        onMoreOptionsClick = { viewModel.onShowBottomSheet(item.notebook) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { nav.navigate(Screen.AddNotebook.route) },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Notebook",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onHideBottomSheet() },
            sheetState = rememberModalBottomSheetState()
        ) {
            selectedNotebook?.let { notebook ->
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Text(
                        text = notebook.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.edit_details)) },
                        leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
//                        modifier = Modifier.clickable { viewModel.onShowAddEditDialog(notebook) }
                        modifier = Modifier.clickable{
                            val notebookJson = Serialization.encodeToString(Notebook.serializer(),notebook)
                            nav.navigate("${Screen.WordList.route}/${notebookJson}")
                        },
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.del_notebook), color = MaterialTheme.colorScheme.error) },
                        leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        modifier = Modifier.clickable { viewModel.onShowDeleteDialog() }
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        selectedNotebook?.let { notebook ->
            AlertDialog(
                onDismissRequest = { viewModel.onHideDeleteDialog() },
                icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                title = { Text(stringResource(R.string.del_notebook)+"?") },
                text = { Text(stringResource(R.string.del_notebook_tip,notebook.name)) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteSelectedNotebook() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.del_notebook))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onHideDeleteDialog() }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }

    if (showAddEditDialog) {
        AddEditNotebookDialog(
            notebook = selectedNotebook,
            onDismiss = { viewModel.onHideAddEditDialog() },
            onConfirm = { name, iconResId, description ->
                if (selectedNotebook == null) {
                    viewModel.createNotebook(name, iconResId, description)
                } else {
                    viewModel.updateNotebook(name, iconResId, description)
                }
            }
        )
    }
}

@Composable
fun NotebookCard(
    notebookWithStats: NotebookWithStats,
    onCardClick: () -> Unit,
    onReviewClick: () -> Unit,
    onMoreOptionsClick: (Notebook) -> Unit
) {
    val context = LocalContext.current
    val notebook = notebookWithStats.notebook
    val iconResId = remember(notebook.iconResName) {
        context.resources.getIdentifier(notebook.iconResName, "drawable", context.packageName)
    }

    Card(
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, color = MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            // Upper part of the card - clickable to navigate to detail
            Column(
                modifier = Modifier
                    .clickable(onClick = onCardClick)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (iconResId != 0) {
                                Icon(
                                    painter = painterResource(id = iconResId),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(notebook.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                stringResource(R.string.word_phrase_number, "${notebookWithStats.masteredWordCount} / ${notebookWithStats.totalWordCount}"),
//                                "${notebookWithStats.masteredWordCount} / ${notebookWithStats.totalWordCount} words mastered",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = { onMoreOptionsClick(notebook) }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (notebook.wordCount > 0) {
                    val progress = notebook.masteredWordCount.toFloat() / notebook.wordCount.toFloat()
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Lower part of the card - for stats and actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stats Section
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    StatItem(count = notebookWithStats.totalWordCount, label = stringResource(R.string.total_of_word))
                    StatItem(count = notebookWithStats.dueForReviewCount, label = stringResource(R.string.to_be_reviewed), highlight = true)
                }

                // Action Button
                if (notebookWithStats.isReviewable) {
                    Button(
                        onClick = onReviewClick,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.review), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {},
                        shape = CircleShape,
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.done), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(count: Int, label: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AddEditNotebookDialog(
    notebook: Notebook?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, iconResId: Int, description: String?) -> Unit
) {
    var name by remember { mutableStateOf(notebook?.name ?: "") }
    var description by remember { mutableStateOf(notebook?.description ?: "") }
    val context = LocalContext.current
    val initialIconResId = remember(notebook) {
        notebook?.iconResName?.let {
            context.resources.getIdentifier(it, "drawable", context.packageName)
        } ?: R.drawable.ph_book_open
    }
    var selectedIconResId by remember { mutableStateOf(initialIconResId) }

    val availableIcons = remember {
        CustomIcons.all
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (notebook == null) "New Notebook" else "Edit Notebook",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.notebook_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.desc_optional)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))

                Text(stringResource(R.string.choose_icon), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 56.dp),
                    modifier = Modifier.height(130.dp) // Adjust height as needed
                ) {
                    items(availableIcons) { iconRes ->
                        val isSelected = iconRes == selectedIconResId
                        IconButton(
                            onClick = { selectedIconResId = iconRes },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                                .border(2.dp, if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape)
                        ) {
                            Icon(painterResource(id = iconRes), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(onClick = {
                        onConfirm(name, selectedIconResId, description.ifBlank { null })
                    }) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    LexiNoteTheme {
        HomeScreen(nav = NavigationService())
    }
}