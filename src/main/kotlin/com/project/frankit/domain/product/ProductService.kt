package com.project.frankit.domain.product


import com.project.frankit.common.response.SuccessMessages
import com.project.frankit.domain.admin.rqrs.ProductAndOptionRq
import com.project.frankit.domain.admin.rqrs.ProductRq
import com.project.frankit.domain.product.product.Product
import com.project.frankit.domain.product.productOption.ProductOption
import com.project.frankit.domain.product.rqrs.ProductListRs
import com.project.frankit.domain.product.rqrs.ProductRs
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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


  fun searchProductList(productName: String?, pageable: Pageable): Page<ProductListRs> {
    return productCRUD.searchProductList(productName, pageable)
  }


  fun searchDetailProduct(productSn: Long): ProductRs {
    val product: Product = productCRUD.findProductByProductSn(productSn)
    val productOptionList: List<ProductOption>? = productCRUD.findProductOptionAllByProductSn(productSn)

    return ProductRs.createProductRs(product, productOptionList)
  }


  fun updateProduct(productSn: Long, rq: ProductRq): String {
    val product: Product = productCRUD.findProductByProductSn(productSn)
    productCRUD.updateProduct(product.updateProduct(rq))

    return SuccessMessages.UPDATE_PRODUCT.message
  }


  fun deleteProduct(productSn: Long): String {
    val product: Product = productCRUD.findProductByProductSn(productSn).apply {
      isDelete = true
      deleteDate = LocalDateTime.now()
    }
    productCRUD.updateProduct(product)

    return SuccessMessages.DELETE_PRODUCT.message
  }


}