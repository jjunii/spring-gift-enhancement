package gift.service;

import gift.dto.WishRequestDto;
import gift.dto.WishResponseDto;
import gift.entity.Member;
import gift.entity.Product;
import gift.entity.Wish;
import gift.exception.PermissionDeniedException;
import gift.exception.UnAuthenticationException;
import gift.exception.WishAlreadyExistsException;
import gift.exception.WishNotFoundException;
import gift.repository.MemberRepository;
import gift.repository.WishRepository;
import gift.util.CurrentMemberContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishService {

    private final WishRepository wishRepository;
    private final ProductService productService;
    private final MemberRepository memberRepository;

    public WishService(WishRepository wishRepository, ProductService productService,
            MemberRepository memberRepository) {
        this.wishRepository = wishRepository;
        this.productService = productService;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public WishResponseDto addWish(WishRequestDto wishRequestDto) {
        Long memberId = CurrentMemberContext.getAuthenticatedMemberId();

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new UnAuthenticationException("인증되지 않은 사용자입니다"));
        Product product = productService.findProductOrThrow(wishRequestDto.productId());

        if (wishRepository.existsByMemberAndProduct(member, product)) {
            throw new WishAlreadyExistsException(product.getId());
        }

        Wish newWish = new Wish(member, product);
        Wish savedwish = wishRepository.save(newWish);

        return WishResponseDto.from(savedwish);
    }

    @Transactional(readOnly = true)
    public Page<WishResponseDto> getWishlistByMemberId(Pageable pageable) {
        Long memberId = CurrentMemberContext.getAuthenticatedMemberId();

        Page<Wish> wishes = wishRepository.findAllByMemberId(memberId, pageable);

        return wishes.map(WishResponseDto::from);
    }

    @Transactional
    public void deleteWishById(Long wishId) {
        Long memberId = CurrentMemberContext.getAuthenticatedMemberId();

        Wish wish = wishRepository.findById(wishId)
                                  .orElseThrow(() -> new WishNotFoundException(wishId));

        if (!memberId.equals(wish.getMember().getId())) {
            throw new PermissionDeniedException("해당 상품을 삭제할 권한이 없습니다.");
        }

        wishRepository.deleteById(wishId);
    }
}
