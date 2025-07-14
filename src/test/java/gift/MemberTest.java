package gift;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gift.dto.MemberRequestDto;
import gift.dto.TokenResponseDto;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MemberTest {

    @LocalServerPort
    private int port;

    private RestClient client = RestClient.builder().build();

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void 회원가입_성공() {
        String url = "http://localhost:" + port + "/api/members/register";
        MemberRequestDto memberRequestDto = new MemberRequestDto("user@email.com", "password");

        ResponseEntity<TokenResponseDto> responseEntity = client.post()
                                                                .uri(url)
                                                                .body(memberRequestDto)
                                                                .retrieve()
                                                                .toEntity(
                                                                        TokenResponseDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody().token()).isNotNull();
    }

    @Test
    void 이메일_중복으로_회원가입_실패() {
        String url = "http://localhost:" + port + "/api/members/register";
        jdbcClient.sql(
                          "INSERT INTO member (email, password, role) VALUES ('user@email.com', 'password', 'ROLE_USER')")
                  .update();

        MemberRequestDto memberRequestDto = new MemberRequestDto("user@email.com", "password");

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.Conflict.class,
                () -> client.post()
                            .uri(url)
                            .body(memberRequestDto)
                            .retrieve()
                            .toBodilessEntity()
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void 로그인_성공() {
        String url = "http://localhost:" + port + "/api/members/login";

        jdbcClient.sql(
                          "INSERT INTO member (email, password, role) VALUES ('user@email.com', :hashedPassword, 'ROLE_USER')")
                  .param("hashedPassword", BCrypt.hashpw("password", BCrypt.gensalt()))
                  .update();

        MemberRequestDto memberRequestDto = new MemberRequestDto("user@email.com", "password");

        ResponseEntity<TokenResponseDto> responseEntity = client.post()
                                                                .uri(url)
                                                                .body(memberRequestDto)
                                                                .retrieve()
                                                                .toEntity(
                                                                        TokenResponseDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().token()).isNotNull();
    }

    @Test
    void 존재하지_않는_이메일로_로그인_실패() {
        String url = "http://localhost:" + port + "/api/members/login";

        MemberRequestDto memberRequestDto = new MemberRequestDto("notexist@email.com", "password");

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.Forbidden.class,
                () -> client.post()
                            .uri(url)
                            .body(memberRequestDto)
                            .retrieve()
                            .toBodilessEntity()
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void 비밀번호_불일치로_로그인_실패() {
        String url = "http://localhost:" + port + "/api/members/login";

        jdbcClient.sql(
                          "INSERT INTO member (email, password, role) VALUES ('user@email.com', :hashedPassword, 'ROLE_USER')")
                  .param("hashedPassword", BCrypt.hashpw("password", BCrypt.gensalt()))
                  .update();

        MemberRequestDto memberRequestDto = new MemberRequestDto("user@email.com",
                "wrong_password");

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.Forbidden.class,
                () -> client.post()
                            .uri(url)
                            .body(memberRequestDto)
                            .retrieve()
                            .toBodilessEntity()
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

}
