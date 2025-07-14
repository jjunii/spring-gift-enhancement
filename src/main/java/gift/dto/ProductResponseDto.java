package gift.dto;

import gift.entity.Product;
import gift.entity.ProductStatus;

public record ProductResponseDto(
        Long id,
        String name,
        Integer price,
        String imageUrl,
        ProductStatus status
) {

    public static ProductResponseDto from(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl(),
                product.getStatus()
        );
    }
}
