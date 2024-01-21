package cl.nreyes.android.evaluacionunidad3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import cl.nreyes.android.evaluacionunidad3.R
import cl.nreyes.android.evaluacionunidad3.data.database.AppBaseDatos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private var etUsuario:EditText? = null
    private var etContrasena:EditText? = null
    private val btnIngresar:Button? = null

    companion object{
        lateinit var database: AppBaseDatos
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_inicio)

        val btnSolicitarNuevaCuenta = findViewById<Button>(R.id.btnSolicitarNuevaCuenta)

        btnSolicitarNuevaCuenta.setOnClickListener{
            startActivity(Intent(this, SolicitudCuentaActivity::class.java))
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                database = Room.databaseBuilder(
                    applicationContext,
                    AppBaseDatos::class.java,
                    "iplabank_database"
                ).build()
            }
        }

        Log.d("SolicitudCuentaActivity", "Guardando solicitud en la base de datos")
    }
}

