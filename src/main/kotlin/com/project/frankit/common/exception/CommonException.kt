package com.project.frankit.common.exception


class CommonException(val exceptionCode: CommonExceptionCode) : RuntimeException(exceptionCode.message) {

}