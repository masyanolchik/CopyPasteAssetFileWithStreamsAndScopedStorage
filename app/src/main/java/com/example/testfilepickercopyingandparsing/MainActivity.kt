package com.example.testfilepickercopyingandparsing

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.testfilepickercopyingandparsing.entity.Station
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class MainActivity : AppCompatActivity() {
    private var requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }
    private var requestFileLocationLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            result.data?.let {
                if(result.resultCode == RESULT_OK) {
                    result.data?.data?.let { uri ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                        } else {
                            val jsonString = contentResolver.openInputStream(uri)?.bufferedReader()
                                ?.use { it.readText() }
                            val listStationType = object : TypeToken<List<Station>>() {}.type
                            val m = Gson().fromJson<List<Station>>(jsonString, listStationType)
                            val k = 9
                        }
                    }
                }
            }
        }
    private lateinit var chooseDestinationButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        chooseDestinationButton = findViewById<Button>(R.id.copy_destination)
        val chooseFileButton = findViewById<Button>(R.id.import_button)
        chooseDestinationButton.setOnClickListener {
            saveTemplateToExternalStorage()
        }
        chooseFileButton.setOnClickListener {
            requestReadPermission {
                chooseFile()
            }
        }
    }


    private fun chooseFile() {
        val intent = Intent()
        intent.type = "*/*"
        intent.action = Intent.ACTION_GET_CONTENT
        //launch picker screen
        requestFileLocationLauncher.launch(intent)
    }

    private fun saveTemplateToExternalStorage(
        filename: String = "stations_template.json",
        mimeType: String = "application/json",
        directory: String = Environment.DIRECTORY_DOWNLOADS,
    ) {
        if (!File(Environment.getExternalStoragePublicDirectory(directory), filename).exists()) {
            var templateOutputStream: OutputStream? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val mediaContentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, filename)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, directory)
                }

                contentResolver.run {
                    val uri = contentResolver.insert(mediaContentUri, values) ?: return
                    templateOutputStream = openOutputStream(uri) ?: return
                }
                val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
                startActivity(intent)
            } else {
                requestWritePermission {
                    val templatePath = Environment.getExternalStoragePublicDirectory(directory)
                    val template = File(templatePath, filename)
                    templateOutputStream = FileOutputStream(template)
                }
            }


            templateOutputStream?.let {
                it.use { outputStream ->
                    val templateBytes = assets.open("stations.json").readBytes()
                    outputStream.write(templateBytes)
                }
                Snackbar.make(
                    chooseDestinationButton,
                    "Template is in your Downloads folder",
                    Snackbar.LENGTH_SHORT
                ).show()
            }

        } else {
            Snackbar.make(
                chooseDestinationButton,
                "Template is already in your Downloads folder",
                Snackbar.LENGTH_SHORT
            ).show()
        }

    }

    private fun requestWritePermission(blockWithPermission: () -> Unit = {}) {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                blockWithPermission.invoke()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                Snackbar
                    .make(
                        chooseDestinationButton,
                        "We need write permission in order to copy a template file into your Download folder.",
                        Snackbar.LENGTH_INDEFINITE)
                    .setAction("GRANT PERMISSION") {
                        requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                    .show()
            }
            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun requestReadPermission(blockWithPermission: () -> Unit = {}) {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                blockWithPermission.invoke()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                Snackbar
                    .make(
                        chooseDestinationButton,
                        "We need read permission in order to parse your edited template",
                        Snackbar.LENGTH_INDEFINITE)
                    .setAction("GRANT PERMISSION") {
                        requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                    .show()
            }
            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
}