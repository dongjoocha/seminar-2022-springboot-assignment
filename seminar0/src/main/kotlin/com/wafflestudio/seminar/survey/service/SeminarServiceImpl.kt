package com.wafflestudio.seminar.survey.service

import com.wafflestudio.seminar.survey.domain.OperatingSystem
import com.wafflestudio.seminar.survey.domain.SurveyResponse
import com.wafflestudio.seminar.survey.domain.database.SurveyResponseRepository
import com.wafflestudio.seminar.survey.domain.database.OsRepository

import org.springframework.stereotype.Service

@Service
class SeminarServiceImpl(
    private val surveyResponseRepository: SurveyResponseRepository, private val osRepository: OsRepository
) : SeminarService {
    override fun getSurveyResponseAll(): List<SurveyResponse> {
        return surveyResponseRepository.findAll()
    }

    override fun getSurveyResponseById(id: Long): SurveyResponse {
        return surveyResponseRepository.findById(id)
    }

    override fun getOsAll(): List<OperatingSystem> {
        return osRepository.findAll()
    }

    override fun getOsById(id: Long): OperatingSystem {
        return osRepository.findById(id)
    }

    override fun getOsByName(osName: String): List<OperatingSystem> {
        return osRepository.findByName(osName)
    }
}