package com.wafflestudio.seminar.survey.api

import com.wafflestudio.seminar.survey.domain.OperatingSystem
import com.wafflestudio.seminar.survey.domain.SurveyResponse
import com.wafflestudio.seminar.survey.service.SeminarService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/survey-response")
class SeminarController(
    private val seminarService: SeminarService
) {
    @GetMapping
    fun getSurveyResponseAll(): List<SurveyResponse> {
        return seminarService.getSurveyResponseAll() 
    }
    
    @GetMapping("/{id}")
    fun getSurveyResponse(@PathVariable id: Long): SurveyResponse {
        return seminarService.getSurveyResponseById(id)
    }

    @GetMapping("/os")
    fun getOs(@RequestParam osName: String? = null): List<OperatingSystem> {
        if (osName == null) {
            return seminarService.getOsAll()
        }
        return seminarService.getOsByName(osName)
    }
    
    @GetMapping("/os/{id}")
    fun getOsById(@PathVariable id: Long): OperatingSystem {
        return seminarService.getOsById(id)
    }
}