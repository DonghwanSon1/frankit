package com.project.frankit.domain.product

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/product")
@Tag(name = "Product", description = "상품 관련 API")
class ProductController(
  private val productService: ProductService,
) {


}