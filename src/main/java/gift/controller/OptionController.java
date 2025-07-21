package gift.controller;

import gift.dto.OptionRequestDto;
import gift.dto.OptionResponseDto;
import gift.service.OptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products/{productId}/options")
public class OptionController {

    private final OptionService optionService;

    public OptionController(OptionService optionService) {
        this.optionService = optionService;
    }

    // 옵션 추가
    @PostMapping
    public ResponseEntity<OptionResponseDto> addOption(
            @PathVariable Long productId,
            @Valid @RequestBody OptionRequestDto optionRequestDto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(optionService.addOption(productId, optionRequestDto));
    }
}
