package com.project.frankit.domain.product.productOption

import com.project.frankit.domain.product.product.Product
import org.springframework.data.jpa.repository.JpaRepository

interface ProductOptionRepository: JpaRepository<ProductOption, Long> {

  fun findAllByProduct(product: Product): List<ProductOption>
}