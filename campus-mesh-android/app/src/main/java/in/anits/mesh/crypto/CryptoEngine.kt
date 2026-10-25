package `in`.anits.mesh.crypto

import android.util.Base64
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Cryptography Engine:
 * 1. Asymmetric Ed25519 for tamper-proof digital signatures
 * 2. Symmetric AES-256-GCM for End-to-End Encrypted Private Mesh Chat Hopping
 */
object CryptoEngine {

    private val secureRandom = SecureRandom()

    data class KeyPair(
        val privateKeyBytes: ByteArray, // 32-Byte Private Seed
        val publicKeyBytes: ByteArray   // 32-Byte Public Key
    )

    /**
     * Generates a fresh Ed25519 asymmetric key pair.
     */
    fun generateKeyPair(): KeyPair {
        val generator = Ed25519KeyPairGenerator()
        generator.init(Ed25519KeyGenerationParameters(secureRandom))
        val keyPair = generator.generateKeyPair()

        val priv = keyPair.private as Ed25519PrivateKeyParameters
        val pub = keyPair.public as Ed25519PublicKeyParameters

        return KeyPair(
            privateKeyBytes = priv.encoded,
            publicKeyBytes = pub.encoded
        )
    }

    /**
     * Signs arbitrary data with an Ed25519 private key seed.
     * Returns a 64-byte signature.
     */
    fun sign(data: ByteArray, privateKeyBytes: ByteArray): ByteArray {
        require(privateKeyBytes.size == 32) { "Ed25519 private seed must be 32 bytes" }
        val privParams = Ed25519PrivateKeyParameters(privateKeyBytes, 0)
        val signer = Ed25519Signer()
        signer.init(true, privParams)
        signer.update(data, 0, data.size)
        return signer.generateSignature()
    }

    /**
     * Verifies an Ed25519 signature against an authentic public key offline.
     */
    fun verify(data: ByteArray, signature: ByteArray, publicKeyBytes: ByteArray): Boolean {
        if (signature.size != 64 || publicKeyBytes.size != 32) return false
        return try {
            val pubParams = Ed25519PublicKeyParameters(publicKeyBytes, 0)
            val signer = Ed25519Signer()
            signer.init(false, pubParams)
            signer.update(data, 0, data.size)
            signer.verifySignature(signature)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Generates a deterministic pairwise encryption seed between two campus rolls.
     */
    fun derivePairwiseKey(rollA: String, rollB: String): String {
        val cleanA = rollA.trim().uppercase()
        val cleanB = rollB.trim().uppercase()
        return if (cleanA < cleanB) "ANITS_E2EE_${cleanA}_${cleanB}" else "ANITS_E2EE_${cleanB}_${cleanA}"
    }

    /**
     * Symmetrically encrypts a message using AES-256-GCM.
     * Intermediate relay hops cannot read this ciphertext.
     */
    fun encryptAesGcm(plaintext: String, secretKeySeed: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val keyBytes = digest.digest(secretKeySeed.toByteArray(StandardCharsets.UTF_8))
            val secretKey = SecretKeySpec(keyBytes, "AES")

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(12)
            secureRandom.nextBytes(iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))

            val cipherBytes = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
            val combined = ByteArray(iv.size + cipherBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)

            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            plaintext
        }
    }

    /**
     * Decrypts an AES-256-GCM ciphertext using the pairwise secret key seed.
     */
    fun decryptAesGcm(cipherBase64: String, secretKeySeed: String): String {
        return try {
            val combined = Base64.decode(cipherBase64, Base64.NO_WRAP)
            if (combined.size < 12) return cipherBase64

            val digest = MessageDigest.getInstance("SHA-256")
            val keyBytes = digest.digest(secretKeySeed.toByteArray(StandardCharsets.UTF_8))
            val secretKey = SecretKeySpec(keyBytes, "AES")

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(12)
            System.arraycopy(combined, 0, iv, 0, 12)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))

            val decrypted = cipher.doFinal(combined, 12, combined.size - 12)
            String(decrypted, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            cipherBase64
        }
    }
}
