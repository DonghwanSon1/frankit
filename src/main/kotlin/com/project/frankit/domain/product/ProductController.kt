package com.project.frankit.domain.product

import com.project.frankit.common.response.BaseResponse
import com.project.frankit.domain.product.rqrs.ProductListRs
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/product")
@Tag(name = "Product", description = "상품 관련 API")
class ProductController(
  private val productService: ProductService,
) {

  @GetMapping("/list")
  @Operation(summary = "상품 목록", description = "상품의 목록 및 상품의 이름 검색을 통한 목록을 페이징 처리하여 조회한다.")
  fun searchProduct(@RequestParam productName: String?,
                    @PageableDefault(size = 10) pageable: Pageable
  ): BaseResponse<Page<ProductListRs>> {
    return BaseResponse(data = productService.searchProduct(productName, pageable))
  }

}