package camera.tauri.a

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.core.content.FileProvider
import app.tauri.PermissionState
import app.tauri.annotation.ActivityCallback
import app.tauri.annotation.Command
import app.tauri.annotation.InvokeArg
import app.tauri.annotation.Permission
import app.tauri.annotation.PermissionCallback
import app.tauri.annotation.TauriPlugin
import app.tauri.plugin.Invoke
import app.tauri.plugin.JSObject
import app.tauri.plugin.Plugin
import org.json.JSONArray
import java.io.File

@InvokeArg
class TakePictureRequest {}

@TauriPlugin(permissions = [Permission(alias = "camera", strings = [Manifest.permission.CAMERA])])
class ExamplePlugin(private val activity: Activity): Plugin(activity) {

    private var currentPhotoUri: Uri? = null

    @Command
    fun take_picture(invoke: Invoke) {
        requestPermissionForAlias("camera", invoke, "openCamera")
    }

    @PermissionCallback
    private fun openCamera(invoke: Invoke) {
        if (getPermissionState("camera") != PermissionState.GRANTED) {
            invoke.reject("没有相机权限")
            return
        }
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(activity.packageManager) == null) {
            invoke.reject("启动相机失败")
            return
        }
        runCatching {
            createGalleryImageUri() ?: throw Exception()
        }.onSuccess { uri ->
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            currentPhotoUri = uri
            startActivityForResult(invoke, takePictureIntent, "onCameraResult")
        }.onFailure { e ->
            invoke.reject("无法创建图像文件")
        }
    }

    @ActivityCallback
    private fun onCameraResult(invoke: Invoke, result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
            invoke.reject("用户取消拍摄")
            return
        }
        invoke.resolve(JSObject().apply { put("imageUri", currentPhotoUri.toString()) })
    }

    private fun createGalleryImageUri(): Uri? {
        val contentResolver = activity.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "JPEG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

}
