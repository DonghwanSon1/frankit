package com.project.frankit.domain.product


import com.project.frankit.common.exception.CommonException
import com.project.frankit.common.exception.CommonExceptionCode
import com.project.frankit.domain.admin.rqrs.SelectOptionRs
import com.project.frankit.domain.product.product.Product
import com.project.frankit.domain.product.product.ProductRepository
import com.project.frankit.domain.product.productOption.ProductOption
import com.project.frankit.domain.product.productOption.ProductOptionRepository
import com.project.frankit.domain.product.rqrs.ProductListRs
import com.project.frankit.domain.product.selectOption.SelectOptionRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class ProductCRUD(
  private val productRepository: ProductRepository,
  private val productOptionRepository: ProductOptionRepository,
  private val selectOptionRepository: SelectOptionRepository
) {

  /**
   * append / update
   */
  @Transactional
  fun saveProductAndProductOptions(productEntity: Product, productOptionEntities: List<ProductOption>) {
    productRepository.save(productEntity)
    productOptionRepository.saveAll(productOptionEntities)
  }

  @Transactional
  fun saveProduct(updateProduct: Product) {
    productRepository.save(updateProduct)
  }

  @Transactional
  fun saveAllProductOptions(productOptionList: List<ProductOption>) {
    productOptionRepository.saveAll(productOptionList)
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

  fun findProductOptionAllByProduct(product: Product): List<ProductOption> {
    return productOptionRepository.findAllByProduct(product)
  }

  fun findSelectOptionAll(): List<SelectOptionRs> {
    return selectOptionRepository.findAll().map {
      SelectOptionRs(name = it.name)
    }
  }
}