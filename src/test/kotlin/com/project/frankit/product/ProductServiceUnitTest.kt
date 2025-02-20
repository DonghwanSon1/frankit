package com.project.frankit.product

import com.project.frankit.common.exception.CommonException
import com.project.frankit.common.exception.CommonExceptionCode
import com.project.frankit.common.response.SuccessMessages
import com.project.frankit.domain.admin.rqrs.ProductAndOptionRq
import com.project.frankit.domain.admin.rqrs.ProductOptionRq
import com.project.frankit.domain.admin.rqrs.ProductRq
import com.project.frankit.domain.product.ProductCRUD
import com.project.frankit.domain.product.ProductService
import com.project.frankit.domain.product.enums.Status
import com.project.frankit.domain.product.product.Product
import com.project.frankit.domain.product.productOption.ProductOption
import com.project.frankit.domain.product.rqrs.ProductListRs
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

class ProductServiceUnitTest {

  private val productCRUD: ProductCRUD = mockk<ProductCRUD>()
  private val productService: ProductService = ProductService(productCRUD)

  @Test
  fun `상품 등록 (상품 옵션 같이 등록)`() {
    // given
    val rq = ProductAndOptionRq(
      name = "상품",
      description = "상품 설명",
      price = 10000,
      shippingFee = 5000,
      status = Status.ON_SALE,
      productOption = listOf(
        ProductOptionRq(optionName = "상품 옵션1", additionalPrice = 2000),
        ProductOptionRq(optionName = "상품 옵션2", additionalPrice = 1000)
      )
    )
    every { productCRUD.saveProductAndProductOptions(any(), any()) } just Runs

    // when
    val result = productService.saveProductAndProductOption(rq)

    // then
    assertThat(result).isEqualTo(SuccessMessages.CREATE_PRODUCT.message)
  }

  @Test
  fun `상품 목록 조회 (페이징 처리 및 상품 검색)`() {
    // given
    val pageable: Pageable = PageRequest.of(0, 10)
    val results: List<ProductListRs> = listOf(
      ProductListRs(sn = 1, name = "상품1", 5000, 1),
      ProductListRs(sn = 2, name = "상품2", 10000, 1),
      ProductListRs(sn = 3, name = "상품3", 20000, 2),
      ProductListRs(sn = 4, name = "상품4", 50000, 2),
    )
    val result: Page<ProductListRs> = PageImpl(results, pageable, results.size.toLong())

    every { productCRUD.searchProductList(any(), any()) } returns result

    // when
    val resultRs = productService.searchProductList(null, pageable)

    // then
    assertThat(resultRs.content[0].sn).isEqualTo(results[0].sn)
    assertThat(resultRs.content[1].name).isEqualTo(results[1].name)
    assertThat(resultRs.content[2].price).isEqualTo(results[2].price)
    assertThat(resultRs.content[3].status).isEqualTo(results[3].status)
  }

  @Test
  fun `상품 상세 조회 (상품 옵션 포함)`() {
    // given
    val productSn: Long = 1L
    val product: Product = Product(
      sn = 1L,
      name = "상품",
      description = "상품 설명",
      price = 10000,
      shippingFee = 2000,
      status = 1,
      isDelete = false,
      registrationDate = LocalDateTime.now(),
      null)
    val productOptionList: List<ProductOption> = listOf(
      ProductOption(sn = 1L, product = product, name = "상품 옵션1", additionalPrice = 1000, isDelete = false),
      ProductOption(sn = 2L, product = product, name = "상품 옵션2", additionalPrice = 5000, isDelete = false),
      ProductOption(sn = 3L, product = product, name = "상품 옵션3", additionalPrice = 2000, isDelete = false)
    )

    every { productCRUD.findProductByProductSn(any()) } returns product
    every { productCRUD.findProductOptionAllByProduct(any()) } returns productOptionList

    // when
    val resultRs = productService.searchDetailProduct(productSn)

    // then
    assertThat(resultRs.name).isEqualTo(product.name)
    assertThat(resultRs.description).isEqualTo(product.description)
    assertThat(resultRs.productOptionList[0].optionName).isEqualTo(productOptionList[0].name)
    assertThat(resultRs.productOptionList[2].additionalPrice).isEqualTo(productOptionList[2].additionalPrice)
  }

  @Test
  fun `상품 상세 조회 (상품 옵션 포함) - 실패 시`() {
    // given
    val productSn: Long = 1L
    every { productCRUD.findProductByProductSn(any()) } throws CommonException(CommonExceptionCode.NOT_EXIST_PRODUCT)

    // when
    val message = assertThrows<CommonException> {
      productService.searchDetailProduct(productSn)
    }.message

    // then
    assertThat(message).isEqualTo(CommonExceptionCode.NOT_EXIST_PRODUCT.message)
  }

  @Test
  fun `상품 수정 (단건 수정 및 상품만 수정 - 상품 옵션 X)`() {
    // given
    val productSn: Long = 1L
    val rq: ProductRq = ProductRq(name = "수정 상품", price = 10000)

    val product: Product = Product(
      sn = 1L,
      name = "상품",
      description = "상품 설명",
      price = 5000,
      shippingFee = 2000,
      status = 1,
      isDelete = false,
      registrationDate = LocalDateTime.now(),
      null)

    every { productCRUD.findProductByProductSn(any()) } returns product
    every { productCRUD.saveProduct(any()) } just Runs

    // when
    val result = productService.updateProduct(productSn, rq)

    // then
    assertThat(result).isEqualTo(SuccessMessages.UPDATE_PRODUCT.message)
  }

  @Test
  fun `상품 수정 (단건 수정 및 상품만 수정 - 상품 옵션 X) - 실패 시`() {
    // given
    val productSn: Long = 1L
    val rq: ProductRq = ProductRq(name = "수정 상품", price = 10000)

    every { productCRUD.findProductByProductSn(any()) } throws CommonException(CommonExceptionCode.NOT_EXIST_PRODUCT)

    // when
    val message = assertThrows<CommonException> {
      productService.updateProduct(productSn, rq)
    }.message

    // then
    assertThat(message).isEqualTo(CommonExceptionCode.NOT_EXIST_PRODUCT.message)
  }

  @Test
  fun `상품 삭제 (상품 삭제 시 상품 옵션도 같이 삭제 - Soft Delete)`() {
    // given
    val productSn: Long = 1L
    val product: Product = Product(
      sn = 1L,
      name = "상품",
      description = "상품 설명",
      price = 5000,
      shippingFee = 2000,
      status = 1,
      isDelete = false,
      registrationDate = LocalDateTime.now(),
      null)
    val productOptionList: List<ProductOption> = listOf(
      ProductOption(sn = 1L, product = product, name = "상품 옵션1", additionalPrice = 1000, isDelete = false),
      ProductOption(sn = 2L, product = product, name = "상품 옵션2", additionalPrice = 5000, isDelete = false),
      ProductOption(sn = 3L, product = product, name = "상품 옵션3", additionalPrice = 2000, isDelete = false)
    )

    every { productCRUD.findProductByProductSn(any()) } returns product
    every { productCRUD.findProductOptionAllByProduct(any()) } returns productOptionList
    every { productCRUD.saveAllProductOptions(any()) } just Runs
    every { productCRUD.saveProduct(any()) } just Runs

    // when
    val result = productService.deleteProduct(productSn)

    // then
    assertThat(result).isEqualTo(SuccessMessages.DELETE_PRODUCT.message)
    assertThat(product.isDelete).isTrue()
    assertThat(productOptionList[2].isDelete).isTrue()
  }

  @Test
  fun `상품 삭제 (상품 삭제 시 상품 옵션도 같이 삭제 - Soft Delete) - 실패 시`() {
    // given
    val productSn: Long = 1L

    every { productCRUD.findProductByProductSn(any()) } throws CommonException(CommonExceptionCode.NOT_EXIST_PRODUCT)

    // when
    val message = assertThrows<CommonException> {
      productService.deleteProduct(productSn)
    }.message

    // then
    assertThat(message).isEqualTo(CommonExceptionCode.NOT_EXIST_PRODUCT.message)
  }



}