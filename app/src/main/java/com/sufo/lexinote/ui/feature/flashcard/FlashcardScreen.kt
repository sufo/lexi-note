package com.sufo.lexinote.ui.feature.flashcard

import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.sufo.lexinote.data.local.db.entity.Word
import kotlin.math.abs
import com.sufo.lexinote.R

@Composable
fun FlashcardScreen(viewModel: FlashcardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.currentWord) {
        // Speak the word as soon as it changes and is not null
        uiState.currentWord?.let {
            viewModel.speak(it.word)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar with Progress
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { viewModel.onCloseClicked() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.flashcard_close_button_desc))
            }
            if (!uiState.isLoading) {
                LinearProgressIndicator(
                    progress = { uiState.progress },
                    modifier = Modifier.weight(1f).height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = uiState.progressText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Main Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.isFinished -> {
                    FinishedView(onClose = { viewModel.onCloseClicked() })
                }
                uiState.currentWord != null -> {
                    when (uiState.currentMode) {
                        ReviewMode.FLASHCARD -> {
                            FlashcardContent(
                                uiState = uiState,
                                onFlip = { viewModel.onFlipCard() },
                                onProcessReview = { quality -> viewModel.processReview(quality) },
                                onSpeak = viewModel::speak
                            )
                        }
                        ReviewMode.SPELLING -> {
                            SpellingTestContent(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                        }
                        ReviewMode.MULTIPLE_CHOICE -> {
                            MultipleChoiceContent(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardContent(
    uiState: FlashcardUiState,
    onFlip: () -> Unit,
    onProcessReview: (Int) -> Unit,
    onSpeak: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.currentWord != null) {
                key(uiState.currentWord) {
                    SwipableCard(
                        onSwiped = { quality -> onProcessReview(quality) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        FlippableCard(
                            word = uiState.currentWord!!,
                            bgImg = uiState.backgroundImage,
                            isFlipped = uiState.isFlipped,
                            onFlip = onFlip,
                            onSpeak = {onSpeak(uiState.currentWord!!.word)}
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        NewFeedbackButtons(
            onProcessReview = onProcessReview,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp)
        )
    }
}

@Composable
fun SpellingTestContent(
    uiState: FlashcardUiState,
    viewModel: FlashcardViewModel // Pass the whole viewModel to call different methods
) {
    when (uiState.spellingState) {
        SpellingState.INITIAL -> {
            InitialSpellingView(
                uiState = uiState,
                onSpellingChanged = { viewModel.onSpellingInputChanged(it) },
                onCheck = { viewModel.onCheckSpelling() },
                onGiveUp = { viewModel.onSpellingGiveUp() },
                onSpeak = { viewModel.speak(uiState.currentWord?.word ?: "") }
            )
        }
        SpellingState.INCORRECT -> {
            CorrectingSpellingView(
                uiState = uiState,
                onSpellingChanged = { viewModel.onSpellingInputChanged(it) },
                onNext = { viewModel.onSpellingNextAfterCorrection() }
            )
        }
    }
}

@Composable
private fun InitialSpellingView(
    uiState: FlashcardUiState,
    onSpellingChanged: (String) -> Unit,
    onCheck: () -> Unit,
    onGiveUp: () -> Unit,
    onSpeak: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onSpeak) {
            Icon(Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = stringResource(R.string.flashcard_speak_word_desc), modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.height(48.dp))
        OutlinedTextField(
            value = uiState.spellingInput,
            onValueChange = onSpellingChanged,
            label = { Text(stringResource(R.string.spelling_test_textfield_label)) },
            modifier = Modifier.fillMaxWidth(0.9f),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onCheck() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = if (uiState.isSpellingCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = if (uiState.isSpellingCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        )
        
        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = onGiveUp, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.spelling_test_give_up_button),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun CorrectingSpellingView(
    uiState: FlashcardUiState,
    onSpellingChanged: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(stringResource(R.string.spelling_test_correct_answer_label), style = MaterialTheme.typography.labelLarge)
        Text(
            text = uiState.currentWord?.word ?: "",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.spelling_test_you_typed_label, uiState.userIncorrectSpelling ?: ""), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.spellingInput,
            onValueChange = onSpellingChanged,
            label = { Text(stringResource(R.string.spelling_test_correction_prompt)) },
            modifier = Modifier.fillMaxWidth(0.9f),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onNext() })
        )

        Spacer(modifier = Modifier.weight(1f))

//        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
//            Text("下一题")
//        }
        TextButton(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.multiple_choice_next_button))
        }
    }
}

private enum class DragValue {
    Start, SwipedLeft, SwipedRight
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipableCard(
    onSwiped: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }

    val positionalThreshold = { distance: Float -> distance * 0.5f }
    val velocityThreshold = { with(density) { 100.dp.toPx() } }
    val snapAnimationSpec = tween<Float>()
    val decayAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay()

    val draggableState = remember {
        AnchoredDraggableState(
            initialValue = DragValue.Start,
            positionalThreshold = positionalThreshold,
            velocityThreshold = velocityThreshold,
            snapAnimationSpec = snapAnimationSpec,
            decayAnimationSpec = decayAnimationSpec,
            confirmValueChange = { true }
        )
    }

    val anchors = DraggableAnchors {
        DragValue.SwipedLeft at -screenWidthPx
        DragValue.Start at 0f
        DragValue.SwipedRight at screenWidthPx
    }

    LaunchedEffect(anchors) {
        draggableState.updateAnchors(anchors)
    }

    LaunchedEffect(draggableState.currentValue) {
        if (draggableState.currentValue == DragValue.SwipedLeft) {
            onSwiped(0)
        } else if (draggableState.currentValue == DragValue.SwipedRight) {
            onSwiped(5)
        }
    }

    val offsetX = draggableState.offset.coerceIn(-screenWidthPx, screenWidthPx)

    // Calculate alpha based on swipe progress
    val rightSwipeProgress = (offsetX / screenWidthPx).coerceIn(0f, 1f)
    val leftSwipeProgress = (abs(offsetX) / screenWidthPx).coerceIn(0f, 1f)

    Box(modifier = Modifier.fillMaxSize()) { // Use a wrapper Box to hold hints and content
        HintLabel(
            text = stringResource(R.string.flashcard_hint_known),
            color = Color(0xFF059669), // Green
            rotation = -45f,
            alpha = rightSwipeProgress,
            modifier = Modifier.align(Alignment.TopStart).padding(32.dp)
        )
        HintLabel(
            text = stringResource(R.string.flashcard_hint_forgot),
            color = Color(0xFFE11D48), // Rose
            rotation = 45f,
            alpha = leftSwipeProgress,
            modifier = Modifier.align(Alignment.TopEnd).padding(32.dp)
        )

        Box(
            modifier = modifier
                .align(Alignment.Center)
                .anchoredDraggable(draggableState, Orientation.Horizontal)
                .graphicsLayer {
                    translationX = offsetX
                    rotationZ = (offsetX / screenWidthPx) * 15f
                    transformOrigin = TransformOrigin(0.5f, 1f)
                }
        ) {
            content()
        }
    }
}

@Composable
private fun HintLabel(text: String, color: Color, rotation: Float, alpha: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .graphicsLayer {
                rotationZ = rotation
                this.alpha = alpha
            }
            .border(BorderStroke(2.dp, color), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun FlippableCard(
    word: Word,
    bgImg: String?,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onSpeak: () -> Unit,
) {
    val rotationY by animateFloatAsState(targetValue = if (isFlipped) 180f else 0f, label = "flip")
    val density = LocalDensity.current
    Card(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                this.rotationY = rotationY
                cameraDistance = 12 * density.density
            }
            .clickable { onFlip() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        if (rotationY <= 90f) {
            FlashcardFront(word = word.word, bgImg = bgImg)
        } else {
            // Important: We rotate the content of the back card again by 180 degrees
            // so it's not mirrored. This is nested inside the parent's rotation.
            Box(modifier = Modifier.graphicsLayer { this.rotationY = 180f }) {
                FlashcardBack(word,onSpeak)
            }
        }
    }
}

@Composable
fun FlashcardFront(word: String,bgImg: String?) {
    Box(modifier = Modifier.fillMaxSize()) {
        // You can add a background image here if you want
        if(bgImg != null && bgImg.isNotEmpty()){
            AsyncImage(
                model = bgImg,
                contentDescription = "Full screen image preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = word,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(if(bgImg != null && bgImg.isNotEmpty()) MaterialTheme.colorScheme.secondary else Color.Transparent, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp)).padding(horizontal = 8.dp),
                color = if(bgImg != null && bgImg.isNotEmpty()) MaterialTheme.colorScheme.onSecondary else Color.Unspecified
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.flashcard_tap_to_see_definition),
                style = MaterialTheme.typography.bodyLarge,
                color = if(bgImg != null && bgImg.isNotEmpty()) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun FlashcardBack(word: Word,onSpeak: ()->Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = word.word,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.wrapContentWidth()
                .clickable{onSpeak()},
            horizontalArrangement = Arrangement.Center,
        ){
            Icon(Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = "phonetic",
                modifier = Modifier.padding(horizontal = 4.dp),
                tint = MaterialTheme.colorScheme.onSurface)
            word.phonetic?.let{
                Text(
                    text = "/$it/",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = word.translation ?: "",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        if (word.example != null && word.example.isNotBlank()) {
            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.flashcard_example_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = word.example ?: "",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun NewFeedbackButtons(onProcessReview: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 不认识 (quality=0), 模糊 (quality=3), 认识 (quality=5)
        TextButton(
            onClick = { onProcessReview(0) },
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.feedback_forgot), color = MaterialTheme.colorScheme.error)
        }
        TextButton(
            onClick = { onProcessReview(3) },
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.feedback_vague), color = MaterialTheme.colorScheme.tertiary)
        }
        TextButton(
            onClick = { onProcessReview(5) },
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.feedback_know), color = MaterialTheme.colorScheme.primary)
        }
    }
}


@Composable
fun MultipleChoiceContent(
    uiState: FlashcardUiState,
    viewModel: FlashcardViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.5f))
        Text(
            text = uiState.currentWord?.word ?: "",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            uiState.multipleChoiceOptions.forEach { option ->
                val isCorrect = option == uiState.currentWord?.translation
                val buttonColors = if (uiState.isAnswerChecked) {
                    when {
                        isCorrect -> ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)) // Green
                        option == uiState.selectedOption -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // Red
                        else -> ButtonDefaults.outlinedButtonColors()
                    }
                } else {
                    ButtonDefaults.outlinedButtonColors()
                }

                Button(
                    onClick = { if (!uiState.isAnswerChecked) viewModel.onOptionSelected(option) },
                    modifier = Modifier.fillMaxWidth().align(alignment = Alignment.Start),
                    shape = RoundedCornerShape(4.dp), // Set corner radius to 4dp
                    colors = buttonColors,
                    border = if (!uiState.isAnswerChecked || (uiState.isAnswerChecked && !isCorrect && option != uiState.selectedOption)) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    } else null
                ) {
                    Text(option, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = { viewModel.onNextAfterMultipleChoice() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.multiple_choice_next_button))
        }
    }
}

@Composable
fun FinishedView(onClose: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(stringResource(R.string.finish_view_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.finish_view_subtitle), style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onClose) {
            Text(stringResource(R.string.finish_view_back_button))
        }
    }
}