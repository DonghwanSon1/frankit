package com.project.frankit.domain.product.product

import com.project.frankit.domain.product.productOption.QProductOption
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class ProductCustomRepositoryImpl(private val queryFactory: JPAQueryFactory) : ProductCustomRepository {
    private val product: QProduct = QProduct.product
    private val productOption: QProductOption = QProductOption.productOption


}