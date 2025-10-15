package com.example.learning_jazz_app.ui.songdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.learning_jazz_app.R
import com.example.learning_jazz_app.domain.model.ChordEntry
import com.example.learning_jazz_app.domain.model.RecommendedPhrase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SongDetailScreen(
    onBack: () -> Unit,
    viewModel: SongDetailViewModel = viewModel(factory = SongDetailViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val songChords = uiState.songChords
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmValueChange = { it != ModalBottomSheetValue.HalfExpanded }
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.recommendations) {
        if (uiState.recommendations != null) {
            coroutineScope.launch { sheetState.show() }
        } else {
            coroutineScope.launch { sheetState.hide() }
        }
    }
    
    if (sheetState.currentValue == ModalBottomSheetValue.Hidden && sheetState.targetValue == ModalBottomSheetValue.Hidden) {
        LaunchedEffect(Unit) {
            viewModel.clearRecommendations()
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            RecommendationSheetContent(
                isLoading = uiState.isLoadingRecommendations,
                recommendations = uiState.recommendations ?: emptyList(),
                onFetchMore = { viewModel.fetchMoreRecommendations() },
                playingPhraseId = uiState.playingPhraseId,
                loadingPhraseId = uiState.loadingPhraseId,
                onPlayClick = { phrase -> viewModel.playPhrase(phrase) }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        if (songChords != null) {
                            Column {
                                Text(text = songChords.title)
                                Text(
                                    text = songChords.composer,
                                    style = MaterialTheme.typography.caption
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back_button_description))
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { viewModel.togglePatterns() }) {
                    if (uiState.isLoadingPatterns) {
                        CircularProgressIndicator(color = MaterialTheme.colors.onPrimary)
                    } else {
                        Icon(Icons.Default.Done, contentDescription = stringResource(id = R.string.highlight_patterns_button_description))
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    uiState.error != null -> {
                        Text(
                            text = "Error: ${uiState.error}",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    songChords != null -> {
                        ChordGrid(
                            chords = songChords.chords,
                            highlightedBarColors = uiState.highlightedBarColors,
                            onBarClick = { barNum -> viewModel.onBarClicked(barNum) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChordGrid(
    chords: List<ChordEntry>,
    highlightedBarColors: Map<Int, Color>,
    onBarClick: (Int) -> Unit
) {
    val chunkedChords = chords.chunked(4)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp)
    ) {
        items(chunkedChords) { rowChords ->
            ChordRow(
                chords = rowChords,
                highlightedBarColors = highlightedBarColors,
                onBarClick = onBarClick
            )
        }
    }
}

@Composable
fun ChordRow(
    chords: List<ChordEntry>,
    highlightedBarColors: Map<Int, Color>,
    onBarClick: (Int) -> Unit
) {
    Column {
        FormRow(chords)
        ChordContentRow(chords, highlightedBarColors, onBarClick)
    }
}

@Composable
private fun FormRow(chords: List<ChordEntry>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(10.dp))

        for (i in 0 until 5) {
            Box(
                modifier = Modifier.width(2.dp),
                contentAlignment = Alignment.Center
            ) {
                val form = chords.getOrNull(i)?.form
                if (form != null) {
                    Text(
                        text = form,
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            if (i < 4) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ChordContentRow(
    chords: List<ChordEntry>,
    highlightedBarColors: Map<Int, Color>,
    onBarClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(10.dp),
            contentAlignment = Alignment.Center
        ) {
            val firstChord = chords.firstOrNull()
            if (firstChord?.bar == 1) {
                val signature = firstChord.signature ?: "4/4"
                val parts = signature.split('/')
                if (parts.size == 2) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val signatureTextStyle = MaterialTheme.typography.subtitle1.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp
                        )
                        Text(text = parts[0], style = signatureTextStyle)
                        Text(text = parts[1], style = signatureTextStyle)
                    }
                }
            }
        }

        for (i in 0 until 5) {
            Divider(modifier = Modifier.height(28.dp).width(2.dp), color = Color.Black)
            if (i < 4) {
                val chordEntry = chords.getOrNull(i)
                MeasureCell(
                    chord = chordEntry?.chords,
                    highlightColor = chordEntry?.let { highlightedBarColors[it.bar] },
                    onClick = { chordEntry?.let { onBarClick(it.bar) } }
                )
            }
        }
    }
}

@Composable
fun RowScope.MeasureCell(
    chord: String?,
    highlightColor: Color?,
    onClick: () -> Unit
) {
    val clickableModifier = if (highlightColor != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }
    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .then(clickableModifier)
            .background(highlightColor?.copy(alpha = 0.3f) ?: Color.Transparent)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (chord != null) {
            val chordParts = chord.split(' ')
            val fontSize = when {
                chord == "%" -> 24.sp
                chordParts.size > 1 -> 16.sp
                else -> 20.sp
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                chordParts.forEach { part ->
                    Text(
                        text = part,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
fun RecommendationSheetContent(
    isLoading: Boolean,
    recommendations: List<RecommendedPhrase>,
    onFetchMore: () -> Unit,
    playingPhraseId: Int?,
    loadingPhraseId: Int?,
    onPlayClick: (RecommendedPhrase) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val shouldFetchMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
            if (lastVisibleItemIndex != null && lastVisibleItemIndex >= recommendations.size - 1) {
                true
            } else {
                false
            }
        }
    }

    LaunchedEffect(shouldFetchMore) {
        if (shouldFetchMore && !isLoading) {
            onFetchMore()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.recommended_phrases_title),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(recommendations, key = { "${it.melid}-${it.startNoteIndex}" }) { phrase ->
                RecommendedPhraseItem(
                    phrase = phrase,
                    isPlaying = playingPhraseId == phrase.melid,
                    isLoading = loadingPhraseId == phrase.melid,
                    onPlayClick = { onPlayClick(phrase) }
                )
                Divider()
            }
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendedPhraseItem(
    phrase: RecommendedPhrase,
    isPlaying: Boolean,
    isLoading: Boolean,
    onPlayClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = phrase.title, style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Bold)
            Text(text = phrase.performer, style = MaterialTheme.typography.body2, color = Color.Gray)
        }
        IconButton(onClick = onPlayClick, enabled = !isLoading) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                isPlaying -> Icon(Icons.Default.Stop, contentDescription = stringResource(id = R.string.stop_playback_description))
                else -> Icon(Icons.Default.PlayArrow, contentDescription = stringResource(id = R.string.play_phrase_description))
            }
        }
    }
} 