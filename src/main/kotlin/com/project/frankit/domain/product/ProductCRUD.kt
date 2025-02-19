package com.project.frankit.domain.product


import com.project.frankit.common.exception.CommonException
import com.project.frankit.common.exception.CommonExceptionCode
import com.project.frankit.domain.product.product.Product
import com.project.frankit.domain.product.product.ProductRepository
import com.project.frankit.domain.product.productOption.ProductOption
import com.project.frankit.domain.product.productOption.ProductOptionRepository
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
  fun findByProductSn(productSn: Long): Product {
    return productRepository.findById(productSn).orElseThrow {
      throw CommonException(CommonExceptionCode.NOT_EXIST_PRODUCT)
    }
  }

  /**
   * update
   */
  @Transactional
  fun updateProduct(updateProduct: Product) {
    productRepository.save(updateProduct)
  }

}