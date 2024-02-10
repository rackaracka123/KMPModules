package se.alster.kmp.storage

interface Storage {
    suspend fun write(file: FilePath, data: ByteArray)
    suspend fun read(file: FilePath): ByteArray
    suspend fun delete(file: FilePath): Boolean
}
