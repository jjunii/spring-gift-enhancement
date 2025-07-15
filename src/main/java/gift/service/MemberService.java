package gift.service;

import gift.config.JwtProvider;
import gift.dto.MemberRequestDto;
import gift.dto.TokenResponseDto;
import gift.entity.Member;
import gift.entity.MemberRole;
import gift.exception.EmailAlreadyExistsException;
import gift.exception.LoginFailedException;
import gift.repository.MemberRepository;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    public MemberService(MemberRepository memberRepository, JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
    }

    @Transactional
    public TokenResponseDto registerMember(MemberRequestDto memberRequestDto) {
        if (memberRepository.findByEmail(memberRequestDto.email()).isPresent()) {
            throw new EmailAlreadyExistsException(memberRequestDto.email());
        }

        Member member = new Member(
                memberRequestDto.email(),
                BCrypt.hashpw(memberRequestDto.password(), BCrypt.gensalt()),
                MemberRole.ROLE_USER
        );

        Member savedMember = memberRepository.save(member);

        return new TokenResponseDto(
                jwtProvider.generateToken(savedMember.getId(), savedMember.getRole()));
    }

    @Transactional(readOnly = true)
    public TokenResponseDto loginMember(MemberRequestDto memberRequestDto) {
        Member member = memberRepository.findByEmail(memberRequestDto.email())
                                        .orElseThrow(LoginFailedException::new);

        if (!BCrypt.checkpw(memberRequestDto.password(), member.getPassword())) {
            throw new LoginFailedException();
        }

        return new TokenResponseDto(jwtProvider.generateToken(member.getId(), member.getRole()));
    }

    @Transactional(readOnly = true)
    public Optional<Member> getMemberById(Long id) {

        return memberRepository.findById(id);
    }
}
