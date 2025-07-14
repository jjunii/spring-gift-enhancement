package gift.config;

import gift.entity.MemberRole;
import gift.exception.UnAuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private static final long EXPIRE_TIME = 60 * 60 * 1000L;

    @Value("${jwt.secret.key}")
    private String secretKey;

    public String generateToken(Long id, MemberRole role) {

        Instant now = Instant.now();
        Date expirationDate = Date.from(now.plusMillis(EXPIRE_TIME));

        return Jwts.builder()
                   .subject(id.toString())
                   .claim("role", role.name())
                   .issuer("spring-gift")
                   .expiration(expirationDate)
                   .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                   .compact();
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                       .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                       .requireIssuer("spring-gift")
                       .build()
                       .parseSignedClaims(token)
                       .getPayload();
        } catch (RuntimeException e) {
            throw new UnAuthenticationException("로그인 정보가 유효하지 않습니다.");
        }
    }

    public Long getMemberIdFromToken(String token) {
        return Long.valueOf(getClaims(token).getSubject());
    }
}
