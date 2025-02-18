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
   * (Role => 가맹 점주 : FRANCHISEE, 프랜차이즈 대표 : FRANCHISE_OWNER)
   *
   * - 설명
   *  1. email 에 대해 중복된 ID 가 있는지 체크한다. - (있을 시 Exception 발생)
   *  2. Member 를 생성 한다. (비밀번호는 BCrypt 암호화)
   *  3. 해당 Member 를 저장 후 해당 결과값 String 을 Return 한다.
   */
  fun signUp(memberRq: MemberRq): String {
    var member: Member? = memberCRUD.findDuplicateId(memberRq.email)
    if (member != null) throw CommonException(CommonExceptionCode.DUPLICATE_ID)

    member = Member.createMember(memberRq, passwordEncoder.encode(memberRq.password))
    memberCRUD.appendMember(member)

    return "회원가입이 완료되었습니다."
  }

  /**
   * 로그인 (토큰 발행)
   *
   * - 설명
   *  1. 로그인 할 ID/PW 를 UsernamePasswordAuthenticationToken 통해 인증 준비한다.
   *  2. AuthenticationManager 를 통해 준비된 값을 인증한다. - (실패 시 발생하는 Exception 을 try Catch 로 잡아 CustomException 으로 발생시킨다.)
   *  3. 인증된 값을 JWT 를 통해 토큰 발급 하며 토큰값을 Return 한다.
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