package gift.exception;

public class ProductNotFoundException extends RuntimeException {

    private final Long productId;

    public ProductNotFoundException(Long productId) {
        super("상품 ID가 " + productId + "인 상품을 찾을 수 없습니다.");
        this.productId = productId;
    }
}
