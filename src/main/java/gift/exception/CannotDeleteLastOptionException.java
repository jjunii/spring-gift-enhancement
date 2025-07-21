package gift.exception;

public class CannotDeleteLastOptionException extends RuntimeException {

    public CannotDeleteLastOptionException() {
        super("상품에는 항상 하나 이상의 옵션이 있어야 합니다.");
    }
}
