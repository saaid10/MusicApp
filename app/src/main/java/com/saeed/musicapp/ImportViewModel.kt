package com.saeed.musicapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import java.net.UnknownHostException

class ImportViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    // skipDuplicateCheck = true when the user chose Replace: overwrite the existing file
    fun importAudio(url: String, title: String, artist: String?, album: String?, skipDuplicateCheck: Boolean = false) {
        _uiState.value = ImportUiState.Importing

        viewModelScope.launch(Dispatchers.IO )
        {
            try{
                val importedAudio = getAudioStream(url)
                Log.d("MusicApp", "Audio URL: ${importedAudio.audioStream.url}, Format: ${importedAudio.audioStream.format} Bitrate: ${importedAudio.audioStream.averageBitrate} VideoId: ${importedAudio.videoId}")
                if (!skipDuplicateCheck && isAlreadyImported(importedAudio.videoId, context = getApplication<Application>()))
                {

                    Log.d("MusicApp", "Already in the library")
                    _uiState.value = ImportUiState.AlreadyInLibrary(importedAudio)
                }
                else
                {
                    val audioCache = downloadAudioToCache(importedAudio.audioStream.url!!, context =getApplication<Application>())
                    Log.d("MusicApp", "Download file: ${audioCache.absoluteFile}")
                    if (importedAudio.audioStream.format != MediaFormat.M4A)
                    {
                        withContext(Dispatchers.Main)
                        {
                            transcodeToAac(audioCache, context =getApplication<Application>(), importedAudio.videoId,
                                onSuccess = {_uiState.value = ImportUiState.Success(title, artist)},
                                onFailure = {message -> _uiState.value = ImportUiState.Failed(message, "Try again or use a different link") })
                        }
                    }
                    else
                    {
                        val movedToStorage = moveToPermanentStorage(file = audioCache, context = getApplication<Application>(), importedAudio.videoId)
                        _uiState.value = ImportUiState.Success(title, artist)
                        Log.d("MusicApp", "File saved permanently: ${movedToStorage.absolutePath}")
                    }
                }
            }
            catch (e: Exception)
            {
               val msg = when (e)
                {
                    is UnknownHostException -> "No Internet." to "Connect to start converting"
                    is IOException -> "Connection Problem." to "Check your internet connection"
                    is ContentNotAvailableException -> "Video private/blocked." to "Try another source"
                    is ExtractionException -> "Link not supported." to "Try another source"
                    else -> "Something went wrong." to "Try again or use different link"
                }
                _uiState.value = ImportUiState.Failed(msg.first, msg.second)
                Log.e("MusicApp", "The import failed", e)
            }
        }
    }

    fun resetState() {
        _uiState.value = ImportUiState.Idle
    }
}





