package com.project.frankit.domain.product


import com.project.frankit.common.exception.CommonException
import com.project.frankit.common.exception.CommonExceptionCode
import com.project.frankit.common.response.SuccessMessages
import com.project.frankit.domain.admin.rqrs.*
import com.project.frankit.domain.product.product.Product
import com.project.frankit.domain.product.productOption.ProductOption
import com.project.frankit.domain.product.rqrs.ProductListRs
import com.project.frankit.domain.product.rqrs.ProductOptionRs
import com.project.frankit.domain.product.rqrs.ProductRs
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ProductService(
  private val productCRUD: ProductCRUD,
) {

  /**
   * 상품 등록 (상품 옵션 같이 등록 - 선택)
   *
   * - 설명
   *  1. 클라이언트에서 주는 Rq를 가지고 상품 엔티티를 생성 한다.
   *  2. 상품 옵션도 같이 Rq 에 있다면, 상품 옵션 엔티티를 생성 한다. (없으면 빈배열)
   *  3. 레포지토리 접근 컴포넌트에 전달하여 상품 및 상품 옵션을 저장한다.
   *  4. 저장 후 성공 메시지를 클라이언트에게 응답한다.
   */
  fun saveProductAndProductOption(rq: ProductAndOptionRq): String {
    val productEntity: Product = Product.createProduct(rq)
    val productOptionEntities: List<ProductOption> = rq.productOption.map {
      ProductOption.createProductOption(productEntity, it)
    }

    productCRUD.saveProductAndProductOptions(productEntity, productOptionEntities)

    return SuccessMessages.CREATE_PRODUCT.message
  }

  /**
   * 상품 목록 조회 (페이징 처리 및 상품 검색)
   *
   * - 설명
   *  1. 상품 이름 및 pageable 을 받아 레포지토리 접근 컴포넌트에 전달하여 QueryDsl 을 통해 Rs 형식으로 가져온다.
   *  1.1 상품 이름이 있을 시 상품 이름으로 조회, 없을 시 전체 상품 목록 조회 - 상품 최신순으로 정렬
   *  2. 가져온 페이지 Rs 를 클라이언트에게 응답한다.
   */
  fun searchProductList(productName: String?, pageable: Pageable): Page<ProductListRs> {
    return productCRUD.searchProductList(productName, pageable)
  }

  /**
   * 상품 상세 조회 (상품 옵션 포함)
   *
   * - 설명
   *  1. productSn 을 레포지토리 접근 컴포넌트에 전달하여 상품을 가져온다. - 없을 시 Exception
   *  2. 가져온 상품을 레포지토리 접근 컴포넌트에 전달하여 상품에 속한 옵션들을 가져온다. - 없을 시 빈배열
   *  3. Rs 형식으로 변환하여 클라이언트에게 응답한다.
   */
  fun searchDetailProduct(productSn: Long): ProductRs {
    val product: Product = productCRUD.findProductByProductSn(productSn)
    val productOptionList: List<ProductOption> = productCRUD.findProductOptionAllByProduct(product)

    return ProductRs.createProductRs(product, productOptionList)
  }

  /**
   * 상품 수정 (단건 수정 및 상품만 수정 - 상품 옵션 X)
   *
   * - 설명
   *  1. 요청받은 productSn을 레포지토리 접근 컴포넌트에 전달하여 상품을 조회한다. - 없을 시 Exception
   *  2. 상품이 있다면, Rq의 요청대로 수정하여 레포지토리 접근 컴포넌트에 전달하여 저장한다.
   *  3. 저장 후 성공 메시지를 클라이언트에게 응답한다.
   */
  fun updateProduct(productSn: Long, rq: ProductRq): String {
    val product: Product = productCRUD.findProductByProductSn(productSn)
    productCRUD.saveProduct(product.updateProduct(rq))

    return SuccessMessages.UPDATE_PRODUCT.message
  }

  /**
   * 상품 삭제 (상품 삭제 시 상품 옵션도 같이 삭제 - Soft Delete)
   *
   * - 설명
   *  1. 요청받은 productSn을 레포지토리 접근 컴포넌트에 전달하여 상품을 조회한다. - 없을 시 Exception
   *  1.1 조회 받아온 후 바로 Soft Delete 를 하기위해 isDelete, deleteDate 를 수정한다.
   *  2. 상품을 레포지토리 접근 컴포넌트에 전달하여 상품에 속한 옵션들을 조회한다.
   *  3. 조회된 상품 옵션들을 상품 옵션 삭제 메서드를 통해 상품 옵션들의 isDelete 를 수정한다. - 없을 시 해당 메서드 호출 X
   *  4. 레포지토리 접근 컴포넌트에 수정된 상품 옵션들을 전달하여 저장(소프트 삭제)한다.
   *  5. 레포지토리 접근 컴포넌트에 수정된 상품을 전달하여 저장(소프트 삭제)한다.
   *  6. 저장(소프트 삭제) 후 성공 메시지를 클라이언트에게 응답한다.
   */
  fun deleteProduct(productSn: Long): String {
    val product: Product = productCRUD.findProductByProductSn(productSn).apply {
      isDelete = true
      deleteDate = LocalDateTime.now()
    }

    val productOptionList: List<ProductOption> = productCRUD.findProductOptionAllByProduct(product)
    productOptionList.forEach { this.deleteProductOption(it.sn!!) }

    productCRUD.saveAllProductOptions(productOptionList)
    productCRUD.saveProduct(product)

    return SuccessMessages.DELETE_PRODUCT.message
  }


  /**
   * 상품 옵션 목록 조회 (상품Sn, 상품 이름, 상품 상태, 상품에 속한 옵션 개수)
   *
   * - 설명
   *  1. 상품 이름 및 pageable 을 받아 레포지토리 접근 컴포넌트에 전달하여 QueryDsl 을 통해 Rs 형식으로 가져온다.
   *  1.1 상품 이름이 있을 시 상품 이름으로 조회, 없을 시 전체 상품으로 조회, 상품 옵션이 없을 시 0 - 상품 최신순으로 정렬
   *  2. 가져온 페이지 Rs 를 클라이언트에게 응답한다.
   */
  fun searchProductOptionList(productName: String?, pageable: Pageable): Page<ProductOptionListRs> {
    return productCRUD.searchProductOptionList(productName, pageable)
  }

  /**
   * 상품에 속한 옵션 상세 조회
   *
   * - 설명
   *  1. productSn을 레포지토리 접근 컴포넌트에 전달하여 상품을 조회한다. - 없을 시 Exception
   *  2. 조회된 상품을 레포지토리 접근 컴포넌트에 전달하여 상품에 속한 옵션들을 가져와 Rs 형식으로 변경하여 클라이언트에게 응답한다.
   */
  fun searchProductOption(productSn: Long): List<ProductOptionRs> {
    val product: Product = productCRUD.findProductByProductSn(productSn)
    return productCRUD.findProductOptionAllByProduct(product).map { ProductOptionRs.createProductOptionRs(it) }

  }

  /**
   * 상품에 속한 상품 옵션 수정 (생성/수정 가능)
   *
   * - 설명
   *  1. ProductSn을 레포지토리 접근 컴포넌트에 전달하여 상품을 조회한다. - 없을 시 Exception
   *  2. 가져온 상품을 레포지토리 접근 컴포넌트에 전달하여 상품에 속한 상품 옵션들을 조회한다.
   *  2.1 조회된 상품 옵션들을 수정 할 rqList 의 optionSn 을 빠르게 꺼낼 수 있게 Map 형식으로 변경한다.
   *  3. 만약 수정 시 생성을 요청한 개수와 가져온 상품 옵션 개수의 합이 3개 초과면 최대 3개까지만 저장할 수 있기에 Exception 발생 시킨다.
   *  4. 수정 및 생성 할 상품 옵션들을 담을 리스트를 선언한다.
   *  5. 클라이언트에서 요청한 rqList 를 돌려 optionSn 이 null 이 아니면 수정, null 이면 생성 이므로 각자의 역할에 맞게 생성/수정 한다.
   *  5.1 수정 시 가져온 상품 옵션 Map 에서 빠르게 꺼내 Rq 대로 수정한 후 리스트에 담는다.
   *  5.2 생성 시 생성자 메서드를 통해 상품 옵션 엔티티를 생성한다.
   *  6. 생성 및 수정 할 상품 옵션(엔티티)들을 레포지토리 접근 컴포넌트에 전달하여 저장한다.
   *  7. 저장 후 성공 메시지를 클라이언트에게 응답한다.
   */
  fun updateProductOption(productSn: Long, rqList: List<ProductOptionRq>): String {
    val product: Product = productCRUD.findProductByProductSn(productSn)
    val productOptionMap: Map<Long, ProductOption> = productCRUD.findProductOptionAllByProduct(product)
      .associateBy { it.sn!! }

    if (productOptionMap.size + rqList.filter { it.optionSn == null }.size > 3) {
      throw CommonException(CommonExceptionCode.PRODUCT_OPTION_LIMIT_EXCEEDED)
    }

    val saveEntities = ArrayList<ProductOption>()

    rqList.forEach { rq ->
      if (rq.optionSn != null) {
        productOptionMap[rq.optionSn]?.let { saveEntities.add(it.updateProductOption(rq)) }
      } else {
        saveEntities.add(ProductOption.createProductOption(product, rq))
      }
    }

    productCRUD.saveAllProductOptions(saveEntities)

    return SuccessMessages.UPDATE_PRODUCT_OPTION.message
  }

  /**
   * 상품에 속한 상품 옵션 개별 삭제
   *
   * - 설명
   *  1. productOptionSn 을 레포지토리 접근 컴포넌트에 전달하여 상품 옵션을 조회한다. - 없을 시 Exception
   *  1.1 가져온 상품 옵션을 isDelete 를 수정한다. (Soft Delete)
   *  2. 수정된 상품 옵션을 레포지토리 접근 컴포넌트에 전달하여 저장(소프트 삭제) 한다.
   *  3. 저장(소프트 삭제) 후 성공 메시지를 클라이언트에게 응답한다.
   */
  fun deleteProductOption(productOptionSn: Long): String {
    val productOption: ProductOption = productCRUD.findProductOptionByProductOptionSn(productOptionSn)
      .apply { isDelete = true }

    productCRUD.saveProductOption(productOption)
    return SuccessMessages.DELETE_PRODUCT_OPTION.message
  }


  /**
   * 선택 옵션 리스트 조회
   *
   * - 설명
   *  1. 레포지토리 접근할 컴포넌트에 가서 선택 옵션 전체 가져온다.
   *  1.1 DB 에서 가져온 후 클라이언트에게 줄 Rs 형식으로 변경
   *  2. Rs를 클라이언트에게 응답한다.
   */
  fun searchSelectOptionList(): List<SelectOptionRs> {
    return productCRUD.findSelectOptionAll().map {
      SelectOptionRs(name = it.name)
    }
  }

}
