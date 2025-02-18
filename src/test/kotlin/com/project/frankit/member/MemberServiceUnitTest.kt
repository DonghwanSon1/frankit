package com.project.frankit.member

import com.project.frankit.common.authority.JwtTokenProvider
import com.project.frankit.common.authority.TokenInfo
import com.project.frankit.common.exception.CommonException
import com.project.frankit.common.exception.CommonExceptionCode
import com.project.frankit.domain.member.MemberCRUD
import com.project.frankit.domain.member.MemberService
import com.project.frankit.domain.member.enums.Role
import com.project.frankit.domain.member.rqrs.LoginRq
import com.project.frankit.domain.member.rqrs.MemberRq
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MemberServiceUnitTest {

  private val memberCRUD: MemberCRUD = mockk<MemberCRUD>()
  private val authenticationManagerBuilder: AuthenticationManagerBuilder = mockk<AuthenticationManagerBuilder>()
  private val jwtTokenProvider: JwtTokenProvider = mockk<JwtTokenProvider>()
  private val passwordEncoder: PasswordEncoder = mockk<PasswordEncoder>()
  private val memberService: MemberService = MemberService(
    memberCRUD,
    authenticationManagerBuilder,
    jwtTokenProvider,
    passwordEncoder
  )

  @Test
  fun `회원가입 성공`() {
    // given
    val memberRq = MemberRq(_email = "testMember@naver.com", password = "admin", name = "김개인", storeName = "프랜킷_봉천점", role = Role.FRANCHISEE)
    every { memberCRUD.findDuplicateId(memberRq.email) } returns null
    every { passwordEncoder.encode(memberRq.password) } returns "{bcrypt}\$2a\$10\$0sLKphEOIJqo9WaIIegJvewcAE/L2BnJcLKdbI7poUIZzqr6BHfWm"
    every { memberCRUD.appendMember(any()) } answers { firstArg() }

    // when
    val result = memberService.signUp(memberRq)

    // then
    assertThat(result).isEqualTo("회원가입이 완료되었습니다.")
  }

  @Test
  fun `회원가입 실패 - 중복된 ID`() {
    // Given
    val memberRq = MemberRq(_email = "testMember@naver.com", password = "admin", name = "김개인", storeName = "프랜킷_봉천점", role = Role.FRANCHISEE)
    every { memberCRUD.findDuplicateId(memberRq.email) } returns mockk()

    // When & Then
    val message = assertThrows<CommonException> {
      memberService.signUp(memberRq)
    }.message

    assertThat(message).isEqualTo(CommonExceptionCode.DUPLICATE_ID.message)
  }

  @Test
  fun `로그인 성공`() {
    // Given
    val loginRq = LoginRq(_email = "testMember@naver.com", password = "admin")
    val authenticationToken = UsernamePasswordAuthenticationToken(loginRq.email, loginRq.password)
    val authentication: Authentication = mockk()
    every { authenticationManagerBuilder.`object`.authenticate(authenticationToken) } returns authentication
    every { jwtTokenProvider.createToken(authentication) } returns TokenInfo("accessToken")

    // When
    val tokenInfo = memberService.login(loginRq)

    // Then
    assertNotNull(tokenInfo)
    assertEquals("accessToken", tokenInfo.token)
  }

  @Test
  fun `로그인 실패 - 인증 실패`() {
    // Given
    val loginRq = LoginRq(_email = "testMember@naver.com", password = "admin")
    val authenticationToken = UsernamePasswordAuthenticationToken(loginRq.email, loginRq.password)
    every { authenticationManagerBuilder.`object`.authenticate(authenticationToken) } throws BadCredentialsException("유효하지 않은 Credential")

    // When & Then
    val exception = assertThrows<CommonException> {
      memberService.login(loginRq)
    }
    assertEquals(CommonExceptionCode.LOGIN_FAIL, exception.exceptionCode)
  }

}