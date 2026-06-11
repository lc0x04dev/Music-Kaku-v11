package com.example.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Playlist
import com.example.model.Song
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MusicViewModel : ViewModel() {

    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    val allSongs: StateFlow<List<Song>> = _allSongs

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs

    private val _permissionState = MutableStateFlow<Boolean?>(null) // null = not defined, true = granted, false = denied
    val permissionState: StateFlow<Boolean?> = _permissionState

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Filter songs based on search query
    val filteredSongs: StateFlow<List<Song>> = combine(_allSongs, _searchQuery) { songs, query ->
        if (query.isBlank()) {
            songs
        } else {
            songs.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true) ||
                        it.album.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    // Preloaded mock songs for design, testing, and fallback on emulator
    private val mockSongs = listOf(
        Song(
            id = -1,
            title = "Sabor Neón",
            artist = "Lumina",
            album = "HyperLight Vol. 1",
            durationMs = 186000,
            lyrics = "Letra de [Sabor Neón]\n\n" +
                    "Caminando bajo las luces de la ciudad\n" +
                    "Con el brillo cian de mi soledad.\n" +
                    "El asfalto húmedo refleja el color\n" +
                    "De un futuro frío sin tu calor.\n\n" +
                    "(Coro)\n" +
                    "Bailando en la lluvia de neón\n" +
                    "Sintiendo los latidos del corazón.\n" +
                    "Eres la frecuencia de mi canción...\n\n" +
                    "Fluyendo en el vacío sideral\n" +
                    "Un ritmo magenta y artificial.\n\n" +
                    "Perdido en tu red, ya no sé volver\n" +
                    "El tiempo es efímero y empieza a llover."
        ),
        Song(
            id = -2,
            title = "Cyberbeat",
            artist = "RetroSynth",
            album = "Retrowave Anthems",
            durationMs = 212000,
            lyrics = "Letra de [Cyberbeat]\n\n" +
                    "Cables y silicio, impulsos en la red\n" +
                    "Una melodía saciando mi sed.\n" +
                    "Un latido sordo en el sintetizador\n" +
                    "Borrando el silencio, sembrando el calor.\n\n" +
                    "(Coro)\n" +
                    "Cyberbeat, cyberbeat en tu piel\n" +
                    "Las líneas de código caen a granel.\n" +
                    "Un bajo profundo te hará renacer\n" +
                    "En el mundo virtual vas a vencer.\n\n" +
                    "El ciberespacio se tiñe de azul\n" +
                    "Flota con el beat bajo de este tul."
        ),
        Song(
            id = -3,
            title = "Melodía Perdida",
            artist = "Vaporwave Boy",
            album = "Aesthetics 1995",
            durationMs = 145000,
            lyrics = null // Test empty / null lyrics state
        ),
        Song(
            id = -4,
            title = "Sinfonía Violeta",
            artist = "Lavanda Waves",
            album = "Purple Dust",
            durationMs = 198000,
            lyrics = "Letra de [Sinfonía Violeta]\n\n" +
                    "Un velo lavanda sobre el horizonte\n" +
                    "Una voz que canta desde el monte.\n" +
                    "El piano destila gotas de cristal\n" +
                    "En esta sintonía espiritual.\n\n" +
                    "(Coro)\n" +
                    "La sinfonía violeta empezó\n" +
                    "El universo entero se suspendió.\n" +
                    "Colores de ensueño en mi mente azul\n" +
                    "Cubriéndolo todo con un suave tul.\n\n" +
                    "Respira el color, suspira la voz\n" +
                    "Somos un destello en la mente de Dios."
        )
    )

    init {
        // Load initial mock songs
        _allSongs.value = mockSongs
        _currentSong.value = mockSongs.firstOrNull()
        
        // Initialize simple playlists
        _playlists.value = listOf(
            Playlist("favs", "Favoritos Neón", "Las pistas más brillantes de tu catálogo", listOf(mockSongs[0], mockSongs[1])),
            Playlist("chill", "Lavanda Chill", "Música suave para relajarse y programar", listOf(mockSongs[2], mockSongs[3])),
            Playlist("cyber", "Cyberpunk Focus", "Electrónica pesada con bajos intensos", mockSongs)
        )
    }

    fun setPermissionState(granted: Boolean) {
        _permissionState.value = granted
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun scanLocalAudio(context: Context) {
        viewModelScope.launch {
            try {
                val localSongs = mutableListOf<Song>()
                val resolver: ContentResolver = context.contentResolver
                val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA
                )

                val cursor: Cursor? = resolver.query(uri, projection, selection, null, "${MediaStore.Audio.Media.TITLE} ASC")
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                        val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                        val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                        val album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                        val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                        val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))

                        localSongs.add(
                            Song(
                                id = id,
                                title = title,
                                artist = if (artist == "<unknown>") "Artistas Locales" else artist,
                                album = album,
                                durationMs = if (duration <= 0) 180000 else duration,
                                data = path,
                                lyrics = "Letra local autogenerada para $title\n\nDisfruta de este track cargado desde el almacenamiento de tu dispositivo.\nNo hay letra disponible de forma síncrona en el archivo local por el momento.",
                                isLocal = true
                            )
                        )
                    }
                    cursor.close()
                }

                // If any local songs are found, combine them with mock songs (or prepend them)
                if (localSongs.isNotEmpty()) {
                    val combined = localSongs + mockSongs
                    _allSongs.value = combined
                    _currentSong.value = combined.firstOrNull()
                    
                    // Update main playlist with combined
                    _playlists.value = _playlists.value.map {
                        if (it.id == "cyber") it.copy(songs = combined) else it
                    }
                    Log.d("MusicViewModel", "Escaneadas ${localSongs.size} canciones locales correctamente.")
                } else {
                    Log.d("MusicViewModel", "No se encontraron canciones locales. Usando modo simulación con mock assets.")
                }
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error al escanear pistas de audio: ", e)
            }
        }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            // Stop current player
            stopPlayback()
            _currentSong.value = song
            _playbackPositionMs.value = 0L

            if (song.isLocal && song.data != null) {
                try {
                    val file = File(song.data)
                    if (file.exists()) {
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(song.data)
                            prepare()
                            start()
                            setOnCompletionListener {
                                nextSong()
                            }
                        }
                        _isPlaying.value = true
                        startProgressTicker()
                    } else {
                        // Fallback simulation if file removed or unreachable
                        simulatePlayback()
                    }
                } catch (e: Exception) {
                    Log.e("MusicViewModel", "No se pudo reproducir el archivo físico local: ${e.message}. Simulando...", e)
                    simulatePlayback()
                }
            } else {
                // Mock song playback via simulated ticks
                simulatePlayback()
            }
        }
    }

    private fun simulatePlayback() {
        _isPlaying.value = true
        startProgressTicker()
    }

    fun togglePlayPause() {
        val current = _currentSong.value ?: return
        if (_isPlaying.value) {
            _isPlaying.value = false
            progressJob?.cancel()
            try {
                mediaPlayer?.pause()
            } catch (e: Exception) {
                // ignore
            }
        } else {
            _isPlaying.value = true
            if (current.isLocal && mediaPlayer != null) {
                try {
                    mediaPlayer?.start()
                } catch (e: Exception) {
                    // Refetch payload if player was cleared
                    playSong(current)
                    return
                }
                startProgressTicker()
            } else {
                startProgressTicker()
            }
        }
    }

    fun nextSong() {
        val songsList = _allSongs.value
        if (songsList.isEmpty()) return
        val current = _currentSong.value ?: return
        val currentIndex = songsList.indexOfFirst { it.id == current.id }
        val nextIndex = if (currentIndex == -1 || currentIndex == songsList.size -1) 0 else currentIndex + 1
        playSong(songsList[nextIndex])
    }

    fun previousSong() {
        val songsList = _allSongs.value
        if (songsList.isEmpty()) return
        val current = _currentSong.value ?: return
        val currentIndex = songsList.indexOfFirst { it.id == current.id }
        val prevIndex = if (currentIndex <= 0) songsList.size - 1 else currentIndex - 1
        playSong(songsList[prevIndex])
    }

    fun seekTo(positionMs: Long) {
        val current = _currentSong.value ?: return
        val validPos = positionMs.coerceIn(0, current.durationMs)
        _playbackPositionMs.value = validPos
        if (current.isLocal && mediaPlayer != null) {
            try {
                mediaPlayer?.seekTo(validPos.toInt())
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (_isPlaying.value) {
                delay(100)
                val currentSongDuration = _currentSong.value?.durationMs ?: 180000L
                val currentPos = if (mediaPlayer != null && _currentSong.value?.isLocal == true) {
                    try {
                        mediaPlayer!!.currentPosition.toLong()
                    } catch (e: Exception) {
                        _playbackPositionMs.value + 100
                    }
                } else {
                    _playbackPositionMs.value + 100
                }

                if (currentPos >= currentSongDuration) {
                    _playbackPositionMs.value = currentSongDuration
                    _isPlaying.value = false
                    nextSong()
                    break
                } else {
                    _playbackPositionMs.value = currentPos
                }
            }
        }
    }

    private fun stopPlayback() {
        _isPlaying.value = false
        progressJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
        mediaPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }
}
