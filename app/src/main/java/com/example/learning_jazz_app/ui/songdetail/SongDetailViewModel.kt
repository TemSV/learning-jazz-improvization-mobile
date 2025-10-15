package com.example.learning_jazz_app.ui.songdetail

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.learning_jazz_app.data.remote.dto.*
import com.example.learning_jazz_app.data.repository.SongRepositoryImpl
import com.example.learning_jazz_app.di.NetworkModule
import com.example.learning_jazz_app.domain.model.ChordEntry
import com.example.learning_jazz_app.domain.model.RecommendedPhrase
import com.example.learning_jazz_app.domain.model.SongChords
import com.example.learning_jazz_app.domain.repository.SongRepository
import com.example.learning_jazz_app.util.PhrasePlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SongDetailUiState(
    val songChords: SongChords? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val highlightedBarColors: Map<Int, Color> = emptyMap(),
    val isLoadingPatterns: Boolean = false,
    val recommendations: List<RecommendedPhrase>? = null,
    val isLoadingRecommendations: Boolean = false,
    val playingPhraseId: Int? = null,
    val loadingPhraseId: Int? = null
)

class SongDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: SongRepository
) : ViewModel() {

    private val songId: Int = checkNotNull(savedStateHandle["songId"])
    private val songTitle: String = checkNotNull(savedStateHandle["songTitle"])
    private val songComposer: String = checkNotNull(savedStateHandle["songComposer"])

    private val _uiState = MutableStateFlow(SongDetailUiState())
    val uiState = _uiState.asStateFlow()
    
    private val phrasePlayer = PhrasePlayer()

    private var rawChords: List<SongChordEntry> = emptyList()
    private var rawPatternsResponse: SongPatternsResponse? = null
    private var currentPatternFeatures: Map<String, Double>? = null
    private var barNumToBarIdMap: Map<Int, Int> = emptyMap()

    private val patternColors = listOf(
        Color(0xFFF9E79F), // Light Yellow
        Color(0xFFABEBC6), // Light Green
        Color(0xFFAED6F1), // Light Blue
        Color(0xFFF5B7B1), // Light Red
        Color(0xFFD7BDE2), // Light Purple
        Color(0xFFFAD7A0)  // Light Orange
    )

    init {
        fetchSongChords()
    }

    override fun onCleared() {
        super.onCleared()
        phrasePlayer.stop()
    }

    fun playPhrase(phrase: RecommendedPhrase) {
        // If we are already loading a phrase, do nothing to prevent multiple requests.
        if (_uiState.value.loadingPhraseId != null) return

        val currentlyPlayingId = _uiState.value.playingPhraseId

        // If the clicked phrase is already playing, stop it.
        if (currentlyPlayingId == phrase.melid) {
            stopPlayback()
            return
        }

        // If a different phrase is playing, stop it first.
        if (currentlyPlayingId != null) {
            stopPlayback()
        }
        
        viewModelScope.launch {
            // Set loading state for the specific phrase
            _uiState.update { it.copy(loadingPhraseId = phrase.melid) }

            repository.getPhraseNotes(phrase.melid, phrase.startNoteIndex, phrase.endNoteIndex)
                .onSuccess { notes ->
                    // Ensure the action is still relevant (user might have clicked stop)
                    if (_uiState.value.loadingPhraseId != phrase.melid) return@onSuccess

                    if (notes.isNotEmpty()) {
                        _uiState.update { it.copy(loadingPhraseId = null, playingPhraseId = phrase.melid) }
                        phrasePlayer.play(
                            notes = notes,
                            onCompletion = {
                                // When playback is finished, update the state
                                _uiState.update {
                                    if (it.playingPhraseId == phrase.melid) {
                                        it.copy(playingPhraseId = null)
                                    } else {
                                        it
                                    }
                                }
                            }
                        )
                    } else {
                        // No notes to play, just reset loading state
                        _uiState.update { it.copy(loadingPhraseId = null) }
                    }
                }
                .onFailure {
                    // On error, reset loading state
                    _uiState.update { it.copy(loadingPhraseId = null) }
                }
        }
    }

    private fun stopPlayback() {
        phrasePlayer.stop()
        _uiState.update { it.copy(playingPhraseId = null, loadingPhraseId = null) }
    }

    fun onBarClicked(barNum: Int) {
        if (_uiState.value.highlightedBarColors[barNum] == null) return

        val clickedBarId = rawChords
            .filter { it.bar <= barNum }
            .maxByOrNull { it.bar }
            ?.barid ?: return

        val pattern = rawPatternsResponse?.patterns?.find { p ->
            p.originalChordRefs.any { ref -> ref.barid == clickedBarId }
        } ?: return

        currentPatternFeatures = pattern.features
        getPhraseRecommendations()
    }

    fun fetchMoreRecommendations() {
        getPhraseRecommendations(append = true)
    }

    private fun getPhraseRecommendations(append: Boolean = false) {
        val features = currentPatternFeatures ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRecommendations = true) }
            val request = RecommendationRequest(patternFeatures = features)
            repository.getPhraseRecommendations(request)
                .onSuccess { response ->
                    val newRecs = response.recommendations.map { it.toDomain() }
                    _uiState.update {
                        val currentRecs = if (append) it.recommendations ?: emptyList() else emptyList()
                        it.copy(
                            isLoadingRecommendations = false,
                            recommendations = currentRecs + newRecs
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingRecommendations = false,
                            error = throwable.message
                        )
                    }
                }
        }
    }

    fun clearRecommendations() {
        _uiState.update { it.copy(recommendations = null) }
        currentPatternFeatures = null
    }

    fun togglePatterns() {
        if (_uiState.value.highlightedBarColors.isNotEmpty()) {
            _uiState.update { it.copy(highlightedBarColors = emptyMap()) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPatterns = true) }
            repository.getSongPatterns(songId)
                .onSuccess { response ->
                    rawPatternsResponse = response
                    val patternTypes = response.patterns.map { it.type }.distinct()
                    val typeToColorMap = patternTypes.zip(patternColors).toMap()

                    val highlightedBarColors = mutableMapOf<Int, Color>()

                    response.patterns.forEach { pattern ->
                        val color = typeToColorMap[pattern.type] ?: Color.Gray
                        val patternBarIds = pattern.originalChordRefs.map { it.barid }.toSet()

                        barNumToBarIdMap.forEach { (barNum, barId) ->
                            if (barId in patternBarIds) {
                                highlightedBarColors[barNum] = color
                            }
                        }
                    }

                    _uiState.update {
                        it.copy(
                            isLoadingPatterns = false,
                            highlightedBarColors = highlightedBarColors
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingPatterns = false,
                            error = throwable.message
                        )
                    }
                }
        }
    }

    private fun fetchSongChords() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getSongChords(songId)
                .onSuccess { response ->
                    rawChords = response.chords
                    processChords(response.chords)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            songChords = SongChords(
                                songId = songId,
                                title = songTitle,
                                composer = songComposer,
                                chords = processChords(response.chords)
                            )
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message
                        )
                    }
                }
        }
    }
    
    private fun processChords(chords: List<SongChordEntry>): List<ChordEntry> {
        val result = mutableListOf<ChordEntry>()
        val map = mutableMapOf<Int, Int>()
        var lastBarId = -1
        var lastChord = "%"
        var lastSignature: String? = null

        if (chords.isNotEmpty()) {
            val totalBars = chords.maxOfOrNull { it.bar } ?: 0
            val chordsByBar = chords.associateBy { it.bar }
            lastSignature = chords.firstNotNullOfOrNull { it.signature }

            for (bar in 1..totalBars) {
                val entry = chordsByBar[bar]
                if (entry?.chords != null) {
                    lastChord = entry.chords
                }
                if (entry != null) {
                    lastBarId = entry.barid
                }
                if (lastBarId != -1) {
                    map[bar] = lastBarId
                }
                result.add(
                    ChordEntry(
                        bar = bar,
                        chords = entry?.chords ?: lastChord,
                        signature = lastSignature,
                        form = entry?.form
                    )
                )
            }
        }
        barNumToBarIdMap = map
        return result
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val repository = SongRepositoryImpl(NetworkModule.apiService)
                SongDetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = repository
                )
            }
        }
    }
} 