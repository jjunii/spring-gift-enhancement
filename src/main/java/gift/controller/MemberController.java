package gift.controller;

import gift.dto.MemberRequestDto;
import gift.dto.TokenResponseDto;
import gift.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 회원 가입
    @PostMapping("/register")
    public ResponseEntity<TokenResponseDto> registerMember(
            @Valid @RequestBody MemberRequestDto memberRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(memberService.registerMember(memberRequestDto));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> loginMember(
            @Valid @RequestBody MemberRequestDto memberRequestDto) {
        return ResponseEntity.ok(memberService.loginMember(memberRequestDto));
    }
}
