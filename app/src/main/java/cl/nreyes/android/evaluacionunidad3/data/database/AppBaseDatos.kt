package cl.nreyes.android.evaluacionunidad3.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import cl.nreyes.android.evaluacionunidad3.data.dao.SolicitudDao
import cl.nreyes.android.evaluacionunidad3.data.entity.SolicitudNuevaCuenta

@Database(entities = [SolicitudNuevaCuenta::class], version = 1)
abstract class AppBaseDatos : RoomDatabase (){
    abstract fun solicitudDao(): SolicitudDao
}