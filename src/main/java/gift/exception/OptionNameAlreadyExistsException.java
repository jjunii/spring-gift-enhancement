package gift.exception;

public class OptionNameAlreadyExistsException extends RuntimeException {

    public OptionNameAlreadyExistsException(String name) {
        super(name + "은 이미 존재하는 옵션 이름입니다.");
    }
}
