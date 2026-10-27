package com.saeed.musicapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    val songs: StateFlow<List<Song>> = getApplication<MusicApplication>().database.songDao().getAllSongs().
    stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}