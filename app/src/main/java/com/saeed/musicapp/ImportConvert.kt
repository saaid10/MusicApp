package com.saeed.musicapp

import android.R
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import okhttp3.OkHttpClient
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.timeago.patterns.fil
import java.io.File


data class ImportedAudio (val videoId: String, val audioStream: AudioStream)

fun getAudioStream(url: String): ImportedAudio {
    val info = StreamInfo.getInfo(url)
    if (info.audioStreams.isEmpty()) error("No audio streams found")
    val m4aStreams = info.audioStreams.filter { it.format == MediaFormat.M4A }
    val candidates = if (m4aStreams.isEmpty()) info.audioStreams else m4aStreams
    val knownBitrate = candidates.filter { it.averageBitrate > 0 }.ifEmpty { candidates }
    val withinLimit = knownBitrate.filter { it.averageBitrate <= 192 }
    val best = withinLimit.maxByOrNull { it.averageBitrate }
    val item = best ?: knownBitrate.minBy { it.averageBitrate }
    return ImportedAudio(videoId = info.id, audioStream = item)
}

fun downloadAudioToCache(url: String, context: Context): File {
    val client = OkHttpClient()
    val request = okhttp3.Request.Builder()
        .url(url)
        .header("Range", "bytes=0-")
        .build()

    val response = client.newCall(request).execute()
    if (!response.isSuccessful) error("Download failed code: ${response.code}")

    val inputStream = response.body?.byteStream()

    val outputFile = File(context.cacheDir, "raw_audio")

    val outputStream = outputFile.outputStream()
    inputStream?.copyTo(outputStream)
    outputStream.close()
    response.close()

    return outputFile
}

@OptIn(UnstableApi::class)
fun transcodeToAac(file: File, context: Context, videoId: String): Unit {
    val mediaItem = MediaItem.fromUri(Uri.fromFile(file))

    val outputDest = File(context.cacheDir, "converted_audio")

    val transformerBuilder = Transformer.Builder(context)
        .setAudioMimeType(MimeTypes.AUDIO_AAC)
        .addListener(object : Transformer.Listener {
            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                Log.d("MusicApp", "Transcode complete: ${outputDest.absolutePath}")
                file.delete()
                try {
                    val movedToStorage = moveToPermanentStorage(outputDest, context, videoId)
                    Log.d("MusicApp", "File moved to storage: ${movedToStorage.absolutePath}")
                }
                catch (e: Exception)
                {
                    Log.e("MusicApp", "The move failed", e)
                    outputDest.delete()
                }
            }

            override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {

                Log.e("MusicApp", "Transcode failed: ${outputDest.absolutePath}", exportException)
                file.delete()
            }
        })

    val transformer = transformerBuilder.build()
    transformer.start(mediaItem, outputDest.absolutePath)
}

fun moveToPermanentStorage(file: File, context: Context, videoId: String): File {
    val destination = File(context.filesDir, "${videoId}.m4a" )
    val fileTransfer = file.copyTo(destination, true)
    file.delete()
    return fileTransfer
}

fun isAlreadyImported(videoId: String, context: Context): Boolean{
    val destination = File(context.filesDir, "${videoId}.m4a" )
    return destination.exists()
}
