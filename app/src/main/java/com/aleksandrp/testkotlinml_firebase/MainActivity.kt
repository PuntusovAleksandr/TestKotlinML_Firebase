package com.aleksandrp.testkotlinml_firebase

import android.Manifest.permission.*
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AlertDialogLayout
import android.support.v7.widget.DialogTitle
import android.support.v7.widget.SnapHelper
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector
import kotlinx.android.synthetic.main.choose_from_dialog.*
import java.io.File

class MainActivity : AppCompatActivity(), View.OnClickListener {


    lateinit var btnBrowse: Button
    lateinit var imgPrew: ImageView
    lateinit var tvCamera: TextView
    lateinit var tvGallery: TextView
    lateinit var tvOut: TextView
    lateinit var b: AlertDialog

    lateinit var fileFanalPath: File
    lateinit var uriFinaler: Uri
    lateinit var path: String

    var permission: Array<String> = arrayOf(WRITE_EXTERNAL_STORAGE, CAMERA)
    var isPermissionGrantes: Boolean = false
    val PERMISSION_REW_CODE: Int = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUi()
    }

    private fun initUi() {
        btnBrowse = findViewById(R.id.btnBrowse)
        imgPrew = findViewById(R.id.img_preview)
        tvOut = findViewById(R.id.tv_output)

        btnBrowse.setOnClickListener(this)
    }


    override fun onClick(v: View?) {

        if (v!!.id == R.id.btnBrowse) {
            openMenu()
        } else if (v!!.id == R.id.tv_camera) {
            if (checkPermission()) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                    fileFanalPath = File(Environment.getExternalStorageDirectory(), "Image_" + System.currentTimeMillis() + ".jpg")
                    var cameraIntent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileFanalPath))
                    cameraIntent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)
                    startActivityForResult(cameraIntent, 2)
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
                    fileFanalPath = File(Environment.getExternalStorageDirectory(), "Image_" + System.currentTimeMillis() + ".jpg")
                    var cameraIntent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    uriFinaler = FileProvider.getUriForFile(applicationContext, packageName + ".provider", fileFanalPath)
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriFinaler)
                    cameraIntent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)
                    startActivityForResult(cameraIntent, 2)
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                    fileFanalPath = File(Environment.getExternalStorageDirectory(), "Image_" + System.currentTimeMillis() + ".jpg")
                    var cameraIntent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    uriFinaler = FileProvider.getUriForFile(applicationContext, packageName + ".provider", fileFanalPath)
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriFinaler)
                    cameraIntent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)
                    startActivityForResult(cameraIntent, 2)
                } else {
                    fileFanalPath = File(Environment.getExternalStorageDirectory(), "Image_" + System.currentTimeMillis() + ".jpg")
                    var cameraIntent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileFanalPath))
                    startActivityForResult(cameraIntent, 2)
                }
            }
        } else if (v!!.id == R.id.tv_gallery) {
            if (checkPermission()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    var intent: Intent = Intent(ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    intent.type = "image/*"
                    startActivityForResult(intent, 1)
                } else {
                    var intent: Intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, 1)
                }
            }
        }
    }

    private fun openMenu() {
        var dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        var inflater: LayoutInflater = layoutInflater
        dialogBuilder.setTitle("Choose image from")
        var rv: View = inflater.inflate(R.layout.choose_from_dialog, null, false)
        dialogBuilder.setView(rv)

        tvCamera = rv.findViewById(R.id.tv_camera)
        tvGallery = rv.findViewById(R.id.tv_gallery)

        tvCamera.setOnClickListener(this)
        tvGallery.setOnClickListener(this)

        b = dialogBuilder.create()
        b.show()
    }

    fun checkPermission(): Boolean {
        var permissionGranted: Boolean = false
        if (ActivityCompat.checkSelfPermission(this, permission[0]) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionGranted = false
                requestPermissions(permission, PERMISSION_REW_CODE)
            }
        } else {
            permissionGranted = true
        }
        return permissionGranted
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REW_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    isPermissionGrantes = true
                } else {
                    isPermissionGrantes = false
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission[0])) {
                        openDialog()
                    } else {
                        if (ActivityCompat.checkSelfPermission(this, permission[0]) == PackageManager.PERMISSION_GRANTED) {

                        } else {
                            var snackBar: Snackbar = Snackbar.make(btnBrowse, "Permission Denied", Snackbar.LENGTH_LONG)
                            snackBar.setAction("Retry", object : View.OnClickListener {
                                override fun onClick(v: View?) {
                                    openSettings()
                                }

                            })
                            snackBar.show()
                        }
                    }
                }
        }
    }

    private fun openSettings() {
        var intent: Intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
        var uri: Uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startActivity(intent)
    }

    private fun openDialog() {
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Need Permission")
        builder.setMessage("Please allow this app to use this permission")
        builder.setNeutralButton("Ok", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog!!.dismiss()
                checkPermission()
            }
        })
        var dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                b.dismiss()
                try {

                    var selectedImage: Uri = data!!.data
                    managerImageFromUri(selectedImage)

//                    var selectedImage: Uri = data!!.data
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                        path = getPath(selectedImage, this).toString()
//                    } else {
//                        path = getPath1(selectedImage, this)
//                    }
//                    var bmOption: BitmapFactory.Options = BitmapFactory.Options()
//                    var bitmap: Bitmap = BitmapFactory.decodeFile(path, bmOption)
//                    imgPrew.setImageBitmap(bitmap)
                } catch (e: Exception) {
                }
            } else if (requestCode == 2) {
                b.dismiss()
                try {
                    var pictureUri: Uri = Uri.fromFile(fileFanalPath)
                    var file: File = File(pictureUri.path)
                    path = file.absolutePath
                    var bmOption: BitmapFactory.Options = BitmapFactory.Options()
                    var bitmap: Bitmap = BitmapFactory.decodeFile(path, bmOption)
                    imgPrew.setImageBitmap(bitmap)

                    onDeviceRecognizeText(bitmap)
                } catch (e: Exception) {
                }
            }
        }
    }

    private fun managerImageFromUri(selectedImage: Uri) {
        var bitmap: Bitmap? = null
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
            imgPrew.setImageBitmap(bitmap)
            onDeviceRecognizeText(bitmap)
        } catch (e: Exception) {
        }
    }

    private fun onDeviceRecognizeText(bitmap: Bitmap?) {
        var text: String = ""
        var image: FirebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap!!)
        var detector: FirebaseVisionTextDetector = FirebaseVision.getInstance().visionTextDetector
        var result: Task<FirebaseVisionText> = detector.detectInImage(image)
                .addOnSuccessListener {
                    object : OnSuccessListener<FirebaseVisionText> {
                        override fun onSuccess(p0: FirebaseVisionText?) {
                            for (block: FirebaseVisionText.Block in p0!!.blocks) {
                                var boundingBox: Rect = block.boundingBox!!
                                var cornerPoints: Array<Point> = block.cornerPoints!!
                                text += block.text
                            }
                            tvOut.setText(text)
                        }
                    }
                }
                .addOnFailureListener(object : OnFailureListener{
                    override fun onFailure(p0: java.lang.Exception) {
                        showToast(p0)
                    }
                })

    }

    private fun OnFailureListener.showToast(p0: java.lang.Exception) {
        Toast.makeText(applicationContext, p0.message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun getPath(uri: Uri, activity: MainActivity): String? {
            if (uri == null) {
                return null
            }
            var projection: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
            var cursor: Cursor = activity.contentResolver.query(uri, projection, null, null, null)
            var colum_index: Int
            if (cursor != null) {
                colum_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                return cursor.getString(colum_index)
            }
            return uri.path
        }

        fun getPath1(uri: Uri, activity: MainActivity): String? {
            var projection: Array<String> = arrayOf(MediaStore.MediaColumns.DATA)
            var cursor: Cursor = activity.contentResolver.query(uri, projection, null, null, null)
            var colum_index: Int = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            cursor.moveToFirst()
            return cursor.getString(colum_index)
        }
    }

}
