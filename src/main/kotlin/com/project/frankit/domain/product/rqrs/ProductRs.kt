package com.project.frankit.domain.product.rqrs

import com.fasterxml.jackson.annotation.JsonFormat
import com.project.frankit.domain.product.enums.Status
import com.project.frankit.domain.product.product.Product
import com.project.frankit.domain.product.productOption.ProductOption
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val registrationDate: LocalDateTime,

    @Schema(description = "상품 옵션 리스트")
    val productOptionList: List<ProductOptionRs>?
) {

    companion object {
        fun createProductRs(product: Product, productOptionList: List<ProductOption>?): ProductRs {
            return ProductRs(
                sn = product.sn!!,
                name = product.name,
                description = product.description,
                price = product.price,
                shippingFee = product.shippingFee,
                status = Status.fromValue(product.status).desc,
                registrationDate = product.registrationDate,
                productOptionList = productOptionList?.map {
                    ProductOptionRs(
                        optionSn = it.sn!!,
                        optionName = it.name,
                        additionalPrice = it.additionalPrice
                    )
                }
            )
        }
    }
}
