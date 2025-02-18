package com.project.frankit.domain.product.productOption

import com.project.frankit.domain.admin.rqrs.ProductOptionRq
import com.project.frankit.domain.product.product.Product
import jakarta.persistence.*

@Entity
@Table(name = "product_option")
class ProductOption(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn")
    val sn: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_sn", nullable = false, foreignKey = ForeignKey(name = "FK_PRODUCT_OPTION_PRODUCT_SN"))
    val product: Product,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "additional_price", nullable = false)
    val additionalPrice: Long,
) {
    companion object {
        fun createProductOption(product: Product, rq: ProductOptionRq): ProductOption {
            return ProductOption(
                product = product,
                name = rq.optionName,
                additionalPrice = rq.additionalPrice
            )
        }
    }
}