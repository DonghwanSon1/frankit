package com.project.frankit.domain.product.productOption

import com.project.frankit.domain.product.product.Product
import org.springframework.data.jpa.repository.JpaRepository

interface ProductOptionRepository: JpaRepository<ProductOption, Long> {

  fun findAllByProductAndIsDelete(product: Product, isDelete: Boolean): List<ProductOption>
  fun findBySnAndIsDelete(sn: Long, isDelete: Boolean): ProductOption?
}