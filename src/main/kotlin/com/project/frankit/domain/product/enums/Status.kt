package com.project.frankit.domain.product.enums

import com.project.frankit.common.exception.CommonException
import com.project.frankit.common.exception.CommonExceptionCode

enum class Status(val desc: String, val value: Int) {
  ON_SALE("판매중", 1),
  SOLD_OUT("품절", 2);

  companion object {
    fun fromValue(value: Int): Status {
      return values().find { it.value == value }
        ?: throw CommonException(CommonExceptionCode.INVALID_PRODUCT_STATUS)
    }
  }
}