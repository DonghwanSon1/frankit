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

  /**
   * 상품 및 옵션 등록
   * - given / when / then
   *    1. 상품 및 옵션 등록 할 Rq를 생성한다.
   *    2. 상품 등록 메서드를 통해 상품 및 옵션을 등록 요청한다.
   *    3. 상품 및 옵션 테이블을 각각 조회한 후 Rq와 비교한다.
   *
   * - 테스트 확인
   *    1. 클라이언트에게 응답 주는 메시지가 동일한지 확인.
   *    2. 상품 저장된게 1개 인지 확인.
   *    3. 상품 저장된 이름이 Rq 에서 요청한 상품 이름인지 확인.
   *    4. 상품 옵션 저장된게 2개 인지 확인.
   *    5. 상품 옵션 저장된 추가 금액이 Rq 에서 요청한 상품 옵션 추가 금액인지 확인.
   */
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

  /**
   * 상품만 등록
   * - given / when / then
   *    1. 상품만 등록 할 Rq를 생성한다. - 상품 옵션은 빈배열
   *    2. 상품 등록 메서드를 통해 상품을 등록 요청한다.
   *    3. 상품 및 옵션 테이블을 각각 조회한 후 Rq와 비교한다.
   *
   * - 테스트 확인
   *    1. 클라이언트에게 응답 주는 메시지가 동일한지 확인.
   *    2. 상품 저장된게 1개 인지 확인.
   *    3. 상품 저장된 이름이 Rq 에서 요청한 상품 이름인지 확인.
   *    4. 상품 옵션이 저장된게 없는지 확인.
   */
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

  /**
   * 상품 목록 조회 (페이징 처리) - 검색 X
   * - given / when / then
   *    1. 다수의 상품들을 먼저 저장한다.
   *    2. 페이징 처리를 위한 pageable 을 생성한다. - 기본값
   *    3. 상품 목록 조회 메서드를 통해 상품 목록 조회 요청한다.
   *    4. 요청한 Rq 와 조회 후 결과값이랑 비교한다.
   *
   * - 테스트 확인
   *    1. pageable 을 (0,10)으로 요청했기에 총 페이지 수가 1인지 확인.
   *    2. 총 개수가 저장 시 4개 중 1개는 isDelete = true 이기에 3개인지 확인.
   *    3. result 는 최신순으로 제공되기에 첫번째의 데이터는 마지막으로 저장한 값의 status 의 값과 동일한지 확인.
   *        - 저장한 [3]은 삭제된 값으로 넣었기에 [2]가 마지막 값
   *    4. 두번째 데이터와 [1] 데이터와 가격의 값이 동일한지 확인.
   *    5. 세번째 데이터와 [0] 데이터와 이름이 동일한지 확인.
   */
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

  /**
   * 상품 목록 조회 (페이징 처리) - 검색 O
   * - given / when / then
   *    1. 다수의 상품들을 먼저 저장한다.
   *    2. 검색할 상품 이름 값과 페이징 처리를 위한 pageable 을 생성한다. - 기본값
   *    3. 상품 목록 조회 메서드를 통해 상품 목록 조회 요청한다.
   *    4. 요청한 Rq 와 조회 후 결과값이랑 비교한다.
   *
   * - 테스트 확인
   *    1. pageable 을 (0,10)으로 요청했기에 총 페이지 수가 1인지 확인.
   *    2. 조회된 총 개수가 상품 이름이 일치하는 데이터만 조회 결과로 주기 때문에 총 개수 2개 인지 확인.
   *    3. 각각의 조회 결과에서 각각의 Rq의 상태값 및 이름이 동일한지 확인.
   */
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

  /**
   * 상품 상세 조회
   * - given / when / then
   *    1. 하나의 상품을 먼저 저장한다.
   *    2. 상품에 속한 다수의 옵션들을 저장한다.
   *    3. 상품 상세 조회 메서드를 통해 상품 상세 조회 요청한다.
   *    4. 저장된 데이터와 조회 후 결과값이랑 비교한다.
   *
   * - 테스트 확인
   *    1. 조회된 데이터의 상품 이름이 저장된 상품 이름과 동일한지 확인.
   *    2. 조회된 데이터의 상품 배송료와 저장된 상품의 배송료가 동일한지 확인.
   *    3. 조회된 데이터의 상품 옵션 개수가 저장한 상품 옵션 개수와 동일한지 확인.
   *    4. 조회된 데이터의 상품 옵션 이름이 저장한 상품 옵션 이름과 동일한지 확인.
   *       - 대표로 [0]값만 확인.
   */
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

  /**
   * 상품 상세 조회 - 실패 시
   * - given / when / then
   *    1. 여러개의 상품들을 먼저 저장한다.
   *    2. 삭제된 상품 Sn 을 파라미터로 줄 변수에 담는다.
   *    3. 상품 상세 조회 메서드를 통해 상품 상세 조회 요청한다.
   *    4. 예외 처리된 Exception 이 알맞게 나왔는지 확인한다.
   *
   * - 테스트 확인
   *    1. 상품이 없다는 실패 메시지가 결과값으로 주는 지 확인. (Exception Check)
   */
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

  /**
   * 상품 수정 - 상품 단건
   * - given / when / then
   *    1. 하나의 상품을 먼저 저장한다.
   *    2. 저장된 상품 Sn과 수정 요청 보낼 Rq 를 생성한다.
   *    3. 상품 수정 메서드를 통해 상품 수정 요청한다.
   *    4. 데이터를 조회하여 수정된 값과 일치하는지 확인 및 성공 메시지 확인한다.
   *
   * - 테스트 확인
   *    1. 성공 메시지가 동일한지 확인.
   *    2. 가져온 데이터와 수정 요청한 Rq 와 이름이 동일한지 확인.
   *    3. 가져온 데이터와 수정 요청한 Rq 와 설명이 동일한지 확인.
   *    4. 상태값은 수정 요청 시 제공하지 않았으니 기존값 그대로 인지 확인
   */
  @Test
  fun `상품 수정 - 상품 단건`() {
    // given
    val saveProduct: Product = productRepository.save(
      Product(name = "가나 상품", description = "상품 설명1", price = 10000, shippingFee = 1000, status = 1, isDelete = false, registrationDate = LocalDateTime.now(), deleteDate = null)
    )
    val productSn: Long = saveProduct.sn!!
    val rq: ProductRq = ProductRq("상품 수정", "수정 설명", 2000, 0)

    // when
    val result = productService.updateProduct(productSn, rq)

    // then
    val product = productRepository.findAll()
    assertThat(result).isEqualTo(SuccessMessages.UPDATE_PRODUCT.message)
    assertThat(product[0].name).isEqualTo(rq.name)
    assertThat(product[0].description).isEqualTo(rq.description)
    assertThat(product[0].status).isEqualTo(Status.ON_SALE.value)
  }

  /**
   * 상품 수정 - 실패 시
   * - given / when / then
   *    1. 아무 상품도 저장하지 않는다.
   *    2. 없는 상품 Sn 과 임의의 수정 요청 할 Rq 를 생성한다.
   *    3. 상품 수정 메서드를 통해 상품 수정 요청한다.
   *    4. 예외 처리된 Exception 이 알맞게 나왔는지 확인한다.
   *
   * - 테스트 확인
   *    1. 상품이 없다는 실패 메시지가 결과값으로 주는 지 확인. (Exception Check)
   */
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

  /**
   * 상품 삭제 - 상품에 속한 옵션도 같이 삭제 (Soft Delete)
   * - given / when / then
   *    1. 하나의 상품을 먼저 저장한다.
   *    2. 상품에 속한 하나의 상품 옵션을 저장한다.
   *    3. 요청할 파라미터인 저장된 상품 Sn 을 변수에 담는다.
   *    4. 상품 삭제 메서드를 통해 상품 삭제 요청한다.
   *    5. 소프트 삭제이기에 데이터를 가져와 isDelete 가 true 로 변경되었는지 확인한다.
   *
   * - 테스트 확인
   *    1. 성공메시지가 동일한지 확인.
   *    2. 상품의 isDelete = true 인지 확인.
   *    3. 상품의 deleteDate 가 넣어졌는지 (Null 이 아닌지) 확인.
   *    4. 상품에 속한 옵션도 IsDelete = true 인지 확인.
   */
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

  /**
   * 상품 삭제 - 실패 시
   * - given / when / then
   *    1. 아무 상품도 저장하지 않는다.
   *    2. 파라미터로 요청 할 없는 상품 Sn 을 변수에 담는다.
   *    3. 상품 삭제 메서드를 통해 상품 삭제 요청한다.
   *    4. 예외 처리된 Exception 이 알맞게 나왔는지 확인한다.
   *
   * - 테스트 확인
   *    1. 상품이 없다는 실패 메시지가 결과값으로 주는 지 확인. (Exception Check)
   */
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

  /**
   * 상품 옵션 목록 조회 - 검색 X
   * - given / when / then
   *    1. 여러개의 상품들을 먼저 저장한다.
   *    2. 여러개의 상품들에 속한 각각의 상품 옵션들을 저장한다.
   *    3. 파라미터로 제공 할 pageable 을 기본값으로 생성한다.
   *    4. 상품 옵션 목록 메서드를 통해 상품 옵션 목록 요청한다.
   *    5. 결과로 주는 데이터와 미리 저장한 데이터가 동일한지 확인한다.
   *
   * - 테스트 확인
   *    1. Pageable(0,10) 이고, 저장한 상품이 3개(하나는 삭제된 값으로 저장) 이니 페이지 개수는 1인거 확인.
   *    2. 결과값에서 주는 총 데이터가 3개(하나는 삭제된 값으로 저장) 인지 확인.
   *    3. 결과값에서 주는 각 상품들에 속한 옵션의 개수(productOptionCount)와 저장된 각각의 상품 옵션 개수가 동일한지 확인.
   */
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

  /**
   * 상품 옵션 목록 조회 - 검색 O
   * - given / when / then
   *    1. 여러개의 상품들을 먼저 저장한다.
   *    2. 여러개의 상품들에 속한 각각의 상품 옵션들을 저장한다.
   *    3. 검색할 상품 이름과 파라미터로 제공 할 pageable 을 기본값으로 생성한다.
   *    4. 상품 옵션 목록 메서드를 통해 상품 옵션 목록 요청한다.
   *    5. 결과로 주는 데이터와 미리 저장한 데이터가 동일한지 확인한다.
   *
   * - 테스트 확인
   *    1. Pageable(0,10) 이고, 저장한 상품이 3개(하나는 삭제된 값으로 저장) 이니 페이지 개수는 1인거 확인.
   *    2. 결과값에서 주는 총 데이터가 2개(상품 이름으로 조회하기 때문) 인지 확인.
   *    3. 결과값에서 주는 각 상품들에 속한 옵션의 개수(productOptionCount)와 저장된 각각의 상품 옵션 개수가 동일한지 확인.
   */
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

  /**
   * 상품에 속한 옵션 상세 조회
   * - given / when / then
   *    1. 하나의 상품을 먼저 저장한다.
   *    2. 하나의 상품에 속한 각각의 상품 옵션들을 저장한다.
   *    3. 파라미터로 제공할 저장된 상품 Sn 을 변수에 담는다.
   *    4. 상품에 속한 옵션 상세 조회 메서드를 통해 상품에 속한 옵션 상세 조회 요청한다.
   *    5. 결과로 주는 데이터와 미리 저장한 데이터가 동일한지 확인한다.
   *
   * - 테스트 확인
   *    1. 상품의 옵션이 3개를 미리 저장하였지만 2개는 삭제된 값으로 저장하였기에 결과값의 사이즈가 1개인지 확인.
   *    2. 결과값의 옵션 이름과 추가 금액이 저장된(삭제되지 않은 데이터)와 일치하는 지 확인.
   */
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

  /**
   * 상품에 속한 옵션 상세 조회 - 실패 시
   * - given / when / then
   *    1. 아무 상품도 저장하지 않는다.
   *    2. 파라미터로 요청 할 없는 상품 Sn 을 변수에 담는다.
   *    3. 상품에 속한 옵션 상세 조회 메서드를 통해 상품에 속한 옵션 상세 조회 요청한다.
   *    4. 예외 처리된 Exception 이 알맞게 나왔는지 확인한다.
   *
   * - 테스트 확인
   *    1. 상품이 없다는 실패 메시지가 결과값으로 주는 지 확인. (Exception Check)
   */
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

  /**
   * 상품에 속한 상품 옵션 수정 - 생성 및 수정
   * - given / when / then
   *    1. 하나의 상품을 먼저 저장한다.
   *    2. 하나의 상품에 속한 각각의 상품 옵션들을 저장한다.
   *    3. 파라미터로 제공할 저장된 상품 Sn 을 변수와 수정 요청 할 rqList 를 생성한다.
   *    4. 상품에 속한 상품 옵션 수정 메서드를 통해 상품에 속한 상품 옵션 수정 요청한다.
   *    5. 상품 옵션 데이터를 불러와 Rq 대로 수정 / 생성 되었는지 확인한다.
   *
   * - 테스트 확인
   *    1. 성공 메시지가 동일한지 확인.
   *    2. 저장된 총 옵션 개수가 3개인지 확인.
   *        - 미리 저장된 옵션은 3개 중 2개는 삭제된 값이며, Rq 로 2개 생성, 1개 수정 했기에 총 결과값은 3개
   *    3. 각각의 저장된 상품 옵션의 옵션 sn, 이름, 추가 금액이 요청한 값들이랑 동일한지 확인.
   *        - sn 은 수정된값이므로 미리 저장된 상품 옵션 Sn 값이랑 동일한지 확인 (생성된 것이 아닌 수정된게 맞는지 확인)
   */
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

  /**
   * 상품에 속한 상품 옵션 수정 - 실패 시(상품 X)
   * - given / when / then
   *    1. 아무 상품도 저장하지 않는다.
   *    2. 파라미터로 요청 할 없는 상품 Sn 과 임의의 RqList 를 생성한다.
   *    3. 상품에 속한 상품 옵션 수정 메서드를 통해 상품에 속한 상품 옵션 수정 요청한다.
   *    4. 예외 처리된 Exception 이 알맞게 나왔는지 확인한다.
   *
   * - 테스트 확인
   *    1. 상품이 없다는 실패 메시지가 결과값으로 주는 지 확인. (Exception Check)
   */
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

  /**
   * 상품에 속한 상품 옵션 수정 - 실패 시(옵션 최대 개수 초과 시)
   * - given / when / then
   *    1. 하나의 상품을 미리 저장한다.
   *    2. 상품에 속한 상품 옵션들을 2개는 삭제된 값 1개는 삭제되지 않은 값으로 미리 저장한다.
   *    2. 파라미터로 요청 할 상품 Sn 과 RqList 에 생성할 3개의 값으로 생성한다.
   *    3. 상품에 속한 상품 옵션 수정 메서드를 통해 상품에 속한 상품 옵션 수정 요청한다.
   *    4. 예외 처리된 Exception 이 알맞게 나왔는지 확인한다.
   *
   * - 테스트 확인
   *    1. 1개는 이미 저장되어 있고, 3개의 생성을 요청했기에 알맞는 Exception 이 발생했는지 확인. (Exception Check)
   *        - 최대 3개만 저장 가능 (위에는 총 4개를 저장하려 했기에 실패)
   */
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

  /**
   * 상품에 속한 상품 옵션 개별 삭제 (Soft Delete)
   * - given / when / then
   *    1. 하나의 상품을 먼저 저장한다.
   *    2. 하나의 상품에 속한 하나의 상품 옵션을 저장한다.
   *    3. 파라미터로 제공할 저장된 상품 Sn 을 변수에 담는다.
   *    4. 상품에 속한 상품 옵션 개별 삭제 메서드를 통해 상품에 속한 상품 옵션 개별 삭제 요청한다.
   *    5. 상품 옵션 데이터를 불러와 isDelete = true 인지 확인 및 성공 메시지를 확인한다.
   *
   * - 테스트 확인
   *    1. 성공 메시지가 동일한지 확인.
   *    2. 불러온 상품 옵션 데이터에서 isDelete = true 인지 확인.
   */
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

  /**
   * 상품에 속한 상품 옵션 개별 삭제 - 실패 시
   * - given / when / then
   *    1. 아무 상품 옵션을 저장하지 않는다.
   *    2. 파라미터로 요청 할 없는 상품 옵션 Sn을 변수에 담는다.
   *    3. 상품에 속한 상품 옵션 개별 삭제 메서드를 통해 상품에 속한 상품 옵션 개별 삭제 요청한다.
   *    4. 예외 처리된 Exception 이 알맞게 나왔는지 확인한다.
   *
   * - 테스트 확인
   *    1. 상품 옵션이 없다는 실패 메시지가 결과값으로 주는 지 확인. (Exception Check)
   */
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

  /**
   * 선택 옵션 리스트 조회
   * - given / when / then
   *    1. 선택 옵션 전체 조회하기 위해 미리 데이터를 저장한다.
   *    2. 선택 옵션 리스트 조회 메서드를 통해 선택 옵션 리스트 조회 요청한다.
   *    3. 응답한 결과값의 개수와 저장한 데이터의 개수가 동일한지 확인한다.
   *
   * - 테스트 확인
   *    1. 응답한 데이터 개수가 저장된 데이터 개수(5개) 와 일치하는지 확인.
   */
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