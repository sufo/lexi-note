package com.sufo.lexinote.ui.feature.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sufo.lexinote.R
import com.sufo.lexinote.ui.navigation.NavigationService
import com.sufo.lexinote.ui.theme.LexiNoteTheme

@Composable
fun ReviewScreen (
    nav: NavigationService,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            ReviewCtaCard(
                totalReviewCount = uiState.totalReviewCount,
                onStartLearningClicked = { viewModel.onStartReviewAllClicked() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    stringResource(id = R.string.review_by_notebook_label),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
//                Card(
//                    shape = RoundedCornerShape(20.dp),
//                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        uiState.notebooks.forEachIndexed { index, notebookWithCount ->
                            ReviewNotebookItem(
                                notebookWithReviewCount = notebookWithCount,
                                onReviewClicked = { viewModel.onStartReviewNotebookClicked(notebookWithCount.notebook.id!!) }
                            )
                            if (index < uiState.notebooks.lastIndex) {
                                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
//                }
            }
        }
    }
}

@Composable
fun ReviewCtaCard(totalReviewCount: Int, onStartLearningClicked: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(id = R.string.review_streak_pep_talk), style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            Text(
                text = totalReviewCount.toString(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(stringResource(id = R.string.review_words_to_review_today), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onStartLearningClicked,
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(vertical = 16.dp),
                enabled = totalReviewCount > 0
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.review_start_learning_button), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ReviewNotebookItem(notebookWithReviewCount: NotebookWithReviewCount, onReviewClicked: () -> Unit) {
    val hasReviews = notebookWithReviewCount.reviewCount > 0
    val context = LocalContext.current
    val iconResName = notebookWithReviewCount.notebook.iconResName
    val iconResId = remember(iconResName) {
        context.resources.getIdentifier(iconResName, "drawable", context.packageName)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .let { if (!hasReviews) it.alpha(0.6f) else it },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
//                Icon(
//                    Icons.Default.PlayArrow, // Replace with actual icon later
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.size(24.dp)
//                )
                if (iconResId != 0) {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(notebookWithReviewCount.notebook.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (hasReviews) {
                    Text(
                        stringResource(id = R.string.review_item_to_review_count, notebookWithReviewCount.reviewCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFD97706), // Amber
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        stringResource(id = R.string.review_all_caught_up),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF059669), // Green
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        Button(
            onClick = onReviewClicked,
            enabled = hasReviews,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.Gray.copy(alpha = 0.5f)
            ),
            border = BorderStroke(1.dp, if (hasReviews) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.3f))
        ) {
            Text(stringResource(id = R.string.review), fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewScreenPreview() {
    LexiNoteTheme {
        // This preview will show the loading state by default.
        // To preview the loaded state, you would need to create a fake ViewModel
        // or pass a preview-specific state.
        ReviewScreen(nav = NavigationService())
    }
}
