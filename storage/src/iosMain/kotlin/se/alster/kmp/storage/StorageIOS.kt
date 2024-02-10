package se.alster.kmp.storage

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.URLByAppendingPathComponent
import platform.Foundation.dataWithBytes
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToURL
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
val NSFileManager.DocumentDirectory
    get() = URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        create = true,
        appropriateForURL = null,
        error = null
    )!!


class StorageIOS : Storage {
    override suspend fun write(file: FilePath, data: ByteArray) {
        NSFileManager.defaultManager.DocumentDirectory
            .URLByAppendingPathComponent(file.path)?.let {
                println("Writing to $it")
                data.toData().writeToURL(
                    url = it,
                    atomically = true
                )
            }
    }

    override suspend fun read(file: FilePath): ByteArray {
        return runBlocking {
            println("Reading from ${file.path}")
            NSFileManager.defaultManager.DocumentDirectory
                .URLByAppendingPathComponent(file.path)?.readBytes()!!
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun delete(file: FilePath): Boolean {
        NSFileManager.defaultManager.DocumentDirectory
            .URLByAppendingPathComponent(file.path)?.let {
                NSFileManager.defaultManager.removeItemAtURL(it, error = null)
            }
        return !NSFileManager.defaultManager.fileExistsAtPath(
            NSFileManager.defaultManager.DocumentDirectory
                .URLByAppendingPathComponent(file.path)?.path!!
        )
    }

    private suspend fun NSURL.readData(): NSData {
        while (true) {
            val data = NSData.dataWithContentsOfURL(this)
            if (data != null)
                return data
            yield()
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun NSURL.readBytes(): ByteArray =
        with(readData()) {
            ByteArray(length.toInt()).apply {
                usePinned {
                    memcpy(it.addressOf(0), bytes, length)
                }
            }
        }
}
