package top.foxmoe.releasely.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxmoe.releasely.dto.RequestStatus
import top.foxmoe.releasely.dto.RequestType
import top.foxmoe.releasely.entity.PrivacyRequest
import top.foxmoe.releasely.mapper.PrivacyRequestMapper
import java.time.LocalDateTime

/**
 * 隐私请求服务
 * 处理数据主体请求（GDPR/个人信息保护法合规）
 */
@Service
class PrivacyRequestService(
    private val privacyRequestMapper: PrivacyRequestMapper,
    private val accountDeletionService: AccountDeletionService
) {

    /**
     * 用户提交数据主体请求
     */
    @Transactional(rollbackFor = [Exception::class])
    fun submitRequest(userId: Long, requestType: String, details: String?): PrivacyRequestResult {
        // 验证请求类型
        val validTypes = RequestType.entries.map { it.value }
        if (requestType !in validTypes) {
            return PrivacyRequestResult(
                success = false,
                message = "无效的请求类型：$requestType"
            )
        }

        // 检查是否有待处理的同类型请求
        val existingRequest = privacyRequestMapper.selectOne(
            QueryWrapper<PrivacyRequest>()
                .eq("user_id", userId)
                .eq("request_type", requestType)
                .eq("status", RequestStatus.PENDING.value)
        )
        if (existingRequest != null) {
            return PrivacyRequestResult(
                success = false,
                message = "您已有待处理的相同类型请求，请等待处理完成后再提交"
            )
        }

        // 创建新请求
        val privacyRequest = PrivacyRequest(
            userId = userId,
            requestType = requestType,
            status = RequestStatus.PENDING.value,
            details = details,
            requestedAt = LocalDateTime.now()
        )

        privacyRequestMapper.insert(privacyRequest)

        return PrivacyRequestResult(
            success = true,
            message = "请求提交成功",
            request = privacyRequest
        )
    }

    /**
     * 用户查询自己的数据请求列表
     */
    fun getUserRequests(userId: Long): List<PrivacyRequest> {
        return privacyRequestMapper.selectList(
            QueryWrapper<PrivacyRequest>()
                .eq("user_id", userId)
                .orderByDesc("requested_at")
        )
    }

    /**
     * 用户取消自己的待处理请求
     */
    @Transactional(rollbackFor = [Exception::class])
    fun cancelRequest(userId: Long, requestId: Long): PrivacyRequestResult {
        val request = privacyRequestMapper.selectOne(
            QueryWrapper<PrivacyRequest>()
                .eq("id", requestId)
                .eq("user_id", userId)
                .eq("status", RequestStatus.PENDING.value)
        )

        if (request == null) {
            return PrivacyRequestResult(
                success = false,
                message = "请求不存在或无法取消"
            )
        }

        request.status = RequestStatus.CANCELLED.value
        request.processedAt = LocalDateTime.now()
        privacyRequestMapper.updateById(request)

        return PrivacyRequestResult(
            success = true,
            message = "请求已取消",
            request = request
        )
    }

    /**
     * 管理员：获取所有数据请求列表
     */
    fun getAllRequests(status: String? = null, requestType: String? = null): List<PrivacyRequest> {
        val queryWrapper = QueryWrapper<PrivacyRequest>()
            .orderByDesc("requested_at")

        status?.let { queryWrapper.eq("status", it) }
        requestType?.let { queryWrapper.eq("request_type", it) }

        return privacyRequestMapper.selectList(queryWrapper)
    }

    /**
     * 管理员：处理数据请求
     */
    @Transactional(rollbackFor = [Exception::class])
    fun processRequest(requestId: Long, adminId: Long, newStatus: String, adminNotes: String?): PrivacyRequestResult {
        val validStatuses = listOf(
            RequestStatus.APPROVED.value,
            RequestStatus.REJECTED.value,
            RequestStatus.COMPLETED.value
        )
        if (newStatus !in validStatuses) {
            return PrivacyRequestResult(
                success = false,
                message = "无效的处理状态：$newStatus"
            )
        }

        val request = privacyRequestMapper.selectById(requestId)
        if (request == null) {
            return PrivacyRequestResult(
                success = false,
                message = "请求不存在"
            )
        }

        if (request.status != RequestStatus.PENDING.value) {
            return PrivacyRequestResult(
                success = false,
                message = "请求已处理，无法重复处理"
            )
        }

        // 更新请求状态
        request.status = newStatus
        request.processedAt = LocalDateTime.now()
        request.processedBy = adminId
        request.adminNotes = adminNotes

        // 如果是批准删除请求，自动执行账户删除
        if (newStatus == RequestStatus.APPROVED.value && request.requestType == RequestType.DELETE.value) {
            val deleteResult = accountDeletionService.deleteAccount(request.userId!!)
            if (!deleteResult.success) {
                return PrivacyRequestResult(
                    success = false,
                    message = "处理请求失败：${deleteResult.message}"
                )
            }
        }

        privacyRequestMapper.updateById(request)

        return PrivacyRequestResult(
            success = true,
            message = "请求已处理",
            request = request
        )
    }

    data class PrivacyRequestResult(
        val success: Boolean,
        val message: String,
        val request: PrivacyRequest? = null
    )
}