package gift.service;

import gift.dto.OptionRequestDto;
import gift.dto.OptionResponseDto;
import gift.entity.Option;
import gift.entity.Product;
import gift.exception.CannotDeleteLastOptionException;
import gift.exception.OptionNameAlreadyExistsException;
import gift.exception.OptionNotFoundException;
import gift.exception.PermissionDeniedException;
import gift.repository.OptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OptionService {

    private final OptionRepository optionRepository;
    private final ProductService productService;

    public OptionService(OptionRepository optionRepository, ProductService productService) {
        this.optionRepository = optionRepository;
        this.productService = productService;
    }

    @Transactional
    public OptionResponseDto addOption(Long productId, OptionRequestDto optionRequestDto) {
        Product product = productService.findProductOrThrow(productId);

        if (optionRepository.existsByProductIdAndName(productId, optionRequestDto.name())) {
            throw new OptionNameAlreadyExistsException(optionRequestDto.name());
        }

        Option option = new Option(optionRequestDto.name(), optionRequestDto.quantity());
        product.addOption(option);

        Option savedOption = optionRepository.save(option);

        return OptionResponseDto.from(savedOption);
    }

    @Transactional
    public OptionResponseDto updateOption(Long productId, Long optionId,
            OptionRequestDto optionRequestDto) {
        Option option = findOptionOrThrow(optionId);

        if (!option.getProduct().getId().equals(productId)) {
            throw new PermissionDeniedException("해당 상품에 속한 옵션이 아닙니다.");
        }

        if (optionRepository.existsByProductIdAndNameAndIdNot(productId, optionRequestDto.name(),
                optionId)) {
            throw new OptionNameAlreadyExistsException(optionRequestDto.name());
        }

        option.update(optionRequestDto.name(), optionRequestDto.quantity());

        optionRepository.save(option);

        return OptionResponseDto.from(option);
    }

    @Transactional
    public void deleteOption(Long productId, Long optionId) {
        Option option = findOptionOrThrow(optionId);

        Product product = option.getProduct();
        if (!product.getId().equals(productId)) {
            throw new PermissionDeniedException("해당 상품에 속한 옵션이 아닙니다.");
        }

        if (product.getOptions().size() == 1) {
            throw new CannotDeleteLastOptionException();
        }

        product.removeOption(option);
        optionRepository.deleteById(optionId);
    }

    @Transactional(readOnly = true)
    public Page<OptionResponseDto> getOptionsByProductId(Long productId, Pageable pageable) {
        productService.findProductOrThrow(productId);

        Page<Option> options = optionRepository.findAllByProductId(productId, pageable);

        return options.map(OptionResponseDto::from);
    }

    private Option findOptionOrThrow(Long optionId) {
        return optionRepository.findById(optionId)
                               .orElseThrow(() -> new OptionNotFoundException(optionId));
    }
}
