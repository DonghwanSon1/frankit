package com.project.frankit.domain.product.product

import com.project.frankit.domain.admin.rqrs.ProductOptionListRs
import com.project.frankit.domain.product.productOption.QProductOption
import com.project.frankit.domain.product.rqrs.ProductListRs
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class ProductCustomRepositoryImpl(private val queryFactory: JPAQueryFactory) : ProductCustomRepository {
    private val product: QProduct = QProduct.product
    private val productOption: QProductOption = QProductOption.productOption


    override fun searchProductList(productName: String?, pageable: Pageable): Page<ProductListRs> {
        val builder = BooleanBuilder()
        if (!productName.isNullOrEmpty()) builder.and(product.name.like("%$productName%"))

        val results = queryFactory
            .select(
                Projections.fields(
                    ProductListRs::class.java,
                    product.sn,
                    product.name,
                    product.price,
                    product.status.`as`("_status")
                )
            )
            .from(product)
            .where(
                product.isDelete.isFalse,
                builder
            )
            .orderBy(product.sn.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val count = queryFactory
            .selectFrom(product)
            .where(product.isDelete.isFalse, builder)
            .fetch().size.toLong()

        return PageImpl(results, pageable, count)
    }

    override fun searchProductOptionList(productName: String?, pageable: Pageable): Page<ProductOptionListRs> {
        val builder = BooleanBuilder()
        if (!productName.isNullOrEmpty()) builder.and(product.name.like("%$productName%"))

        val results = queryFactory
            .select(
                Projections.fields(
                    ProductOptionListRs::class.java,
                    product.sn.`as`("productSn"),
                    product.name.`as`("productName"),
                    product.status.`as`("_productStatus"),
                    product.registrationDate.`as`("productRegistrationDate"),
                    productOption.count().`as`("productOptionCount")
                )
            )
            .from(product)
            .leftJoin(productOption).on(
                productOption.product.eq(product),
                productOption.isDelete.isFalse
            )
            .where(
                product.isDelete.isFalse,
                builder
            )
            .groupBy(product.sn)
            .orderBy(product.sn.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val count = queryFactory
            .selectFrom(product)
            .where(product.isDelete.isFalse,builder)
            .fetch().size.toLong()

        return PageImpl(results, pageable, count)
    }
}