package com.example.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String = "Unknown Album",
    val durationMs: Long = 180000,
    val data: String? = null, // Path to local file if scanned
    val lyrics: String? = null,
    val isLocal: Boolean = false
)

data class Playlist(
    val id: String,
    val name: String,
    val description: String,
    val songs: List<Song> = emptyList()
)
