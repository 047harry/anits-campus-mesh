package `in`.anits.mesh

import android.app.Application
import `in`.anits.mesh.data.MeshDatabase

class CampusMeshApp : Application() {

    val database: MeshDatabase by lazy {
        MeshDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Initialization logic for background daemons
    }
}
