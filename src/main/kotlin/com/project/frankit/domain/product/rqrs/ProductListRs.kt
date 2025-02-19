package com.project.frankit.domain.product.rqrs

import com.project.frankit.domain.product.enums.Status
import io.swagger.v3.oas.annotations.media.Schema

data class ProductListRs(

    @Schema(description = "상품 sn")
    val sn: Long? = null,

    @Schema(description = "상품 이름")
    val name: String? = null,

    @Schema(description = "상품 가격")
    val price: Long? = null,

    @Schema(description = "상품 상태")
    private val _status: Int? = null,

) {
    val status: String
        get() = Status.fromValue(_status!!).desc
}
