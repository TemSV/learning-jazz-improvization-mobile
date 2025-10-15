package com.example.learning_jazz_app.ui.songs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
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
import com.example.learning_jazz_app.domain.model.Song

@Composable
fun SongsScreen(
    viewModel: SongsViewModel = viewModel(factory = SongsViewModel.Factory),
    onSongClick: (Int, String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.songs_screen_title)) },
                backgroundColor = MaterialTheme.colors.surface,
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(id = R.string.error_placeholder, uiState.error!!))
                }
            }
            else -> {
                SongList(
                    songs = uiState.songs,
                    modifier = Modifier.padding(padding),
                    onSongClick = onSongClick
                )
            }
        }
    }
}

@Composable
fun SongList(
    songs: List<Song>,
    modifier: Modifier = Modifier,
    onSongClick: (Int, String, String) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(songs) { song ->
            SongCard(
                song = song,
                onSongClick = { onSongClick(song.id, song.title, song.composer) }
            )
            Divider(color = Color.DarkGray, thickness = 1.dp)
        }
    }
}

@Composable
fun SongCard(
    song: Song,
    onSongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSongClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.h6.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.composer,
                style = MaterialTheme.typography.body1,
                color = Color.Gray
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = song.key,
                style = MaterialTheme.typography.body1,
                color = Color.LightGray,
                fontSize = 18.sp
            )
            IconButton(onClick = { /* Handle more options */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.LightGray
                )
            }
        }
    }
} 