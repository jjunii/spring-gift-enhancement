package gift.exception;

public class LastOptionException extends RuntimeException {

    public LastOptionException() {
        super("상품에는 항상 하나 이상의 옵션이 있어야 합니다.");
    }
}
