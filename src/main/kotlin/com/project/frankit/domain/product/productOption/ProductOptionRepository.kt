package com.project.frankit.domain.product.productOption

import org.springframework.data.jpa.repository.JpaRepository

interface ProductOptionRepository: JpaRepository<ProductOption, Long> {

  fun findAllByProductSn(productSn: Long): List<ProductOption>?
}