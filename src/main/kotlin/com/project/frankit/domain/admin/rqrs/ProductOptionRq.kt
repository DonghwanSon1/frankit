package com.project.frankit.domain.admin.rqrs

import io.swagger.v3.oas.annotations.media.Schema

data class ProductOptionRq(

    @Schema(description = "상품 옵션 이름")
    val optionName: String,

    @Schema(description = "상품 옵션 추가 금액")
    val additionalPrice: Long,

    ) {
}
