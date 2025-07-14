package gift.exception;

public class WishNotFoundException extends RuntimeException {

    private final Long wishId;

    public WishNotFoundException(Long wishId) {
        super("위시리스트 ID가 " + wishId + "인 상품을 찾을 수 없습니다.");
        this.wishId = wishId;
    }
}
