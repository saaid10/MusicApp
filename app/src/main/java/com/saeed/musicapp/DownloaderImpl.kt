package com.saeed.musicapp


import okhttp3.OkHttpClient
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import okhttp3.RequestBody.Companion.toRequestBody

    class DownloaderImpl : Downloader() {
        override fun execute(request: Request): Response {

            val client = OkHttpClient()
            val body = request.dataToSend()?.toRequestBody()

            val requestBuilder = okhttp3.Request.Builder()
                .url(request.url())
                .method(request.httpMethod(), body)

                request.headers().forEach { (headerName, headerValueList) ->
                headerValueList.forEach { value ->
                    requestBuilder.addHeader(headerName, value)
                }
            }

            val okRequest = requestBuilder.build()

            val okResponse = client.newCall(okRequest).execute()

            val response = Response(okResponse.code,
                okResponse.message,
                okResponse.headers.toMultimap(),
                okResponse.body?.string(),
                okResponse.request.url.toString())

            return response
        }
    }