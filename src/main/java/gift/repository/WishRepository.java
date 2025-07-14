package gift.repository;

import gift.entity.Wish;
import java.util.List;
import java.util.Optional;

public interface WishRepository {

    Long saveWish(Wish wish);

    List<Wish> findAllWishesByMemberId(Long memberId);

    void deleteWishById(Long wishId);

    Optional<Wish> findWishById(Long wishId);

    boolean existsByMemberIdAndProductId(Long memberId, Long productId);
}
