package com.example.app_mnist

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.graphics.drawable.ClipDrawable
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app_mnist.ml.ModelMnist
import com.google.mlkit.vision.common.InputImage
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer



class MainActivity : AppCompatActivity() {

    lateinit var  capReq: CaptureRequest.Builder
    lateinit var  handler: Handler
    private lateinit var handlerThread: HandlerThread
    private lateinit var cameraManager: CameraManager
    lateinit var textureView: TextureView
    lateinit var cameraCaptureSession: CameraCaptureSession
    lateinit var cameraDevice: CameraDevice
    lateinit var imageReader: ImageReader
    private lateinit var tvResult: TextView
    private lateinit var btPredict: Button
    private var output: String = "Output"
    private var isPredicting = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getPermissions()


        textureView = findViewById(R.id.textureView)
        tvResult = findViewById(R.id.tvResult)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler((handlerThread).looper)
        textureView.surfaceTextureListener = object: TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {

            }

        }

        imageReader = ImageReader.newInstance( 400, 400, ImageFormat.JPEG, 1)
        btPredict = findViewById(R.id.btCapture)

        imageReader.setOnImageAvailableListener({ p0 ->
            val image = p0?.acquireLatestImage()
            if(image != null){
                val inputImage = InputImage.fromMediaImage(image, 90)
                runOnUiThread {
                    showImage(inputImage.bitmapInternal!!)
                }


                predictML(inputImage)
                runOnUiThread{
                    tvResult.text = output
                    btPredict.text = "Predict"
                }
                image.close()
            }
            Toast.makeText(this@MainActivity, "Predicted", Toast.LENGTH_SHORT).show()
        },handler )


        tvResult.text = output
        btPredict.apply{
            setOnClickListener{
                if(!isPredicting) {
                    isPredicting=true
                    tvResult.text = "..."
                    btPredict.text = "..."
                    capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                    capReq.addTarget(imageReader.surface)
                    cameraCaptureSession.capture(capReq.build(), null, null)
                }
            }
        }
    }

    private fun showImage(bitmap: Bitmap){
        val imageView = findViewById<ImageView>(R.id.imageView)
        imageView.setImageBitmap(bitmap)
    }

    private fun predictML(inputImage:InputImage){
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(28,28, ResizeOp.ResizeMethod.BILINEAR))
            .add(TransformToGrayscaleOp())
            .add(NormalizeOp(0.0f, 255.0f))
            .build()

        var tensorImage = TensorImage(DataType.UINT8)



        tensorImage.load(inputImage.bitmapInternal)
        tensorImage = imageProcessor.process(tensorImage)


        val model = ModelMnist.newInstance(this)
        try {
            // Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 784), DataType.FLOAT32)

            inputFeature0.loadBuffer(tensorImage.buffer)

            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

            var maxInx = 0
            outputFeature0.forEachIndexed{index, fl  ->
                if(outputFeature0[maxInx]<fl){
                    maxInx = index
                }
            }
            Log.d("outM", "out: $maxInx")
            output = maxInx.toString()
        }catch (e:Exception){
            Log.d("modelError", e.toString())
        }

        // Releases model resources if no longer used.
        model.close()
        isPredicting = false


    }

    @SuppressLint("MissingPermission")
    private fun openCamera(){
        cameraManager.openCamera(cameraManager.cameraIdList[0], object: CameraDevice.StateCallback(){
            override fun onOpened(p0: CameraDevice) {
                cameraDevice = p0
                capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                val surface = Surface(textureView.surfaceTexture)
                capReq.addTarget(surface)
                cameraDevice.createCaptureSession(listOf(surface, imageReader.surface), object: CameraCaptureSession.StateCallback(){

                    override fun onConfigured(p0: CameraCaptureSession) {
                        cameraCaptureSession = p0
                        cameraCaptureSession.setRepeatingRequest(capReq.build(), null, null)
                        }

                    override fun onConfigureFailed(p0: CameraCaptureSession) {
                        TODO("Not yet implemented")
                    }
                }, handler)
            }

            override fun onDisconnected(p0: CameraDevice) {
                TODO("Not yet implemented")
            }

            override fun onError(p0: CameraDevice, p1: Int) {
                TODO("Not yet implemented")
            }
        }, handler)

    }

    private fun getPermissions(){
        val permissions = mutableListOf<String>()

        if(checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.CAMERA)
        }
        if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if(permissions.size > 0) requestPermissions(permissions.toTypedArray(), 101)


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            grantResults.forEach {
                if(it != PackageManager.PERMISSION_GRANTED){
                    getPermissions()
                }
            }
    }

}