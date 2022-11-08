package com.wafflestudio.seminar.core.seminar

import com.wafflestudio.seminar.core.seminar.api.request.CreateSeminarRequest
import com.wafflestudio.seminar.core.seminar.api.request.EditSeminarRequest
import com.wafflestudio.seminar.core.seminar.api.request.JoinSeminarRequest
import com.wafflestudio.seminar.core.seminar.database.SeminarEntity
import com.wafflestudio.seminar.core.seminar.database.SeminarRepository
import com.wafflestudio.seminar.core.seminar.database.UserSeminarEntity
import com.wafflestudio.seminar.core.seminar.service.SeminarService
import com.wafflestudio.seminar.core.user.UserTestHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.time.LocalTime
import javax.transaction.Transactional

@SpringBootTest
internal class SeminarServiceTest @Autowired constructor(
        private val seminarService: SeminarService,
        private val seminarRepository: SeminarRepository,
        private val userTestHelper: UserTestHelper,
) {

    @Test
    fun `세미나를 등록할 수 있다`() {

        // given
        val user = userTestHelper.createInstructorUser("w1@ffle.com", password = "1234")
        val seminar = CreateSeminarRequest("seminar1", 30, 16, "12:30")

        // when
        val result = seminarService.createSeminar(user.id, seminar)

        // then
        assertThat(seminarRepository.findAll()).hasSize(1)
        assertThat(result.name).isEqualTo("seminar1")
    }


    @Test
    @Transactional
    fun `세미나를 수정할 수 있다`() {

        // given 
        createSeminars(1)
        val seminarEntity = seminarRepository.findById(1)
        val newSeminar = EditSeminarRequest(seminarEntity.get().id, 20, 15, null, null, null)

        // when
        val result = seminarService.editSeminar(seminarEntity.get().id, newSeminar)

        // then
        assertThat(seminarRepository.findAll()).hasSize(1)
        assertThat(result.capacity).isEqualTo(20)
        assertThat(result.name).isEqualTo("seminar1")
    }



    @Test
    @Transactional
    fun `세미나에 가입할 수 있다`() {

        // when
        val instructor = userTestHelper.createInstructorUser("ins@tructor.com")
        val participants = (1..10).map { userTestHelper.createParticipantUser("par@ticipant#$it.com") }
        val seminar = CreateSeminarRequest("seminar1", 30, 16, "12:30")
        val seminarEntity = seminarService.createSeminar(instructor.id, seminar)


        // then

        (0..9).map { seminarService.joinSeminar(seminarEntity.id, participants["$it".toInt()].id, JoinSeminarRequest("PARTICIPANT")) }

        assertThat(seminarRepository.findByIdOrNull(1)?.getParticipantCount()).isEqualTo(10)
        
    }


    private fun createSeminars(n: Int) {

        val user = userTestHelper.createInstructorUser("w$n@ffle.com", password = "1234")
        val seminar = CreateSeminarRequest("seminar1", 30, 16, "12:30")
        seminarService.createSeminar(user.id, seminar)

    }

}