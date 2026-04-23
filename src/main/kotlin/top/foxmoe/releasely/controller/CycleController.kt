package top.foxmoe.releasely.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import top.foxmoe.releasely.dto.*
import top.foxmoe.releasely.entity.Cycle
import top.foxmoe.releasely.service.CycleService
import java.time.LocalDate

@RestController
@RequestMapping("/api/cycles")
class CycleController(private val cycleService: CycleService) {

    @GetMapping
    fun getCycles(@RequestParam userId: Long): ResponseEntity<ApiResponse<List<Cycle>>> {
        val cycles = cycleService.getCyclesByUserId(userId)
        return ResponseEntity.ok(ApiResponse.success(cycles))
    }

    @GetMapping("/{id}")
    fun getCycleById(@PathVariable id: Long): ResponseEntity<ApiResponse<Cycle>> {
        val cycle = cycleService.getCycleById(id)
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        return ResponseEntity.ok(ApiResponse.success(cycle))
    }

    @PostMapping
    fun createCycle(@RequestBody request: CreateCycleRequest): ResponseEntity<ApiResponse<Cycle>> {
        val cycle = Cycle(
            userId = request.userId,
            startDate = request.startDate,
            duration = request.duration
        )
        val id = cycleService.createCycle(cycle)
        val createdCycle = cycleService.getCycleById(id)
        return ResponseEntity.ok(ApiResponse.success(createdCycle))
    }

    @PutMapping("/{id}")
    fun updateCycle(
        @PathVariable id: Long,
        @RequestBody request: UpdateCycleRequest
    ): ResponseEntity<ApiResponse<Cycle>> {
        val existingCycle = cycleService.getCycleById(id)
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))

        request.startDate?.let { existingCycle.startDate = it }
        request.duration?.let { existingCycle.duration = it }

        cycleService.updateCycle(existingCycle)
        return ResponseEntity.ok(ApiResponse.success(existingCycle))
    }

    @DeleteMapping("/{id}")
    fun deleteCycle(@PathVariable id: Long): ResponseEntity<ApiResponse<Nothing>> {
        val success = cycleService.deleteCycle(id)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("Cycle deleted"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        }
    }

    @GetMapping("/prediction")
    fun getPrediction(@RequestParam userId: Long): ResponseEntity<ApiResponse<CyclePredictionResponse>> {
        val predictedNext = cycleService.predictNextPeriod(userId)
        val averageLength = cycleService.calculateAverageCycleLength(userId)

        val daysUntilNext = predictedNext?.let {
            LocalDate.now().until(it).days.toLong()
        }

        val response = CyclePredictionResponse(
            predictedNext = predictedNext,
            averageCycleLength = averageLength,
            daysUntilNext = daysUntilNext
        )

        return ResponseEntity.ok(ApiResponse.success(response))
    }
}