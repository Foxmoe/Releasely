package top.foxmoe.releasely.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import top.foxmoe.releasely.annotation.AuditLog
import top.foxmoe.releasely.dto.*
import top.foxmoe.releasely.service.PrivacyRequestService
import top.foxmoe.releasely.mapper.UserMapper
import top.foxmoe.releasely.entity.User
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper

@RestController
@RequestMapping("/api/privacy")
class PrivacyController(
    private val privacyRequestService: PrivacyRequestService,
    private val userMapper: UserMapper
) {

    /**
     * 用户提交数据主体请求
     * POST /api/privacy/request
     */
    @AuditLog(action = "PRIVACY_REQUEST_SUBMIT", resourceType = "PRIVACY")
    @PostMapping("/request")
    fun submitRequest(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: DataSubjectRequest
    ): ResponseEntity<ApiResponse<DataSubjectRequestResponse>> {
        val user = userMapper.selectOne(QueryWrapper<User>().eq("username", userDetails.username))
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.USER_NOT_FOUND))

        val result = privacyRequestService.submitRequest(
            userId = user.id!!,
            requestType = request.requestType,
            details = request.details
        )

        return if (result.success && result.request != null) {
            ResponseEntity.ok(ApiResponse.success(
                DataSubjectRequestResponse(
                    id = result.request.id!!,
                    requestType = result.request.requestType!!,
                    status = result.request.status!!,
                    requestedAt = result.request.requestedAt.toString(),
                    details = result.request.details
                )
            ))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.VALIDATE_FAILED.code, result.message))
        }
    }

    /**
     * 用户查询自己的数据请求状态
     * GET /api/privacy/request/status
     */
    @GetMapping("/request/status")
    fun getUserRequests(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<PrivacyRequestListResponse>> {
        val user = userMapper.selectOne(QueryWrapper<User>().eq("username", userDetails.username))
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.USER_NOT_FOUND))

        val requests = privacyRequestService.getUserRequests(user.id!!)

        val responseList = requests.map { request ->
            DataSubjectRequestResponse(
                id = request.id!!,
                requestType = request.requestType!!,
                status = request.status!!,
                requestedAt = request.requestedAt.toString(),
                details = request.details
            )
        }

        return ResponseEntity.ok(ApiResponse.success(PrivacyRequestListResponse(responseList)))
    }

    /**
     * 用户取消自己的待处理请求
     * DELETE /api/privacy/request/{id}
     */
    @AuditLog(action = "PRIVACY_REQUEST_CANCEL", resourceType = "PRIVACY")
    @DeleteMapping("/request/{id}")
    fun cancelRequest(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<String>> {
        val user = userMapper.selectOne(QueryWrapper<User>().eq("username", userDetails.username))
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.USER_NOT_FOUND))

        val result = privacyRequestService.cancelRequest(user.id!!, id)

        return if (result.success) {
            ResponseEntity.ok(ApiResponse.success(result.message))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.VALIDATE_FAILED.code, result.message))
        }
    }

    /**
     * 管理员：获取所有数据请求列表
     * GET /api/privacy/admin/requests
     */
    @GetMapping("/admin/requests")
    fun getAllRequests(
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) requestType: String?
    ): ResponseEntity<ApiResponse<PrivacyRequestListResponse>> {
        val requests = privacyRequestService.getAllRequests(status, requestType)

        val responseList = requests.map { request ->
            DataSubjectRequestResponse(
                id = request.id!!,
                requestType = request.requestType!!,
                status = request.status!!,
                requestedAt = request.requestedAt.toString(),
                details = request.details
            )
        }

        return ResponseEntity.ok(ApiResponse.success(PrivacyRequestListResponse(responseList)))
    }

    /**
     * 管理员：处理数据请求
     * POST /api/privacy/admin/requests/{id}/process
     */
    @AuditLog(action = "PRIVACY_REQUEST_PROCESS", resourceType = "PRIVACY")
    @PostMapping("/admin/requests/{id}/process")
    fun processRequest(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long,
        @RequestBody processRequest: ProcessPrivacyRequest
    ): ResponseEntity<ApiResponse<DataSubjectRequestResponse>> {
        val admin = userMapper.selectOne(QueryWrapper<User>().eq("username", userDetails.username))
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.USER_NOT_FOUND))

        val result = privacyRequestService.processRequest(
            requestId = id,
            adminId = admin.id!!,
            newStatus = processRequest.status,
            adminNotes = processRequest.adminNotes
        )

        return if (result.success && result.request != null) {
            ResponseEntity.ok(ApiResponse.success(
                DataSubjectRequestResponse(
                    id = result.request.id!!,
                    requestType = result.request.requestType!!,
                    status = result.request.status!!,
                    requestedAt = result.request.requestedAt.toString(),
                    details = result.request.details
                )
            ))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.VALIDATE_FAILED.code, result.message))
        }
    }
}