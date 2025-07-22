package gift.repository;

import gift.entity.Option;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {

    boolean existsByProductIdAndName(Long productId, String name);

    boolean existsByProductIdAndNameAndIdNot(Long productId, String name, Long id);

    Page<Option> findAllByProductId(Long productId, Pageable pageable);
}
