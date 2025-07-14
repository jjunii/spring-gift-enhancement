package gift.exception;

public class WishAlreadyExistsException extends RuntimeException {

    private final Long productId;

    public WishAlreadyExistsException(Long productId) {
        super("상품 ID가 " + productId + "인 상품은 이미 위시리스트에 추가된 상품입니다.");
        this.productId = productId;
    }
}
