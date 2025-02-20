package com.project.frankit.member

import com.project.frankit.common.authority.JwtTokenProvider
import com.project.frankit.common.exception.CommonException
import com.project.frankit.common.exception.CommonExceptionCode
import com.project.frankit.common.response.CustomMember
import com.project.frankit.domain.member.Member
import com.project.frankit.domain.member.MemberRepository
import com.project.frankit.domain.member.MemberService
import com.project.frankit.domain.member.enums.Role
import com.project.frankit.domain.member.rqrs.LoginRq
import com.project.frankit.domain.member.rqrs.MemberRq
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
class MemberServiceIntegrationTest @Autowired constructor(
  private val memberService: MemberService,
  private val memberRepository: MemberRepository,
  private val jwtTokenProvider: JwtTokenProvider,
  private val passwordEncoder: PasswordEncoder,
) {

  @AfterEach
  fun clean() {
    memberRepository.deleteAll()
  }

  /**
   * 가맹 점주 회원가입
   * - given / when / then
   *    1. 회원가입 Rq를 생성한다.
   *    2. 회원가입 메서드를 통해 회원가입을 요청한다.
   *    3. 멤버 테이블을 조회한 후 Rq와 비교한다.
   *
   * - 테스트 확인
   *    1. 저장된게 하나인지 확인.
   *    2. Rq 에서의 email 과 저장된 값에서 email 이 일치하는지 확인.
   *    3. 저장된 Password(암호화 된) 를 가져오고, Rq로 보낸 평문이 아닌지 확인
   *    4. 저장된 이름이 Rq의 이름과 일치하는지 확인.
   *    5. 저장된 가게 이름이 Rq의 가게이름과 일치하는지 확인.
   *    6. 저장된 역할이 Rq의 역할과 일치하는지 확인.
   */
  @Test
  fun `개인 회원 - 회원 가입`() {
    // given
    val memberRq = MemberRq(_email = "testMember@naver.com", password = "admin", name = "김개인", storeName = "프랜킷_봉천점", role = Role.FRANCHISEE)

    // when
    memberService.signUp(memberRq)

    // then
    val member = memberRepository.findAll()
    assertThat(member).hasSize(1)
    assertThat(member[0].email).isEqualTo(memberRq.email)
    assertThat(member[0].password).isNotEqualTo(memberRq.password)
    assertThat(member[0].name).isEqualTo(memberRq.name)
    assertThat(member[0].storeName).isEqualTo(memberRq.storeName)
    assertThat(member[0].role).isEqualTo(memberRq.role)
  }

  /**
   * 프렌차이즈 대표 회원가입
   * - given / when / then
   *    1. 회원가입 Rq를 생성한다.
   *    2. 회원가입 메서드를 통해 회원가입을 요청한다.
   *    3. 멤버 테이블을 조회한 후 Rq와 비교한다.
   *
   * - 테스트 확인
   *    1. 저장된게 하나인지 확인.
   *    2. Rq 에서의 email 과 저장된 값에서 email 이 일치하는지 확인.
   *    3. 저장된 Password(암호화 된) 를 가져오고, Rq로 보낸 평문이 아닌지 확인
   *    4. 저장된 이름이 Rq의 이름과 일치하는지 확인.
   *    5. 저장된 가게 이름이 Rq의 가게이름과 일치하는지 확인.
   *    6. 저장된 역할이 Rq의 역할과 일치하는지 확인.
   */
  @Test
  fun `법인 회원 - 회원 가입`() {
    // given
    val memberRq = MemberRq(_email = "testAdmin@naver.com", password = "admin", name = "김대표", storeName = "프랜킷", role = Role.FRANCHISE_OWNER)

    // when
    memberService.signUp(memberRq)

    // then
    val member = memberRepository.findAll()
    assertThat(member).hasSize(1)
    assertThat(member[0].email).isEqualTo(memberRq.email)
    assertThat(member[0].password).isNotEqualTo(memberRq.password)
    assertThat(member[0].name).isEqualTo(memberRq.name)
    assertThat(member[0].storeName).isEqualTo(memberRq.storeName)
    assertThat(member[0].role).isEqualTo(memberRq.role)
  }

  /**
   * 회원 가입 실패 - 중복 ID
   * - given / when / then
   *    1. 먼저 멤버를 저장한다.
   *    2. 저장한 멤버와 동일한 email 을 가진 Rq를 생성한다.
   *    3. 회원가입 메서드를 통해 회원가입을 요청한다.
   *
   * - 테스트 확인
   *    1. 중복 ID(Email) 회원가입 시 발생하는 Exception 의 Message 를 가져온 후 발생된 Message 가 일치 하는지 확인.
   */
  @Test
  fun `회원 가입 실패 - 중복 Email`() {
    // given
    memberRepository.save(
      Member(email = "testMember@naver.com", password = "admin", name = "김개인", storeName = "프랜킷_봉천점", role = Role.FRANCHISEE)
    )
    val memberRq = MemberRq(_email = "testMember@naver.com", password = "admin123", name = "김아무개", storeName = "프랜킷_봉천점", role = Role.FRANCHISEE)

    // when & then
    val message = assertThrows<CommonException> {
      memberService.signUp(memberRq)
    }.message

    assertThat(message).isEqualTo(CommonExceptionCode.DUPLICATE_ID.message)
  }

  /**
   * 로그인
   * - given / when / then
   *    1. 로그인 할 멤버를 먼저 저장한다.
   *    2. memberRq 를 저장한 멤버의 내용으로 Rq 를 생성한다.
   *    3. 로그인 메서드를 통해 로그인 요청한다.
   *
   * - 테스트 확인
   *    1. 결과값이 isNotNull 인지 확인.
   *    2. 결과값의 token 이 비어 있지 않은걸 확인.
   *
   */
  @Test
  fun `로그인`() {
    // given
    memberRepository.save(
      Member(
        email = "testMember@naver.com",
        password = passwordEncoder.encode("admin"),
        name = "김개인",
        storeName = "프랜킷_봉천점",
        role = Role.FRANCHISEE
      )
    )
    val loginRq = LoginRq(_email = "testMember@naver.com", password = "admin")

    // when
    val result= memberService.login(loginRq)

    // then
    assertThat(result).isNotNull
    assertThat(result.token).isNotEmpty()
  }

  /**
   * 로그인 토큰 정보 검증
   * - given / when / then
   *    1. 로그인 할 멤버를 먼저 저장한다.
   *    2. loginRq 를 저장한 멤버의 내용으로 Rq 를 생성한다.
   *    3. 로그인 메서드를 통해 로그인 요청한다.
   *    4. 로그인 시 받은 토큰을 가지고 CustomMember 로 가져온다.
   *
   * - 테스트 확인
   *    1. Token 의 정보를 추출해서 ID 가 동일한지 확인.
   *    2. Token 의 정보를 추출해서 Role 이 동일한지 확인.
   *
   */
  @Test
  fun `로그인 토큰 정보 검증`() {
    // given
    val member: Member = memberRepository.save(
      Member(
        email = "testMember@naver.com",
        password = passwordEncoder.encode("admin"),
        name = "김개인",
        storeName = "프랜킷_봉천점",
        role = Role.FRANCHISEE
      )
    )
    val loginRq = LoginRq(_email = "testMember@naver.com", password = "admin")

    // when
    val result= memberService.login(loginRq)
    val authentication = jwtTokenProvider.getAuthentication(result.token)
    val customMember: CustomMember = authentication.principal as CustomMember

    // then
    assertThat(customMember.sn).isEqualTo(member.sn)
    assertThat(customMember.authorities.first().toString()).isEqualTo(Role.FRANCHISEE.name)
  }

  /**
   * 로그인 실패 - 인증 실패
   * - given / when / then
   *    1. 로그인 할 멤버를 먼저 저장한다.
   *    2. 로그인 실패 할 멤버를 loginRq를 생성한다. - (비밀번호 틀림)
   *    3. 로그인 메서드를 통해 로그인을 요청한다.
   *
   * - 테스트 확인
   *    1. 로그인 실패 시 발생하는 Exception 의 Message 를 가져온 후 발생된 Message 가 일치 하는지 확인.
   */
  @Test
  fun `로그인 실패 - 인증 실패`() {
    // given
    memberRepository.save(
      Member(
        email = "testMember@naver.com",
        password = passwordEncoder.encode("admin"),
        name = "김개인",
        storeName = "프랜킷_봉천점",
        role = Role.FRANCHISEE
      )
    )
    val loginRq = LoginRq(_email = "testUser@naver.com", password = "admin123123")

    // when & then
    val message = assertThrows<CommonException> {
      memberService.login(loginRq)
    }.message

    assertThat(message).isEqualTo(CommonExceptionCode.LOGIN_FAIL.message)
  }
}