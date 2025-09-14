package com.sufo.lexinote.ui.feature.dictionary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sufo.lexinote.constants.HEADER_HEIGHT
import com.sufo.lexinote.ui.navigation.NavigationService
import com.sufo.lexinote.utils.getTagNames
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.sufo.lexinote.R
import com.sufo.lexinote.data.local.db.entity.Notebook
import com.sufo.lexinote.data.local.db.entity.Word
import com.sufo.lexinote.utils.DateUtils
import com.sufo.lexinote.utils.wordExchange
import java.io.File
import kotlin.text.indexOf
import kotlin.text.substring

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WordDetailScreen (
    nav: NavigationService,
    viewModel: WordDetailViewModel = hiltViewModel(),
){
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showNotebookSheet) {
        NotebookSelectionSheet(
            notebooks = uiState.notebooks,
            onDismiss = { viewModel.onDismissNotebookSheet() },
            onNotebookSelected = { viewModel.onNotebookSelected(it) }
        )
    }

    if (uiState.previewImageState != null) {
        ImagePreviewDialog(
            previewState = uiState.previewImageState!!,
            onDismiss = { viewModel.onDismissImagePreview() }
        )
    }

    Column (
        modifier = Modifier.fillMaxWidth()
    ) {
        //header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(HEADER_HEIGHT.dp) // Standard TopAppBar height
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBackIosNew, contentDescription = "go back",
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(text = uiState.word, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.weight(1f))

            if (uiState.savedWord != null) {
                IconButton(onClick = { viewModel.onEditWordClicked() }) {
                    Icon(
                        Icons.Default.ModeEditOutline,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                IconButton(onClick = { viewModel.onAddToNotebookClicked() }) {
                    Icon(
                        Icons.Default.AddCircleOutline,
                        modifier = Modifier.size(16.dp),
                        contentDescription = "Add to notebook",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        //phonetic
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end=16.dp, bottom = 8.dp)
        ) {
            IconButton( modifier = Modifier.size(24.dp), onClick = { viewModel.speak(uiState.word) }) {
                Icon(Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = "pronounce", tint = MaterialTheme.colorScheme.primary)
            }
            uiState.dictWord?.let {
                if(!it.phonetic.isNullOrBlank()) {
                    Text("/${uiState.dictWord?.phonetic}/", modifier = Modifier.padding(start = 2.dp))
                }
            }
        }


        //tags
        val tag = uiState.dictWord?.tag
        if (tag != null && !tag.isEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 8.dp)
            ) {
                val tags = getTagNames(tag)
                items(tags) { item ->
                    Text(
                        item, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                            .padding(start = 2.dp, end = 2.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
        //个人单词数据
        if(uiState.savedWord != null) {
            MyWord(uiState, viewModel)
        }
        if (uiState.dictWord != null) {
            //字典
            DictResult(uiState, viewModel)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImagePreviewDialog(previewState: PreviewImageState, onDismiss: () -> Unit) {
    val pagerState = rememberPagerState(initialPage = previewState.initialIndex) { previewState.images.size }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = previewState.images[it],
                    contentDescription = "Full screen image preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close preview", tint = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookSelectionSheet(
    notebooks: List<Notebook>,
    onDismiss: () -> Unit,
    onNotebookSelected: (Notebook) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.add_notebook), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
            LazyColumn {
                items(notebooks) { notebook ->
                    ListItem(
                        headlineContent = { Text(notebook.name) },
                        modifier = Modifier.clickable { onNotebookSelected(notebook) }
                    )
                }
            }
        }
    }
}

@Composable
fun MyWord(uiState: WordDetailUiState, viewModel: WordDetailViewModel){
    val savedWord = uiState.savedWord!!
    CollapsibleItem(
        title = stringResource(R.string.add_word_title_notes),
        initState = true
    ){
        SrsInfo(savedWord)
        Spacer(modifier = Modifier.height(16.dp))

        // Translation
        if (!savedWord.translation.isNullOrBlank()) {
            SelectionContainer {
                Text(savedWord.translation, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Example
        if (!savedWord.example.isNullOrBlank()) {
            SelectionContainer {
                Text(savedWord.example, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Notes
        if (!savedWord.notes.isNullOrBlank()) {
            SelectionContainer {
                Text(savedWord.notes, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(12.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Images
        val imagePaths = remember(savedWord.imgs) {
            savedWord.imgs?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        }
        if (imagePaths.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(imagePaths) { path ->
                    AsyncImage(
                        model = path,
                        contentDescription = "User image",
                        modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp)).clickable { viewModel.onImageClicked(path) },
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Audios
        val audioPaths = remember(savedWord.audios) {
            savedWord.audios?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        }
        if (audioPaths.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(audioPaths) { path ->
                    AudioChip(
                        path = path,
                        isPlaying = uiState.playingAudioPath == path && uiState.isAudioPlaying,
                        onClick = { viewModel.playOrPauseAudio(path) }
                    )
                }
            }
        }
    }
}

@Composable
fun AudioChip(path: String, isPlaying: Boolean, onClick: () -> Unit) {
    val icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow
    val containerColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = Modifier
            .background(containerColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = "Play/Pause audio", modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = File(path).name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.widthIn(max = 100.dp) // Prevent long names from stretching the chip too much
        )
    }
}

@Composable
fun SrsInfo(word: Word) {
    val starCount = word.repetitions.coerceIn(0, 5)

    val lastReviewText = if (word.interval == 0) {
        stringResource(R.string.not_reciewd_yet)
    } else {
        val lastReviewDate = DateUtils.subtractDays(word.nextReviewDate, word.interval)
        DateUtils.getTimeAgo(lastReviewDate)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Familiarity
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.familiarity)+": ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < starCount) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        tint = if (index < starCount) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        // Last Review
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.last_review), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(lastReviewText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun DictResult(uiState: WordDetailUiState, viewModel: WordDetailViewModel){
    //字典数据
    CollapsibleItem(
        title = "Dictionary",
        initState = true
    ){

        val transition = uiState.dictWord!!.translation
        if(!transition.isNullOrBlank()) {
            SelectionContainer {
                Text(
                    transition ?: "",
                    modifier = Modifier.padding(horizontal = 4.dp),
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 32.sp,
                    )
                )
            }
        }

        //时态
        val exchanges = wordExchange(uiState.dictWord)
        if(exchanges.containsKey("tense")){
            val tense = exchanges["tense"]
            Row(modifier = Modifier.fillMaxWidth().padding(top=16.dp)) {
                Text("${stringResource(R.string.tense)}: ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${tense}", color = MaterialTheme.colorScheme.primary)
            }
        }
        //比较级
        if(exchanges.containsKey("comparative")){
            val comparative = exchanges["comparative"]
            Row(modifier = Modifier.fillMaxWidth().padding(top=16.dp)){
                Text("${stringResource(R.string.comparative)}: ")
                Text("${comparative}", color = MaterialTheme.colorScheme.primaryContainer)
            }
        }

        //图片
        val imgUrl = uiState.dictWord!!.imageUrl
        if(imgUrl.isNullOrBlank()) {
            AsyncImage(
                model = imgUrl,
                contentDescription = uiState.word,
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Crop
            )
        }
        //例句
        val examples = uiState.dictWord!!.examples
        LazyColumn {
            items(examples) { item->
                Row(Modifier.fillMaxWidth()
                    .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = buildAnnotatedString {
                            val startIndex = item.indexOf(uiState.word)
                            if (startIndex >= 0) {
                                append(item.substring(0, startIndex))
                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                    append(uiState.word)
                                }
                                append(item.substring(startIndex + uiState.word.length))
                            } else {
                                append(item)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = 24.sp
                    )
                    IconButton(onClick = { viewModel.speak(item) }) {
                        Icon(Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = "pronounce", modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}


@Composable
fun CollapsibleItem(
    title: String,
    initState: Boolean = false,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(initState) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300),
        label = "rotate"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 16.dp)
                .clickable { expanded = !expanded },
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if(expanded) "fold" else "expand",
                modifier = Modifier.rotate(rotationAngle)
            )
        }

        AnimatedVisibility(visible = expanded, modifier = Modifier.background(Color.Transparent).padding(16.dp)) {
            Column {
                content()
            }
        }
    }
}


