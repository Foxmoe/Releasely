package top.foxmoe.releasely.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import top.foxmoe.releasely.annotation.AuditLog
import top.foxmoe.releasely.dto.ApiResponse
import top.foxmoe.releasely.dto.ResultCode
import top.foxmoe.releasely.service.AccountDeletionService

@RestController
@RequestMapping("/api/users")
class UserController(private val accountDeletionService: AccountDeletionService) {

    @AuditLog(action = "ACCOUNT_DELETE", resourceType = "USER")
    @DeleteMapping("/{userId}")
    fun deleteAccount(@PathVariable userId: Long): ResponseEntity<ApiResponse<String>> {
        val result = accountDeletionService.deleteAccount(userId)
        return if (result.success) {
            ResponseEntity.ok(ApiResponse.success(result.message))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.INTERNAL_ERROR.code, result.message))
        }
    }
}
