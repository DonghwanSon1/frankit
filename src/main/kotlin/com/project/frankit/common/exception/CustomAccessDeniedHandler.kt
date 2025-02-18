package com.project.frankit.common.exception

import com.fasterxml.jackson.databind.ObjectMapper
import com.project.frankit.common.response.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class CustomAccessDeniedHandler : AccessDeniedHandler {
  private val objectMapper = ObjectMapper()
  override fun handle(
    request: HttpServletRequest,
    response: HttpServletResponse,
    accessDeniedException: AccessDeniedException
  ) {
    val invalidTokenException = CommonExceptionCode.INVALID_ROLE

    val errorResponse = ErrorResponse(
      status = invalidTokenException.status.value(),
      message = invalidTokenException.message
    )

    response.status = HttpServletResponse.SC_UNAUTHORIZED
    response.contentType = "application/json;charset=UTF-8"
    response.writer.write(objectMapper.writeValueAsString(errorResponse))
    response.flushBuffer()
  }
}
