package com.saeed.musicapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert (song: Song)
    @Query("SELECT * FROM SONGS ORDER BY DATEADDED DESC")
    fun getAllSongs(): Flow<List<Song>>
    @Delete
    suspend fun delete(song: Song)
}