package com.sufo.lexinote.ui.feature.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sufo.lexinote.ui.components.PageHeader
import com.sufo.lexinote.ui.theme.LexiNoteTheme
import com.sufo.lexinote.ui.icons.CustomIcons
import com.sufo.lexinote.ui.navigation.NavigationService

@Composable
fun AddNotebookScreen(
    nav: NavigationService,
    viewModel: AddNotebookViewmodel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val notebook = uiState.notebook

    Column(modifier = Modifier.fillMaxSize()) {
        PageHeader(
            title = if (uiState.isEditMode) "Edit Notebook" else "New Notebook",
            onClose = { viewModel.popBackStack() },
            actions = {
                TextButton(
                    onClick = { viewModel.createOrUpdateNotebook() },
                    enabled = notebook.name.isNotBlank() && !uiState.isLoading
                ) {
                    Text(if (uiState.isEditMode) "Save" else "Create", fontWeight = FontWeight.Bold)
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoCard(title = "Notebook Name") {
                TextField(
                    value = notebook.name,
                    onValueChange = viewModel::onNameChange,
                    placeholder = { Text("e.g., Business English") },
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            InfoCard(title = "Choose an Icon") {
                IconSelector(
                    selectedIconResName = notebook.iconResName,
                    onIconSelected = viewModel::onIconChange,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            InfoCard(title = "Description (Optional)") {
                TextField(
                    value = notebook.description ?: "",
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder = { Text("Add a short description...") },
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            content()
        }
    }
}

@Composable
private fun IconSelector(
    selectedIconResName: String,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val icons = CustomIcons.all
    val selectedIconResId = remember(selectedIconResName) {
        if (selectedIconResName.isNotBlank()) {
            context.resources.getIdentifier(selectedIconResName, "drawable", context.packageName)
        } else 0
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(64.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.height(150.dp) // Adjust height as needed
    ) {
        items(icons) { iconRes ->
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickable { 
                        val resName = context.resources.getResourceEntryName(iconRes)
                        onIconSelected(resName)
                     }
                    .background(if (selectedIconResId == iconRes) MaterialTheme.colorScheme.primaryContainer else Color.LightGray.copy(alpha = 0.2f))
//                    .border(
//                        width = 2.dp,
//                        color = if (selectedIconResId == iconRes) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
//                        shape = CircleShape
//                    )
                    .padding(17.dp),
                tint = if (selectedIconResId == iconRes) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.scrim
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddNotebookScreenPreview() {
    LexiNoteTheme {
        AddNotebookScreen(NavigationService())
    }
}