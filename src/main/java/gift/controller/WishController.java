package gift.controller;

import gift.dto.WishRequestDto;
import gift.dto.WishResponseDto;
import gift.service.WishService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishes")
public class WishController {

    private final WishService wishService;

    public WishController(WishService wishService) {
        this.wishService = wishService;
    }

    // 상품 추가
    @PostMapping
    public ResponseEntity<WishResponseDto> addWish(@RequestBody WishRequestDto wishRequestDto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(wishService.addWish(wishRequestDto));
    }

    // 상품 목록 조회
    @GetMapping
    public ResponseEntity<List<WishResponseDto>> getWishlistByMemberId() {

        return ResponseEntity.ok(wishService.getWishlistByMemberId());
    }

    // 상품 삭제
    @DeleteMapping("/{wishId}")
    public ResponseEntity<Void> deleteWishById(@PathVariable Long wishId) {
        wishService.deleteWishById(wishId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
