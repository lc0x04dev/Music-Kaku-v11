package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonLavender
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.viewmodel.MusicViewModel
import java.util.Locale

@Composable
fun PlayerScreen(
    viewModel: MusicViewModel,
    onMinimize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackPositionMs by viewModel.playbackPositionMs.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper status bar and back minimize launcher
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMinimize,
                modifier = Modifier.testTag("player_minimize_button")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Minimize player",
                    tint = TextWhite,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = "REPRODUCIENDO",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NeonLavender,
                letterSpacing = 2.sp
            )
            Box(modifier = Modifier.size(32.dp)) // Equalizer placeholder or space
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large Album Art Box with Neon pulsing glows
        val scalePulse by animateFloatAsState(targetValue = if (isPlaying) 1.02f else 1.0f)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f * scalePulse)
                .aspectRatio(1f)
                .testTag("album_art_container"),
            contentAlignment = Alignment.BottomEnd
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .testTag("album_art_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    NeonMagenta.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(NeonMagenta.copy(alpha = 0.1f))
                            .border(1.5.dp, NeonMagenta.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(NeonMagenta)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = "Song artwork shape",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(32.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }

            // LOSSLESS Badge absolute overlay
            Box(
                modifier = Modifier
                    .padding(end = 16.dp, bottom = 16.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(NeonLavender)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "LOSSLESS",
                    color = Color.Black,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Titles and artist descriptions
        Text(
            text = currentSong?.title ?: "No seleccionado",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("player_song_title")
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = currentSong?.artist ?: "Selecciona una canción en el Inicio",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = NeonMagenta,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("player_song_artist")
        )
        Text(
            text = currentSong?.album ?: "",
            fontSize = 12.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Seek Slider progress controllers
        val durationMs = currentSong?.durationMs ?: 180000L
        val progressRatio = (playbackPositionMs.toFloat() / durationMs).coerceIn(0f, 1f)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = formatTime(playbackPositionMs), fontSize = 11.sp, color = TextMuted)
            Text(text = formatTime(durationMs), fontSize = 11.sp, color = TextMuted)
        }

        Slider(
            value = progressRatio,
            onValueChange = { ratio ->
                val seekPos = (ratio * durationMs).toLong()
                viewModel.seekTo(seekPos)
            },
            colors = SliderDefaults.colors(
                thumbColor = NeonCyan,
                activeTrackColor = NeonCyan,
                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("player_seek_slider")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Playback command buttons: Prev | Play/Pause FAB | Next
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.previousSong() },
                modifier = Modifier
                    .size(56.dp)
                    .testTag("player_prev_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Song",
                    tint = NeonLavender,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(28.dp))

            FilledIconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier
                    .size(72.dp)
                    .border(1.dp, NeonCyan, CircleShape)
                    .testTag("player_play_pause_fab"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = NeonMagenta,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(28.dp))

            IconButton(
                onClick = { viewModel.nextSong() },
                modifier = Modifier
                    .size(56.dp)
                    .testTag("player_next_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Song",
                    tint = NeonLavender,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // SISTEMA DE LETRAS DE CANCIONES (Lyrics Verification System)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(20.dp))
                .padding(20.dp)
                .testTag("lyrics_container")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lyrics,
                            contentDescription = "Lyrics Icon",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Letra de Canción",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .border(1.dp, NeonCyan.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "VERIFIED",
                            color = NeonCyan,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val lyricsText = currentSong?.lyrics
                
                // Technical Conditonal lyric validation implementation
                if (!lyricsText.isNullOrBlank()) {
                    Text(
                        text = lyricsText,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lyrics_text")
                    )
                } else {
                    // Styled elegant validation fallback avoiding empty containers
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("lyrics_empty_state"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No hay letra disponible para esta canción.",
                            color = NeonLavender.copy(alpha = 0.7f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Las pistas sin metadata de texto se muestran vacías de forma limpia.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

// Format durationMs to a standard mm:ss
private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format(Locale.getDefault(), "%02d:%02d", min, sec)
}
