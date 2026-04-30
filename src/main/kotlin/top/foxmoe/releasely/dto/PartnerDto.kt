package top.foxmoe.releasely.dto

import java.time.LocalDateTime

data class PartnerDto(
    val id: Long? = null,
    val userId: Long? = null,
    val partnerId: Long? = null,
    val inviteCode: String? = null,
    val status: String? = null,
    val sharedPermissions: String? = null,
    val calendarSharingEnabled: Boolean = false,
    val createdAt: LocalDateTime? = null
)

data class InvitePartnerRequest(
    val userId: Long,
    val partnerInviteCode: String,
    val sharedPermissions: String = "calendar,records"
)

data class CreateInviteCodeResponse(
    val inviteCode: String,
    val inviteLink: String
)

data class AcceptInviteRequest(
    val inviteCode: String,
    val userId: Long
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

data class PartnerListResponse(
    val partners: List<PartnerDto>,
    val total: Int
)

enum class PartnerStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}