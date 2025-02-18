package com.project.frankit.domain.admin.rqrs

import com.project.frankit.domain.product.enums.Status
import io.swagger.v3.oas.annotations.media.Schema

data class ProductRq(

    @Schema(description = "상품 이름")
    val name: String,

    @Schema(description = "상품 설명")
    val description: String,

    @Schema(description = "상품 가격")
    val price: Long,

    @Schema(description = "상품 배송비")
    val shippingFee: Long,

    @Schema(description = "상품 상태")
    val status: Status,

    @Schema(description = "상품 옵션")
    val productOption: List<ProductOptionRq>,

    ) {
}
