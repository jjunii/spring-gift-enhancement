package gift.dto;

import gift.entity.Product;
import gift.entity.ProductStatus;
import java.util.List;

public record ProductResponseDto(
        Long id,
        String name,
        Integer price,
        String imageUrl,
        ProductStatus status,
        List<OptionResponseDto> options
) {

    public static ProductResponseDto from(Product product) {
        List<OptionResponseDto> optionResponseDtos = product.getOptions().stream()
                                                            .map(OptionResponseDto::from)
                                                            .toList();
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl(),
                product.getStatus(),
                optionResponseDtos
        );
    }
}
