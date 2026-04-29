package top.foxmoe.releasely.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxmoe.releasely.dto.ApiResponse

@RestController
@RequestMapping("/api/feedback")
class FeedbackController {

    private val logger = LoggerFactory.getLogger(FeedbackController::class.java)

    data class FeedbackRequest(
        val type: String,
        val content: String,
        val contact: String?
    )

    @PostMapping
    fun submitFeedback(@RequestBody request: FeedbackRequest): ResponseEntity<ApiResponse<String>> {
        return try {
            if (request.content.isBlank()) {
                return ResponseEntity.ok(ApiResponse.error(400, "反馈内容不能为空"))
            }
            logger.info("[Feedback] type=${request.type}, contact=${request.contact}, content=${request.content}")
            ResponseEntity.ok(ApiResponse.success("反馈已提交，感谢你的建议！"))
        } catch (e: Exception) {
            logger.error("Feedback submission failed", e)
            ResponseEntity.ok(ApiResponse.error(500, "提交失败，请稍后重试"))
        }
    }
}
