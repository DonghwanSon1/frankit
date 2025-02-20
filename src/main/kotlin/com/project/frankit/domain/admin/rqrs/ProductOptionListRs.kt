package com.project.frankit.domain.admin.rqrs

import com.fasterxml.jackson.annotation.JsonFormat
import com.project.frankit.domain.product.enums.Status
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class ProductOptionListRs(

    @Schema(description = "상품 sn")
    val productSn: Long? = null,

    @Schema(description = "상품 이름")
    val productName: String? = null,

    @Schema(description = "상품 상태")
    private val _productStatus: Int? = null,

    @Schema(description = "상품 옵션 개수")
    val productOptionCount: Long? = null,

    @Schema(description = "상품 등록일")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val productRegistrationDate: LocalDateTime? = null,

    ) {
    val productStatus: String
        get() = Status.fromValue(_productStatus!!).desc
}
