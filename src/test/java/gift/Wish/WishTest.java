package gift.Wish;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import gift.config.JwtProvider;
import gift.dto.WishRequestDto;
import gift.dto.WishResponseDto;
import gift.entity.MemberRole;
import gift.entity.ProductStatus;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WishTest {

    @LocalServerPort
    private int port;

    private RestClient client = RestClient.builder().build();

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private JwtProvider jwtProvider;

    @Test
    void 상품_추가_성공() {
        String url = "http://localhost:" + port + "/api/wishes";
        Long memberId = setUpMember("user@email.com", "password", MemberRole.ROLE_USER);
        Long productId = setUpProduct("상품", 1000,
                "https://example.com/image.jpg", ProductStatus.APPROVED);
        String token = jwtProvider.generateToken(memberId, MemberRole.ROLE_USER);
        WishRequestDto requestDto = new WishRequestDto(productId);

        ResponseEntity<WishResponseDto> responseEntity = client.post()
                                                               .uri(url)
                                                               .header("Authorization",
                                                                       "Bearer " + token)
                                                               .body(requestDto)
                                                               .retrieve()
                                                               .toEntity(WishResponseDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().product().id()).isEqualTo(productId);
    }

    @Test
    void 존재하지_않는_상품_ID로_추가_실패() {
        String url = "http://localhost:" + port + "/api/wishes";
        Long memberId = setUpMember("user@email.com", "password", MemberRole.ROLE_USER);
        String token = jwtProvider.generateToken(memberId, MemberRole.ROLE_USER);
        Long wrongProductId = 100L;
        WishRequestDto requestDto = new WishRequestDto(wrongProductId);

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> client.post()
                            .uri(url)
                            .header("Authorization", "Bearer " + token)
                            .body(requestDto)
                            .retrieve()
                            .toBodilessEntity()
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void 상품_중복_추가_실패() {
        String url = "http://localhost:" + port + "/api/wishes";
        Long memberId = setUpMember("user@email.com", "password", MemberRole.ROLE_USER);
        Long productId = setUpProduct("상품", 1000,
                "https://example.com/image.jpg", ProductStatus.APPROVED);
        addWishToDb(memberId, productId);
        String token = jwtProvider.generateToken(memberId, MemberRole.ROLE_USER);
        WishRequestDto requestDto = new WishRequestDto(productId);

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.Conflict.class,
                () -> client.post()
                            .uri(url)
                            .header("Authorization", "Bearer " + token)
                            .body(requestDto)
                            .retrieve()
                            .toBodilessEntity()
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }


    @Test
    void 상품_목록_조회_성공() {
        String url = "http://localhost:" + port
                + "/api/wishes?page={page}&size={size}&sort={sortBy}";
        URI uri = UriComponentsBuilder.fromUriString(url)
                                      .build(0, 5, "product_price,desc");
        Long member1Id = setUpMember("user1@email.com", "password1", MemberRole.ROLE_USER);
        Long member2Id = setUpMember("user2@email.com", "password2", MemberRole.ROLE_USER);

        Long product1Id = setUpProduct("상품1", 1000, "https://example.com/image1.jpg",
                ProductStatus.APPROVED);
        Long product2Id = setUpProduct("상품2", 2000, "https://example.com/image2.jpg",
                ProductStatus.APPROVED);
        Long product3Id = setUpProduct("상품3", 3000, "https://example.com/image3.jpg",
                ProductStatus.APPROVED);

        addWishToDb(member1Id, product1Id);
        addWishToDb(member1Id, product3Id);
        addWishToDb(member2Id, product2Id);

        String token1 = jwtProvider.generateToken(member1Id, MemberRole.ROLE_USER);

        ResponseEntity<PageResponse<WishResponseDto>> responseEntity = client.get()
                                                                             .uri(uri)
                                                                             .header("Authorization",
                                                                                     "Bearer "
                                                                                             + token1)
                                                                             .retrieve()
                                                                             .toEntity(
                                                                                     new ParameterizedTypeReference<>() {
                                                                                     });

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        PageResponse<WishResponseDto> page = responseEntity.getBody();
        assertAll(
                () -> assertThat(page.getTotalElements()).isEqualTo(2),
                () -> assertThat(page.getNumber()).isEqualTo(0),
                () -> assertThat(page.getSize()).isEqualTo(5),
                () -> assertThat(page.getContent()).hasSize(2),
                () -> assertThat(page.getContent().get(0).product().price()).isEqualTo(3000)
        );
    }

    @Test
    void 상품_삭제_성공() {
        String url = "http://localhost:" + port + "/api/wishes";
        Long memberId = setUpMember("user@email.com", "password", MemberRole.ROLE_USER);
        Long productId = setUpProduct("상품", 1000,
                "https://example.com/image.jpg", ProductStatus.APPROVED);
        Long wishId = addWishToDb(memberId, productId);
        String token = jwtProvider.generateToken(memberId, MemberRole.ROLE_USER);

        ResponseEntity<Void> responseEntity = client.delete()
                                                    .uri(url + "/{wishId}", wishId)
                                                    .header("Authorization", "Bearer " + token)
                                                    .retrieve()
                                                    .toBodilessEntity();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Integer count = jdbcClient.sql("SELECT COUNT(*) FROM wish WHERE id = :id")
                                  .param("id", wishId)
                                  .query(Integer.class)
                                  .single();
        assertThat(count).isZero();
    }

    @Test
    void 존재하지_않는_wishId로_삭제_실패() {
        String url = "http://localhost:" + port + "/api/wishes";
        Long memberId = setUpMember("user@email.com", "password", MemberRole.ROLE_USER);
        String token = jwtProvider.generateToken(memberId, MemberRole.ROLE_USER);
        Long wrongWishId = 100L;

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> client.delete()
                            .uri(url + "/{wishId}", wrongWishId)
                            .header("Authorization", "Bearer " + token)
                            .retrieve()
                            .toBodilessEntity()
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void 다른_memberId로_삭제_실패() {
        String url = "http://localhost:" + port + "/api/wishes";
        Long member1Id = setUpMember("user1@email.com", "password1", MemberRole.ROLE_USER);
        Long member2Id = setUpMember("user2@email.com", "password2", MemberRole.ROLE_USER);

        Long productId = setUpProduct("상품", 1000, "https://example.com/image.jpg",
                ProductStatus.APPROVED);
        Long wishId = addWishToDb(member1Id, productId);

        String tokenByMember2 = jwtProvider.generateToken(member2Id, MemberRole.ROLE_USER);

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.Forbidden.class,
                () -> client.delete()
                            .uri(url + "/{wishId}", wishId)
                            .header("Authorization", "Bearer " + tokenByMember2)
                            .retrieve()
                            .toBodilessEntity()
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void 토큰_없이_인증_실패() {
        String url = "http://localhost:" + port + "/api/wishes";
        WishRequestDto requestDto = new WishRequestDto(1L);

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.Unauthorized.class,
                () -> client.post()
                            .uri(url)
                            .body(requestDto)
                            .retrieve()
                            .toBodilessEntity()
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void 유효하지_않은_토큰으로_인증_실패() {
        String url = "http://localhost:" + port + "/api/wishes";
        String wrongToken = "12345";
        WishRequestDto requestDto = new WishRequestDto(1L);

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.Unauthorized.class,
                () -> client.post()
                            .uri(url)
                            .header("Authorization", "Bearer " + wrongToken)
                            .body(requestDto)
                            .retrieve()
                            .toBodilessEntity()
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private Long setUpMember(String email, String password, MemberRole role) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(
                          "INSERT INTO member (email, password, role) VALUES (:email, :hashedPassword, :role)")
                  .param("email", email)
                  .param("hashedPassword", BCrypt.hashpw("password", BCrypt.gensalt()))
                  .param("role", role.name())
                  .update(keyHolder);

        return keyHolder.getKey().longValue();
    }

    private Long setUpProduct(String name, int price, String imageUrl, ProductStatus status) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(
                          "INSERT INTO product (name, price, image_url, status) VALUES (:name, :price, :imageUrl, :status)")
                  .param("name", name)
                  .param("price", price)
                  .param("imageUrl", imageUrl)
                  .param("status", status.name())
                  .update(keyHolder);

        return keyHolder.getKey().longValue();
    }

    private Long addWishToDb(Long memberId, Long productId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("INSERT INTO wish (member_id, product_id) VALUES (:memberId, :productId)")
                  .param("memberId", memberId)
                  .param("productId", productId)
                  .update(keyHolder);

        return keyHolder.getKey().longValue();
    }

    static class PageResponse<T> extends PageImpl<T> {

        @JsonCreator(mode = Mode.PROPERTIES)
        public PageResponse(@JsonProperty("content") List<T> content,
                @JsonProperty("number") int number,
                @JsonProperty("size") int size,
                @JsonProperty("totalElements") Long totalElements,
                @JsonProperty("pageable") JsonNode pageable,
                @JsonProperty("last") boolean last,
                @JsonProperty("totalPages") int totalPages,
                @JsonProperty("sort") JsonNode sort,
                @JsonProperty("first") boolean first,
                @JsonProperty("numberOfElements") int numberOfElements
        ) {
            super(content, PageRequest.of(number, size), totalElements);
        }
    }
}
