package top.foxmoe.releasely.dto

import java.time.LocalDateTime

data class PartnerDto(
    val id: Long? = null,
    val userId: Long? = null,
    val partnerId: Long? = null,
    val status: String? = null,
    val sharedPermissions: String? = null,
    val createdAt: LocalDateTime? = null
)

data class InvitePartnerRequest(
    val userId: Long,
    val partnerId: Long,
    val sharedPermissions: String = "calendar,records"
)

data class UpdatePermissionsRequest(
    val id: Long,
    val sharedPermissions: String
)

data class PartnerInvitationResponse(
    val invitationId: Long,
    val fromUserId: Long,
    val status: String,
    val createdAt: LocalDateTime?
)

enum class PartnerStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}