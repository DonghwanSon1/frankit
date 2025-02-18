package com.project.frankit.domain.product.enums

enum class Status(val desc: String, val value: Int) {
  ON_SALE("판매중", 1),
  SOLD_OUT("품절", 2)
}