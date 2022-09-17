package com.wafflestudio.seminar.survey.service

import com.wafflestudio.seminar.survey.domain.OperatingSystem
import com.wafflestudio.seminar.survey.domain.SurveyResponse

interface SeminarService {
    fun getSurveyResponseAll(): List<SurveyResponse>
    
    fun getSurveyResponseById(id: Long): SurveyResponse
    
    fun getOsAll(): List<OperatingSystem>
    
    fun getOsById(id: Long): OperatingSystem
    
    fun getOsByName(osName: String): List<OperatingSystem>
}