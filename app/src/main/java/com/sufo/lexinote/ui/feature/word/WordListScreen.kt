package com.sufo.lexinote.ui.feature.word

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sufo.lexinote.R
import com.sufo.lexinote.constants.HEADER_HEIGHT
import com.sufo.lexinote.data.local.db.entity.Word
import com.sufo.lexinote.ui.components.PageHeader
import com.sufo.lexinote.ui.navigation.NavigationService
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun WordListScreen(
    nav: NavigationService,
    viewModel: WordListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is WordListUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is WordListUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is WordListUiState.Success -> {
            WordListContent(
                nav = nav,
                state = state,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun WordListContent(
    nav: NavigationService,
    state: WordListUiState.Success,
    viewModel: WordListViewModel
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            PageHeader(
                title = state.notebookName,
                onClose = { nav.popBackStack() },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            val sortOptions = mapOf(
                                SortOrder.A_TO_Z to "A-Z",
                                SortOrder.Z_TO_A to "Z-A",
                                SortOrder.PROFICIENCY to stringResource(R.string.familiarity),
                                SortOrder.MODIFICATION_TIME to stringResource(R.string.update_time),
                                SortOrder.STUDY_TIME to stringResource(R.string.review_time),
                                SortOrder.RANDOM to stringResource(R.string.random)
                            )

                            sortOptions.forEach { (sortOrder, title) ->
                                DropdownMenuItem(
                                    text = { Text(title) },
                                    onClick = {
                                        viewModel.onSortOrderChanged(sortOrder)
                                        menuExpanded = false
                                    },
                                    leadingIcon = {
                                        if (state.sortOrder == sortOrder) {
                                            Icon(Icons.Default.Check, contentDescription = "Selected")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
            SearchBar(
                state = state.searchQuery,
            ) { keyboardController?.hide() }

            if (state.words.isEmpty()) {
                EmptyState(
                    modifier = Modifier.weight(1f),
                    addClick = viewModel::toWordAdd
                )
            } else {
                Spacer(Modifier.height(16.dp))
                WordList(
                    words = state.words,
                    isLoading = state.isLoadingMore,
                    endOfListReached = state.endOfListReached,
                    onLoadMore = viewModel::loadMoreWords,
                    onMastered = viewModel::onMastered,
                    onPlayAudio = viewModel::speak,
                    onDelete = viewModel::deleteWord,
                    onItemCick = {
                        keyboardController?.hide()
                        viewModel.toWordDetail(it.word)
                    }
                )
            }
        }

        if (state.words.isNotEmpty()) {
            FloatingActionButton(
                onClick = viewModel::toWordAdd,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add word", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}


@Composable
private fun WordList(
    words: List<Word>,
    isLoading: Boolean,
    endOfListReached: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    onMastered: (Word) -> Unit,
    onPlayAudio: (String) -> Unit,
    onDelete: (Word) -> Unit,
    onItemCick: (Word) -> Unit,
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
    ) {
        items(words, key = { it.id }) { word ->
            WordItem(
                word = word,
                onMastered = { onMastered(word) },
                onPlayAudio = { onPlayAudio(word.word) },
                onDelete = { onDelete(word) },
                onItemCick = { onItemCick(word) }
            )
        }

        item {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (endOfListReached && words.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "- No more words -",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    // Scroll listener
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .map { lastVisibleIndex ->
                lastVisibleIndex != null && lastVisibleIndex >= listState.layoutInfo.totalItemsCount - 5
            }
            .distinctUntilChanged()
            .filter { it }
            .collect { 
                if (!isLoading && !endOfListReached) {
                    onLoadMore()
                }
            }
    }
}

@Composable
private fun SearchBar(
    state: TextFieldState,
    onSearchAction: () -> Unit
) {
    OutlinedTextField(
        state = state,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
            .height(HEADER_HEIGHT.dp),
        lineLimits = TextFieldLineLimits.SingleLine,
        contentPadding = PaddingValues(horizontal = 16.dp),
        placeholder = { Text("Search words...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        shape = RoundedCornerShape(16.dp), keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search
        ),
        onKeyboardAction = KeyboardActionHandler {
            val keyword = state.text
            if (!keyword.isBlank()) {
                onSearchAction()
            }
        },
    )
}

@Composable
private fun WordItem(
    word: Word,
    onMastered: () -> Unit,
    onPlayAudio: () -> Unit,
    onDelete: () -> Unit,
    onItemCick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val actionWidth = 128.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val actionWidthPx = with(LocalDensity.current) { actionWidth.toPx() }
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .clipToBounds()
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            val newOffset = (offsetX.value + delta).coerceIn(-actionWidthPx, 0f)
                            offsetX.snapTo(newOffset)
                        }
                    },
                    onDragStopped = {
                        coroutineScope.launch {
                            if (offsetX.value < -actionWidthPx / 2) {
                                offsetX.animateTo(-actionWidthPx, animationSpec = tween(300))
//                                onMastered()
                            } else {
                                offsetX.animateTo(0f, animationSpec = tween(300))
                            }
                        }
                    }
                )
        ) {
            // 1. Visible Part
            Column(
                modifier = Modifier
                    .width(screenWidth)
                    .wrapContentHeight()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onPlayAudio) {
                        Icon(
                            Icons.AutoMirrored.Outlined.VolumeUp,
                            contentDescription = "Play audio"
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)
                        .clickable{onItemCick()}
                    ) {
                        Text(
                            text = word.word,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = word.translation?:"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    MasteryIndicator(repetitions = word.repetitions)
                }
                HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }

            // 2. Hidden Part (Action)
            Row(
                modifier = Modifier
                    .width(actionWidth)
                    .fillMaxHeight(),
//                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.secondary)
                        .clickable { onDelete() },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.Delete, contentDescription = "Delete",
                        Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
//                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.del),
                        color = MaterialTheme.colorScheme.onSecondary,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onMastered() }  //掌握或重学
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircleOutline, contentDescription = "mastered",
                        Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
//                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.mastered),
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun MasteryIndicator(repetitions: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        val maxRepetitions = 5
        for (i in 1..maxRepetitions) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (i <= repetitions) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }
    }
}


@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    addClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ph_text_underline),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.add_first),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.add_first_tip),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { addClick() },
            shape = MaterialTheme.shapes.extraLarge,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.add_new_word))
        }
    }
}
