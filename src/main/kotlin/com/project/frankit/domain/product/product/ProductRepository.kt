package com.project.frankit.domain.product.product

import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository: JpaRepository<Product, Long>, ProductCustomRepository {

  fun findBySnAndIsDelete(sn: Long, isDelete: Boolean): Product?
}