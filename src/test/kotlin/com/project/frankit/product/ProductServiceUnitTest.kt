package com.project.frankit.product

import com.project.frankit.common.exception.CommonException
import com.project.frankit.common.exception.CommonExceptionCode
import com.project.frankit.common.response.SuccessMessages
import com.project.frankit.domain.admin.rqrs.ProductAndOptionRq
import com.project.frankit.domain.admin.rqrs.ProductOptionListRs
import com.project.frankit.domain.admin.rqrs.ProductOptionRq
import com.project.frankit.domain.admin.rqrs.ProductRq
import com.project.frankit.domain.product.ProductCRUD
import com.project.frankit.domain.product.ProductService
import com.project.frankit.domain.product.enums.Status
import com.project.frankit.domain.product.product.Product
import com.project.frankit.domain.product.productOption.ProductOption
import com.project.frankit.domain.product.rqrs.ProductListRs
import com.project.frankit.domain.product.selectOption.SelectOption
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
    val productListRsList: List<ProductListRs> = listOf(
      ProductListRs(sn = 1, name = "상품1", 5000, 1),
      ProductListRs(sn = 2, name = "상품2", 10000, 1),
      ProductListRs(sn = 3, name = "상품3", 20000, 2),
      ProductListRs(sn = 4, name = "상품4", 50000, 2),
    )
    val result: Page<ProductListRs> = PageImpl(productListRsList, pageable, productListRsList.size.toLong())

    every { productCRUD.searchProductList(any(), any()) } returns result

    // when
    val resultRs = productService.searchProductList(null, pageable)

    // then
    assertThat(resultRs.content[0].sn).isEqualTo(productListRsList[0].sn)
    assertThat(resultRs.content[1].name).isEqualTo(productListRsList[1].name)
    assertThat(resultRs.content[2].price).isEqualTo(productListRsList[2].price)
    assertThat(resultRs.content[3].status).isEqualTo(productListRsList[3].status)
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

  @Test
  fun `상품 옵션 목록 조회`() {
    // given
    val pageable: Pageable = PageRequest.of(0, 10)
    val productOptionListRsList: List<ProductOptionListRs> = listOf(
      ProductOptionListRs(1, "상품1", 1, 0, LocalDateTime.now()),
      ProductOptionListRs(2, "상품2", 1, 1, LocalDateTime.now()),
      ProductOptionListRs(3, "상품3", 2, 2, LocalDateTime.now()),
      ProductOptionListRs(4, "상품4", 2, 3, LocalDateTime.now()),
    )
    val result: Page<ProductOptionListRs> = PageImpl(productOptionListRsList, pageable, productOptionListRsList.size.toLong())

    every { productCRUD.searchProductOptionList(any(), any()) } returns result

    // when
    val resultRs = productService.searchProductOptionList(null, pageable)

    // then
    assertThat(resultRs.content[0].productSn).isEqualTo(productOptionListRsList[0].productSn)
    assertThat(resultRs.content[1].productName).isEqualTo(productOptionListRsList[1].productName)
    assertThat(resultRs.content[2].productStatus).isEqualTo(productOptionListRsList[2].productStatus)
    assertThat(resultRs.content[3].productOptionCount).isEqualTo(productOptionListRsList[3].productOptionCount)
  }

  @Test
  fun `상품에 속한 옵션 상세 조회`() {
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
    val resultRs = productService.searchProductOption(productSn)

    // then
    assertThat(resultRs[0].optionSn).isEqualTo(productOptionList[0].sn)
    assertThat(resultRs[1].optionName).isEqualTo(productOptionList[1].name)
    assertThat(resultRs[2].additionalPrice).isEqualTo(productOptionList[2].additionalPrice)
  }

  @Test
  fun `상품에 속한 옵션 상세 조회 - 실패 시`() {
    // given
    val productSn: Long = 1L

    every { productCRUD.findProductByProductSn(any()) } throws CommonException(CommonExceptionCode.NOT_EXIST_PRODUCT)

    // when
    val message = assertThrows<CommonException> {
      productService.searchProductOption(productSn)
    }.message

    // then
    assertThat(message).isEqualTo(CommonExceptionCode.NOT_EXIST_PRODUCT.message)
  }

  @Test
  fun `상품에 속한 상품 옵션 수정 (생성 및 수정 가능)`() {
    // given
    val productSn: Long = 1L
    val rqList: List<ProductOptionRq> = listOf(
      ProductOptionRq(null, "수정에서 추가 옵션", 4000),
      ProductOptionRq(1L, "수정한 옵션1", 1000),
      ProductOptionRq(2L, "수정한 옵션2", 2000)
    )
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
      ProductOption(sn = 2L, product = product, name = "상품 옵션2", additionalPrice = 5000, isDelete = false)
    )

    every { productCRUD.findProductByProductSn(any()) } returns product
    every { productCRUD.findProductOptionAllByProduct(any()) } returns productOptionList
    every { productCRUD.saveAllProductOptions(any()) } just Runs

    // when
    val result = productService.updateProductOption(productSn, rqList)

    // then
    assertThat(result).isEqualTo(SuccessMessages.UPDATE_PRODUCT_OPTION.message)
  }

  @Test
  fun `상품에 속한 상품 옵션 수정 - 실패 시 (상품이 존재 X)`() {
    // given
    val productSn: Long = 1L
    val rqList: List<ProductOptionRq> = listOf(
      ProductOptionRq(null, "수정에서 추가 옵션", 4000),
      ProductOptionRq(1L, "수정한 옵션1", 1000),
      ProductOptionRq(2L, "수정한 옵션2", 2000)
    )

    every { productCRUD.findProductByProductSn(any()) } throws CommonException(CommonExceptionCode.NOT_EXIST_PRODUCT)

    // when
    val message = assertThrows<CommonException> {
      productService.updateProductOption(productSn, rqList)
    }.message

    // then
    assertThat(message).isEqualTo(CommonExceptionCode.NOT_EXIST_PRODUCT.message)
  }

  @Test
  fun `상품에 속한 상품 옵션 수정 - 실패 시 (옵션이 3개 초과)`() {
    // given
    val productSn: Long = 1L
    val rqList: List<ProductOptionRq> = listOf(
      ProductOptionRq(null, "수정에서 추가 옵션1", 4000),
      ProductOptionRq(null, "수정에서 추가 옵션2", 1000),
      ProductOptionRq(2L, "수정한 옵션2", 2000)
    )
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
      ProductOption(sn = 2L, product = product, name = "상품 옵션2", additionalPrice = 5000, isDelete = false)
    )

    every { productCRUD.findProductByProductSn(any()) } returns product
    every { productCRUD.findProductOptionAllByProduct(any()) } returns productOptionList

    // when
    val message = assertThrows<CommonException> {
      productService.updateProductOption(productSn, rqList)
    }.message

    // then
    assertThat(message).isEqualTo(CommonExceptionCode.PRODUCT_OPTION_LIMIT_EXCEEDED.message)
  }

  @Test
  fun `상품에 속한 상품 옵션 개별 삭제 (Soft Delete)`() {
    // given
    val productOptionSn: Long = 1L
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
    val productOption: ProductOption = ProductOption(sn = 1L, product = product, name = "상품 옵션1", additionalPrice = 1000, isDelete = false)

    every { productCRUD.findProductOptionByProductOptionSn(any()) } returns productOption
    every { productCRUD.saveProductOption(any()) } just Runs

    // when
    val result = productService.deleteProductOption(productOptionSn)

    // then
    assertThat(result).isEqualTo(SuccessMessages.DELETE_PRODUCT_OPTION.message)
    assertThat(productOption.isDelete).isTrue()
  }

  @Test
  fun `상품에 속한 상품 옵션 개별 삭제 (Soft Delete) - 실패 시`() {
    // given
    val productOptionSn: Long = 1L

    every { productCRUD.findProductOptionByProductOptionSn(any()) } throws CommonException(CommonExceptionCode.NOT_EXIST_PRODUCT_OPTION)

    // when
    val message = assertThrows<CommonException> {
      productService.deleteProductOption(productOptionSn)
    }.message

    // then
    assertThat(message).isEqualTo(CommonExceptionCode.NOT_EXIST_PRODUCT_OPTION.message)
  }

  @Test
  fun `선택 옵션 리스트 조회`() {
    // given
    val selectOptionList: List<SelectOption> = listOf(
      SelectOption(1, "브랜드 로고 추가"),
      SelectOption(2, "SMALL SIZE"),
      SelectOption(3, "MEDIUM SIZE"),
      SelectOption(4, "LARGE SIZE"),
      SelectOption(5, "EXTRA-LARGE SIZE")
    )
    every { productCRUD.findSelectOptionAll() } returns selectOptionList

    // when
    val result = productService.searchSelectOptionList()

    // then
    assertThat(result.size).isEqualTo(selectOptionList.size)
    assertThat(result[0].name).isEqualTo(selectOptionList[0].name)
    assertThat(result[4].name).isEqualTo(selectOptionList[4].name)
  }

}