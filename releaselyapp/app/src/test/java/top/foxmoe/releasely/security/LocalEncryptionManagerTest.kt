package top.foxmoe.releasely.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for LocalEncryptionManager.
 *
 * Note: These tests use Android KeyStore which is only available on Android devices.
 * For true unit testing on JVM, you would need to abstract the encryption logic into
 * a platform-independent interface and mock it. These tests are designed to run
 * on Android instrumented tests or with Robolectric.
 */
class LocalEncryptionManagerTest {

    @BeforeEach
    fun setUp() {
        // Clear any existing key before each test to ensure clean state
        try {
            LocalEncryptionManager.clearKey()
        } catch (e: Exception) {
            // Key may not exist, ignore
        }
    }

    @Test
    fun `encrypt and decrypt returns original plaintext`() {
        val plaintext = "Hello, this is a test message!"

        val encrypted = LocalEncryptionManager.encrypt(plaintext)
        val decrypted = LocalEncryptionManager.decrypt(encrypted)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `encrypt produces different output each time (random IV)`() {
        val plaintext = "Same message"

        val encrypted1 = LocalEncryptionManager.encrypt(plaintext)
        val encrypted2 = LocalEncryptionManager.encrypt(plaintext)

        // Due to random IV, the ciphertexts should be different
        assertNotEquals(encrypted1, encrypted2)

        // But both should decrypt to the same value
        assertEquals(plaintext, LocalEncryptionManager.decrypt(encrypted1))
        assertEquals(plaintext, LocalEncryptionManager.decrypt(encrypted2))
    }

    @Test
    fun `encrypt handles empty string`() {
        val plaintext = ""

        val encrypted = LocalEncryptionManager.encrypt(plaintext)
        val decrypted = LocalEncryptionManager.decrypt(encrypted)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `encrypt handles unicode characters`() {
        val plaintext = "中文测试 🎉 한국어 العربية"

        val encrypted = LocalEncryptionManager.encrypt(plaintext)
        val decrypted = LocalEncryptionManager.decrypt(encrypted)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `encrypt handles long text`() {
        val plaintext = "A".repeat(10000)

        val encrypted = LocalEncryptionManager.encrypt(plaintext)
        val decrypted = LocalEncryptionManager.decrypt(encrypted)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `encrypt handles special characters`() {
        val plaintext = "!@#$%^&*()_+-=[]{}|;':\",./<>?\\`~"

        val encrypted = LocalEncryptionManager.encrypt(plaintext)
        val decrypted = LocalEncryptionManager.decrypt(encrypted)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `decrypt with tampered data throws exception`() {
        val plaintext = "Sensitive data"
        val encrypted = LocalEncryptionManager.encrypt(plaintext)

        // Tamper with the encrypted data
        val tamperedData = encrypted.substring(0, encrypted.length - 5) + "XXXXX"

        assertThrows(Exception::class.java) {
            LocalEncryptionManager.decrypt(tamperedData)
        }
    }

    @Test
    fun `clearKey removes the key from keystore`() {
        // First, create a key
        val plaintext = "Test message"
        LocalEncryptionManager.encrypt(plaintext)

        // Clear the key
        LocalEncryptionManager.clearKey()

        // After clearing, we should be able to encrypt again (new key created)
        // But decrypting old data will fail
        val newEncrypted = LocalEncryptionManager.encrypt(plaintext)
        assertEquals(plaintext, LocalEncryptionManager.decrypt(newEncrypted))
    }
}