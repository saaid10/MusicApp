package com.saeed.musicapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.MediaFormat

class ImportViewModel(application: Application) : AndroidViewModel(application) {
    fun importAudio(url: String) {
        viewModelScope.launch(Dispatchers.IO )
        {
            try{
                val importedAudio = getAudioStream(url)
                Log.d("MusicApp", "Audio URL: ${importedAudio.audioStream.url}, Format: ${importedAudio.audioStream.format} Bitrate: ${importedAudio.audioStream.averageBitrate} VideoId: ${importedAudio.videoId}")
                if (isAlreadyImported(importedAudio.videoId, context = getApplication<Application>()))
                {
                    Log.d("MusicApp", "Already in the library")
                }
                else
                {
                    val audioCache = downloadAudioToCache(importedAudio.audioStream.url!!, context =getApplication<Application>())
                    Log.d("MusicApp", "Download file: ${audioCache.absoluteFile}")
                    if (importedAudio.audioStream.format != MediaFormat.M4A)
                    {
                        withContext(Dispatchers.Main)
                        {
                            transcodeToAac(audioCache, context =getApplication<Application>(), importedAudio.videoId)
                        }
                    }
                    else
                    {
                        val movedToStorage = moveToPermanentStorage(file = audioCache, context = getApplication<Application>(), importedAudio.videoId)
                        Log.d("MusicApp", "File saved permanently: ${movedToStorage.absolutePath}")
                    }
                }
            }
            catch (e: Exception)
            {
                Log.e("MusicApp", "The import failed", e)
            }
        }
    }
}





