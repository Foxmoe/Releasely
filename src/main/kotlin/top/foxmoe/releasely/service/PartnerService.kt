package top.foxmoe.releasely.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import org.springframework.stereotype.Service
import top.foxmoe.releasely.entity.Partner
import top.foxmoe.releasely.mapper.PartnerMapper
import java.time.LocalDateTime

@Service
class PartnerService(private val partnerMapper: PartnerMapper) {

    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_ACCEPTED = "accepted"
        const val STATUS_REJECTED = "rejected"
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

    fun getPendingInvitations(userId: Long): List<Partner> {
        val wrapper = QueryWrapper<Partner>()
            .eq("partner_id", userId)
            .eq("status", STATUS_PENDING)
            .orderByDesc("created_at")
        return partnerMapper.selectList(wrapper)
    }
}