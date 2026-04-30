package top.foxmoe.releasely.dto

/**
 * 数据主体请求类型
 * GDPR 和个人信息保护法规定的数据主体权利类型
 */
enum class RequestType(val value: String, val description: String) {
    EXPORT("EXPORT", "数据导出 - 申请导出个人数据副本"),
    DELETE("DELETE", "账户注销 - 申请删除账户及所有个人数据"),
    RECTIFY("RECTIFY", "数据更正 - 申请更正不准确的个人数据")
}

/**
 * 数据请求状态
 */
enum class RequestStatus(val value: String, val description: String) {
    PENDING("PENDING", "待处理"),
    APPROVED("APPROVED", "已批准"),
    REJECTED("REJECTED", "已拒绝"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消")
}

/**
 * 用户提交数据主体请求的请求体
 */
data class DataSubjectRequest(
    val requestType: String,  // EXPORT, DELETE, RECTIFY
    val details: String? = null  // 可选的补充说明
)

/**
 * 数据主体请求响应
 */
data class DataSubjectRequestResponse(
    val id: Long,
    val requestType: String,
    val status: String,
    val requestedAt: String,
    val details: String? = null
)

/**
 * 用户查询自己的数据请求列表响应
 */
data class PrivacyRequestListResponse(
    val requests: List<DataSubjectRequestResponse>
)

/**
 * 管理员处理数据请求的请求体
 */
data class ProcessPrivacyRequest(
    val status: String,  // APPROVED, REJECTED, COMPLETED
    val adminNotes: String? = null  // 管理员备注
)