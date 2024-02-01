package se.alster.kmp.media.camera

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import se.alster.kmp.media.extensions.moveToByteArray
import se.alster.kmp.media.extensions.toImageBitmap

class ImagePicturedCallbackAndroid(private val onPictureTaken: (CaptureResultAndroid) -> Unit) :
    OnImageCapturedCallback() {
    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun onCaptureSuccess(image: ImageProxy) {
        super.onCaptureSuccess(image)
        image.image?.planes?.getOrNull(0)?.buffer
            ?.moveToByteArray()
            ?.toImageBitmap()
            ?.let { onPictureTaken.invoke(CaptureResultAndroid.Success(it)) }
            ?: onPictureTaken.invoke(CaptureResultAndroid.Failure)
        image.close()
    }

    override fun onError(exception: ImageCaptureException) {
        super.onError(exception)
        onPictureTaken.invoke(CaptureResultAndroid.Failure)
    }
}
