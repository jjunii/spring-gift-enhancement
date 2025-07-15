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
        Product product = new Product(
                productRequestDto.name(),
                productRequestDto.price(),
                productRequestDto.imageUrl(),
                ProductStatus.getProductStatus(productRequestDto.name()));

        Product savedProduct = productRepository.save(product);

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

        return ProductResponseDto.from(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        findProductOrThrow(productId);

        productRepository.deleteById(productId);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> findAllProducts() {

        return productRepository.findAll()
                                .stream()
                                .map(ProductResponseDto::from)
                                .toList();
    }

    @Transactional
    public void updateProductStatus(Long productId, ProductStatus newStatus) {
        Product product = findProductOrThrow(productId);

        product.changeStatus(newStatus);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> findProductsByIdsIn(List<Long> ids) {

        return productRepository.findAllById(ids)
                                .stream()
                                .map(ProductResponseDto::from)
                                .toList();
    }

    Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
