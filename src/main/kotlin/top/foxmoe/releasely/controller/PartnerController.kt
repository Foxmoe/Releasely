package top.foxmoe.releasely.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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

    @PostMapping("/invite")
    fun invitePartner(@RequestBody request: InvitePartnerRequest): ResponseEntity<ApiResponse<Partner>> {
        val existingRelation = partnerService.getPartnerRelation(request.userId, request.partnerId)
        if (existingRelation != null) {
            return ResponseEntity.ok(ApiResponse.error(409, "Partnership already exists"))
        }

        val partner = Partner(
            userId = request.userId,
            partnerId = request.partnerId,
            sharedPermissions = request.sharedPermissions
        )
        val id = partnerService.invitePartner(partner)
        val createdPartner = partnerService.getPartnerById(id)
        return ResponseEntity.ok(ApiResponse.success(createdPartner))
    }

    @PutMapping("/{id}/accept")
    fun acceptInvitation(@PathVariable id: Long): ResponseEntity<ApiResponse<Nothing>> {
        val success = partnerService.acceptInvitation(id)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("Invitation accepted"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.VALIDATE_FAILED))
        }
    }

    @PutMapping("/{id}/reject")
    fun rejectInvitation(@PathVariable id: Long): ResponseEntity<ApiResponse<Nothing>> {
        val success = partnerService.rejectInvitation(id)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("Invitation rejected"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.VALIDATE_FAILED))
        }
    }

    @PutMapping("/permissions")
    fun updatePermissions(@RequestBody request: UpdatePermissionsRequest): ResponseEntity<ApiResponse<Nothing>> {
        val success = partnerService.updateSharedPermissions(request.id, request.sharedPermissions)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("Permissions updated"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        }
    }

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