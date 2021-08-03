package com.example.cloudinaryupload
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private var mProfile: ImageView? = null
     lateinit var mImageAdd: ImageView
    lateinit var mBtnUpload: Button
    private var mText: TextView? = null
    var filePath: String? = null
    var config: MutableMap<String, String> = HashMap()
    private fun configCloudinary() {

        config["cloud_name"] = "ghfghfghf"
        config["api_key"] = "4565434352hf444g2977847"
        config["api_secret"] = "JyUejk5fg44h88H34-pYSCJffgfsdfJnxhDU"
        MediaManager.init(this@MainActivity, config)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mProfile = findViewById(R.id.imgProfile)
        mImageAdd = findViewById(R.id.imgAdd)
        mBtnUpload = findViewById(R.id.btnUpload)
        mText = findViewById(R.id.txt)
        configCloudinary()

        //when click mImageAdd request the permission to access the gallery
        mImageAdd.setOnClickListener(View.OnClickListener { //request permission to access external storage
            requestPermission()
        })
        mBtnUpload.setOnClickListener(View.OnClickListener { uploadToCloudinary(filePath) })
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            accessTheGallery()
        } else {
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                accessTheGallery()
            } else {
                Toast.makeText(this@MainActivity, "permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun accessTheGallery() {
        val i = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        i.type = "video/*"
        startActivityForResult(i, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //get the image's file location
        filePath = getRealPathFromUri(data!!.data, this@MainActivity)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            try {
                //set picked image to the mProfile
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data.data)
                mProfile!!.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun getRealPathFromUri(imageUri: Uri?, activity: Activity): String? {
        val cursor = activity.contentResolver.query(imageUri!!, null, null, null, null)
        return if (cursor == null) {
            imageUri.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            cursor.getString(idx)
        }
    }

    private fun uploadToCloudinary(filePath: String?) {
        Log.d("A", "sign up uploadToCloudinary- ")
        MediaManager.get()
            .upload(filePath)
            .option("resource_type", "video")
            .option("folder", "my_folder/my_sub_folder/")
            .callback(object : UploadCallback {
            override  fun onStart(requestId: String?) {
                mText!!.text = "start"
            }

            override   fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                mText!!.text = "Uploading... "
            }

            override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                mText!!.text = "image URL: " + resultData["url"].toString()
            }

            override fun onError(requestId: String?, error: ErrorInfo) {
                mText!!.text = "error " + error.getDescription()
            }

            override fun onReschedule(requestId: String?, error: ErrorInfo) {
                mText!!.text = "Reshedule " + error.getDescription()
            }
        }).dispatch()
    }

    companion object {
        private const val PERMISSION_CODE = 1
        private const val PICK_IMAGE = 1
    }
}


