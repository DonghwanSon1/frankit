package com.project.frankit.domain.product.selectOption

import jakarta.persistence.*

@Entity
@Table(name = "select_option")
class SelectOption(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn")
    val sn: Long? = null,

    @Column(name = "name", nullable = false)
    val name: String,
) {
}