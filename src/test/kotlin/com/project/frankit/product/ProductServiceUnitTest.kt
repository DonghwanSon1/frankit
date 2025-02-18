package com.project.frankit.product

import com.project.frankit.domain.product.ProductCRUD
import com.project.frankit.domain.product.ProductService
import io.mockk.mockk
import org.junit.jupiter.api.Test

class ProductServiceUnitTest {

  private val productCRUD: ProductCRUD = mockk<ProductCRUD>()
  private val productService: ProductService = ProductService(productCRUD)

  @Test
  fun `상품 등록 및 상품 옵션 등록 성공`() {

  }

  @Test
  fun `상품 등록 및 상품 옵션 등록 실패`() {

  }

  @Test
  fun `상품 수정 성공`() {

  }

  @Test
  fun `상품 수정 실패`() {

  }



}