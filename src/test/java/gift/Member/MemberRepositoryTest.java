package gift.Member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import gift.entity.Member;
import gift.entity.MemberRole;
import gift.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    private String hashedPassword;

    @BeforeEach
    void setUp() {
        hashedPassword = BCrypt.hashpw("password", BCrypt.gensalt());
    }

    @Test
    void save() {
        Member member = new Member("user@email.com", hashedPassword, MemberRole.ROLE_USER);

        Member savedMember = memberRepository.save(member);

        assertAll(
                () -> assertThat(savedMember.getId()).isNotNull(),
                () -> assertThat(savedMember.getEmail()).isEqualTo("user@email.com")
        );
    }

    @Test
    void findById() {
        Member member = new Member("user@email.com", hashedPassword, MemberRole.ROLE_USER);
        memberRepository.save(member);

        Member foundMember = memberRepository.findById(member.getId()).orElseThrow();

        assertThat(foundMember.getEmail()).isEqualTo("user@email.com");
    }

    @Test
    void findByEmail() {
        Member member = new Member("user@email.com", hashedPassword, MemberRole.ROLE_USER);
        memberRepository.save(member);

        Optional<Member> foundMember1 = memberRepository.findByEmail("fakeUser@email.com");
        Optional<Member> foundMember2 = memberRepository.findByEmail("user@email.com");

        assertAll(
                () -> assertThat(foundMember1).isEmpty(),
                () -> assertThat(foundMember2.get().getPassword()).isEqualTo(hashedPassword)
        );
    }
}
