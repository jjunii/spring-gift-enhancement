package gift.entity;

import java.util.List;

public enum ProductStatus {
    APPROVED,
    PENDING_APPROVAL;

    private static final List<String> APPROVAL_REQUIRED_WORDS = List.of("카카오");

    public static ProductStatus getProductStatus(String productName) {
        if (APPROVAL_REQUIRED_WORDS.stream().anyMatch(productName::contains)) {
            return PENDING_APPROVAL;
        }

        return APPROVED;
    }
}
