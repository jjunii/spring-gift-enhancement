package gift.Option;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import gift.dto.OptionRequestDto;
import gift.entity.Option;
import gift.entity.Product;
import gift.entity.ProductStatus;
import gift.exception.CannotDeleteLastOptionException;
import gift.exception.OptionNameAlreadyExistsException;
import gift.exception.PermissionDeniedException;
import gift.repository.OptionRepository;
import gift.service.OptionService;
import gift.service.ProductService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OptionServiceTest {

    @Mock
    private ProductService productService;

    @Mock
    private OptionRepository optionRepository;

    @InjectMocks
    private OptionService optionService;

    private Product testProduct;
    private Option testOption1;
    private Option testOption2;

    @BeforeEach
    void setUp() {
        testProduct = new Product("테스트 상품", 1000, "url", ProductStatus.APPROVED);
        ReflectionTestUtils.setField(testProduct, "id", 1L);

        testOption1 = new Option("옵션1", 10);
        ReflectionTestUtils.setField(testOption1, "id", 101L);
        testOption1.setProduct(testProduct);

        testOption2 = new Option("옵션2", 20);
        ReflectionTestUtils.setField(testOption2, "id", 102L);
        testOption2.setProduct(testProduct);
    }

    @Test
    void 이름중복으로_추가_실패() {
        Long productId = 1L;
        OptionRequestDto optionRequestDto = new OptionRequestDto("중복된 이름", 10);

        given(productService.findProductOrThrow(productId)).willReturn(testProduct);
        given(optionRepository.existsByProductIdAndName(productId, "중복된 이름")).willReturn(true);

        assertThrows(OptionNameAlreadyExistsException.class, () -> {
            optionService.addOption(productId, optionRequestDto);
        });
    }

    @Test
    void 권한없음으로_옵션_수정_실패() {
        Long wrongProductId = 999L;
        Long optionId = 101L;
        OptionRequestDto optionRequestDto = new OptionRequestDto("수정된 이름", 15);

        given(optionRepository.findById(optionId)).willReturn(Optional.of(testOption1));

        assertThrows(PermissionDeniedException.class, () -> {
            optionService.updateOption(wrongProductId, optionId, optionRequestDto);
        });
    }

    @Test
    void 이름중복으로_옵션_수정_실패() {
        Long productId = 1L;
        Long optionId = 101L;
        OptionRequestDto optionRequestDto = new OptionRequestDto("옵션2", 15);

        given(optionRepository.findById(optionId)).willReturn(Optional.of(testOption1));
        given(optionRepository.existsByProductIdAndNameAndIdNot(productId, "옵션2",
                optionId)).willReturn(true);

        assertThrows(OptionNameAlreadyExistsException.class, () -> {
            optionService.updateOption(productId, optionId, optionRequestDto);
        });
    }

    @Test
    void 마지막옵션으로_옵션_삭제_실패() {
        Long productId = 1L;
        Long optionId = 101L;
        testProduct.addOption(testOption1);

        given(optionRepository.findById(optionId)).willReturn(Optional.of(testOption1));

        assertThrows(CannotDeleteLastOptionException.class, () -> {
            optionService.deleteOption(productId, optionId);
        });
    }

    @Test
    void 권한없음으로_옵션_삭제_실패() {
        Long wrongProductId = 999L;
        Long optionId = 101L;

        given(optionRepository.findById(optionId)).willReturn(Optional.of(testOption1));

        assertThrows(PermissionDeniedException.class, () -> {
            optionService.deleteOption(wrongProductId, optionId);
        });
    }
}
