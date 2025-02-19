package com.project.frankit.domain.product

import com.project.frankit.common.response.BaseResponse
import com.project.frankit.domain.product.rqrs.ProductListRs
import com.project.frankit.domain.product.rqrs.ProductRs
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/product")
@Tag(name = "Product", description = "상품 관련 API")
class ProductController(
  private val productService: ProductService,
) {

  @GetMapping("/list")
  @Operation(summary = "상품 목록", description = "상품의 목록 및 상품의 이름 검색을 통한 목록을 페이징 처리하여 조회한다.")
  fun searchProductList(@RequestParam productName: String?,
                        @PageableDefault(size = 10) pageable: Pageable): BaseResponse<Page<ProductListRs>> {
    return BaseResponse(data = productService.searchProductList(productName, pageable))
  }

  @GetMapping("/detail/{productSn}")
  @Operation(summary = "상품 상세 조회", description = "상품 상세 조회한다.")
  fun searchDetailProduct(@PathVariable productSn: Long): BaseResponse<ProductRs> {
    return BaseResponse(data = productService.searchDetailProduct(productSn))
  }

}