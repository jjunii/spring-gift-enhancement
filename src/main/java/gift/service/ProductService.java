package gift.service;

import gift.dto.ProductRequestDto;
import gift.dto.ProductResponseDto;
import gift.entity.Product;
import gift.entity.ProductStatus;
import gift.exception.ProductNotFoundException;
import gift.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<ProductResponseDto> findAllProducts(Pageable pageable) {

        return productRepository.findAll(pageable)
                                .map(ProductResponseDto::from);
    }

    @Transactional
    public void updateProductStatus(Long productId, ProductStatus newStatus) {
        Product product = findProductOrThrow(productId);

        product.changeStatus(newStatus);
    }

    Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
