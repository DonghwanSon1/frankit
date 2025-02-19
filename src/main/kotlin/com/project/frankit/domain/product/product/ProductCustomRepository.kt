package com.project.frankit.domain.product.product

import com.project.frankit.domain.product.rqrs.ProductListRs
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable


interface ProductCustomRepository {

  fun searchProduct(productName: String?, pageable: Pageable): Page<ProductListRs>
}