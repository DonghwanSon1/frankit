package com.project.frankit.common.exception

import org.springframework.http.HttpStatus

enum class CommonExceptionCode(
        val status: HttpStatus,
        val message: String
) {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "입력값을 확인해주세요."),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 오류가 발생했습니다."),
    INVALID_PRODUCT_STATUS(HttpStatus.INTERNAL_SERVER_ERROR, "잘못된 상품 상태입니다."),
    DUPLICATE_ID(HttpStatus.BAD_REQUEST, "중복된 아이디가 있습니다."),
    LOGIN_FAIL(HttpStatus.UNAUTHORIZED, "해당 유저가 존재하지 않거나, 아이디 또는 비밀번호가 틀렸습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    INVALID_ROLE(HttpStatus.FORBIDDEN, "관리자 및 프렌차이즈 관리자만 접근 가능합니다."),
    DUPLICATE_DATA_ERROR(HttpStatus.CONFLICT, "중복 데이터 발생했습니다. 입력값을 확인 해주세요."),
    CONSTRAINTS_ERROR(HttpStatus.BAD_REQUEST, "데이터 처리 중 오류가 발생했습니다. 입력값을 확인한 후 다시 시도해주세요."),
    PRODUCT_OPTION_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "상품 옵션을 최대 3개까지 가능합니다."),
    NOT_EXIST_PRODUCT(HttpStatus.BAD_REQUEST, "존재 하지 않는 상품입니다."),

}