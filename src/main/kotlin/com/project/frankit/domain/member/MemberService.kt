package com.project.frankit.domain.member


import com.project.frankit.common.authority.JwtTokenProvider
import com.project.frankit.common.authority.TokenInfo
import com.project.frankit.common.exception.CommonException
import com.project.frankit.common.exception.CommonExceptionCode
import com.project.frankit.domain.member.rqrs.LoginRq
import com.project.frankit.domain.member.rqrs.MemberRq
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class MemberService(
  private val memberCRUD: MemberCRUD,
  private val authenticationManagerBuilder: AuthenticationManagerBuilder,
  private val jwtTokenProvider: JwtTokenProvider,
  private val passwordEncoder: PasswordEncoder
) {

  /**
   * 회원가입
   *
   * - 설명
   */
  fun signUp(memberRq: MemberRq): String {
    var member: Member? = memberCRUD.findDuplicateId(memberRq.email)
    if (member != null) throw CommonException(CommonExceptionCode.DUPLICATE_ID)

    member = Member.createMember(memberRq, passwordEncoder.encode(memberRq.password))
    memberCRUD.appendUser(member)

    return "회원가입이 완료되었습니다."
  }

  /**
   * 로그인 (토큰 발행)
   *
   * - 설명
   */
  fun login(loginRq: LoginRq): TokenInfo {
    try {
      val authenticationToken = UsernamePasswordAuthenticationToken(loginRq.email, loginRq.password)
      val authentication = authenticationManagerBuilder.`object`.authenticate(authenticationToken)

      return jwtTokenProvider.createToken(authentication)
    } catch (e: AuthenticationException) {
      throw CommonException(CommonExceptionCode.LOGIN_FAIL)
    }
  }
}