package com.sufo.lexinote.ui.feature.word

import android.app.Application
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sufo.lexinote.data.local.db.entity.Word
import com.sufo.lexinote.data.repo.DictionaryRepository
import com.sufo.lexinote.data.repo.WordRepository
import com.sufo.lexinote.ui.base.BaseViewModel
import com.sufo.lexinote.ui.navigation.NavigationService
import com.sufo.lexinote.utils.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*
import android.media.MediaPlayer
import android.media.MediaRecorder
import com.sufo.lexinote.data.repo.NotebookRepository
import javax.inject.Inject

data class WordFormData(
    val id: Int = 0, // Add id for editing
    val word: String = "",
    val phonetic: String = "",
    val translation: String = "",
    val example: String = "",
    val notes: String = "",
    val myExample: String = "",
    val imgs: String = "",
    val audios: String = "",
    val tags: List<String> = emptyList()
)

data class AddWordUiState(
    val notebookName: String = "",
    val formState: WordFormData = WordFormData(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isTtsReady: Boolean = false,
    val recordingState: RecordingState = RecordingState.Idle,
    val currentlyPlayingAudio: String? = null,
    val showRecordingSheet: Boolean = false,
    val isEditMode: Boolean = false
)

enum class RecordingState {
    Idle, Recording, Finishing
}

@HiltViewModel
class AddWordViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val notebookRepo: NotebookRepository,
    private val nav: NavigationService,
    private val application: Application,
    savedStateHandle: SavedStateHandle
) : BaseViewModel(application, nav) {

    private val notebookId: Int = checkNotNull(savedStateHandle["notebookId"])
//    private val notebookName: String? = savedStateHandle["notebookName"]
//    private val wordId: Int? = savedStateHandle["wordId"]
    private val _word: String? = savedStateHandle["word"]

    private val _uiState = MutableStateFlow(AddWordUiState(isEditMode = _word != null))
    val uiState = _uiState.asStateFlow()
    private var ttsManager: TtsManager? = null
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var recordingFile: File? = null

    init {
        ttsManager = TtsManager(getApplication()){ isSuccess ->
            _uiState.update { it.copy(isTtsReady = isSuccess) }
        }

        loadNotebookName()

        if (_word != null) {
            loadWordForEditing(_word)
        }
    }

    private fun loadNotebookName(){
        viewModelScope.launch {
            notebookRepo.getNotebookById(notebookId).collect { notebook ->
                notebook?.let{
                    _uiState.update { it.copy(notebookName = notebook.name) }
                }
            }
        }
    }

    private fun loadWordForEditing(word: String) {
        viewModelScope.launch {
            wordRepository.getWordByWord(word).filterNotNull().firstOrNull()?.let { word ->
                _uiState.update {
                    it.copy(
                        formState = WordFormData(
                            id = word.id,
                            word = word.word,
                            phonetic = word.phonetic?:"",
                            translation = word.translation?:"",
                            example = word.example?:"",
                            notes = word.notes?:"",
                            imgs = word.imgs?:"",
                            audios = word.audios?:""
                        )
                    )
                }
            }
        }
    }

    fun speak(text: String) {
        if (_uiState.value.isTtsReady) {
            ttsManager?.speak(text)
        }
    }

    override fun onCleared() {
        ttsManager?.shutdown()
        recorder?.release()
        player?.release()
        super.onCleared()
    }

    fun onFormChange(formData: WordFormData) {
        _uiState.update { it.copy(formState = formData, errorMessage = null) }
    }

    fun updateShowRecordingSheet(showRecordingSheet: Boolean){
        _uiState.update { it.copy(showRecordingSheet = showRecordingSheet) }
    }

    fun onWordBlur() {
        viewModelScope.launch {
            val word = _uiState.value.formState.word.trim()
            if (word.isNotBlank()) {
                val phonetic = dictionaryRepository.getPhonetic(word)
                if (phonetic != null) {
                    onFormChange(_uiState.value.formState.copy(phonetic = phonetic))
                }
            }
        }
    }

    fun addImages(uris: List<Uri>) {
        viewModelScope.launch {
            val currentPaths = _uiState.value.formState.imgs.split(",").filter { it.isNotBlank() }.toMutableList()
            uris.forEach { uri ->
                copyUriToExternalStorage(uri, "Images", ".jpg")?.let { path ->
                    currentPaths.add(path)
                }
            }
            onFormChange(_uiState.value.formState.copy(imgs = currentPaths.joinToString(",")))
        }
    }

    fun removeImage(path: String) {
        val currentPaths = _uiState.value.formState.imgs.split(",").toMutableList()
        if (currentPaths.remove(path)) {
            try {
                File(path).delete()
            } catch (e: Exception) {
                // Handle file deletion error if needed
            }
        }
        onFormChange(_uiState.value.formState.copy(imgs = currentPaths.joinToString(",")))
    }

    fun addAudios(uris: List<Uri>) {
        viewModelScope.launch {
            val currentPaths = _uiState.value.formState.audios.split(",").filter { it.isNotBlank() }.toMutableList()
            uris.forEach { uri ->
                copyUriToExternalStorage(uri, "Audio", ".mp3")?.let { path ->
                    currentPaths.add(path)
                }
            }
            onFormChange(_uiState.value.formState.copy(audios = currentPaths.joinToString(",")))
        }
    }

    fun removeAudio(path: String) {
        val currentPaths = _uiState.value.formState.audios.split(",").toMutableList()
        if (currentPaths.remove(path)) {
            try {
                File(path).delete()
            } catch (e: Exception) {
                // Handle file deletion error if needed
            }
        }
        onFormChange(_uiState.value.formState.copy(audios = currentPaths.joinToString(",")))
    }

    private suspend fun copyUriToExternalStorage(uri: Uri, subfolder: String, fileExtension: String): String? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = getApplication<Application>().contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
            val targetDir = File(getApplication<Application>().getExternalFilesDir(null), subfolder)
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            val file = File(targetDir, "${UUID.randomUUID()}$fileExtension")
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun startRecording() {
        if (recorder != null) return
        viewModelScope.launch {
            val file = createAudioFile("recorded_audio_${UUID.randomUUID()}")
            recordingFile = file
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(application)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                try {
                    prepare()
                    start()
                    _uiState.update { it.copy(recordingState = RecordingState.Recording) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Handle error
                }
            }
        }
    }

    fun stopRecording() {
        if (_uiState.value.recordingState != RecordingState.Recording) return
        _uiState.update { it.copy(recordingState = RecordingState.Finishing) }
        recorder?.apply {
            try {
                stop()
                release()
                //停止录音，则隐藏RecordingSheet
                updateShowRecordingSheet(false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        recorder = null
        recordingFile?.let { file ->
            val currentAudios = _uiState.value.formState.audios.split(",").filter { it.isNotBlank() }.toMutableList()
            currentAudios.add(file.absolutePath)
            onFormChange(_uiState.value.formState.copy(audios = currentAudios.joinToString(",")))
        }
        _uiState.update { it.copy(recordingState = RecordingState.Idle) }
    }

    fun playAudio(path: String) {
        if (player?.isPlaying == true) {
            player?.stop()
            player?.release()
            player = null
            if (_uiState.value.currentlyPlayingAudio == path) {
                _uiState.update { it.copy(currentlyPlayingAudio = null) }
                return
            }
        }
        player = MediaPlayer().apply {
            try {
                setDataSource(path)
                prepare()
                start()
                _uiState.update { it.copy(currentlyPlayingAudio = path) }
                setOnCompletionListener {
                    _uiState.update { it.copy(currentlyPlayingAudio = null) }
                    release()
                    player = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun createAudioFile(fileName: String): File = withContext(Dispatchers.IO) {
        val audioDir = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        if (audioDir != null && !audioDir.exists()) {
            audioDir.mkdirs()
        }
        File(audioDir, "$fileName.mp3")
    }

    fun saveWord() {
        val form = _uiState.value.formState

        if (form.word.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Word cannot be empty.") }
            return
        }

        if (form.translation.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Translation cannot be empty.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val wordToSave = Word(
                    id = if (_uiState.value.isEditMode) form.id else 0,
                    notebookId = notebookId,
                    word = form.word.trim(),
                    phonetic = form.phonetic.trim(),
                    translation = form.translation.trim(),
                    example = form.example.trim(),
                    notes = form.notes.trim(),
                    imgs = form.imgs,
                    audios = form.audios,
                    updatedAt = Date(),
                    nextReviewDate = Date() // This might need adjustment for edits
                )

                if (_uiState.value.isEditMode) {
                    wordRepository.updateWord(wordToSave)
                } else {
                    wordRepository.addWord(wordToSave)
                }
                nav.popBackStack()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to save word. Please try again.") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}