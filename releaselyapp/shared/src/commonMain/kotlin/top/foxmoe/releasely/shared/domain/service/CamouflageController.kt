package top.foxmoe.releasely.shared.domain.service

interface CamouflageController {
    fun setCamouflageEnabled(enabled: Boolean)
    fun isCamouflageEnabled(): Boolean
}

class NoOpCamouflageController : CamouflageController {
    override fun setCamouflageEnabled(enabled: Boolean) = Unit
    override fun isCamouflageEnabled(): Boolean = false
}

