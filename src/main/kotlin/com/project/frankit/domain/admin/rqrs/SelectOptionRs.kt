package com.project.frankit.domain.admin.rqrs

import io.swagger.v3.oas.annotations.media.Schema

data class SelectOptionRs(

    @Schema(description = "선택 옵션 이름")
    val name: String,

) {
}
