package gift.Option;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;

import gift.dto.OptionRequestDto;
import gift.dto.OptionResponseDto;
import gift.entity.Option;
import gift.entity.Product;
import gift.entity.ProductStatus;
import gift.exception.CannotDeleteLastOptionException;
import gift.exception.OptionNameAlreadyExistsException;
import gift.exception.PermissionDeniedException;
import gift.repository.OptionRepository;
import gift.service.OptionService;
import gift.service.ProductService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    void 옵션_추가_성공() {
        Long productId = 1L;
        OptionRequestDto optionRequestDto = new OptionRequestDto("새 옵션", 50);

        Option savedOption = new Option("새 옵션", 50);
        ReflectionTestUtils.setField(savedOption, "id", 103L);

        given(productService.findProductOrThrow(productId)).willReturn(testProduct);
        given(optionRepository.existsByProductIdAndName(productId, "새 옵션")).willReturn(false);
        given(optionRepository.save(any(Option.class))).willReturn(savedOption);

        OptionResponseDto optionResponseDto = optionService.addOption(productId, optionRequestDto);

        assertThat(optionResponseDto.id()).isEqualTo(103L);
        assertThat(optionResponseDto.name()).isEqualTo("새 옵션");
        assertThat(optionResponseDto.quantity()).isEqualTo(50);
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
    void 옵션_수정_성공() {
        Long productId = 1L;
        Long optionId = 101L;
        OptionRequestDto optionRequestDto = new OptionRequestDto("수정된 이름", 50);

        given(optionRepository.findById(optionId)).willReturn(Optional.of(testOption1));
        given(optionRepository.existsByProductIdAndNameAndIdNot(productId, "수정된 이름", optionId))
                .willReturn(false);
        given(optionRepository.save(any(Option.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        OptionResponseDto optionResponseDto = optionService.updateOption(productId, optionId,
                optionRequestDto);

        assertThat(optionResponseDto.id()).isEqualTo(101L);
        assertThat(optionResponseDto.name()).isEqualTo("수정된 이름");
        assertThat(optionResponseDto.quantity()).isEqualTo(50);
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
    void 옵션_삭제_성공() {
        Long productId = 1L;
        Long optionId = 101L;
        testProduct.addOption(testOption1);
        testProduct.addOption(testOption2);

        given(optionRepository.findById(optionId)).willReturn(Optional.of(testOption1));
        doNothing().when(optionRepository).deleteById(optionId);

        optionService.deleteOption(productId, optionId);

        then(optionRepository).should().deleteById(optionId);
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

    @Test
    void 옵션_목록_조회_성공() {
        Long productId = 1L;
        Pageable pageable = PageRequest.of(0, 5);

        List<Option> options = List.of(testOption1, testOption2);
        Page<Option> optionpage = new PageImpl<>(options, pageable, options.size());

        given(productService.findProductOrThrow(productId)).willReturn(testProduct);
        given(optionRepository.findAllByProductId(productId, pageable)).willReturn(optionpage);

        Page<OptionResponseDto> responseDtoPage = optionService.getOptionsByProductId(productId,
                pageable);

        assertThat(responseDtoPage.getTotalElements()).isEqualTo(2);
        assertThat(responseDtoPage.getContent()).hasSize(2);
        assertThat(responseDtoPage.getContent().get(0).id()).isEqualTo(testOption1.getId());
        assertThat(responseDtoPage.getContent().get(1).id()).isEqualTo(testOption2.getId());
    }
}
