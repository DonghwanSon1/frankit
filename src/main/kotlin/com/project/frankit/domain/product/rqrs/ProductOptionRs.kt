package com.project.frankit.domain.product.rqrs

import com.project.frankit.domain.product.productOption.ProductOption
import io.swagger.v3.oas.annotations.media.Schema

data class ProductOptionRs(

    @Schema(description = "상품 옵션 sn")
    val optionSn: Long,

    @Schema(description = "상품 옵션 이름")
    val optionName: String,

    @Schema(description = "상품 옵션 추가 금액")
    val additionalPrice: Long,

    ) {

    companion object {
        fun createProductOptionRs(productOption: ProductOption): ProductOptionRs {
            return ProductOptionRs(
                optionSn = productOption.sn!!,
                optionName = productOption.name,
                additionalPrice = productOption.additionalPrice
            )
        }
    }
}
