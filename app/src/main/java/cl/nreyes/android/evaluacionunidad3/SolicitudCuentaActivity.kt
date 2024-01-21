package cl.nreyes.android.evaluacionunidad3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import cl.nreyes.android.evaluacionunidad3.data.entity.SolicitudNuevaCuenta
import cl.nreyes.android.evaluacionunidad3.data.ubicacion.ObtenerUbicacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SolicitudCuentaActivity : AppCompatActivity() {
    private var etNombreCompleto:EditText? = null
    private var etRut:EditText? = null
    private var etFechaNacimiento:EditText? = null
    private var etEmail:EditText? = null
    private var etTelefono:EditText? = null
    private var btnSolicitar:Button? = null
    private var btnFotoFrontalCedula:Button? = null
    private var btnFotoTraseraCedula:Button? = null
    private val fechaActual = obtenerFechaActual()
    private val obUbicacion = ObtenerUbicacion()
    private var tipoFoto: String? = null
    private var imagenCedulaFrente: String? = null
    private var imagenCedulaTrasera: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.solicitud_cuenta)

        etNombreCompleto = findViewById(R.id.etNombreCompleto)
        etRut = findViewById(R.id.etRut)
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento)
        etEmail = findViewById(R.id.etEmail)
        etTelefono = findViewById(R.id.etTelefono)
        btnSolicitar = findViewById(R.id.btnSolicitar)
        btnFotoFrontalCedula = findViewById(R.id.btnFotoFrontalCedula)
        btnFotoTraseraCedula = findViewById(R.id.btnFotoTraseraCedula)

       btnSolicitar?.setOnClickListener {
           if (ContextCompat.checkSelfPermission(
                   this,
                   Manifest.permission.ACCESS_COARSE_LOCATION
               ) == PackageManager.PERMISSION_GRANTED
           ) {
               obtenerUbicacion()
           } else {
               peticionPermiso.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
           }
       }

        btnFotoFrontalCedula?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ){
            abrirCamara("fotoFrontal")
            } else {
                peticionPermiso.launch(Manifest.permission.CAMERA)
            }
        }

        btnFotoTraseraCedula?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ){
                abrirCamara("fotoTrasera")
            } else {
                peticionPermiso.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun guardarSolicitudEnBaseDeDatos(ubicacion: Location){
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                val solicitud = SolicitudNuevaCuenta(
                    nombreCompleto = etNombreCompleto?.text.toString(),
                    rut = etRut?.text.toString() ,
                    fechaNacimiento = etFechaNacimiento?.text.toString() ,
                    email = etEmail?.text.toString() ,
                    telefono = etTelefono?.text.toString(),
                    latitud = ubicacion.latitude,
                    longitud = ubicacion.longitude,
                    imagenCedulaFrente = "",
                    imagenCedulaTrasera = "",
                    fechaCreacionSolicitud = fechaActual
                )
                MainActivity.database.solicitudDao().insertSolicitud(solicitud)
            }
            if (!isFinishing && !isDestroyed){
                startActivity(Intent(this@SolicitudCuentaActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun obtenerFechaActual(): String {
        val calendario = Calendar.getInstance()
        val formatoFecha = SimpleDateFormat("dd/MM/yyy HH:mm:ss", Locale.getDefault())
        return formatoFecha.format(calendario.time)
    }


    private val peticionPermiso =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permisoConcedido ->
            if (permisoConcedido) {
                Log.v("peticionPermiso", "Permiso Concedido")
                obUbicacion
            } else {
                Log.v("peticionPermiso", "Permiso Denegado")
            }
    }

    private fun obtenerUbicacion() {
        obUbicacion.obtenerUbicacion(
            this,
            alObtenerUbicacion = { ubicacion ->
                Log.d(
                    "Ubicacion,",
                    "Latitud: ${ubicacion.latitude}, Longitud: ${ubicacion.longitude}"
                )
                guardarSolicitudEnBaseDeDatos(ubicacion)
            },
            alFallarObtencion = { excepcion ->
                Log.e(  "Ubicacion",
                        "Error al obtener la ubicacion :$excepcion")
            }
        )
    }

    private val tomarFotoResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            if (resultado.resultCode == RESULT_OK) {
                val data: Intent? = resultado.data
                val ubicacionImagen = data?.getStringExtra("ubicacionImagen")
                if (ubicacionImagen != null){
                    if (tipoFoto == "fotoFrontal"){
                        imagenCedulaFrente = ubicacionImagen
                    } else if (tipoFoto == "fotoTrasera"){
                        imagenCedulaTrasera = ubicacionImagen
                    }
                }
            } else {
                //nada
        }
    }

    private fun abrirCamara(tipo: String){
        val intentCamara = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        tipoFoto = tipo
        tomarFotoResult.launch(intentCamara)
    }
}
