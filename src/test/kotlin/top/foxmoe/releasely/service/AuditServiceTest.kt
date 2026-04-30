package top.foxmoe.releasely.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import top.foxmoe.releasely.entity.AuditLog
import top.foxmoe.releasely.mapper.AuditLogMapper
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class AuditServiceTest {

    @Mock
    private lateinit var auditLogMapper: AuditLogMapper

    @InjectMocks
    private lateinit var auditService: AuditService

    private var testUserId: Long = 1L
    private var testIpAddress: String = "192.168.1.1"
    private var testUserAgent: String = "Mozilla/5.0 Test Browser"

    @BeforeEach
    fun setup() {
        testUserId = 1L
        testIpAddress = "192.168.1.1"
        testUserAgent = "Mozilla/5.0 Test Browser"
    }

    @Test
    fun `log should insert audit log with all parameters`() {
        val action = "LOGIN"
        val resourceType = "AUTH"
        val resourceId = "123"
        val details = "User logged in successfully"

        `when`(auditLogMapper.insert(any(AuditLog::class.java))).thenReturn(1)

        auditService.log(
            action = action,
            userId = testUserId,
            resourceType = resourceType,
            resourceId = resourceId,
            details = details,
            success = true,
            ipAddress = testIpAddress,
            userAgent = testUserAgent
        )

        val captor = ArgumentCaptor.forClass(AuditLog::class.java)
        org.mockito.Mockito.verify(auditLogMapper).insert(captor.capture())

        val captured = captor.value
        assertEquals(testUserId, captured.userId)
        assertEquals(action, captured.action)
        assertEquals(resourceType, captured.resourceType)
        assertEquals(resourceId, captured.resourceId)
        assertEquals(details, captured.details)
        assertEquals(testIpAddress, captured.ipAddress)
        assertEquals(testUserAgent, captured.userAgent)
        assertTrue(captured.success!!)
        assertNotNull(captured.timestamp)
    }

    @Test
    fun `log should insert audit log with minimal parameters`() {
        val action = "BACKGROUND_TASK"

        `when`(auditLogMapper.insert(any(AuditLog::class.java))).thenReturn(1)

        auditService.log(action = action)

        val captor = ArgumentCaptor.forClass(AuditLog::class.java)
        org.mockito.Mockito.verify(auditLogMapper).insert(captor.capture())

        val captured = captor.value
        assertNull(captured.userId)
        assertEquals(action, captured.action)
        assertNull(captured.resourceType)
        assertNull(captured.resourceId)
        assertNull(captured.details)
        assertNull(captured.ipAddress)
        assertNull(captured.userAgent)
        assertTrue(captured.success!!)
    }

    @Test
    fun `log should record failed operation`() {
        val action = "LOGIN"
        val details = "Invalid credentials"

        `when`(auditLogMapper.insert(any(AuditLog::class.java))).thenReturn(1)

        auditService.log(
            action = action,
            userId = testUserId,
            details = details,
            success = false
        )

        val captor = ArgumentCaptor.forClass(AuditLog::class.java)
        org.mockito.Mockito.verify(auditLogMapper).insert(captor.capture())

        val captured = captor.value
        assertFalse(captured.success!!)
        assertEquals(details, captured.details)
    }

    @Test
    fun `log should record activity creation`() {
        val action = "ACTIVITY_CREATE"
        val resourceType = "ACTIVITY"
        val resourceId = "456"
        val userId = 1L

        `when`(auditLogMapper.insert(any(AuditLog::class.java))).thenReturn(1)

        auditService.log(
            action = action,
            userId = userId,
            resourceType = resourceType,
            resourceId = resourceId,
            success = true
        )

        val captor = ArgumentCaptor.forClass(AuditLog::class.java)
        org.mockito.Mockito.verify(auditLogMapper).insert(captor.capture())

        val captured = captor.value
        assertEquals(userId, captured.userId)
        assertEquals(action, captured.action)
        assertEquals(resourceType, captured.resourceType)
        assertEquals(resourceId, captured.resourceId)
    }

    @Test
    fun `log should record data deletion`() {
        val action = "ACTIVITY_DELETE"
        val resourceType = "ACTIVITY"
        val resourceId = "789"
        val userId = 2L

        `when`(auditLogMapper.insert(any(AuditLog::class.java))).thenReturn(1)

        auditService.log(
            action = action,
            userId = userId,
            resourceType = resourceType,
            resourceId = resourceId,
            success = true
        )

        val captor = ArgumentCaptor.forClass(AuditLog::class.java)
        org.mockito.Mockito.verify(auditLogMapper).insert(captor.capture())

        val captured = captor.value
        assertEquals(userId, captured.userId)
        assertEquals(action, captured.action)
        assertEquals(resourceType, captured.resourceType)
        assertEquals(resourceId, captured.resourceId)
    }

    @Test
    fun `log should record 2FA verification`() {
        val action = "2FA_VERIFY"
        val resourceType = "AUTH"
        val userId = 3L

        `when`(auditLogMapper.insert(any(AuditLog::class.java))).thenReturn(1)

        auditService.log(
            action = action,
            userId = userId,
            resourceType = resourceType,
            success = true
        )

        val captor = ArgumentCaptor.forClass(AuditLog::class.java)
        org.mockito.Mockito.verify(auditLogMapper).insert(captor.capture())

        val captured = captor.value
        assertEquals(userId, captured.userId)
        assertEquals(action, captured.action)
        assertEquals(resourceType, captured.resourceType)
    }

    @Test
    fun `log should record sync operation`() {
        val action = "SYNC_PUSH"
        val resourceType = "SYNC"
        val userId = 4L

        `when`(auditLogMapper.insert(any(AuditLog::class.java))).thenReturn(1)

        auditService.log(
            action = action,
            userId = userId,
            resourceType = resourceType,
            success = true
        )

        val captor = ArgumentCaptor.forClass(AuditLog::class.java)
        org.mockito.Mockito.verify(auditLogMapper).insert(captor.capture())

        val captured = captor.value
        assertEquals(userId, captured.userId)
        assertEquals(action, captured.action)
        assertEquals(resourceType, captured.resourceType)
    }

    private fun <T> any(type: Class<T>): T {
        return org.mockito.ArgumentMatchers.any(type)
    }

    private fun <T> `when`(mock: T): org.mockito.stubbing.OngoingStubbing<T> {
        return org.mockito.Mockito.`when`(mock)
    }
}
