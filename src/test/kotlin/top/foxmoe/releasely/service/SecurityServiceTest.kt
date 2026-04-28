package top.foxmoe.releasely.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder
import top.foxmoe.releasely.entity.SecuritySettings
import top.foxmoe.releasely.mapper.SecuritySettingsMapper
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class SecurityServiceTest {

    @Mock
    private lateinit var securitySettingsMapper: SecuritySettingsMapper

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var totpService: TotpService

    @InjectMocks
    private lateinit var securityService: SecurityService

    private val testSettings: SecuritySettings = SecuritySettings(
            id = 1L,
            userId = 1L,
            lockType = SecuritySettings.LOCK_TYPE_NONE,
            isAppLockEnabled = false,
            isDisguiseEnabled = false,
            disguiseType = SecuritySettings.DISGUISE_CALCULATOR,
            isScreenshotProtected = true,
            is2FAEnabled = false,
            failedAttempts = 0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    private val testUserId: Long = 1L

    @BeforeEach
    fun setup() {
        // testSettings and testUserId are already initialized
    }

    @Test
    fun `getSettingsByUserId should return settings when exists`() {
        `when`(securitySettingsMapper.selectOne(any())).thenReturn(testSettings)

        val result = securityService.getSettingsByUserId(testUserId)

        assertNotNull(result)
        assertEquals(testSettings.id, result.id)
    }

    @Test
    fun `getSettingsByUserId should return null when not exists`() {
        `when`(securitySettingsMapper.selectOne(any())).thenReturn(null)

        val result = securityService.getSettingsByUserId(testUserId)

        assertNull(result)
    }

    @Test
    fun `createDefaultSettings should create settings with correct defaults`() {
        `when`(securitySettingsMapper.insert(any<SecuritySettings>())).thenAnswer { 1 }

        val result = securityService.createDefaultSettings(testUserId)

        assertEquals(testUserId, result.userId)
        assertEquals(SecuritySettings.LOCK_TYPE_NONE, result.lockType)
        assertFalse(result.isAppLockEnabled)
        assertFalse(result.isDisguiseEnabled)
        assertTrue(result.isScreenshotProtected)
    }

    @Test
    fun `setPin should encode and save pin`() {
        val pin = "123456"
        `when`(securitySettingsMapper.selectOne(any())).thenReturn(testSettings)
        `when`(passwordEncoder.encode(pin)).thenReturn("encoded_pin_hash")
        `when`(securitySettingsMapper.updateById(any<SecuritySettings>())).thenReturn(1)

        val result = securityService.setPin(testUserId, pin)

        assertTrue(result)
    }

    @Test
    fun `verifyPin should return success when pin is correct`() {
        val pin = "123456"
        testSettings.pinHash = "encoded_hash"
        `when`(securitySettingsMapper.selectOne(any())).thenReturn(testSettings)
        `when`(passwordEncoder.matches(pin, testSettings.pinHash)).thenReturn(true)
        `when`(securitySettingsMapper.updateById(any<SecuritySettings>())).thenReturn(1)

        val result = securityService.verifyPin(testUserId, pin)

        assertTrue(result.success)
        assertNull(result.lockedUntil)
    }

    @Test
    fun `verifyPin should return failure when pin is incorrect`() {
        val pin = "wrong_pin"
        testSettings.pinHash = "encoded_hash"
        `when`(securitySettingsMapper.selectOne(any())).thenReturn(testSettings)
        `when`(passwordEncoder.matches(pin, testSettings.pinHash)).thenReturn(false)
        `when`(securitySettingsMapper.updateById(any<SecuritySettings>())).thenReturn(1)

        val result = securityService.verifyPin(testUserId, pin)

        assertFalse(result.success)
        assertEquals(4, result.attemptsRemaining)
    }

    @Test
    fun `verifyPin should lock after max failed attempts`() {
        val pin = "wrong_pin"
        testSettings.pinHash = "encoded_hash"
        testSettings.failedAttempts = 4
        `when`(securitySettingsMapper.selectOne(any())).thenReturn(testSettings)
        `when`(passwordEncoder.matches(pin, testSettings.pinHash)).thenReturn(false)
        `when`(securitySettingsMapper.updateById(any<SecuritySettings>())).thenReturn(1)

        val result = securityService.verifyPin(testUserId, pin)

        assertFalse(result.success)
        assertEquals(0, result.attemptsRemaining)
        assertNotNull(result.lockedUntil)
    }

    @Test
    fun `removePin should clear pin and disable lock`() {
        testSettings.pinHash = "some_hash"
        testSettings.lockType = SecuritySettings.LOCK_TYPE_PIN
        testSettings.isAppLockEnabled = true
        `when`(securitySettingsMapper.selectOne(any())).thenReturn(testSettings)
        `when`(securitySettingsMapper.updateById(any<SecuritySettings>())).thenReturn(1)

        val result = securityService.removePin(testUserId)

        assertTrue(result)
    }

    @Test
    fun `enableDisguise should set disguise enabled`() {
        `when`(securitySettingsMapper.selectOne(any())).thenReturn(testSettings)
        `when`(securitySettingsMapper.updateById(any<SecuritySettings>())).thenReturn(1)

        val result = securityService.enableDisguise(testUserId, SecuritySettings.DISGUISE_WEATHER)

        assertTrue(result)
    }

    @Test
    fun `disableDisguise should set disguise disabled`() {
        testSettings.isDisguiseEnabled = true
        `when`(securitySettingsMapper.selectOne(any())).thenReturn(testSettings)
        `when`(securitySettingsMapper.updateById(any<SecuritySettings>())).thenReturn(1)

        val result = securityService.disableDisguise(testUserId)

        assertTrue(result)
    }

    @Test
    fun `setScreenshotProtection should update setting`() {
        `when`(securitySettingsMapper.selectOne(any())).thenReturn(testSettings)
        `when`(securitySettingsMapper.updateById(any<SecuritySettings>())).thenReturn(1)

        val result = securityService.setScreenshotProtection(testUserId, false)

        assertTrue(result)
    }

    @Test
    fun `resetFailedAttempts should clear lock state`() {
        testSettings.failedAttempts = 5
        testSettings.lockedUntil = LocalDateTime.now().plusMinutes(30)
        `when`(securitySettingsMapper.selectOne(any())).thenReturn(testSettings)
        `when`(securitySettingsMapper.updateById(any<SecuritySettings>())).thenReturn(1)

        val result = securityService.resetFailedAttempts(testUserId)

        assertTrue(result)
    }

    private fun <T> any(): T {
        return org.mockito.ArgumentMatchers.any()
    }

    private fun <T> `when`(mock: T): org.mockito.stubbing.OngoingStubbing<T> {
        return org.mockito.Mockito.`when`(mock)
    }
}