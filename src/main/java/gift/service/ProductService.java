package gift.service;

import gift.dto.ProductRequestDto;
import gift.dto.ProductResponseDto;
import gift.entity.Product;
import gift.entity.ProductStatus;
import gift.exception.ProductNotFoundException;
import gift.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponseDto saveProduct(ProductRequestDto productRequestDto) {
        Product product = new Product(null, productRequestDto.name(),
                productRequestDto.price(), productRequestDto.imageUrl(),
                ProductStatus.getProductStatus(productRequestDto.name()));

        Product savedProduct = productRepository.saveProduct(product);

        return ProductResponseDto.from(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto findProduct(Long productId) {
        Product product = findProductOrThrow(productId);

        return ProductResponseDto.from(product);
    }

    @Transactional
    public ProductResponseDto updateProduct(Long productId, ProductRequestDto productRequestDto) {
        Product product = findProductOrThrow(productId);
        product.update(
                productRequestDto.name(),
                productRequestDto.price(),
                productRequestDto.imageUrl(),
                ProductStatus.getProductStatus(productRequestDto.name())
        );

        productRepository.updateProduct(product);

        return ProductResponseDto.from(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        findProductOrThrow(productId);

        productRepository.deleteProduct(productId);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> findAllProducts() {

        return productRepository.findAllProducts()
                                .stream()
                                .map(ProductResponseDto::from)
                                .toList();
    }

    @Transactional
    public void updateProductStatus(Long productId, ProductStatus newStatus) {
        findProductOrThrow(productId);
        productRepository.updateProductStatus(productId, newStatus);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> findProductsByIdsIn(List<Long> productIds) {

        return productRepository.findProductsByIdsIn(productIds)
                                .stream()
                                .map(ProductResponseDto::from)
                                .toList();
    }

    Product findProductOrThrow(Long productId) {
        return productRepository.findProduct(productId)
                                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
