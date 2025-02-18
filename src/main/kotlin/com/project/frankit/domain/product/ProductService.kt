package com.project.frankit.domain.product


import com.project.frankit.domain.admin.rqrs.ProductAndOptionRq
import com.project.frankit.domain.admin.rqrs.ProductRq
import com.project.frankit.domain.product.product.Product
import com.project.frankit.domain.product.productOption.ProductOption
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
  private val productCRUD: ProductCRUD,
) {

  @Transactional
  fun saveProductAndProductOption(rq: ProductAndOptionRq): String {

    val productEntity: Product = Product.createProduct(rq)
    val productOptionEntities: List<ProductOption> = rq.productOption.map {
      ProductOption.createProductOption(productEntity, it) }

    productCRUD.saveProductAndProductOptions(productEntity, productOptionEntities)

    return "상품 등록이 성공적으로 완료되었습니다."
  }

  fun updateProductAndProductOption(productSn: Long, rq: ProductRq): String {

    val product: Product = productCRUD.findByProductSn(productSn)
    productCRUD.updateProduct(product.updateProduct(rq))

    return "상품 수정이 성공적으로 완료되었습니다."
  }


}