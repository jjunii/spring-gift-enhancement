package gift.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OptionRequestDto(
        @NotBlank(message = "옵션명은 필수입니다.")
        @Size(max = 50, message = "옵션명은 공백을 포함하여 최대 50자까지 입력할 수 있습니다.")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣\\s()\\[\\]+\\-&/_]*$",
                message = "옵션명에 허용되지 않는 특수문자가 있습니다. 사용가능: ( ), [ ], +, -, &, /, _")
        String name,

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        @Max(value = 99999999, message = "수량은 1억개 미만이어야 합니다.")
        Integer quantity
) {

}
