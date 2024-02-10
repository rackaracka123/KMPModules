package se.alster.kmp.storage

// TODO suspend these functions
interface Storage {
    fun write(file: FilePath, data: ByteArray)
    fun read(file: FilePath): ByteArray
    fun delete(file: FilePath): Boolean
}
