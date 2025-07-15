package gift.dto;

import gift.entity.Wish;

public record WishResponseDto(
        Long id,
        ProductResponseDto product
) {

    public static WishResponseDto from(Wish wish) {
        return new WishResponseDto(
                wish.getId(),
                ProductResponseDto.from(wish.getProduct())
        );
    }
}
