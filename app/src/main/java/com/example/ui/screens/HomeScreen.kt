package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.Song
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonLavender
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.viewmodel.MusicViewModel

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    onNavigateToPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val permissionState by viewModel.permissionState.collectAsState()
    val songs by viewModel.allSongs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()

    // Determine target permission based on API level
    val targetPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    // Permission launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setPermissionState(isGranted)
        if (isGranted) {
            viewModel.scanLocalAudio(context)
            Toast.makeText(context, "Permiso de almacenamiento concedido.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permiso denegado. Se usarán pistas sintéticas.", Toast.LENGTH_LONG).show()
        }
    }

    // Check actual permission on mount
    LaunchedEffect(Unit) {
        val status = ContextCompat.checkSelfPermission(context, targetPermission) == PackageManager.PERMISSION_GRANTED
        viewModel.setPermissionState(status)
        if (status) {
            viewModel.scanLocalAudio(context)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Futuristic Glowing Header
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                NeonMagenta.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(1.dp, NeonMagenta.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "NEONBEAT",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonCyan,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.testTag("app_logo_title")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Siente el ritmo del espectro cibernético",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // STORAGE PERMISSIONS SYSTEM BANNER
        item {
            AnimatedVisibility(visible = permissionState != true) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E0A12))
                        .border(1.dp, NeonMagenta, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                        .testTag("permission_panel")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Security alert",
                                tint = NeonMagenta,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Permiso de Almacenamiento",
                                color = TextWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Para reproducir tu música local requerimos acceso al almacenamiento de audio. Dispositivos con Android 13+ solicitan exclusivamente archivos multimedia de audio.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { launcher.launch(targetPermission) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonMagenta,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("request_permission_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FolderOpen, contentDescription = "Folder open")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Vincular Música Local", fontWeight = FontWeight.Bold)
                            }
                        }
                        if (permissionState == false) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "Denied icon", tint = NeonLavender, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Permiso denegado. Se activaron pistas neón de prueba.",
                                    color = NeonLavender,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Horizontal Featured Tracks (Row)
        item {
            Column {
                Text(
                    text = "Más Escuchadas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(songs.take(3)) { song ->
                        val isSelected = currentSong?.id == song.id
                        val borderGlow by animateColorAsState(if (isSelected) NeonCyan else Color.Transparent)

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .width(150.dp)
                                .border(1.5.dp, borderGlow, RoundedCornerShape(14.dp))
                                .clickable { viewModel.playSong(song) }
                                .testTag("featured_song_${song.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(126.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            Brush.sweepGradient(
                                                colors = listOf(
                                                    NeonCyan.copy(alpha = 0.3f),
                                                    NeonMagenta.copy(alpha = 0.3f),
                                                    NeonCyan.copy(alpha = 0.3f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = "Song artwork tint",
                                        tint = if (isSelected) NeonCyan else NeonLavender,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = song.title,
                                    color = TextWhite,
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
                        }
                    }
                }
            }
        }

        // All Songs List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Todas tus Canciones",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "${songs.size} pistas",
                    fontSize = 12.sp,
                    color = NeonLavender
                )
            }
        }

        // Songs matching
        items(songs) { song ->
            val isSelected = currentSong?.id == song.id
            val bgTint = if (isSelected) NeonCyan.copy(alpha = 0.08f) else Color.Transparent
            val borderTint = if (isSelected) NeonCyan.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.04f)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgTint)
                    .border(1.dp, borderTint, RoundedCornerShape(12.dp))
                    .clickable { viewModel.playSong(song) }
                    .padding(12.dp)
                    .testTag("song_row_${song.id}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Number or Soundwave icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = "Active tracks status",
                            tint = NeonMagenta,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = if (song.isLocal) "LOC" else "NEON",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (song.isLocal) NeonCyan else NeonLavender
                        )
                    }
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
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = song.artist,
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Play icon or indicator
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play button indicator",
                    tint = if (isSelected) NeonCyan else TextMuted.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Bottom space so list content isn't clipped behind bottom bar + mini player
        item {
            Spacer(modifier = Modifier.height(130.dp))
        }
    }
}
