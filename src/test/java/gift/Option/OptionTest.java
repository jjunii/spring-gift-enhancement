package gift.Option;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import gift.dto.OptionRequestDto;
import gift.dto.OptionResponseDto;
import gift.entity.Option;
import gift.entity.Product;
import gift.entity.ProductStatus;
import gift.repository.ProductRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OptionTest {

    @LocalServerPort
    private int port;

    private RestClient client = RestClient.builder().build();

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = productRepository.save(
                new Product("테스트 상품", 1000, "https://example.com/image.jpg",
                        ProductStatus.APPROVED));
    }

    @Test
    void 옵션_추가_성공() {
        String url = "http://localhost:" + port + "/api/products/{productId}/options";
        OptionRequestDto optionRequestDto = new OptionRequestDto("새 옵션", 15);

        ResponseEntity<OptionResponseDto> responseEntity = client.post()
                                                                 .uri(url, testProduct.getId())
                                                                 .body(optionRequestDto)
                                                                 .retrieve()
                                                                 .toEntity(OptionResponseDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody().name()).isEqualTo("새 옵션");
    }

    @Test
    void 존재하지_않는_상품으로_옵션_추가_실패() {
        String url = "http://localhost:" + port + "/api/products/{productId}/options";
        OptionRequestDto optionRequestDto = new OptionRequestDto("새 옵션", 15);
        Long wrongId = 100L;

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> client.post()
                            .uri(url, wrongId)
                            .body(optionRequestDto)
                            .retrieve()
                            .toBodilessEntity()
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "이름이 50자를 초과하는 경우 이름이 50자를 초과하는 경우 이름이 50자를 초과하는 경우 이름이 50자를 초과하는 경우",
            "잘못된 특수문자 #"
    })
    void 옵션명_유효성_검증_실패(String invalidName) {
        String url = "http://localhost:" + port + "/api/products/{productId}/options";
        OptionRequestDto optionRequestDto = new OptionRequestDto(invalidName, 15);

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.BadRequest.class,
                () -> client.post()
                            .uri(url, testProduct.getId())
                            .body(optionRequestDto)
                            .retrieve()
                            .toBodilessEntity()
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void 옵션_수정_성공() {
        String url = "http://localhost:" + port + "/api/products/{productId}/options/{optionId}";
        testProduct.addOption(new Option("테스트 옵션", 10));
        Product savedProduct = productRepository.save(testProduct);

        OptionRequestDto optionRequestDto = new OptionRequestDto("새 옵션", 15);

        ResponseEntity<OptionResponseDto> responseEntity = client.put()
                                                                 .uri(
                                                                         url,
                                                                         savedProduct.getId(),
                                                                         savedProduct.getOptions()
                                                                                     .get(0).getId()
                                                                 )
                                                                 .body(optionRequestDto)
                                                                 .retrieve()
                                                                 .toEntity(OptionResponseDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().name()).isEqualTo("새 옵션");
    }

    @Test
    void 존재하지_않는_옵션으로_수정_실패() {
        String url = "http://localhost:" + port + "/api/products/{productId}/options/{optionId}";
        OptionRequestDto optionRequestDto = new OptionRequestDto("새 옵션", 15);
        Long wrongId = 100L;

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> client.put()
                            .uri(url, 1, wrongId)
                            .body(optionRequestDto)
                            .retrieve()
                            .toBodilessEntity()
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void 옵션_삭제_성공() {
        String deleteUrl =
                "http://localhost:" + port + "/api/products/{productId}/options/{optionId}";
        Option option1 = new Option("테스트 옵션1", 10);
        Option option2 = new Option("테스트 옵션2", 20);
        testProduct.addOption(option1);
        testProduct.addOption(option2);

        Product savedProduct = productRepository.save(testProduct);

        assertThat(savedProduct.getOptions().size()).isEqualTo(2);

        ResponseEntity<Void> deleteResponseEntity = client.delete()
                                                          .uri(deleteUrl, savedProduct.getId(),
                                                                  savedProduct.getOptions().get(0)
                                                                              .getId())
                                                          .retrieve()
                                                          .toBodilessEntity();

        assertThat(deleteResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        String findUrl = "http://localhost:" + port + "/api/products/{productId}/options";
        ResponseEntity<PageResponse<OptionResponseDto>> findResponseEntity = client.get()
                                                                                   .uri(findUrl,
                                                                                           savedProduct.getId())
                                                                                   .retrieve()
                                                                                   .toEntity(
                                                                                           new ParameterizedTypeReference<>() {
                                                                                           });

        assertThat(findResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        OptionTest.PageResponse<OptionResponseDto> page = findResponseEntity.getBody();
        assertThat(page.getContent().size()).isEqualTo(1);
        assertThat(page.getContent().get(0).name()).isEqualTo("테스트 옵션2");
    }

    @Test
    void 옵션_목록_조회_성공() {
        String url = "http://localhost:" + port + "/api/products/{productId}/options";
        Option option1 = new Option("테스트 옵션1", 10);
        Option option2 = new Option("테스트 옵션2", 20);
        testProduct.addOption(option1);
        testProduct.addOption(option2);

        Product savedProduct = productRepository.save(testProduct);

        ResponseEntity<PageResponse<OptionResponseDto>> responseEntity = client.get()
                                                                               .uri(url,
                                                                                       savedProduct.getId())
                                                                               .retrieve()
                                                                               .toEntity(
                                                                                       new ParameterizedTypeReference<>() {
                                                                                       });

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        OptionTest.PageResponse<OptionResponseDto> page = responseEntity.getBody();
        assertAll(
                () -> assertThat(page.getContent().size()).isEqualTo(2),
                () -> assertThat(page.getContent().get(0).name()).isEqualTo("테스트 옵션1"),
                () -> assertThat(page.getContent().get(1).name()).isEqualTo("테스트 옵션2")
        );
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