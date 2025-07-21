package gift.exception;

public class OptionNotFoundException extends RuntimeException {

    private final Long optionId;

    public OptionNotFoundException(Long optionId) {
        super("옵션 ID가 " + optionId + "인 옵션을 찾을 수 없습니다.");
        this.optionId = optionId;
    }
}