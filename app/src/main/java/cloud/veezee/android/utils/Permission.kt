package cloud.veezee.android.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat

class Permission(var context: Context?) {

    companion object {
        const val READ_STORAGE = android.Manifest.permission.READ_EXTERNAL_STORAGE;
        const val WRITE_STORAGE = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        const val INTERNET = android.Manifest.permission.INTERNET;
        const val CAMERA = android.Manifest.permission.CAMERA;
        //...
    }

    fun arePermissionsGranted(permissions: Array<String>): Array<String> {
        val request: ArrayList<String> = ArrayList();
        (0 until permissions.size)
                .filterNot { isPermissionGranted(permissions[it]) }
                .mapTo(request) { permissions[it] }

        val array = arrayOfNulls<String>(request.size);
        return request.toArray(array);

    }

    fun isPermissionGranted(permission: String): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            context?.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        } else
            true;
    }

    fun requestPermission(permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions((context as Activity), permissions, requestCode);
    }
}