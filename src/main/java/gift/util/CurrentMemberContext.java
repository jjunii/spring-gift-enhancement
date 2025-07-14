package gift.util;

import gift.dto.AuthenticatedMemberDto;

public class CurrentMemberContext {

    private static final ThreadLocal<AuthenticatedMemberDto> memberHolder = new ThreadLocal<>();

    public static void setAuthenticatedMember(AuthenticatedMemberDto authenticatedMemberDto) {
        memberHolder.set(authenticatedMemberDto);
    }

    public static Long getAuthenticatedMemberId() {
        AuthenticatedMemberDto authenticatedMember = memberHolder.get();

        return authenticatedMember.id();
    }

    public static void clear() {
        memberHolder.remove();
    }
}
