
package com.sufo.lexinote.ui.feature.dictionary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sufo.lexinote.data.local.db.entity.DictWord
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import com.sufo.lexinote.data.local.db.entity.SearchHistory

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Automatically request focus and show keyboard when the screen is first composed.
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header Row with Back Button and Search Field
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                focusRequester.freeFocus()
                keyboardController?.hide()
                viewModel.popBackStack()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
            }
//            Spacer(modifier = Modifier.width(4.dp))
            TextField(
                state = viewModel.queryState,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .height(48.dp),
                placeholder = { Text("Enter a word") },
                trailingIcon = {
                    if (viewModel.queryState.text.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearQuery() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                lineLimits = TextFieldLineLimits.SingleLine,
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                onKeyboardAction = KeyboardActionHandler {
                    val keyword = viewModel.queryState.text
                    if (!keyword.isBlank()) {
                        viewModel.toWordDetail(keyword.toString())
                        keyboardController?.hide()
                    }
                },
                contentPadding = PaddingValues(horizontal = 16.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    //去掉底部边框
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                )
            )

//            OutlinedTextField(
//                state = viewModel.queryState,
//                modifier = Modifier
//                    .weight(1f)
//                    .focusRequester(focusRequester),
//                placeholder = { Text("Enter a word") },
//                trailingIcon = {
//                    if (viewModel.queryState.text.isNotEmpty()) {
//                        IconButton(onClick = { viewModel.clearQuery() }) {
//                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
//                        }
//                    }
//                },
//                lineLimits = TextFieldLineLimits.SingleLine,
//                shape = CircleShape,
//                keyboardOptions = KeyboardOptions.Default.copy(
//                    imeAction = ImeAction.Search
//                ),
//                onKeyboardAction = KeyboardActionHandler {
//                    viewModel.searchWord()
//                    keyboardController?.hide()
//                },
//                colors = OutlinedTextFieldDefaults.colors(
//                    unfocusedBorderColor = Color.LightGray,
//                    focusedBorderColor = MaterialTheme.colorScheme.primary
//                )
//            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content Box
        Box(modifier = Modifier.fillMaxSize()) {
            // This condition decides what to show: History, Suggestions, or the final Result.
            when {
                // 1. Show History: When the query is empty and there's no result yet.
                viewModel.queryState.text.isBlank() -> {
                    if (uiState.history.isNotEmpty()) {
                        SearchHistoryList(
                            history = uiState.history,
                            viewModel = viewModel,
                            itemClick = {
                                keyboardController?.hide()
                                viewModel.toWordDetail(it)
                            },
                            clearHistory = {
                                viewModel.clearHistory()
                            }
                        )
                    }
                }

                // 2. Show Suggestions: When the user is typing and there's no final result.
                uiState.suggestions.isNotEmpty() -> {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(uiState.suggestions) { suggestion ->
                            SuggestionItem(suggestion = suggestion, onClick = {
                                keyboardController?.hide()
                                viewModel.toWordDetail(suggestion.word)
                            })
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun SearchHistoryList(history: List<SearchHistory>, viewModel: SearchViewModel,
                      itemClick: (word:String) -> Unit,
                      clearHistory: () -> Unit

) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Search History", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline
                )
//                TextButton(onClick = { viewModel.clearHistory() }) {
//                    Text("Clear")
//                }
                Icon(Icons.Outlined.DeleteOutline,contentDescription="clear history",
                    modifier = Modifier.size(14.dp)
                        .clickable{clearHistory()},
                    tint = MaterialTheme.colorScheme.outline
                    )
            }
        }
        items(history) { item ->
            Column (
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .clickable {itemClick(item.word)}
            ){
                Text(
                    text = item.word,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = item.transition?:"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

        }
    }
}


@Composable
fun SuggestionItem(suggestion: DictWord, onClick: () -> Unit) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 8.dp),
    ) {
        Text(text = suggestion.word, style = MaterialTheme.typography.bodyLarge)
//        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = suggestion.translation.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(Modifier.fillMaxWidth(), 0.5.dp, MaterialTheme.colorScheme.surface)
    }
}

@Composable
fun SearchResultCard(
    wordEntry: DictWord,
    onAddToNotebook: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = wordEntry.word, style = MaterialTheme.typography.headlineMedium)
                wordEntry.phonetic.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = it!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = wordEntry.translation?:"", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = onAddToNotebook,
                modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add to Notebook", modifier = Modifier.size(32.dp))
            }
        }
    }
}
