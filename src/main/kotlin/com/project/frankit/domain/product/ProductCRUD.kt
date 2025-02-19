package com.project.frankit.domain.product


import com.project.frankit.common.exception.CommonException
import com.project.frankit.common.exception.CommonExceptionCode
import com.project.frankit.domain.product.product.Product
import com.project.frankit.domain.product.product.ProductRepository
import com.project.frankit.domain.product.productOption.ProductOption
import com.project.frankit.domain.product.productOption.ProductOptionRepository
import com.project.frankit.domain.product.rqrs.ProductListRs
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class ProductCRUD(
  private val productRepository: ProductRepository,
  private val productOptionRepository: ProductOptionRepository,
) {

  /**
   * append
   */
  @Transactional
  fun saveProductAndProductOptions(productEntity: Product, productOptionEntities: List<ProductOption>) {
    productRepository.save(productEntity)
    productOptionRepository.saveAll(productOptionEntities)
  }


  /**
   * find
   */
  fun findProductByProductSn(productSn: Long): Product {
    return productRepository.findBySnAndIsDelete(productSn, false) ?:
      throw CommonException(CommonExceptionCode.NOT_EXIST_PRODUCT)
  }

  fun searchProductList(productName: String?, pageable: Pageable): Page<ProductListRs> {
    return productRepository.searchProductList(productName, pageable)
  }

  fun findProductOptionAllByProductSn(productSn: Long): List<ProductOption>? {
    return productOptionRepository.findAllByProductSn(productSn)
  }

  /**
   * update
   */
  @Transactional
  fun updateProduct(updateProduct: Product) {
    productRepository.save(updateProduct)
  }

}