package gift.repository;

import gift.entity.Member;
import java.util.Optional;

public interface MemberRepository {

    Long saveMember(Member member);

    Optional<Member> findMemberByEmail(String email);

    Optional<Member> findMemberById(Long id);
}
