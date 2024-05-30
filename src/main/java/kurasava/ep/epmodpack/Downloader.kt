package kurasava.ep.epmodpack

import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import kotlin.io.path.outputStream

object Downloader {

    private val threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    fun download(url: URL, directory: Path): CompletableFuture<Path> = CompletableFuture.supplyAsync({
        val file = directory.resolve(url.toURI().path.substring(url.toURI().path.lastIndexOf("/") + 1))
        this.openUrl(url).use { input -> file.outputStream().use { output -> input.copyTo(output) } }
        return@supplyAsync file
    }, this.threadPool)

    fun downloadMod(mod: Mod, version: String, directory: Path): CompletableFuture<Path> {
        val url = URL(mod.versions[version])
        return this.download(url, directory)
    }

    private fun openUrl(url: URL): InputStream {
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 8000
        connection.readTimeout = 8000
        connection.connect()
        val responseCode = connection.responseCode
        if (responseCode in 200..299) return connection.inputStream
        throw IOException("HTTP request to $url failed: $responseCode")
    }
}
