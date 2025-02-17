package com.project.frankit.common.response

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

class CustomMember(
    val sn: Long,
    email: String,
    password: String,
    authorities: Collection<GrantedAuthority>
) : User(email, password, authorities)