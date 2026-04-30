-- V4: 添加隐私请求表 (GDPR/个人信息保护法合规)
-- 用于追踪用户提交的数据主体请求（导出/删除/更正）

CREATE TABLE IF NOT EXISTS privacy_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '请求ID',
    user_id BIGINT NOT NULL COMMENT '申请人用户ID',
    request_type VARCHAR(50) NOT NULL COMMENT '请求类型: EXPORT, DELETE, RECTIFY',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '请求状态: PENDING, APPROVED, REJECTED, COMPLETED, CANCELLED',
    details TEXT COMMENT '补充说明或申请理由',
    requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    processed_at DATETIME COMMENT '处理时间',
    processed_by BIGINT COMMENT '处理人（管理员ID）',
    admin_notes TEXT COMMENT '管理员备注',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_request_type (request_type),
    INDEX idx_requested_at (requested_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='隐私请求表 - 数据主体权利请求';