package com.sufo.lexinote.ui.feature.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.sufo.lexinote.R
import com.sufo.lexinote.ui.navigation.NavigationService
import com.sufo.lexinote.ui.theme.LexiNoteTheme

@Composable
fun ExploreScreen(
    nav: NavigationService,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedTemplate by remember { mutableStateOf<VocabularyTemplate?>(null) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(id = R.string.import_vocabulary_title)) },
            text = { Text(stringResource(id = R.string.import_vocabulary_message, selectedTemplate?.vocabulary?.name ?: "")) },
            confirmButton = {
                Button(
                    onClick = {
                        selectedTemplate?.let { viewModel.importVocabulary(it.vocabulary) }
                        showDialog = false
                    },
                ) {
                    Text(stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        AsyncImage(
            model = R.drawable.vocabulary,
            contentDescription = "Vocabulary",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .clip(RoundedCornerShape(16.dp)) // 设置圆角
        )
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (uiState.error != null) {
            Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                items(uiState.templates) { template ->
                    ExamCard(
                        templateState = template,
                        onAddClick = {
                            selectedTemplate = template
                            showDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ExamCard(
    templateState: VocabularyTemplate,
    onAddClick: () -> Unit
) {
    val template = templateState.vocabulary
    Card(
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = template.iconRes),
                    contentDescription = template.name,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(template.name, fontWeight = FontWeight.Bold)
            Text("~${template.wordCount} ${stringResource(R.string.original_text_label)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                enabled = !templateState.isAdded,
                colors = if (templateState.isAdded) {
                    ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.Gray
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Text(if (templateState.isAdded) "✓ ${stringResource(R.string.added)}" else "+ ${stringResource(R.string.add)}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExploreScreenPreview() {
    LexiNoteTheme {
        ExploreScreen(NavigationService())
    }
}
