package com.project.frankit.domain.admin.rqrs

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class ProductRs(

    @Schema(description = "상품 sn")
    val sn: Long,

    @Schema(description = "상품 이름")
    val name: String,

    @Schema(description = "상품 설명")
    val description: String,

    @Schema(description = "상품 가격")
    val price: Long,

    @Schema(description = "상품 배송비")
    val shippingFee: Long,

    @Schema(description = "상품 상태")
    val status: String,

    @Schema(description = "상품 등록일")
    val registrationDate: LocalDateTime,
) {
}
