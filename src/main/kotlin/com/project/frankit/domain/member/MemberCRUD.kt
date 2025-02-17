package com.project.frankit.domain.member


import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Component

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
  fun appendUser(member: Member): Member {
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