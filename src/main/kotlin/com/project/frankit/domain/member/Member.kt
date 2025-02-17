package com.project.frankit.domain.member

import com.fasterxml.jackson.annotation.JsonIgnore
import com.project.frankit.domain.member.enums.Role
import com.project.frankit.domain.member.rqrs.MemberRq
import jakarta.persistence.*

@Entity
@Table(name = "member")
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn")
    val sn: Long? = null,

    @Column(name = "email", unique = true, nullable = false)
    val email: String,

    @JsonIgnore
    @Column(name = "password", nullable = false)
    val password: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "store_name", nullable = false)
    val storeName: String,

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    val role: Role,

    ) {
    companion object {
        fun createMember(memberRq: MemberRq, encryptedPassword: String): Member {
            return Member(
                email = memberRq.email,
                password = encryptedPassword,
                name = memberRq.name,
                storeName = memberRq.storeName,
                role = memberRq.role
            )
        }
    }
}