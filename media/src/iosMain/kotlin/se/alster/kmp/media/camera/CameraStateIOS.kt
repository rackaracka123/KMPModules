package se.alster.kmp.media.camera

sealed interface CameraStateIOS {
    data object Undefined : CameraStateIOS
    sealed class Access: CameraStateIOS {
        data object Authorized : Access()
        data object Denied : Access()
    }
    data object Simulator : CameraStateIOS
}
