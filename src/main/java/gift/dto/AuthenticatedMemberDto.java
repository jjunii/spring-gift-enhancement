package gift.dto;

import gift.entity.Member;

public record AuthenticatedMemberDto(
        Long id
) {

    public static AuthenticatedMemberDto from(Member member) {
        return new AuthenticatedMemberDto(
                member.getId()
        );
    }
}
