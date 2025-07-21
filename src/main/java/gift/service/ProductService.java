package gift.service;

import gift.dto.ProductCreateRequestDto;
import gift.dto.ProductResponseDto;
import gift.dto.ProductUpdateRequestDto;
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
    public ProductResponseDto saveProduct(ProductCreateRequestDto productCreateDto) {
        Product product = new Product(
                productCreateDto.name(),
                productCreateDto.price(),
                productCreateDto.imageUrl(),
                ProductStatus.getProductStatus(productCreateDto.name()));

        Product savedProduct = productRepository.save(product);

        return ProductResponseDto.from(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto findProduct(Long productId) {
        Product product = findProductOrThrow(productId);

        return ProductResponseDto.from(product);
    }

    @Transactional
    public ProductResponseDto updateProduct(Long productId,
            ProductUpdateRequestDto productUpdateDto) {
        Product product = findProductOrThrow(productId);

        if (productUpdateDto.name() != null && !productUpdateDto.name().isBlank()) {
            ProductStatus newStatus = ProductStatus.getProductStatus(productUpdateDto.name());
            product.updateNameAndStatus(productUpdateDto.name(), newStatus);
        }

        if (productUpdateDto.price() != null) {
            product.updatePrice(productUpdateDto.price());
        }

        if (productUpdateDto.imageUrl() != null && !productUpdateDto.imageUrl().isBlank()) {
            product.updateImageUrl(productUpdateDto.imageUrl());
        }

        productRepository.save(product);

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

        productRepository.save(product);
    }

    Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
