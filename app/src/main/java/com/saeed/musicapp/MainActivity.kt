package com.saeed.musicapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.MediaFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NewPipe.init(DownloaderImpl())
        Thread {
            try {
                val importedAudio = getAudioStream("https://www.youtube.com/watch?v=Y4HWvsGs0rY")
                Log.d("MusicApp", "Audio URL: ${importedAudio.audioStream.url}, Format: ${importedAudio.audioStream.format} Bitrate: ${importedAudio.audioStream.averageBitrate} VideoId: ${importedAudio.videoId}")
                if (isAlreadyImported(importedAudio.videoId, context = this@MainActivity))
                {
                    Log.d("MusicApp", "Already in the library")
                }
                else
                {
                    val audioCache = downloadAudioToCache(importedAudio.audioStream.url!!, context = this@MainActivity)
                    Log.d("MusicApp", "Download file: ${audioCache.absoluteFile}")
                    if (importedAudio.audioStream.format != MediaFormat.M4A) {
                        runOnUiThread { transcodeToAac(audioCache, this@MainActivity, importedAudio.videoId) }
                    }
                    else {
                        val movedToStorage = moveToPermanentStorage(file = audioCache , this@MainActivity, importedAudio.videoId )
                        Log.d("MusicApp", "File saved permanently: ${movedToStorage.absolutePath}")
                    }
                }
            }
            catch (e: Exception)
            {
                Log.e("MusicApp", "The import failed", e)
            }

        }.start()
        setContent {
            MaterialTheme {
                Surface {
                    Text("MusicApp")
                }
            }
        }
    }
}
