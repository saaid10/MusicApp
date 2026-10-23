package com.saeed.musicapp

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity (tableName = "songs")
data class Song(@PrimaryKey val videoId: String, val title: String, val artist: String?, val album: String?, val dateAdded: Long, val duration: Long?)

