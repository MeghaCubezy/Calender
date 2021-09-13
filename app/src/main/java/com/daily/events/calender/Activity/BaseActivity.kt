package com.daily.events.calender.Activity

import android.Manifest

import android.content.Intent
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.daily.events.calender.R
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks

abstract class BaseActivity : AppCompatActivity() , PermissionCallbacks{

    companion object{
        const val RC_READ_EXTERNAL_STORAGE = 123
    }


    var perms = arrayOf(
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR,

    )
    val TAG = BaseActivity::class.java.name


    abstract fun permissionGranted()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        readExternalStorage()
        ActivityCompat.requestPermissions(
            this,
            perms, 1
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    /**
     * Read external storage file
     */
    @AfterPermissionGranted(RC_READ_EXTERNAL_STORAGE)
    private fun readExternalStorage() {
        val isGranted = EasyPermissions.hasPermissions(this, *perms)
        if (isGranted) {
            permissionGranted()
        } else {
            EasyPermissions.requestPermissions(
                this, getString(R.string.permission_str),
                RC_READ_EXTERNAL_STORAGE, *perms
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String?>?) {
//        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
        permissionGranted()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String?>?) {
//        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());
        // If Permission permanently denied, ask user again
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms!!)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            if (EasyPermissions.hasPermissions(this, *perms)) {
                permissionGranted()
            } else {
                finish()
            }
        }
        if (requestCode == 2296) {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    permissionGranted()
                } else {
                    finish()
                    //                    Toasty.info(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}