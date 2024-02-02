package se.alster.kmp.media.camera

sealed interface CameraState {
    data object Undefined : CameraState
    sealed class Access: CameraState {
        data object Authorized : Access()
        data object Denied : Access()
    }
    data object Simulator : CameraState
}
