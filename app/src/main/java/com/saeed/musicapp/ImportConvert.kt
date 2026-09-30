package com.saeed.musicapp

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.transformer.Composition
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import okhttp3.OkHttpClient
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.StreamInfo
import java.io.File

fun getAudioStream(url: String): AudioStream {
    val info = StreamInfo.getInfo(url)
    if (info.audioStreams.isEmpty()) error("No audio streams found")
    val m4aStreams = info.audioStreams.filter { it.format == MediaFormat.M4A }
    val candidates = if (m4aStreams.isEmpty()) info.audioStreams else m4aStreams
    val knownBitrate = candidates.filter { it.averageBitrate > 0 }.ifEmpty { candidates }
    val withinLimit = knownBitrate.filter { it.averageBitrate <= 192 }
    val best = withinLimit.maxByOrNull { it.averageBitrate }
    val item = best ?: knownBitrate.minBy { it.averageBitrate }
    return item
}

fun downloadAudioToCache(url: String, context: Context): File {
    val client = OkHttpClient()
    val request = okhttp3.Request.Builder()
        .url(url)
        .header("Range", "bytes=0-")
        .build()

    val response = client.newCall(request).execute()

    val inputStream = response.body?.byteStream()

    val outputFile = File(context.cacheDir, "raw_audio")

    val outputStream = outputFile.outputStream()
    inputStream?.copyTo(outputStream)
    outputStream.close()
    response.close()

    return outputFile
}

fun transcodeToAac(file: File, context: Context): Unit {
    val mediaItem = MediaItem.fromUri(Uri.fromFile(file))

    val outputDest = File(context.cacheDir, "converted_audio")

    val transformerBuilder = Transformer.Builder(context)
        .setAudioMimeType(MimeTypes.AUDIO_AAC)
        .addListener(object : Transformer.Listener {
            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                Log.d("MusicApp", "Transcode complete: ${outputDest.absolutePath}")
            }

            override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                Log.e("MusicApp", "Transcode failed: ${outputDest.absolutePath}", exportException)
            }
        })

    val transformer = transformerBuilder.build()
    transformer.start(mediaItem, outputDest.absolutePath)
}