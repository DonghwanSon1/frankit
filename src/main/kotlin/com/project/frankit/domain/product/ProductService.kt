package com.project.frankit.domain.product


import com.project.frankit.common.exception.CommonException
import com.project.frankit.common.exception.CommonExceptionCode
import com.project.frankit.common.response.SuccessMessages
import com.project.frankit.domain.admin.rqrs.ProductAndOptionRq
import com.project.frankit.domain.admin.rqrs.ProductOptionRq
import com.project.frankit.domain.admin.rqrs.ProductRq
import com.project.frankit.domain.admin.rqrs.SelectOptionRs
import com.project.frankit.domain.product.product.Product
import com.project.frankit.domain.product.productOption.ProductOption
import com.project.frankit.domain.product.rqrs.ProductListRs
import com.project.frankit.domain.product.rqrs.ProductRs
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ProductService(
  private val productCRUD: ProductCRUD,
) {

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
    val productOptionList: List<ProductOption> = productCRUD.findProductOptionAllByProduct(product)

    return ProductRs.createProductRs(product, productOptionList)
  }



  fun updateProduct(productSn: Long, rq: ProductRq): String {
    val product: Product = productCRUD.findProductByProductSn(productSn)
    productCRUD.saveProduct(product.updateProduct(rq))

    return SuccessMessages.UPDATE_PRODUCT.message
  }


  fun deleteProduct(productSn: Long): String {
    val product: Product = productCRUD.findProductByProductSn(productSn).apply {
      isDelete = true
      deleteDate = LocalDateTime.now()
    }
    productCRUD.saveProduct(product)

    return SuccessMessages.DELETE_PRODUCT.message
  }


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


  fun searchSelectOptionList(): List<SelectOptionRs> {
    return productCRUD.findSelectOptionAll()
  }

}
