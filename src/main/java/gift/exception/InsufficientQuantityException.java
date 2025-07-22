package gift.exception;

public class InsufficientQuantityException extends RuntimeException {

    public InsufficientQuantityException(Integer quantity) {
        super("수량이 부족합니다. 현재 수량: " + quantity);
    }
}
