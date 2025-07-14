package gift.repository;

import gift.entity.Product;
import gift.entity.ProductStatus;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product saveProduct(Product product);

    Optional<Product> findProduct(Long productId);

    void updateProduct(Product product);

    void deleteProduct(Long productId);

    List<Product> findAllProducts();

    void updateProductStatus(Long productId, ProductStatus newStatus);

    List<Product> findProductsByIdsIn(List<Long> productIds);
}
