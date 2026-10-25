package `in`.anits.mesh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import `in`.anits.mesh.CampusMeshApp
import `in`.anits.mesh.data.entity.MessageEntity
import kotlinx.coroutines.flow.*

data class GoldShieldNotice(
    val id: String,
    val senderName: String,
    val senderRole: String,
    val timestampText: String,
    val hopCount: Int,
    val directiveText: String,
    val isVerifiedOffline: Boolean = true
)

class BulletinViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as CampusMeshApp).database
    private val messageDao = db.messageDao()

    // Pre-populate with official campus mock directives
    private val _bulletins = MutableStateFlow(
        listOf(
            GoldShieldNotice(
                id = "notif_1",
                senderName = "Dr. K. Venkata Lakshmi (HOD — CSD)",
                senderRole = "Tier 1 Institutional Broadcast",
                timestampText = "10:14 AM",
                hopCount = 2,
                directiveText = "IMMEDIATE EVACUATION ORDER: Minor tremor aftershock detected. DO NOT use central lifts. Evacuate via North Fire Stairwells directly to the Central Cricket Ground Muster Point. Medical triage tent 1 active."
            ),
            GoldShieldNotice(
                id = "notif_2",
                senderName = "Health Center Emergency Staff",
                senderRole = "Campus Clinic Advisory",
                timestampText = "10:10 AM",
                hopCount = 1,
                directiveText = "First-aid stations set up at Cricket Ground. Stretcher volunteer teams dispatched to CSE & Mechanical blocks."
            )
        )
    )
    val bulletins: StateFlow<List<GoldShieldNotice>> = _bulletins.asStateFlow()
}
