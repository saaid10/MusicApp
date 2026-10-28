package com.saeed.musicapp

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import java.io.File

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val player = ExoPlayer.Builder(application).build()

    fun playSong(song: Song) {
        val file = File(getApplication<Application>().filesDir, "${song.videoId}.m4a" )
        val mediaItem = MediaItem.fromUri(Uri.fromFile(file))
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    // Runs when the ViewModel is destroyed for good (app closed, not rotation)
    override fun onCleared() {
        player.release()
    }
}
