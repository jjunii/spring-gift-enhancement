package gift.config;

import gift.dto.AuthenticatedMemberDto;
import gift.entity.Member;
import gift.exception.UnAuthenticationException;
import gift.service.MemberService;
import gift.util.CurrentMemberContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";

    private final JwtProvider jwtProvider;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final MemberService memberService;

    public JwtAuthenticationFilter(
            JwtProvider jwtProvider,
            HandlerExceptionResolver handlerExceptionResolver,
            MemberService memberService) {
        this.jwtProvider = jwtProvider;
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.memberService = memberService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (authorization == null || !authorization.startsWith(BEARER)) {
                throw new UnAuthenticationException("토큰 형식이 올바르지 않습니다.");
            }

            String token = authorization.substring(BEARER.length());
            Long memberId = jwtProvider.getMemberIdFromToken(token);

            Member authenticatedMember = memberService.getMemberById(memberId)
                                                      .orElseThrow(
                                                              () -> new UnAuthenticationException(
                                                                      "인증되지 않은 사용자입니다"));

            CurrentMemberContext.setAuthenticatedMember(
                    AuthenticatedMemberDto.from(authenticatedMember));

            filterChain.doFilter(request, response);
        } catch (UnAuthenticationException e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        } finally {
            CurrentMemberContext.clear();
        }
    }
}
