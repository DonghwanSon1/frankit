package com.project.frankit.common.response

enum class SuccessMessages(val message: String) {
  SIGN_UP("회원가입이 완료되었습니다."),
  CREATE_PRODUCT("상품 등록이 성공적으로 완료되었습니다."),
  UPDATE_PRODUCT("상품 수정이 성공적으로 완료되었습니다."),
  DELETE_PRODUCT("상품 삭제가 성공적으로 완료되었습니다."),
  UPDATE_PRODUCT_OPTION("상품 옵션 수정 및 생성이 성공적으로 완료되었습니다."),
  DELETE_PRODUCT_OPTION("상품 옵션 삭제가 성공적으로 완료되었습니다."),
}