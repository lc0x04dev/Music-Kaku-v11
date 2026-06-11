package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FeaturedPlayList
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonLavender
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.viewmodel.MusicViewModel

@Composable
fun PlaylistsScreen(
    viewModel: MusicViewModel,
    onNavigateToPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playlists by viewModel.playlists.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    
    var selectedPlaylistId by remember { mutableStateOf<String?>("favs") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "MIS LISTAS",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                letterSpacing = 1.sp,
                modifier = Modifier.testTag("playlists_title")
            )
            Text(
                text = "Organiza tu experiencia musical con acentos cibernéticos",
                fontSize = 12.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Horizontal Row of curated playlists
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                playlists.forEach { playlist ->
                    val isSelected = selectedPlaylistId == playlist.id
                    val borderTint = if (isSelected) NeonMagenta else Color.White.copy(alpha = 0.04f)
                    val playlistBg = if (isSelected) NeonMagenta.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

                    Card(
                        colors = CardDefaults.cardColors(containerColor = playlistBg),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.2.dp, borderTint, RoundedCornerShape(16.dp))
                            .clickable { selectedPlaylistId = playlist.id }
                            .testTag("playlist_card_${playlist.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(NeonMagenta, NeonLavender)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FeaturedPlayList,
                                    contentDescription = "Playlist representation",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = playlist.name,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = playlist.description,
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Arrow right details",
                                tint = if (isSelected) NeonCyan else TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Selected playlist tracks display header
        val currentPlaylist = playlists.find { it.id == selectedPlaylistId }
        if (currentPlaylist != null) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Temas en \"${currentPlaylist.name}\"",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Text(
                        text = "${currentPlaylist.songs.size} pistas",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            items(currentPlaylist.songs) { song ->
                val isSelected = currentSong?.id == song.id
                val trackBorder = if (isSelected) NeonCyan.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.03f)
                val trackBg = if (isSelected) NeonCyan.copy(alpha = 0.05f) else Color.Transparent

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(trackBg)
                        .border(1.dp, trackBorder, RoundedCornerShape(12.dp))
                        .clickable { viewModel.playSong(song) }
                        .padding(12.dp)
                        .testTag("playlist_track_${song.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.Audiotrack else Icons.Default.List,
                            contentDescription = "Track status",
                            tint = if (isSelected) NeonCyan else NeonLavender,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = song.title,
                            color = if (isSelected) NeonCyan else TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play immediately",
                        tint = if (isSelected) NeonCyan else TextMuted.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(130.dp))
        }
    }
}
