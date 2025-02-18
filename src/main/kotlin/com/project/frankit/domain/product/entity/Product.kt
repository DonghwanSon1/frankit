package com.project.frankit.domain.product.entity

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

    @Column(name = "description", nullable = false)
    val description: String,

    @Column(name = "price", nullable = false)
    val price: Long,

    @Column(name = "shipping_fee", nullable = false)
    val shippingFee: Long,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    val status: Status,

    @Column(name = "is_delete", nullable = false)
    val isDelete: Boolean,

    @Column(name = "registration_date", nullable = false)
    val registrationDate: LocalDateTime,

    @Column(name = "delete_date", nullable = false)
    val deleteDate: LocalDateTime,

) {
}