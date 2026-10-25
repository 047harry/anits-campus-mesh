package `in`.anits.mesh.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Master Institutional Public Key Ring pre-cached from Firebase Firestore.
 * Used for instant, zero-internet offline verification of Gold Shield notices.
 */
@Entity(tableName = "table_keyring")
data class KeyRingEntity(
    @PrimaryKey
    @ColumnInfo(name = "roll_number")
    val rollNumber: String,

    @ColumnInfo(name = "full_name")
    val fullName: String,

    @ColumnInfo(name = "role_level")
    val roleLevel: Int, // 1=Principal, 2=HOD, 3=Faculty, 4=Clinic/Security

    @ColumnInfo(name = "department")
    val department: String, // 'CSE', 'ECE', 'ADMIN'

    @ColumnInfo(name = "ed25519_public_key")
    val ed25519PublicKey: ByteArray, // 32-Byte Raw Public Key

    @ColumnInfo(name = "authority_cert_signature")
    val authorityCertSignature: ByteArray, // 64-Byte Root CA signature

    @ColumnInfo(name = "sync_epoch")
    val syncEpoch: Long
)
