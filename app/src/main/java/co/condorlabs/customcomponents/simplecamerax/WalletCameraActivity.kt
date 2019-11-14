package co.condorlabs.customcomponents.simplecamerax

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import co.condorlabs.customcomponents.CAMERA_TAKE_PHOTO_PARAM
import co.condorlabs.customcomponents.R
import co.condorlabs.customcomponents.simplecamerax.fragment.SimpleCameraXFragment
import kotlinx.android.synthetic.main.activity_wallet_camera.*
import java.io.ByteArrayOutputStream

class WalletCameraActivity : AppCompatActivity(), SimpleCameraXFragment.OnCameraXListener {

    private lateinit var cameraFragment: SimpleCameraXFragment
    private var urlToSavePhoto: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_wallet_camera)
        cameraFragment = simpleCameraXFragment as SimpleCameraXFragment
        urlToSavePhoto = intent.getStringExtra(URL_TO_SAVE_PHOTO_EXTRA_PARAM)
    }

    override fun fragmentTextureViewLoaded() {
        initCameraX()
        cancelPhoto?.setOnClickListener {
            resetLayout()
        }
    }

    private fun initCameraX() {
        cameraFragment.startCamera()
        captureButton?.setOnClickListener {
            (captureButton?.drawable as? Animatable)?.start()
            cameraFragment.takePhoto(urlToSavePhoto)
        }
    }

    override fun onCaptureSuccess(bitmap: Bitmap) {
        showImageBitmap(bitmap)
    }

    override fun onImageSaved(bitmap: Bitmap) {
        showImageBitmap(bitmap)
    }

    private fun showImageBitmap(bitmap: Bitmap) {
        val bitmapHeight = bitmap.height
        val bitmapWidth = bitmap.width

        if (bitmapWidth > bitmapHeight) {
            val matrix = Matrix()
            matrix.postRotate(90f)
            val scaledBitmap = Bitmap.createScaledBitmap(
                bitmap,
                bitmapWidth,
                bitmapHeight,
                true
            )
            val rotatedBitmap = Bitmap.createBitmap(
                scaledBitmap,
                0,
                0,
                scaledBitmap.width,
                scaledBitmap.height,
                matrix,
                true
            )
            photoCaptured?.setImageBitmap(rotatedBitmap)
        } else {
            photoCaptured?.setImageBitmap(bitmap)
        }

        cropPhoto?.run {
            visibility = View.VISIBLE
            setOnClickListener {
                photoCaptured?.cropImage()?.let { bitmapResult ->
                    val bStream = ByteArrayOutputStream()
                    bitmapResult.compress(Bitmap.CompressFormat.JPEG, 100, bStream)
                    val byteArray = bStream.toByteArray()

                    val intent = Intent()
                    intent.putExtra(CAMERA_TAKE_PHOTO_PARAM, byteArray)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }
        cancelPhoto?.visibility = View.VISIBLE
        captureButton?.visibility = View.INVISIBLE
        capturePhotoDescription?.visibility = View.INVISIBLE
    }

    override fun onError(
        imageCaptureError: ImageCapture.ImageCaptureError,
        message: String,
        cause: Throwable?
    ) {

    }

    override fun onBackPressed() {
        if (captureButton?.visibility == View.INVISIBLE) {
            resetLayout()
        } else {
            super.onBackPressed()
        }
    }

    private fun resetLayout() {
        photoCaptured?.setImageBitmap(null)
        cancelPhoto?.visibility = View.INVISIBLE
        cropPhoto?.visibility = View.INVISIBLE
        capturePhotoDescription?.visibility = View.VISIBLE
        captureButton?.visibility = View.VISIBLE
        photoCaptured?.run {
            parentDimens = true
            setCropActivated(false)
            invalidate()
        }
    }

    companion object {
        const val URL_TO_SAVE_PHOTO_EXTRA_PARAM = "url_to_save_photo_extra_param"
    }
}
