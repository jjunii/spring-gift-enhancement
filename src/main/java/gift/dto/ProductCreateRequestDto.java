package gift.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ProductCreateRequestDto(
        @NotBlank(message = "상품명은 필수입니다.")
        @Size(max = 15, message = "상품명은 공백을 포함하여 최대 15자까지 입력할 수 있습니다.")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣\\s()\\[\\]+\\-&/_]*$",
                message = "상품명에 허용되지 않는 특수문자가 있습니다. 사용가능: ( ), [ ], +, -, &, /, _")
        String name,

        @NotNull(message = "가격은 필수입니다.")
        @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
        Integer price,

        @NotBlank(message = "이미지 URL은 필수입니다.")
        String imageUrl,

        @NotEmpty(message = "상품에는 하나 이상의 옵션이 반드시 있어야 합니다.")
        @Valid
        List<OptionRequestDto> options
) {

}
