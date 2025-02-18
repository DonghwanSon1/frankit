package com.project.frankit.domain.product.product

import com.project.frankit.domain.admin.rqrs.ProductRq
import com.project.frankit.domain.product.enums.Status
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "product")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn")
    val sn: Long? = null,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "description", nullable = false)
    val description: String,

    @Column(name = "price", nullable = false)
    val price: Long,

    @Column(name = "shipping_fee", nullable = false)
    val shippingFee: Long,

    @Column(name = "status", nullable = false)
    val status: Int = Status.ON_SALE.value,

    @Column(name = "is_delete", nullable = false)
    val isDelete: Boolean,

    @Column(name = "registration_date", nullable = false)
    val registrationDate: LocalDateTime,

    @Column(name = "delete_date", nullable = true)
    val deleteDate: LocalDateTime? = null,

    ) {

    companion object {
        fun createProduct(rq: ProductRq): Product {
            return Product(
                name = rq.name,
                description = rq.description,
                price = rq.price,
                shippingFee = rq.shippingFee,
                status = rq.status.value,
                isDelete = false,
                registrationDate = LocalDateTime.now()
            )
        }
    }
}