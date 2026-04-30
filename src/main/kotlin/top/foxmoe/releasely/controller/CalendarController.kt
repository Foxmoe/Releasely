package top.foxmoe.releasely.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import top.foxmoe.releasely.dto.*
import top.foxmoe.releasely.service.CalendarService
import top.foxmoe.releasely.service.PartnerService

@RestController
@RequestMapping("/api/calendar")
class CalendarController(
    private val calendarService: CalendarService,
    private val partnerService: PartnerService
) {

    @GetMapping("/shared")
    fun getSharedCalendar(@RequestParam userId: Long): ResponseEntity<ApiResponse<SharedCalendarResponse>> {
        val sharedCalendar = calendarService.getSharedCalendar(userId)
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.FORBIDDEN))
        return ResponseEntity.ok(ApiResponse.success(sharedCalendar))
    }

    @GetMapping("/events")
    fun getCalendarEvents(@RequestParam userId: Long): ResponseEntity<ApiResponse<List<CalendarEventDto>>> {
        val events = calendarService.getCalendarEvents(userId)
        return ResponseEntity.ok(ApiResponse.success(events))
    }

    @PutMapping("/sharing")
    fun updateCalendarSharing(@RequestBody request: UpdateCalendarSharingRequest): ResponseEntity<ApiResponse<Nothing>> {
        val success = partnerService.updateCalendarSharing(request.id, request.enabled)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("Calendar sharing updated"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        }
    }
}
