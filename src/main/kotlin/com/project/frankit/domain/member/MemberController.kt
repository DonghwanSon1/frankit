package com.project.frankit.domain.member

import com.project.frankit.common.authority.TokenInfo
import com.project.frankit.common.response.BaseResponse
import com.project.frankit.domain.member.rqrs.LoginRq
import com.project.frankit.domain.member.rqrs.MemberRq
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/member")
@Tag(name = "Member", description = "회원 관련 API")
class MemberController(
  private val memberService: MemberService,
) {

  /**
   * 회원가입 API
   */
  @PostMapping("/signup")
  @Operation(summary = "회원 가입", description = "회원 가입")
  fun signUp(@RequestBody @Valid memberRq: MemberRq): BaseResponse<Unit> {
    val resultMsg: String = memberService.signUp(memberRq)
    return BaseResponse(message = resultMsg)
  }

  /**
   * 로그인 (토큰 발급) API
   */
  @PostMapping("/login")
  @Operation(summary = "로그인", description = "로그인")
  fun login(@RequestBody @Valid loginRq: LoginRq): BaseResponse<TokenInfo> {
    val tokenInfo = memberService.login(loginRq)
    return BaseResponse(data = tokenInfo)
  }

}