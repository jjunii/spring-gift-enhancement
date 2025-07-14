package gift;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gift.dto.ProductRequestDto;
import gift.dto.ProductResponseDto;
import gift.entity.ProductStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductTest {

    @LocalServerPort
    private int port;

    private RestClient client = RestClient.builder().build();

    @Autowired
    private JdbcClient jdbcClient;

    @ParameterizedTest
    @ValueSource(strings = {"이름이 15자를 초과하는 경우", "잘못된 특수문자 #"})
    void 상품명_유효성_검증_실패(String invalidName) {
        String url = "http://localhost:" + port + "/api/products";
        ProductRequestDto productRequestDto = new ProductRequestDto(invalidName, 10000,
                "https://example.com/image.jpg");

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.BadRequest.class,
                () -> client.post()
                            .uri(url)
                            .body(productRequestDto)
                            .retrieve()
                            .toBodilessEntity()
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void 카카오_미포함_상품명_등록시_승인() {
        String url = "http://localhost:" + port + "/api/products";
        ProductRequestDto productRequestDto = new ProductRequestDto("상품", 10000,
                "https://example.com/image.jpg");

        ResponseEntity<ProductResponseDto> responseEntity = client.post()
                                                                  .uri(url)
                                                                  .body(productRequestDto)
                                                                  .retrieve()
                                                                  .toEntity(
                                                                          ProductResponseDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody().status()).isEqualTo(ProductStatus.APPROVED);
    }

    @Test
    void 카카오_포함_상품명_등록시_승인대기() {
        String url = "http://localhost:" + port + "/api/products";
        ProductRequestDto productRequestDto = new ProductRequestDto("카카오상품", 10000,
                "https://example.com/image.jpg");

        ResponseEntity<ProductResponseDto> responseEntity = client.post()
                                                                  .uri(url)
                                                                  .body(productRequestDto)
                                                                  .retrieve()
                                                                  .toEntity(
                                                                          ProductResponseDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody().status()).isEqualTo(ProductStatus.PENDING_APPROVAL);

    }

    @Test
    void 카카오_포함_상품명_수정시_승인대기로_변경() {
        String url = "http://localhost:" + port + "/api/products/{productId}";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(
                          "INSERT INTO product (name, price, image_url, status) VALUES ('상품', 10000, 'https://example.com/image.jpg', 'APPROVED')")
                  .update(keyHolder);

        ProductRequestDto productRequestDto = new ProductRequestDto("카카오 상품", 15000,
                "http://example.com/new.jpg");

        ResponseEntity<ProductResponseDto> responseEntity = client.put()
                                                                  .uri(url, keyHolder.getKey()
                                                                                     .longValue())
                                                                  .body(productRequestDto)
                                                                  .retrieve()
                                                                  .toEntity(
                                                                          ProductResponseDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().status()).isEqualTo(ProductStatus.PENDING_APPROVAL);
    }

    @Test
    void 관리자가_상품상태_변경() {
        String updateStatusUrl =
                "http://localhost:" + port + "/admin/products/{id}/status?status={status}";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(
                          "INSERT INTO product (name, price, image_url, status) VALUES ('카카오 상품', 10000, 'https://example.com/image.jpg', 'PENDING_APPROVAL')")
                  .update(keyHolder);

        ResponseEntity<Void> updateStatusResponseEntity = client.patch()
                                                                .uri(updateStatusUrl,
                                                                        keyHolder.getKey()
                                                                                 .longValue(),
                                                                        ProductStatus.APPROVED)
                                                                .retrieve()
                                                                .toBodilessEntity();

        assertThat(updateStatusResponseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);

        String findProductUrl = "http://localhost:" + port + "/api/products/{productId}";

        ResponseEntity<ProductResponseDto> findProductResponseEntity = client.get()
                                                                             .uri(findProductUrl,
                                                                                     keyHolder.getKey()
                                                                                              .longValue())
                                                                             .retrieve()
                                                                             .toEntity(
                                                                                     ProductResponseDto.class);

        assertThat(findProductResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(findProductResponseEntity.getBody().status()).isEqualTo(ProductStatus.APPROVED);
    }

}
