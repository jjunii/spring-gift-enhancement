package gift.Member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import gift.entity.Member;
import gift.entity.MemberRole;
import gift.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void saveAndFindById() {
        String hashedPassword = BCrypt.hashpw("password", BCrypt.gensalt());
        Member member = new Member("user@email.com", hashedPassword, MemberRole.ROLE_USER);

        Member savedMember = memberRepository.save(member);

        assertAll(
                () -> assertThat(savedMember.getId()).isNotNull(),
                () -> assertThat(savedMember.getEmail()).isEqualTo("user@email.com")
        );

        Member foundMember = memberRepository.findById(savedMember.getId()).orElseThrow();

        assertThat(foundMember.getEmail()).isEqualTo("user@email.com");
    }

    @Test
    void findByEmail() {
        String hashedPassword = BCrypt.hashpw("password", BCrypt.gensalt());
        Member member = new Member("user@email.com", hashedPassword, MemberRole.ROLE_USER);
        Member savedMember = memberRepository.save(member);

        Optional<Member> foundMember1 = memberRepository.findByEmail("fakeUser@email.com");
        Optional<Member> foundMember2 = memberRepository.findByEmail("user@email.com");

        assertAll(
                () -> assertThat(foundMember1).isEmpty(),
                () -> assertThat(foundMember2.get().getPassword()).isEqualTo(hashedPassword)
        );
    }
}
