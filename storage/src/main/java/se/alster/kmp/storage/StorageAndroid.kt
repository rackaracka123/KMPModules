package se.alster.kmp.storage

import java.io.File

class StorageAndroid(private val baseStorage: File): Storage {
    override suspend fun write(file: FilePath, data: ByteArray) {
        File(baseStorage, file.path).writeBytes(data)
    }

    override suspend fun read(file: FilePath): ByteArray {
        return File(baseStorage, file.path).readBytes()
    }

    override suspend fun delete(file: FilePath) : Boolean {
        return File(baseStorage, file.path).delete()
    }
}
