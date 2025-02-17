package com.project.frankit.domain.member

import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository: JpaRepository<Member, Long> {
  fun findByEmail(email: String): Member?
}