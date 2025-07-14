package gift.exception;

public class LoginFailedException extends RuntimeException {

    public LoginFailedException() {
        super("ID 혹은 비밀번호가 일치하지 않습니다.");
    }
}
