package com.project.frankit.common.authority

import com.project.frankit.common.response.CustomMember
import com.project.frankit.domain.member.Member
import com.project.frankit.domain.member.MemberRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomMemberDetailsService(
    private val memberRepository: MemberRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails =
        memberRepository.findByEmail(username)
            ?.let { createUserDetails(it) } ?: throw UsernameNotFoundException("해당 유저는 없습니다.")

    private fun createUserDetails(member: Member): UserDetails =
        CustomMember(
            member.sn!!,
            member.email,
            member.password,
            listOf(SimpleGrantedAuthority(member.role.toString()))
        )
}