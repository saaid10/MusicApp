package com.saeed.musicapp

import android.app.Application
import androidx.room.Room

class MusicApplication() : Application()
{
    val database by lazy { Room.databaseBuilder(this, MusicDatabase::class.java, "music.db").build() }
}