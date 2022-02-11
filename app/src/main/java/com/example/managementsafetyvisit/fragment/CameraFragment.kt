package com.example.managementsafetyvisit.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.managementsafetyvisit.MainActivity.Companion.imageNumber
import com.example.managementsafetyvisit.MainActivity.Companion.msvNumber
import com.example.managementsafetyvisit.R
import com.example.managementsafetyvisit.retrofit.RetrofitFunctions
import com.example.managementsafetyvisit.utils.showDialog
import com.example.managementsafetyvisit.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService


class CameraFragment : Fragment() {

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraCaptureButton: Button
    private lateinit var frame : ConstraintLayout
    private lateinit var accept: Button
    private lateinit var decline: Button
    private lateinit var image: ImageView
    private lateinit var viewFinder: PreviewView
    private val TAG = "CameraXBasic"
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    private val REQUEST_CODE_PERMISSIONS = 10
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    private var number = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)
        cameraCaptureButton = view.findViewById(R.id.camera_capture_button)
        cameraCaptureButton.isEnabled = false
        cameraCaptureButton.setBackgroundResource(R.drawable.round_button_disabled)
        frame = view.findViewById(R.id.save_frame)
        frame.visibility = View.GONE
        accept = view.findViewById(R.id.save_picture)
        decline = view.findViewById(R.id.decline_picture)
        image = view.findViewById(R.id.imageView2)
        outputDirectory = getOutputDirectory()
        viewFinder = view.findViewById(R.id.viewFinder)
        val retro = RetrofitFunctions()
        CoroutineScope(IO).launch {
            number = retro.getImageCount("MSV_$msvNumber").trim().toInt()
            CoroutineScope(Main).launch{
                if(number>=10){
                    showDialog("Nem tudsz több képet készíteni",requireContext())
                    cameraCaptureButton.isEnabled = false
                    cameraCaptureButton.setBackgroundResource(R.drawable.round_button_disabled)
                }else{
                    cameraCaptureButton.isEnabled = true
                    cameraCaptureButton.setBackgroundResource(R.drawable.round_button_2)
                }
            }
        }
        if (allPermissionGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        cameraCaptureButton.setOnClickListener {
            takePhoto()
        }
        accept.setOnClickListener {
            showToast("Mentve",requireContext())
            frame.visibility = View.GONE

        }
        decline.setOnClickListener {
            showToast("Elvetve", requireContext())
            frame.visibility = View.GONE
        }
        return view
    }

    private fun startCamera() {
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

            cameraProviderFuture.addListener(Runnable {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(viewFinder.surfaceProvider)
                    }

                imageCapture = ImageCapture.Builder()
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()

                    cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture
                    )

                } catch (exc: Exception) {
                    Log.e(TAG, "Use case binding failed", exc)
                }

            }, ContextCompat.getMainExecutor(requireContext()))
        } catch (e: Exception) {
            Log.d(TAG, "startCamera: $e")
        }

    }

    private fun takePhoto() {
        cameraCaptureButton.isEnabled = false
        cameraCaptureButton.setBackgroundResource(R.drawable.round_button_disabled)
        val imageCapture = imageCapture ?: return
        val retro = RetrofitFunctions()
        CoroutineScope(IO).launch {
            val number = retro.getImageCount("MSV_$msvNumber")
            CoroutineScope(Main).launch {
                if (number.toInt() < 10) {
                    // Create time-stamped output file to hold the image
                    val photoFile = File(
                        outputDirectory,
                        SimpleDateFormat(
                            FILENAME_FORMAT, Locale.US
                        ).format(System.currentTimeMillis()) + ".jpg"
                    )

                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(requireContext()),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onError(exc: ImageCaptureException) {
                                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                            }

                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                frame.visibility = View.VISIBLE
                                val savedUri = Uri.fromFile(photoFile)
                                //val path = "file:///storage/emulated/0/Android/media/com.example.camerarest/CameraRest/2022-01-13-12-22-06-923.jpg"
                                val path = savedUri.toString()
                                val stringPath = path.substring(8, path.length)
                                //val msg = "A kép mentésre került"
                                // Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                                Log.d(TAG, stringPath)
                                val retro = RetrofitFunctions()
                                image.setImageURI(savedUri)
                                CoroutineScope(IO).launch {
                                    try {
                                        retro.retrofitGet(
                                            photoFile,
                                            """\\fs\MSV\foto""",
                                            "MSV_$msvNumber"
                                        )
                                        CoroutineScope(Main).launch {
                                            showToast("A kép mentve a szerverre", requireContext())
                                            cameraCaptureButton.isEnabled = true
                                            cameraCaptureButton.setBackgroundResource(R.drawable.round_button_2)
                                        }
                                    } catch (e: Exception) {
                                        Log.d("HIBA", "$e")
                                    }
                                }
                            }
                        })
                }else{
                    showDialog("Nem tudsz több képet felvinni",requireContext())
                    cameraCaptureButton.isEnabled = false
                    cameraCaptureButton.setBackgroundResource(R.drawable.round_button_disabled)
                }
            }
        }
        // Get a stable reference of the modifiable image capture use case
    }

    private fun allPermissionGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireActivity().filesDir
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                activity?.finish()
            }
        }
    }
}