package com.project.frankit.domain.product.product

import com.project.frankit.domain.admin.rqrs.ProductOptionListRs
import com.project.frankit.domain.product.rqrs.ProductListRs
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable


interface ProductCustomRepository {

  fun searchProductList(productName: String?, pageable: Pageable): Page<ProductListRs>
  fun searchProductOptionList(productName: String?, pageable: Pageable): Page<ProductOptionListRs>
}