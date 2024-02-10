package se.alster.kmp.storage

import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.URLByAppendingPathComponent

fun FilePath.toNSURL(): NSURL = when (location) {
    Location.Documents -> NSFileManager.defaultManager.DocumentDirectory
        .URLByAppendingPathComponent(path)!!
}

// TODO: Fix this
fun NSURL.toFilePath(): FilePath {
    val basePath = path?.split("/")!!.let {
        it.subList(7, it.size).joinToString("/")
    }
    val directory = basePath.split("/").first()
    val path = basePath.removePrefix("$directory/")

    return when (directory) {
        "Documents" -> FilePath(path, Location.Documents)
        else -> throw IllegalArgumentException("Unknown directory: $directory")
    }
}

