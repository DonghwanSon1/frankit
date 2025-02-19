package com.project.frankit.domain.product


import com.project.frankit.common.response.SuccessMessages
import com.project.frankit.domain.admin.rqrs.ProductAndOptionRq
import com.project.frankit.domain.admin.rqrs.ProductRq
import com.project.frankit.domain.product.product.Product
import com.project.frankit.domain.product.productOption.ProductOption
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ProductService(
  private val productCRUD: ProductCRUD,
) {

  @Transactional
  fun saveProductAndProductOption(rq: ProductAndOptionRq): String {
    val productEntity: Product = Product.createProduct(rq)
    val productOptionEntities: List<ProductOption> = rq.productOption.map {
      ProductOption.createProductOption(productEntity, it)
    }

    productCRUD.saveProductAndProductOptions(productEntity, productOptionEntities)

    return SuccessMessages.CREATE_PRODUCT.message
  }


  fun updateProduct(productSn: Long, rq: ProductRq): String {
    val product: Product = productCRUD.findByProductSn(productSn)
    productCRUD.updateProduct(product.updateProduct(rq))

    return SuccessMessages.UPDATE_PRODUCT.message
  }


  fun deleteProduct(productSn: Long): String {
    val product: Product = productCRUD.findByProductSn(productSn).apply {
      isDelete = true
      deleteDate = LocalDateTime.now()
    }
    productCRUD.updateProduct(product)

    return SuccessMessages.DELETE_PRODUCT.message
  }


}