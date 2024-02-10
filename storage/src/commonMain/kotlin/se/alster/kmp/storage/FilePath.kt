package se.alster.kmp.storage

data class FilePath(val path: String, val location: Location)

enum class Location {
    Documents
}
