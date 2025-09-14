package com.sufo.lexinote.ui.feature.word

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sufo.lexinote.ui.components.PageHeader
import com.sufo.lexinote.ui.navigation.NavigationService
import com.sufo.lexinote.ui.theme.LexiNoteTheme
import com.google.accompanist.permissions.rememberPermissionState
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.draw.clip
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import coil3.compose.AsyncImage
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale

import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import com.google.accompanist.permissions.isGranted
import com.sufo.lexinote.ui.ext.dashedBorder
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import java.io.File
import com.sufo.lexinote.R

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddWordScreen(
    nav: NavigationService,
    viewModel: AddWordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState = uiState.formState
    
//    var showRecordingSheet by remember { mutableStateOf(false) }

    if (uiState.showRecordingSheet) {
        RecordingBottomSheet(
            recordingState = uiState.recordingState,
            onDismiss = { viewModel.updateShowRecordingSheet(false) },
            onStartRecording = viewModel::startRecording,
            onStopRecording = viewModel::stopRecording
        )
    }

    val permissionState = rememberPermissionState(
        permission = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) Manifest.permission.WRITE_EXTERNAL_STORAGE else ""
    )

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> viewModel.addImages(uris) }
    )

    val multipleAudioPickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetMultipleContents(),
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris -> viewModel.addAudios(uris) }
    )

    Column(modifier = Modifier.fillMaxSize().padding(bottom = 20.dp)) {
        PageHeader(
            title = if (uiState.isEditMode) stringResource(R.string.edit_word_title) else uiState.notebookName,
            onClose = { nav.popBackStack() },
            actions = {
                TextButton(
                    onClick = { viewModel.saveWord() },
                    enabled = !uiState.isSaving
                ) {
                    Text(stringResource(R.string.save), fontWeight = FontWeight.Bold)
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f) // Fill all available space
                .verticalScroll(rememberScrollState()) // Make this Column scrollable
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Word and Phonetic Section
            WordInputSection(formState, viewModel)

            // Translation Section
            TranslationInputSection(formState, viewModel)

            // Example Section
            ExampleInputSection(formState, viewModel)

            // Optional Sections
            ExpandableInputSection(title = stringResource(R.string.add_word_title_notes)) {
                CustomTextField(
                    value = formState.notes,
                    onValueChange = { viewModel.onFormChange(formState.copy(notes = it)) },
                    placeholder = "Add your own associations, mnemonics...",
                    modifier = Modifier.height(120.dp)
                )
            }

//            Spacer(modifier = Modifier.height(16.dp))

            //Associated Image
            ExpandableInputSection(title = stringResource(R.string.add_word_title_images)) {
                ImagePickerSection(formState, viewModel) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P || permissionState.status.isGranted) {
                        multiplePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    } else {
                        permissionState.launchPermissionRequest()
                    }
                }
            }

            //My Audio
            ExpandableInputSection(title = stringResource(R.string.add_word_title_audio)) {
                AudioPickerSection(formState, viewModel, uiState,
                    onRecordAudioClick = { viewModel.updateShowRecordingSheet(true) },
                    onAddAudioClick = {multipleAudioPickerLauncher.launch(arrayOf("audio/*"))},
                )
            }

        }
    }
}

@Composable
private fun WordInputSection(formState: WordFormData, viewModel: AddWordViewModel) {
    Column{
        OutlinedTextField(
            value = formState.word,
            onValueChange = { viewModel.onFormChange(formState.copy(word = it)) },
            textStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        viewModel.onWordBlur()
                    }
                },
            placeholder = { Text(stringResource(R.string.add_word_placeholder_word), color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.headlineLarge
            ) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
            )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.offset(y = (-12).dp).clickable { viewModel.speak(formState.word) }
        ) {
            Icon(Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = stringResource(R.string.add_word_cd_pronunciation), tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text("/${formState.phonetic}/", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        Divider()
    }

}

@Composable
private fun TranslationInputSection(formState: WordFormData, viewModel: AddWordViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.add_word_title_translation), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        CustomTextField(
            value = formState.translation,
            onValueChange = { viewModel.onFormChange(formState.copy(translation = it)) },
            placeholder = stringResource(R.string.add_word_placeholder_translation)
        )
    }
}

@Composable
private fun ExampleInputSection(formState: WordFormData, viewModel: AddWordViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.add_word_title_examples), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        CustomTextField(
            value = formState.example,
            onValueChange = { viewModel.onFormChange(formState.copy(example = it)) },
            placeholder = stringResource(R.string.add_word_placeholder_examples),
            modifier = Modifier.height(100.dp)
        )
    }
}

@Composable
private fun ExpandableInputSection(title: String, content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Icon(
                imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = "Expand or collapse section"
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    textStyle: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            focusedBorderColor = MaterialTheme.colorScheme.primary
        ),
        textStyle = textStyle,
        trailingIcon = trailingIcon
    )
}


@Composable
private fun ImagePickerSection(formState: WordFormData, viewModel: AddWordViewModel, onAddImageClick: () -> Unit) {
    val imagePaths = remember(formState.imgs) {
        formState.imgs.split(",").filter { it.isNotBlank() }
    }

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        imagePaths.forEach { path ->
            Box(modifier = Modifier.size(100.dp)) {
                AsyncImage(
                    model = path,
                    contentDescription = "Selected image",
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { viewModel.removeImage(path) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.add_word_cd_remove_image), tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        Box(
            modifier = Modifier
                .size(100.dp)
                .dashedBorder(1.dp,cornerRadius = 12.dp,color = MaterialTheme.colorScheme.outline)
                .clickable { onAddImageClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AddPhotoAlternate, contentDescription = stringResource(R.string.add_word_cd_add_image), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AudioPickerSection(formState: WordFormData,
                               viewModel: AddWordViewModel,
                               uiState: AddWordUiState,
                               onRecordAudioClick: () -> Unit,
                               onAddAudioClick: () -> Unit
) {
    val audioPaths = remember(formState.audios) {
        formState.audios.split(",").filter { it.isNotBlank() }
    }
    val currentlyPlaying = uiState.currentlyPlayingAudio

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier.weight(1f)
                    .height(84.dp)
                    .dashedBorder(1.dp,cornerRadius = 12.dp,color = MaterialTheme.colorScheme.outline)
                    .clickable { onRecordAudioClick() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.TopCenter
            ){
                Icon(Icons.Default.Mic, contentDescription = stringResource(R.string.add_word_record_audio_title))
//                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.record_audio),modifier = Modifier.align(Alignment.BottomCenter))
            }
            Box(
                modifier = Modifier.weight(1f)
                    .height(84.dp)
                    .dashedBorder(1.dp, cornerRadius = 12.dp, color = MaterialTheme.colorScheme.outline)
                    .clickable { onAddAudioClick() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.TopCenter
            ){
                Icon(Icons.Default.Audiotrack, contentDescription = stringResource(R.string.add_word_select_file_button))
                Text(stringResource(R.string.add_word_select_file_button), modifier = Modifier.align(Alignment.BottomCenter))
            }
        }

        Column(
            //modifier = Modifier.horizontalScroll(rememberScrollState()),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            audioPaths.forEach { path ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .clickable { viewModel.playAudio(path) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(if (currentlyPlaying == path) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = stringResource(R.string.add_word_cd_play_audio))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(File(path).name, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { viewModel.removeAudio(path) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.add_word_cd_remove_audio))
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
private fun RecordingBottomSheet(
    recordingState: RecordingState,
    onDismiss: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var progress by remember { mutableStateOf(0f) }
    val recordAudioPermissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "RecordingProgress")

    LaunchedEffect(recordingState) {
        if (recordingState == RecordingState.Recording) {
            val startTime = System.currentTimeMillis()
            while (true) {
                val elapsedTime = System.currentTimeMillis() - startTime
                progress = (elapsedTime / 60000f).coerceAtMost(1f)
                if (progress >= 1f) {
                    onStopRecording()
                    break
                }
                delay(100)
            }
        } else {
            progress = 0f
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(stringResource(R.string.add_word_record_audio_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(120.dp),
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = stringResource(R.string.record_audio),
                    tint = Color.White,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .pointerInput(recordAudioPermissionState) { // Pass state to re-trigger pointerInput
                            detectTapGestures(
                                onPress = {
                                    if (recordAudioPermissionState.status.isGranted) {
                                        onStartRecording()
                                        tryAwaitRelease() // Wait for the press to be released
                                        onStopRecording()
                                    } else {
                                        recordAudioPermissionState.launchPermissionRequest()
                                    }
                                }
                            )
                        }
                        .padding(24.dp)
                )
            }

            Text(
                text = if (recordingState == RecordingState.Recording) stringResource(R.string.add_word_record_release_to_stop) else stringResource(R.string.add_word_record_tap_to_start),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AddWordScreenPreview() {
    LexiNoteTheme {
        AddWordScreen(nav = NavigationService())
    }
}