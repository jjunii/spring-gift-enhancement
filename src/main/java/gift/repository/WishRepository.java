package gift.repository;

import gift.entity.Member;
import gift.entity.Product;
import gift.entity.Wish;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishRepository extends JpaRepository<Wish, Long> {

    List<Wish> findAllByMemberIdOrderById(Long memberId);

    boolean existsByMemberAndProduct(Member member, Product product);
}
