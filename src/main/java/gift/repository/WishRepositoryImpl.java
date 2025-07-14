package gift.repository;

import gift.entity.Wish;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class WishRepositoryImpl implements WishRepository {

    // JdbcClient
    private final JdbcClient client;

    public WishRepositoryImpl(JdbcClient client) {
        this.client = client;
    }

    @Override
    public Long saveWish(Wish wish) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        var sql = "INSERT INTO wish (member_id, product_id) VALUES (:member_id, :product_id)";
        client.sql(sql)
              .param("member_id", wish.getMemberId())
              .param("product_id", wish.getProductId())
              .update(keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public List<Wish> findAllWishesByMemberId(Long memberId) {
        var sql = "SELECT * FROM wish WHERE member_id = :member_id ORDER BY id";

        return client.sql(sql)
                     .param("member_id", memberId)
                     .query(Wish.class)
                     .list();
    }

    @Override
    public void deleteWishById(Long wishId) {
        var sql = "DELETE FROM wish WHERE id = :id";

        client.sql(sql)
              .param("id", wishId)
              .update();
    }

    @Override
    public Optional<Wish> findWishById(Long wishId) {
        var sql = "SELECT * FROM wish WHERE id = :id";

        return client.sql(sql)
                     .param("id", wishId)
                     .query(Wish.class)
                     .optional();
    }

    @Override
    public boolean existsByMemberIdAndProductId(Long memberId, Long productId) {
        var sql = "SELECT COUNT(*) FROM wish WHERE member_id = :member_id AND product_id = :product_id";

        Integer count = client.sql(sql)
                              .param("member_id", memberId)
                              .param("product_id", productId)
                              .query(Integer.class)
                              .single();

        return count > 0;
    }
}
