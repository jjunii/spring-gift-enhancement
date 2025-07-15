package gift.Wish;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import gift.entity.Member;
import gift.entity.MemberRole;
import gift.entity.Product;
import gift.entity.ProductStatus;
import gift.entity.Wish;
import gift.repository.MemberRepository;
import gift.repository.ProductRepository;
import gift.repository.WishRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class WishRepositoryTest {

    @Autowired
    private WishRepository wishRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    private Member testMember;
    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        String hashedPassword = BCrypt.hashpw("password", BCrypt.gensalt());
        testMember = memberRepository.save(
                new Member("user@email.com", hashedPassword, MemberRole.ROLE_USER));
        testProduct1 = productRepository.save(
                new Product("상품A", 10000, "https://example.com/A.jpg", ProductStatus.APPROVED));
        testProduct2 = productRepository.save(
                new Product("상품B", 20000, "https://example.com/B.jpg", ProductStatus.APPROVED));
    }

    @Test
    void saveAndFindById() {
        Wish wish = new Wish(testMember, testProduct1);

        Wish savedWish = wishRepository.save(wish);

        assertThat(savedWish.getId()).isNotNull();

        Wish foundWish = wishRepository.findById(savedWish.getId()).orElseThrow();

        assertAll(
                () -> assertThat(foundWish.getMember().getId()).isEqualTo(testMember.getId()),
                () -> assertThat(foundWish.getProduct().getId()).isEqualTo(testProduct1.getId())
        );
    }

    @Test
    void findAllByMemberIdOrderById() {
        wishRepository.save(new Wish(testMember, testProduct1));
        wishRepository.save(new Wish(testMember, testProduct2));

        List<Wish> wishes = wishRepository.findAllByMemberIdOrderById(testMember.getId());

        assertAll(
                () -> assertThat(wishes).hasSize(2),
                () -> assertThat(wishes.get(0).getProduct()).isEqualTo(testProduct1),
                () -> assertThat(wishes.get(1).getProduct()).isEqualTo(testProduct2)
        );
    }

    @Test
    void deleteById() {
        Wish wish = wishRepository.save(new Wish(testMember, testProduct1));

        wishRepository.deleteById(wish.getId());

        assertThat(wishRepository.findById(wish.getId())).isEmpty();
    }

    @Test
    void existsByMemberAndProduct() {
        wishRepository.save(new Wish(testMember, testProduct1));

        boolean exists = wishRepository.existsByMemberAndProduct(testMember, testProduct1);
        boolean notExists = wishRepository.existsByMemberAndProduct(testMember, testProduct2);

        assertAll(
                () -> assertThat(exists).isTrue(),
                () -> assertThat(notExists).isFalse()
        );
    }
}
