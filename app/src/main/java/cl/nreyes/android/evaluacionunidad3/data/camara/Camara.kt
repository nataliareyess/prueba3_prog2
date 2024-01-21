package cl.nreyes.android.evaluacionunidad3.data.camara

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import cl.nreyes.android.evaluacionunidad3.R
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class Camara : AppCompatActivity() {
    private lateinit var vistaPrevia: Preview
    private lateinit var capturaImagen: ImageCapture
    private lateinit var ejecutorCamara: Executor
    private var imagenCedulaDelantera: String? = null
    private var imagenCedulaTrasera: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vista_camara)

        val pvCamara: PreviewView = findViewById(R.id.pvCamara)
        vistaPrevia = Preview.Builder().build()
        capturaImagen = ImageCapture.Builder().build()
        ejecutorCamara = Executors.newSingleThreadExecutor()
        vistaPrevia.setSurfaceProvider(pvCamara.surfaceProvider)
        iniciarCamara()
    }

    private fun iniciarCamara(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                val directorioSalida = getOutputDirectory()
                val formatoFecha = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val nombreArchivo = "${formatoFecha.format(Date())}.jpg"
                val outputFile = File(directorioSalida, nombreArchivo)

                val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

                capturaImagen.takePicture(
                    outputOptions, ContextCompat.getMainExecutor(this),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onError(error: ImageCaptureException) {
                            Log.e("Camara", "Error al capturar imagen: ${error.message}", error)
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            val savedUri = output.savedUri

                            if (savedUri != null){
                                val tipoFoto = intent.getStringExtra("tipoFoto")
                                if (tipoFoto == "fotoTrasera") {
                                    imagenCedulaTrasera = savedUri.toString()
                                } else if (tipoFoto == "fotoDelantera") {
                                    imagenCedulaDelantera = savedUri.toString()
                                }

                            }else {
                            Log.e("Camara", "La URI de la imagen guardada es nula.")
                            }
                        }
                    }
                )

                cameraProvider.bindToLifecycle(this, cameraSelector, vistaPrevia, capturaImagen)
            } catch (exc: Exception){
                Log.e("Camara", "No se pudo vincular el caso de uso de las camara", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }
}
