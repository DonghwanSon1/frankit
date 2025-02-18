package com.project.frankit.domain.admin

import com.project.frankit.common.exception.CommonException
import com.project.frankit.common.exception.CommonExceptionCode
import com.project.frankit.common.response.BaseResponse
import com.project.frankit.domain.admin.rqrs.ProductRq
import com.project.frankit.domain.product.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "관리자 관련 API")
class AdminController(
  private val productService: ProductService,
) {

  @PostMapping("/product")
  @Operation(summary = "상품 등록 및 상품 옵션 등록", description = "상품 등록 및 상품 옵션 등록 합니다.")
  fun saveProductAndProductOption(@RequestBody rq: ProductRq): BaseResponse<Unit> {
    if (rq.productOption.size > 3) throw CommonException(CommonExceptionCode.PRODUCT_OPTION_LIMIT_EXCEEDED)
    val result: String = productService.saveProductAndProductOption(rq)
    return BaseResponse(message = result)
  }


}