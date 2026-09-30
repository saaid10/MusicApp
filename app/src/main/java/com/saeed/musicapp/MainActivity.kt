package com.saeed.musicapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.timeago.patterns.it
import org.schabi.newpipe.extractor.MediaFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NewPipe.init(DownloaderImpl())
        Thread {
            try {
                val audioStream = getAudioStream("https://www.youtube.com/watch?v=Y4HWvsGs0rY")
                val audioCache = downloadAudioToCache(audioStream.url!!, context = this@MainActivity)
                if (audioStream.format != MediaFormat.M4A) {
                    runOnUiThread { transcodeToAac(audioCache, this@MainActivity) }
                }
                Log.d("MusicApp", "Audio URL: ${audioStream.url}, Format: ${audioStream.format} Bitrate: ${audioStream.averageBitrate}")
                Log.d("MusicApp", "Download file: ${audioCache.absoluteFile}")
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
