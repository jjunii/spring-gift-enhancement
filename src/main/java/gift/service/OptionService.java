package gift.service;

import gift.dto.OptionRequestDto;
import gift.dto.OptionResponseDto;
import gift.entity.Option;
import gift.entity.Product;
import gift.exception.OptionNameAlreadyExistsException;
import gift.repository.OptionRepository;
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
}
