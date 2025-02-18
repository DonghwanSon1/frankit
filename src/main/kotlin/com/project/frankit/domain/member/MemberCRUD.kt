package com.project.frankit.domain.member


import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class MemberCRUD(
  private val memberRepository: MemberRepository,
) {

  /**
   * append
   */
  // 회원 저장
  @Transactional
  fun appendMember(member: Member): Member {
    return memberRepository.save(member)
  }


  /**
   * find
   */
  // 회원 ID 중복 조회
  fun findDuplicateId(email: String): Member? {
    return memberRepository.findByEmail(email)
  }
}