package com.wafflestudio.seminar.core.seminar.api.request

import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

data class EditSeminarRequest(
    @field:NotNull(message = "해당 정보가 없습니다.")
    val seminarId: Long?,

    @field:Positive(message = "세미나 정원은 양수여야 합니다.")
    val capacity: Int?,

    @field:Positive(message = "세미나 횟수는 양수여야 합니다.")
    val count: Int?,

    val name: String?,
    val time: String?,
    val online: Boolean?,
) 