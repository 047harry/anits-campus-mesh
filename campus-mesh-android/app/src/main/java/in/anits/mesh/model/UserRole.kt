package `in`.anits.mesh.model

/**
 * Campus Stakeholder Hierarchy & Trust Levels.
 * Aligns with STAGE_1_REQUIREMENTS.md Section 1.2.
 */
enum class UserRole(val code: Byte, val title: String) {
    SYSTEM_ADMIN(0x00.toByte(), "Campus Root Admin"),
    PRINCIPAL_DEAN(0x01.toByte(), "Principal / Dean"),
    HOD(0x02.toByte(), "Head of Department"),
    FACULTY_WARDEN(0x03.toByte(), "Faculty / Hostel Warden"),
    CLINIC_SECURITY(0x04.toByte(), "Campus Health Center / Security"),
    STUDENT_OCCUPANT(0x05.toByte(), "Student / Occupant");

    companion object {
        fun fromByte(code: Byte): UserRole =
            entries.firstOrNull { it.code == code } ?: STUDENT_OCCUPANT
    }
}
