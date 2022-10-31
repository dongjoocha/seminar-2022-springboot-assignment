package com.wafflestudio.seminar.core.seminar.database

import com.wafflestudio.seminar.common.Seminar400
import com.wafflestudio.seminar.common.Seminar403
import com.wafflestudio.seminar.common.Seminar404
import com.wafflestudio.seminar.core.seminar.api.request.CreateSeminarRequest
import com.wafflestudio.seminar.core.seminar.api.request.EditSeminarRequest
import com.wafflestudio.seminar.core.seminar.api.request.JoinSeminarRequest
import com.wafflestudio.seminar.core.seminar.domain.SearchSeminarResponse
import com.wafflestudio.seminar.core.seminar.domain.SeminarPort
import com.wafflestudio.seminar.core.seminar.domain.SeminarResponse
import com.wafflestudio.seminar.core.user.database.UserRepository
import com.wafflestudio.seminar.core.user.domain.User
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

@Component
class SeminarAdapter(
    private val userRepository: UserRepository,
    private val seminarRepository: SeminarRepository,
    private val userSeminarRepository: UserSeminarRepository
) : SeminarPort {
    override fun createSeminar(userId: Long, createSeminarRequest: CreateSeminarRequest) = createSeminarRequest.run {
        try {
            LocalTime.parse(time)
        } catch (e: DateTimeParseException) {
            throw Seminar400("세미나 시간 입력 형식이 잘못되었습니다.")
        }
        val userEntity = userRepository.findByIdOrNull(userId) ?: throw Seminar404("해당 아이디(${userId})로 등록된 사용자가 없어요.")
        userEntity.instructorProfile ?: throw Seminar403("세미나 진행자만 세미나를 만들 수 있습니다.")
        userEntity.userSeminars.forEach {
            if (it.role == User.Role.INSTRUCTOR) {
                throw Seminar400("이미 진행 중인 세미나가 있으므로 새로운 세미나를 만들 수 없습니다.")
            }
        }
        val seminarEntity = seminarRepository.save(
            SeminarEntity(
                name = name!!,
                capacity = capacity!!,
                count = count!!,
                time = LocalTime.parse(time),
                online = online,
                creatorId = userId
            )
        )
        val userSeminarEntity = userSeminarRepository.save(
            UserSeminarEntity(
                user = userEntity,
                seminar = seminarEntity,
                joinedAt = LocalDateTime.now(),
                isActive = true,
                role = User.Role.INSTRUCTOR
            )
        )
        userEntity.userSeminars.add(userSeminarEntity)
        seminarEntity.userSeminars.add(userSeminarEntity)
        userRepository.save(userEntity)
        seminarRepository.save(seminarEntity).toSeminarResponse()
    }

    override fun editSeminar(userId: Long, editSeminarRequest: EditSeminarRequest) = editSeminarRequest.run {
        if (time != null) {
            try {
                LocalTime.parse(time)
            } catch (e: DateTimeParseException) {
                throw Seminar400("세미나 시간 입력 형식이 잘못되었습니다.")
            }
        }
        val seminarEntity =
            seminarRepository.findByIdOrNull(seminarId) ?: throw Seminar404("해당 아이디(${seminarId})로 등록된 세미나가 없습니다.")
        if (userId != seminarEntity.creatorId) {
            throw Seminar403("세미나를 만든 사람이 아니면 수정을 할 수 없습니다.")
        }

        if (capacity != null) seminarEntity.capacity = capacity
        if (count != null) seminarEntity.count = count
        if (name != null) seminarEntity.name = name
        if (time != null) seminarEntity.time = LocalTime.parse(time)
        if (online != null) seminarEntity.online = online
        seminarRepository.save(seminarEntity).toSeminarResponse()
    }

    override fun getSeminar(seminarId: Long): SeminarResponse {
        val seminarEntity =
            seminarRepository.findByIdOrNull(seminarId) ?: throw Seminar404("해당 아이디(${seminarId})로 등록된 세미나가 없습니다.")
        return seminarEntity.toSeminarResponse()
    }

    override fun searchSeminar(name: String?, order: String?): List<SearchSeminarResponse> {
        val seminars = mutableListOf<SearchSeminarResponse>()
        val seminarEntities: MutableList<SeminarEntity> =
            if (name == null) seminarRepository.findAll() else seminarRepository.findContainingName(name)
        if (order == "earliest") seminarEntities.sort() else seminarEntities.reverse()
        seminarEntities.forEach { seminars.add(it.toSearchSeminarResponse()) }
        return seminars
    }

    override fun joinSeminar(seminarId: Long, userId: Long, joinSeminarRequest: JoinSeminarRequest): SeminarResponse {
        val seminarEntity =
            seminarRepository.findByIdOrNull(seminarId) ?: throw Seminar404("해당 아이디(${seminarId})로 등록된 세미나가 없습니다.")
        val userEntity = userRepository.findByIdOrNull(userId) ?: throw Seminar404("해당 아이디(${userId})로 등록된 사용자가 없어요.")
        val thisUserSeminar = userEntity.userSeminars.find { it.seminar.id == seminarId }
        if (thisUserSeminar != null) throw Seminar400("이미 세미나에 ${thisUserSeminar.role}(으)로 참여 중입니다.")

        if (User.Role.valueOf(joinSeminarRequest.role) == User.Role.PARTICIPANT) {
            if (userEntity.participantProfile == null) throw Seminar403("수강생 신분이 아니므로 세미나에 수강생으로 참여할 수 없습니다.")
            if (!userEntity.participantProfile!!.isRegistered) throw Seminar403("비활성화된 회원이므로 세미나에 수강생으로 참여할 수 없습니다.")
            if (seminarEntity.getParticipantCount() >= seminarEntity.capacity) throw Seminar400("세미나 정원이 가득 차서 참여할 수 없습니다.")
        } else {
            if (userEntity.instructorProfile == null) throw Seminar403("진행자 신분이 아니므로 세미나에 진행자로 참여할 수 없습니다.")
            if (userEntity.getInstructingSeminar() != null) throw Seminar400("이미 진행 중인 세미나가 있으므로, 다른 세미나에 진행자로 참여할 수 없습니다.")
        }
        val userSeminarEntity = userSeminarRepository.save(
            UserSeminarEntity(
                user = userEntity,
                seminar = seminarEntity,
                role = User.Role.valueOf(joinSeminarRequest.role),
                joinedAt = LocalDateTime.now(),
                isActive = true
            )
        )
        userEntity.userSeminars.add(userSeminarEntity)
        seminarEntity.userSeminars.add(userSeminarEntity)
        userRepository.save(userEntity)
        return seminarRepository.save(seminarEntity).toSeminarResponse()
    }
}