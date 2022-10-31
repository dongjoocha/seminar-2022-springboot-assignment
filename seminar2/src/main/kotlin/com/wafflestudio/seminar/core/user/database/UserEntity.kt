package com.wafflestudio.seminar.core.user.database

import com.wafflestudio.seminar.common.BaseTimeEntity
import com.wafflestudio.seminar.core.seminar.database.UserSeminarEntity
import com.wafflestudio.seminar.core.seminar.domain.InstructingSeminar
import com.wafflestudio.seminar.core.seminar.domain.ParticipatingSeminar
import com.wafflestudio.seminar.core.user.domain.InstructorProfile
import com.wafflestudio.seminar.core.user.domain.ParticipantProfile
import com.wafflestudio.seminar.core.user.domain.ProfileResponse
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "user")
class UserEntity(
    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    var username: String,

    @Column(nullable = false)
    var encodedPassword: String,

    var lastLogin: LocalDateTime,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    val userSeminars: MutableSet<UserSeminarEntity> = mutableSetOf(),

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "participant_profile_id", referencedColumnName = "id")
    var participantProfile: ParticipantProfileEntity?,

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "instructor_profile_id", referencedColumnName = "id")
    var instructorProfile: InstructorProfileEntity?
) : BaseTimeEntity() {
    fun toProfileResponse(
        participatingSeminars: List<ParticipatingSeminar>,
        instructingSeminar: InstructingSeminar?
    ): ProfileResponse {
        return ProfileResponse(
            id = id,
            username = username,
            email = email,
            lastLogin = lastLogin,
            dateJoined = createdAt!!,
            participant = if (participantProfile != null) ParticipantProfile(
                id = participantProfile!!.id,
                university = participantProfile!!.university,
                isRegistered = participantProfile!!.isRegistered,
                seminars = participatingSeminars
            ) else null,
            instructor = if (instructorProfile != null) InstructorProfile(
                id = instructorProfile!!.id,
                company = instructorProfile!!.company,
                year = instructorProfile!!.year,
                instructingSeminars = instructingSeminar
            ) else null
        )
    }
}