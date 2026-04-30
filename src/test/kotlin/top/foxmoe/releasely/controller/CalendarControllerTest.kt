package top.foxmoe.releasely.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import top.foxmoe.releasely.dto.*
import top.foxmoe.releasely.service.CalendarService
import top.foxmoe.releasely.service.PartnerService
import java.time.LocalDate
import kotlin.test.assertEquals
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ExtendWith(MockitoExtension::class)
class CalendarControllerTest {

    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var calendarService: CalendarService

    @Mock
    private lateinit var partnerService: PartnerService

    @InjectMocks
    private lateinit var calendarController: CalendarController

    private lateinit var objectMapper: ObjectMapper
    private val testUserId: Long = 1L

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(calendarController).build()
        objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    }

    @Test
    fun `getSharedCalendar should return shared calendar for user`() {
        val response = SharedCalendarResponse(
            myEvents = listOf(
                CalendarEventDto(
                    id = 1L,
                    userId = testUserId,
                    userName = "Test User",
                    eventType = "PERIOD",
                    date = LocalDate.of(2024, 1, 1),
                    label = "Period",
                    isOwnEvent = true
                )
            ),
            partnerEvents = emptyList(),
            myCyclePrediction = null,
            partnerCyclePrediction = null
        )
        `when`(calendarService.getSharedCalendar(testUserId)).thenReturn(response)

        val result = mockMvc.perform(get("/api/calendar/shared")
            .param("userId", testUserId.toString()))
            .andExpect(status().isOk)
            .andReturn()

        val response2 = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, response2.code)
    }

    @Test
    fun `getSharedCalendar should return FORBIDDEN when no shared calendar`() {
        `when`(calendarService.getSharedCalendar(testUserId)).thenReturn(null)

        val result = mockMvc.perform(get("/api/calendar/shared")
            .param("userId", testUserId.toString()))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(ResultCode.FORBIDDEN.code, response.code)
    }

    @Test
    fun `getCalendarEvents should return events for user`() {
        val events = listOf(
            CalendarEventDto(
                id = 1L,
                userId = testUserId,
                userName = "Test User",
                eventType = "PERIOD",
                date = LocalDate.of(2024, 1, 1),
                label = "Period",
                isOwnEvent = true
            )
        )
        `when`(calendarService.getCalendarEvents(testUserId)).thenReturn(events)

        val result = mockMvc.perform(get("/api/calendar/events")
            .param("userId", testUserId.toString()))
            .andExpect(status().isOk)
            .andReturn()

        val response = objectMapper.readValue(
            result.response.contentAsString,
            ApiResponse::class.java
        )
        assertEquals(200, response.code)
    }

    private fun <T> `when`(mock: T): org.mockito.stubbing.OngoingStubbing<T> {
        return org.mockito.Mockito.`when`(mock)
    }
}
