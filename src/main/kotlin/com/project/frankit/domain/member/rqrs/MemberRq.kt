package com.project.frankit.domain.member.rqrs

import com.fasterxml.jackson.annotation.JsonProperty
import com.project.frankit.domain.member.enums.Role
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class MemberRq(

    @field:NotBlank
    @field:Email
    @JsonProperty("email")
    @Schema(description = "유저 이메일 ID")
    private val _email: String?,

    @field:NotBlank
    @Schema(description = "유저 PW")
    val password: String,

    @field:NotBlank
    @Schema(description = "유저 이름")
    val name: String,

    @field:NotBlank
    @Schema(description = "가게 이름")
    val storeName: String,

    @field:Valid
    @Schema(description = "유저 역할")
    val role: Role,

) {
    val email: String
        get() = _email!!
}
