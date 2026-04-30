package top.foxmoe.releasely.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import org.springframework.stereotype.Service
import top.foxmoe.releasely.dto.CreateInviteCodeResponse
import top.foxmoe.releasely.entity.Partner
import top.foxmoe.releasely.mapper.PartnerMapper
import java.time.LocalDateTime

@Service
class PartnerService(private val partnerMapper: PartnerMapper) {

    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_ACCEPTED = "accepted"
        const val STATUS_REJECTED = "rejected"
        const val INVITE_LINK_SCHEME = "releasely://invite/"
        const val INVITE_CODE_LENGTH = 6
        private val INVITE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    }

    fun getPartnersByUserId(userId: Long): List<Partner> {
        val wrapper = QueryWrapper<Partner>()
            .eq("user_id", userId)
            .orderByDesc("created_at")
        return partnerMapper.selectList(wrapper)
    }

    fun getAcceptedPartners(userId: Long): List<Partner> {
        val wrapper = QueryWrapper<Partner>()
            .eq("user_id", userId)
            .eq("status", STATUS_ACCEPTED)
            .orderByDesc("created_at")
        return partnerMapper.selectList(wrapper)
    }

    fun getPartnerById(id: Long): Partner? {
        return partnerMapper.selectById(id)
    }

    fun getPartnerRelation(userId: Long, partnerId: Long): Partner? {
        val wrapper = QueryWrapper<Partner>()
            .eq("user_id", userId)
            .eq("partner_id", partnerId)
        return partnerMapper.selectOne(wrapper)
    }

    fun invitePartner(partner: Partner): Long {
        partner.createdAt = LocalDateTime.now()
        partner.status = STATUS_PENDING
        partnerMapper.insert(partner)
        return partner.id ?: 0L
    }

    fun acceptInvitation(id: Long): Boolean {
        val partner = partnerMapper.selectById(id)
        if (partner != null && partner.status == STATUS_PENDING) {
            partner.status = STATUS_ACCEPTED
            return partnerMapper.updateById(partner) > 0
        }
        return false
    }

    fun rejectInvitation(id: Long): Boolean {
        val partner = partnerMapper.selectById(id)
        if (partner != null && partner.status == STATUS_PENDING) {
            partner.status = STATUS_REJECTED
            return partnerMapper.updateById(partner) > 0
        }
        return false
    }

    fun updateSharedPermissions(id: Long, permissions: String): Boolean {
        val partner = partnerMapper.selectById(id)
        if (partner != null) {
            partner.sharedPermissions = permissions
            return partnerMapper.updateById(partner) > 0
        }
        return false
    }

    fun removePartner(id: Long): Boolean {
        val partner = partnerMapper.selectById(id)
        if (partner != null) {
            return partnerMapper.deleteById(id) > 0
        }
        return false
    }

    fun updateCalendarSharing(id: Long, enabled: Boolean): Boolean {
        val partner = partnerMapper.selectById(id)
        if (partner != null) {
            partner.calendarSharingEnabled = enabled
            return partnerMapper.updateById(partner) > 0
        }
        return false
    }

    fun isCalendarSharingEnabled(userId: Long, partnerId: Long): Boolean {
        val relation = getPartnerRelation(userId, partnerId)
        return relation?.calendarSharingEnabled == true && relation.status == STATUS_ACCEPTED
    }

    fun getAcceptedPartnerRelation(userId: Long): Partner? {
        val wrapper = QueryWrapper<Partner>()
            .eq("user_id", userId)
            .eq("status", STATUS_ACCEPTED)
            .eq("calendar_sharing_enabled", true)
        return partnerMapper.selectOne(wrapper)
    }

    fun getPendingInvitations(userId: Long): List<Partner> {
        val wrapper = QueryWrapper<Partner>()
            .eq("partner_id", userId)
            .eq("status", STATUS_PENDING)
            .orderByDesc("created_at")
        return partnerMapper.selectList(wrapper)
    }

    fun generateInviteCode(): String {
        val random = java.security.SecureRandom()
        return (1..INVITE_CODE_LENGTH)
            .map { INVITE_CODE_CHARS[random.nextInt(INVITE_CODE_CHARS.length)] }
            .joinToString("")
    }

    fun createInviteCode(userId: Long): CreateInviteCodeResponse {
        val inviteCode = generateInviteCode()
        // Create a pending partner record with the invite code
        val partner = Partner(
            userId = userId,
            partnerId = null,
            inviteCode = inviteCode,
            status = STATUS_PENDING,
            sharedPermissions = "calendar,records"
        )
        partnerMapper.insert(partner)
        return CreateInviteCodeResponse(
            inviteCode = inviteCode,
            inviteLink = "$INVITE_LINK_SCHEME$inviteCode"
        )
    }

    fun getPartnerByInviteCode(inviteCode: String): Partner? {
        val wrapper = QueryWrapper<Partner>()
            .eq("invite_code", inviteCode)
            .eq("status", STATUS_PENDING)
        return partnerMapper.selectOne(wrapper)
    }

    fun getPartnerIdByInviteCode(inviteCode: String): Long? {
        return getPartnerByInviteCode(inviteCode)?.id
    }

    fun acceptInvite(inviteCode: String, userId: Long): Boolean {
        val partner = getPartnerByInviteCode(inviteCode)
        if (partner != null && partner.userId != userId) {
            // Update the existing pending record with the accepting user's partnerId
            partner.partnerId = userId
            partner.status = STATUS_ACCEPTED
            partner.createdAt = LocalDateTime.now()
            partnerMapper.updateById(partner)
            return true
        }
        return false
    }
}