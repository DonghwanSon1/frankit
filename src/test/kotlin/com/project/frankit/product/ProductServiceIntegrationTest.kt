package com.project.frankit.product

import com.project.frankit.common.exception.CommonException
import com.project.frankit.common.exception.CommonExceptionCode
import com.project.frankit.common.response.SuccessMessages
import com.project.frankit.domain.admin.rqrs.ProductAndOptionRq
import com.project.frankit.domain.admin.rqrs.ProductOptionRq
import com.project.frankit.domain.admin.rqrs.ProductRq
import com.project.frankit.domain.product.ProductService
import com.project.frankit.domain.product.enums.Status
import com.project.frankit.domain.product.product.Product
import com.project.frankit.domain.product.product.ProductRepository
import com.project.frankit.domain.product.productOption.ProductOption
import com.project.frankit.domain.product.productOption.ProductOptionRepository
import com.project.frankit.domain.product.selectOption.SelectOption
import com.project.frankit.domain.product.selectOption.SelectOptionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("test")
@SpringBootTest
class ProductServiceIntegrationTest @Autowired constructor(
  private val productService: ProductService,
  private val productRepository: ProductRepository,
  private val productOptionRepository: ProductOptionRepository,
  private val selectOptionRepository: SelectOptionRepository,
) {

  @AfterEach
  fun clean() {
    productOptionRepository.deleteAll()
    productRepository.deleteAll()
    selectOptionRepository.deleteAll()
  }

  @Test
  fun `상품 및 옵션 등록`() {
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

    // when
    val result = productService.saveProductAndProductOption(rq)

    // then
    val product = productRepository.findAll()
    val productOptions = productOptionRepository.findAll()

    assertThat(result).isEqualTo(SuccessMessages.CREATE_PRODUCT.message)
    assertThat(product).hasSize(1)
    assertThat(product[0].name).isEqualTo(rq.name)
    assertThat(productOptions).hasSize(2)
    assertThat(productOptions[0].additionalPrice).isEqualTo(rq.productOption[0].additionalPrice)
  }

  @Test
  fun `상품만 등록`() {
    // given
    val rq = ProductAndOptionRq(
      name = "상품",
      description = "상품 설명",
      price = 10000,
      shippingFee = 5000,
      status = Status.ON_SALE,
      productOption = listOf()
    )

    // when
    val result = productService.saveProductAndProductOption(rq)

    // then
    val product = productRepository.findAll()
    val productOptions = productOptionRepository.findAll()

    assertThat(result).isEqualTo(SuccessMessages.CREATE_PRODUCT.message)
    assertThat(product).hasSize(1)
    assertThat(product[0].name).isEqualTo(rq.name)
    assertThat(productOptions).hasSize(0)
  }

  @Test
  fun `상품 목록 조회 (페이징 처리) - 검색 X`() {
    // given
    val saveProduct: List<Product> = productRepository.saveAll(listOf(
      Product(name = "상품1", description = "상품 설명1", price = 10000, shippingFee = 1000, status = 1, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null),
      Product(name = "상품2", description = "상품 설명2", price = 20000, shippingFee = 3000, status = 1, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null),
      Product(name = "상품3", description = "상품 설명3", price = 5000, shippingFee = 100, status = 2, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null),
      Product(name = "상품4", description = "상품 설명4", price = 40000, shippingFee = 5000, status = 1, isDelete = true, registrationDate = LocalDateTime.now(), deleteDate = LocalDateTime.now())
      )
    )
    val pageable: Pageable = PageRequest.of(0, 10)

    // when
    val result = productService.searchProductList(null, pageable)

    // then
    assertThat(result.totalPages).isEqualTo(1)
    assertThat(result.totalElements).isEqualTo(3)
    assertThat(result.content[0].status).isEqualTo(Status.fromValue(saveProduct[2].status).desc)
    assertThat(result.content[1].price).isEqualTo(saveProduct[1].price)
    assertThat(result.content[2].name).isEqualTo(saveProduct[0].name)
  }

  @Test
  fun `상품 목록 조회 (페이징 처리) - 검색 O`() {
    // given
    val productName: String = "다"
    val saveProduct: List<Product> = productRepository.saveAll(listOf(
      Product(name = "가나 상품", description = "상품 설명1", price = 10000, shippingFee = 1000, status = 1, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null),
      Product(name = "다라 상품", description = "상품 설명2", price = 20000, shippingFee = 3000, status = 1, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null),
      Product(name = "라다 상품", description = "상품 설명3", price = 5000, shippingFee = 100, status = 2, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null),
      Product(name = "마바 상품", description = "상품 설명4", price = 40000, shippingFee = 5000, status = 1, isDelete = true, registrationDate = LocalDateTime.now(), deleteDate = LocalDateTime.now())
    ))
    val pageable: Pageable = PageRequest.of(0, 10)

    // when
    val result = productService.searchProductList(productName, pageable)

    // then
    assertThat(result.totalPages).isEqualTo(1)
    assertThat(result.totalElements).isEqualTo(2)
    assertThat(result.content[0].status).isEqualTo(Status.fromValue(saveProduct[2].status).desc)
    assertThat(result.content[1].name).isEqualTo(saveProduct[1].name)
  }

  @Test
  fun `상품 상세 조회`() {
    // given
    val saveProduct: Product = productRepository.save(
      Product(name = "가나 상품", description = "상품 설명1", price = 10000, shippingFee = 1000, status = 1, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null)
    )
    val saveProductOptions: List<ProductOption> = productOptionRepository.saveAll(listOf(
      ProductOption(product = saveProduct, name = "상품 옵션1", additionalPrice = 1000, isDelete = false),
      ProductOption(product = saveProduct, name = "상품 옵션2", additionalPrice = 2000, isDelete = false),
      ProductOption(product = saveProduct, name = "상품 옵션3", additionalPrice = 3000, isDelete = false),
    ))
    val productSn: Long = saveProduct.sn!!

    // when
    val result = productService.searchDetailProduct(productSn)

    // then
    assertThat(result.name).isEqualTo(saveProduct.name)
    assertThat(result.shippingFee).isEqualTo(saveProduct.shippingFee)
    assertThat(result.productOptionList).hasSize(3)
    assertThat(result.productOptionList[0].optionName).isEqualTo(saveProductOptions[0].name)
  }

  @Test
  fun `상품 상세 조회 - 실패 시`() {
    // given
    val saveProductList: List<Product> = productRepository.saveAll(listOf(
      Product(name = "가나 상품", description = "상품 설명1", price = 10000, shippingFee = 1000, status = 1, isDelete = true, registrationDate = LocalDateTime.now(), deleteDate = LocalDateTime.now()),
      Product(name = "다라 상품", description = "상품 설명2", price = 20000, shippingFee = 3000, status = 1, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null),
    ))
    val productSn: Long = saveProductList.first().sn!!

    // when & then
    val message = assertThrows<CommonException> {
      productService.searchDetailProduct(productSn)
    }.message

    assertThat(message).isEqualTo(CommonExceptionCode.NOT_EXIST_PRODUCT.message)
  }

  @Test
  fun `상품 수정 - 상품 단건`() {
    // given
    val rq: ProductRq = ProductRq("상품 수정", "수정 설명", 2000, 0)
    val saveProduct: Product = productRepository.save(
      Product(name = "가나 상품", description = "상품 설명1", price = 10000, shippingFee = 1000, status = 1, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null)
    )
    val productSn: Long = saveProduct.sn!!

    // when
    val result = productService.updateProduct(productSn, rq)

    // then
    val product = productRepository.findAll()
    assertThat(result).isEqualTo(SuccessMessages.UPDATE_PRODUCT.message)
    assertThat(product[0].name).isEqualTo(rq.name)
    assertThat(product[0].description).isEqualTo(rq.description)
    assertThat(product[0].status).isEqualTo(Status.ON_SALE.value)
  }

  @Test
  fun `상품 수정 - 실패 시`() {
    // given
    val productSn: Long = 1L
    val rq: ProductRq = ProductRq("상품 수정", "수정 설명", 2000, 0, Status.SOLD_OUT)

    // when & then
    val message = assertThrows<CommonException> {
      productService.updateProduct(productSn, rq)
    }.message

    assertThat(message).isEqualTo(CommonExceptionCode.NOT_EXIST_PRODUCT.message)
  }

  @Test
  fun `상품 삭제 - 상품에 속한 옵션도 같이 삭제 (Soft Delete)`() {
    // given
    val saveProduct = productRepository.save(
      Product(name = "가나 상품", description = "상품 설명1", price = 10000, shippingFee = 1000, status = 1, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null)
    )
    productOptionRepository.save(
      ProductOption(product = saveProduct, name = "상품 옵션1", additionalPrice = 1000, isDelete = false)
    )
    val productSn: Long = saveProduct.sn!!

    // when
    val result = productService.deleteProduct(productSn)

    // then
    val product = productRepository.findAll()
    val productOptions = productOptionRepository.findAll()
    assertThat(result).isEqualTo(SuccessMessages.DELETE_PRODUCT.message)
    assertThat(product[0].isDelete).isTrue()
    assertThat(product[0].deleteDate).isNotNull()
    assertThat(productOptions[0].isDelete).isTrue()
  }

  @Test
  fun `상품 삭제 - 실패 시`() {
    // given
    val productSn: Long = 1L

    // when & then
    val message = assertThrows<CommonException> {
      productService.deleteProduct(productSn)
    }.message

    assertThat(message).isEqualTo(CommonExceptionCode.NOT_EXIST_PRODUCT.message)
  }

  @Test
  fun `상품 옵션 목록 조회 - 검색 X`() {
    // given
    val saveProduct: List<Product> = productRepository.saveAll(listOf(
      Product(name = "상품1", description = "상품 설명1", price = 10000, shippingFee = 1000, status = 1, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null),
      Product(name = "상품2", description = "상품 설명2", price = 20000, shippingFee = 3000, status = 1, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null),
      Product(name = "상품3", description = "상품 설명3", price = 5000, shippingFee = 100, status = 2, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null),
      Product(name = "상품4", description = "상품 설명4", price = 40000, shippingFee = 5000, status = 1, isDelete = true, registrationDate = LocalDateTime.now(), deleteDate = LocalDateTime.now())
    ))
    productOptionRepository.saveAll(listOf(
      ProductOption(product = saveProduct[0], name = "상품 옵션1", additionalPrice = 1000, isDelete = true),
      ProductOption(product = saveProduct[0], name = "상품 옵션2", additionalPrice = 2000, isDelete = false),
      ProductOption(product = saveProduct[0], name = "상품 옵션3", additionalPrice = 3000, isDelete = false),

      ProductOption(product = saveProduct[1], name = "상품 옵션1", additionalPrice = 1000, isDelete = false),
      ProductOption(product = saveProduct[1], name = "상품 옵션2", additionalPrice = 2000, isDelete = false),

      ProductOption(product = saveProduct[2], name = "상품 옵션3", additionalPrice = 3000, isDelete = true)
    ))
    val pageable: Pageable = PageRequest.of(0, 10)

    // when
    val result = productService.searchProductOptionList(null, pageable)

    // then
    assertThat(result.totalPages).isEqualTo(1)
    assertThat(result.totalElements).isEqualTo(3)
    assertThat(result.content[0].productOptionCount).isEqualTo(0)
    assertThat(result.content[1].productOptionCount).isEqualTo(2)
    assertThat(result.content[2].productOptionCount).isEqualTo(2)
  }

  @Test
  fun `상품 옵션 목록 조회 - 검색 O`() {
    // given
    val productName: String = "라"
    val saveProduct: List<Product> = productRepository.saveAll(listOf(
      Product(name = "가나 상품", description = "상품 설명1", price = 10000, shippingFee = 1000, status = 1, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null),
      Product(name = "다라 상품", description = "상품 설명2", price = 20000, shippingFee = 3000, status = 1, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null),
      Product(name = "라다 상품", description = "상품 설명3", price = 5000, shippingFee = 100, status = 2, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null),
      Product(name = "마바 상품", description = "상품 설명4", price = 40000, shippingFee = 5000, status = 1, isDelete = true, registrationDate = LocalDateTime.now(), deleteDate = LocalDateTime.now())
    ))
    productOptionRepository.saveAll(listOf(
      ProductOption(product = saveProduct[0], name = "상품 옵션1", additionalPrice = 1000, isDelete = true),
      ProductOption(product = saveProduct[0], name = "상품 옵션2", additionalPrice = 2000, isDelete = false),
      ProductOption(product = saveProduct[0], name = "상품 옵션3", additionalPrice = 3000, isDelete = false),

      ProductOption(product = saveProduct[1], name = "상품 옵션1", additionalPrice = 1000, isDelete = false),
      ProductOption(product = saveProduct[1], name = "상품 옵션2", additionalPrice = 2000, isDelete = false),

      ProductOption(product = saveProduct[2], name = "상품 옵션3", additionalPrice = 3000, isDelete = true)
    ))
    val pageable: Pageable = PageRequest.of(0, 10)

    // when
    val result = productService.searchProductOptionList(productName, pageable)

    // then
    assertThat(result.totalPages).isEqualTo(1)
    assertThat(result.totalElements).isEqualTo(2)
    assertThat(result.content[0].productOptionCount).isEqualTo(0)
    assertThat(result.content[1].productOptionCount).isEqualTo(2)
  }

  @Test
  fun `상품에 속한 옵션 상세 조회`() {
    // given
    val saveProduct: Product = productRepository.save(
      Product(name = "상품1", description = "상품 설명1", price = 10000, shippingFee = 1000, status = 1, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null)
    )
    val saveProductOptions: List<ProductOption> = productOptionRepository.saveAll(listOf(
      ProductOption(product = saveProduct, name = "상품 옵션1", additionalPrice = 1000, isDelete = true),
      ProductOption(product = saveProduct, name = "상품 옵션2", additionalPrice = 2000, isDelete = true),
      ProductOption(product = saveProduct, name = "상품 옵션3", additionalPrice = 3000, isDelete = false),
    ))
    val productSn: Long = saveProduct.sn!!

    // when
    val result = productService.searchProductOption(productSn)

    // then
    assertThat(result).hasSize(1)
    assertThat(result[0].optionName).isEqualTo(saveProductOptions[2].name)
    assertThat(result[0].additionalPrice).isEqualTo(saveProductOptions[2].additionalPrice)
  }

  @Test
  fun `상품에 속한 옵션 상세 조회 - 실패 시`() {
    // given
    val productSn: Long = 1L

    // when & then
    val message = assertThrows<CommonException> {
      productService.searchProductOption(productSn)
    }.message

    assertThat(message).isEqualTo(CommonExceptionCode.NOT_EXIST_PRODUCT.message)
  }

  @Test
  fun `상품에 속한 상품 옵션 수정 - 생성 및 수정`() {
    // given
    val saveProduct: Product = productRepository.save(
      Product(name = "상품1", description = "상품 설명1", price = 10000, shippingFee = 1000, status = 1, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null)
    )
    val saveProductOptions: List<ProductOption> = productOptionRepository.saveAll(listOf(
      ProductOption(product = saveProduct, name = "상품 옵션1", additionalPrice = 1000, isDelete = true),
      ProductOption(product = saveProduct, name = "상품 옵션2", additionalPrice = 2000, isDelete = true),
      ProductOption(product = saveProduct, name = "상품 옵션3", additionalPrice = 3000, isDelete = false),
    ))
    val productSn: Long = saveProduct.sn!!
    val rqList: List<ProductOptionRq> = listOf(
      ProductOptionRq(null, "수정에서 생성한 옵션1", 1000),
      ProductOptionRq(null, "수정에서 생성한 옵션2", 2000),
      ProductOptionRq(saveProductOptions[2].sn, "수정한 옵션3", 1000),
    )

    // when
    val result = productService.updateProductOption(productSn, rqList)

    // then
    val productOptions: List<ProductOption> = productOptionRepository.findAll().filter { !it.isDelete }
    assertThat(result).isEqualTo(SuccessMessages.UPDATE_PRODUCT_OPTION.message)
    assertThat(productOptions).hasSize(3)
    assertThat(productOptions[0].sn).isEqualTo(saveProductOptions[2].sn)
    assertThat(productOptions[1].name).isEqualTo(rqList[0].optionName)
    assertThat(productOptions[2].additionalPrice).isEqualTo(rqList[1].additionalPrice)
  }

  @Test
  fun `상품에 속한 상품 옵션 수정 - 실패 시(상품 X)`() {
    // given
    val productSn: Long = 1L
    val rqList: List<ProductOptionRq> = listOf(
      ProductOptionRq(null, "수정에서 생성한 옵션1", 1000),
      ProductOptionRq(null, "수정에서 생성한 옵션2", 2000),
      ProductOptionRq(null, "수정에서 생성한 옵션3", 1000),
    )

    // when & then
    val message = assertThrows<CommonException> {
      productService.updateProductOption(productSn, rqList)
    }.message

    assertThat(message).isEqualTo(CommonExceptionCode.NOT_EXIST_PRODUCT.message)
  }

  @Test
  fun `상품에 속한 상품 옵션 수정 - 실패 시(옵션 최대 개수 초과 시)`() {
    // given
    val saveProduct: Product = productRepository.save(
      Product(name = "상품1", description = "상품 설명1", price = 10000, shippingFee = 1000, status = 1, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null)
    )
    productOptionRepository.saveAll(listOf(
      ProductOption(product = saveProduct, name = "상품 옵션1", additionalPrice = 1000, isDelete = true),
      ProductOption(product = saveProduct, name = "상품 옵션2", additionalPrice = 2000, isDelete = true),
      ProductOption(product = saveProduct, name = "상품 옵션3", additionalPrice = 3000, isDelete = false),
    ))
    val rqList: List<ProductOptionRq> = listOf(
      ProductOptionRq(null, "수정에서 생성한 옵션1", 1000),
      ProductOptionRq(null, "수정에서 생성한 옵션2", 2000),
      ProductOptionRq(null, "수정에서 생성한 옵션3", 1000),
    )
    val productSn: Long = saveProduct.sn!!

    // when & then
    val message = assertThrows<CommonException> {
      productService.updateProductOption(productSn, rqList)
    }.message

    assertThat(message).isEqualTo(CommonExceptionCode.PRODUCT_OPTION_LIMIT_EXCEEDED.message)
  }

  @Test
  fun `상품에 속한 상품 옵션 개별 삭제 (Soft Delete)`() {
    // given
    val saveProduct = productRepository.save(
      Product(name = "가나 상품", description = "상품 설명1", price = 10000, shippingFee = 1000, status = 1, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null)
    )
    val saveProductOption = productOptionRepository.save(
      ProductOption(product = saveProduct, name = "상품 옵션1", additionalPrice = 1000, isDelete = false)
    )
    val productOptionSn: Long = saveProductOption.sn!!

    // when
    val result = productService.deleteProductOption(productOptionSn)

    // then
    val productOptions = productOptionRepository.findAll()
    assertThat(result).isEqualTo(SuccessMessages.DELETE_PRODUCT_OPTION.message)
    assertThat(productOptions[0].isDelete).isTrue()
  }

  @Test
  fun `상품에 속한 상품 옵션 개별 삭제 - 실패 시`() {
    // given
    val productOptionSn: Long = 1L

    // when & then
    val message = assertThrows<CommonException> {
      productService.deleteProductOption(productOptionSn)
    }.message

    assertThat(message).isEqualTo(CommonExceptionCode.NOT_EXIST_PRODUCT_OPTION.message)
  }

  @Test
  fun `선택 옵션 리스트 조회`() {
    // given
    selectOptionRepository.saveAll(listOf(
      SelectOption(null, "브랜드 로고 추가"),
      SelectOption(null, "SMALL SIZE"),
      SelectOption(null, "MEDIUM SIZE"),
      SelectOption(null, "LARGE SIZE"),
      SelectOption(null, "EXTRA-LARGE SIZE")
    ))

    // when
    val result = productService.searchSelectOptionList()

    // then
    assertThat(result).hasSize(5)
  }
}