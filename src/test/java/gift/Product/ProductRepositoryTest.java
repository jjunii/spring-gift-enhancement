package gift.Product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import gift.entity.Product;
import gift.entity.ProductStatus;
import gift.repository.ProductRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void saveAndFindById() {
        Product product = new Product("상품", 10000, "https://example.com/image.jpg",
                ProductStatus.APPROVED);

        Product savedProduct = productRepository.save(product);

        assertAll(
                () -> assertThat(savedProduct.getId()).isNotNull(),
                () -> assertThat(savedProduct.getName()).isEqualTo(product.getName())
        );

        Product foundProduct = productRepository.findById(savedProduct.getId()).orElseThrow();

        assertThat(foundProduct.getName()).isEqualTo(product.getName());
    }

    @Test
    void findAll() {
        // data.sql의 sample products
        List<Product> products = productRepository.findAll();

        assertThat(products).hasSize(6);
    }

    @Test
    void findAllById() {
        Product productA = new Product("상품A", 10000, "https://example.com/A.jpg",
                ProductStatus.APPROVED);
        Product productB = new Product("상품B", 20000, "https://example.com/B.jpg",
                ProductStatus.APPROVED);
        Product productC = new Product("상품C", 30000, "https://example.com/C.jpg",
                ProductStatus.APPROVED);
        productRepository.save(productA);
        productRepository.save(productB);
        productRepository.save(productC);
        List<Long> ids = List.of(productA.getId(), productC.getId());

        List<Product> products = productRepository.findAllById(ids);

        assertAll(
                () -> assertThat(products).hasSize(2),
                () -> assertThat(products.get(0).getName()).isEqualTo(productA.getName()),
                () -> assertThat(products.get(1).getName()).isEqualTo(productC.getName())
        );

    }

    @Test
    void deleteById() {
        Product product = productRepository.save(
                new Product("상품", 10000, "https://example.com/image.jpg",
                        ProductStatus.APPROVED)
        );

        productRepository.deleteById(product.getId());

        assertThat(productRepository.findById(product.getId())).isEmpty();
    }


}
