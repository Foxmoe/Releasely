package top.foxmoe.releasely.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import top.foxmoe.releasely.annotation.AuditLog
import top.foxmoe.releasely.dto.*
import top.foxmoe.releasely.entity.Partner
import top.foxmoe.releasely.service.PartnerService

@RestController
@RequestMapping("/api/partners")
class PartnerController(private val partnerService: PartnerService) {

    @GetMapping
    fun getPartners(@RequestParam userId: Long): ResponseEntity<ApiResponse<List<Partner>>> {
        val partners = partnerService.getPartnersByUserId(userId)
        return ResponseEntity.ok(ApiResponse.success(partners))
    }

    @GetMapping("/accepted")
    fun getAcceptedPartners(@RequestParam userId: Long): ResponseEntity<ApiResponse<List<Partner>>> {
        val partners = partnerService.getAcceptedPartners(userId)
        return ResponseEntity.ok(ApiResponse.success(partners))
    }

    @GetMapping("/pending")
    fun getPendingInvitations(@RequestParam userId: Long): ResponseEntity<ApiResponse<List<Partner>>> {
        val invitations = partnerService.getPendingInvitations(userId)
        return ResponseEntity.ok(ApiResponse.success(invitations))
    }

    @GetMapping("/{id}")
    fun getPartnerById(@PathVariable id: Long): ResponseEntity<ApiResponse<Partner>> {
        val partner = partnerService.getPartnerById(id)
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        return ResponseEntity.ok(ApiResponse.success(partner))
    }

    @AuditLog(action = "PARTNER_INVITE", resourceType = "PARTNER")
    @PostMapping("/invite")
    fun invitePartner(@RequestBody request: InvitePartnerRequest): ResponseEntity<ApiResponse<CreateInviteCodeResponse>> {
        val inviteResponse = partnerService.createInviteCode(request.userId)
        return ResponseEntity.ok(ApiResponse.success(inviteResponse))
    }

    @PostMapping("/accept")
    fun acceptInvite(@RequestBody request: AcceptInviteRequest): ResponseEntity<ApiResponse<Nothing>> {
        val success = partnerService.acceptInvite(request.inviteCode, request.userId)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("Invite accepted"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.VALIDATE_FAILED))
        }
    }

    @GetMapping("/by-code/{inviteCode}")
    fun getPartnerByInviteCode(@PathVariable inviteCode: String): ResponseEntity<ApiResponse<Partner>> {
        val partner = partnerService.getPartnerByInviteCode(inviteCode)
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        return ResponseEntity.ok(ApiResponse.success(partner))
    }

    @AuditLog(action = "PARTNER_ACCEPT", resourceType = "PARTNER")
    @PutMapping("/{id}/accept")
    fun acceptInvitation(@PathVariable id: Long): ResponseEntity<ApiResponse<Nothing>> {
        val success = partnerService.acceptInvitation(id)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("Invitation accepted"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.VALIDATE_FAILED))
        }
    }

    @AuditLog(action = "PARTNER_REJECT", resourceType = "PARTNER")
    @PutMapping("/{id}/reject")
    fun rejectInvitation(@PathVariable id: Long): ResponseEntity<ApiResponse<Nothing>> {
        val success = partnerService.rejectInvitation(id)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("Invitation rejected"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.VALIDATE_FAILED))
        }
    }

    @AuditLog(action = "PARTNER_UPDATE_PERMISSIONS", resourceType = "PARTNER")
    @PutMapping("/permissions")
    fun updatePermissions(@RequestBody request: UpdatePermissionsRequest): ResponseEntity<ApiResponse<Nothing>> {
        val success = partnerService.updateSharedPermissions(request.id, request.sharedPermissions)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("Permissions updated"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        }
    }

    @PutMapping("/{id}/calendar-sharing")
    fun updateCalendarSharing(
        @PathVariable id: Long,
        @RequestParam enabled: Boolean
    ): ResponseEntity<ApiResponse<Nothing>> {
        val success = partnerService.updateCalendarSharing(id, enabled)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("Calendar sharing updated"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        }
    }

    @AuditLog(action = "PARTNER_REMOVE", resourceType = "PARTNER")
    @DeleteMapping("/{id}")
    fun removePartner(@PathVariable id: Long): ResponseEntity<ApiResponse<Nothing>> {
        val success = partnerService.removePartner(id)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("Partner removed"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        }
    }
}
