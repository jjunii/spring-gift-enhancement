package gift.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super(email + "은 이미 존재하는 이메일입니다.");
    }
}
