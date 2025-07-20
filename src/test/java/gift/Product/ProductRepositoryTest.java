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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void save() {
        Product product = new Product("상품", 10000, "https://example.com/image.jpg",
                ProductStatus.APPROVED);

        Product savedProduct = productRepository.save(product);

        assertAll(
                () -> assertThat(savedProduct.getId()).isNotNull(),
                () -> assertThat(savedProduct.getName()).isEqualTo(product.getName())
        );
    }

    @Test
    void findById() {
        Product product = new Product("상품", 10000, "https://example.com/image.jpg",
                ProductStatus.APPROVED);
        productRepository.save(product);

        Product foundProduct = productRepository.findById(product.getId()).orElseThrow();

        assertThat(foundProduct.getName()).isEqualTo(product.getName());
    }

    @Test
    void findAll_with_pagination() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("price").descending());
        Page<Product> productPage = productRepository.findAll(pageable);

        // data.sql의 sample products
        assertAll(
                () -> assertThat(productPage.getTotalElements()).isEqualTo(6),
                () -> assertThat(productPage.getNumber()).isEqualTo(0),
                () -> assertThat(productPage.getSize()).isEqualTo(5),
                () -> assertThat(productPage.getContent()).hasSize(5),
                () -> assertThat(productPage.getContent().get(0).getPrice()).isEqualTo(6000)
        );
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
