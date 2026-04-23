package top.foxmoe.releasely.dto

enum class ResultCode(val code: Int, val message: String) {
    SUCCESS(200, "操作成功"),
    FAILED(400, "操作失败"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    VALIDATE_FAILED(401, "数据验证失败"),
    TOKEN_EXPIRED(401, "令牌已过期"),
    TOKEN_INVALID(401, "令牌无效"),
    USERNAME_EXISTS(409, "用户名已存在"),
    USER_NOT_FOUND(404, "用户不存在"),
    PASSWORD_ERROR(401, "密码错误"),
    2FA_REQUIRED(402, "需要二次验证"),
    CONFLICT(409, "资源冲突");
}