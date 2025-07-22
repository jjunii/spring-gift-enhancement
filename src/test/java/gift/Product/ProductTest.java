package gift.Product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gift.dto.OptionRequestDto;
import gift.dto.ProductCreateRequestDto;
import gift.dto.ProductResponseDto;
import gift.dto.ProductUpdateRequestDto;
import gift.entity.Option;
import gift.entity.Product;
import gift.entity.ProductStatus;
import gift.repository.ProductRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private ProductRepository productRepository;

    @ParameterizedTest
    @ValueSource(strings = {"이름이 15자를 초과하는 경우", "잘못된 특수문자 #"})
    void 상품명_유효성_검증_실패(String invalidName) {
        String url = "http://localhost:" + port + "/api/products";
        List<OptionRequestDto> options = List.of(new OptionRequestDto("기본", 10));
        ProductCreateRequestDto productCreateDto = new ProductCreateRequestDto(invalidName, 10000,
                "https://example.com/image.jpg", options);

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.BadRequest.class,
                () -> client.post()
                            .uri(url)
                            .body(productCreateDto)
                            .retrieve()
                            .toBodilessEntity()
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void 카카오_미포함_상품명_등록시_승인() {
        String url = "http://localhost:" + port + "/api/products";
        List<OptionRequestDto> options = List.of(new OptionRequestDto("기본", 10));
        ProductCreateRequestDto productCreateDto = new ProductCreateRequestDto("상품", 10000,
                "https://example.com/image.jpg", options);

        ResponseEntity<ProductResponseDto> responseEntity = client.post()
                                                                  .uri(url)
                                                                  .body(productCreateDto)
                                                                  .retrieve()
                                                                  .toEntity(
                                                                          ProductResponseDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody().status()).isEqualTo(ProductStatus.APPROVED);
    }

    @Test
    void 카카오_포함_상품명_등록시_승인대기() {
        String url = "http://localhost:" + port + "/api/products";
        List<OptionRequestDto> options = List.of(new OptionRequestDto("기본", 10));
        ProductCreateRequestDto productCreateDto = new ProductCreateRequestDto("카카오상품", 10000,
                "https://example.com/image.jpg", options);

        ResponseEntity<ProductResponseDto> responseEntity = client.post()
                                                                  .uri(url)
                                                                  .body(productCreateDto)
                                                                  .retrieve()
                                                                  .toEntity(
                                                                          ProductResponseDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody().status()).isEqualTo(ProductStatus.PENDING_APPROVAL);

    }

    @Test
    void 카카오_포함_상품명_수정시_승인대기로_변경() {
        String url = "http://localhost:" + port + "/api/products/{productId}";
        Product initialProduct = new Product("상품", 10000, "https://example.com/image.jpg",
                ProductStatus.APPROVED);
        initialProduct.addOption(new Option("기본", 1));
        Product savedProduct = productRepository.save(initialProduct);

        ProductUpdateRequestDto productUpdateDto = new ProductUpdateRequestDto("카카오 상품", 15000,
                "http://example.com/new.jpg", null);

        ResponseEntity<ProductResponseDto> responseEntity = client.put()
                                                                  .uri(url, savedProduct.getId())
                                                                  .body(productUpdateDto)
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
        Product initialProduct = new Product("카카오 상품", 10000, "https://example.com/image.jpg",
                ProductStatus.PENDING_APPROVAL);
        initialProduct.addOption(new Option("기본", 1));
        Product savedProduct = productRepository.save(initialProduct);

        ResponseEntity<Void> updateStatusResponseEntity = client.patch()
                                                                .uri(updateStatusUrl,
                                                                        savedProduct.getId(),
                                                                        ProductStatus.APPROVED)
                                                                .retrieve()
                                                                .toBodilessEntity();

        assertThat(updateStatusResponseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);

        String findProductUrl = "http://localhost:" + port + "/api/products/{productId}";

        ResponseEntity<ProductResponseDto> findProductResponseEntity = client.get()
                                                                             .uri(findProductUrl,
                                                                                     savedProduct.getId())
                                                                             .retrieve()
                                                                             .toEntity(
                                                                                     ProductResponseDto.class);

        assertThat(findProductResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(findProductResponseEntity.getBody().status()).isEqualTo(ProductStatus.APPROVED);
    }

}
