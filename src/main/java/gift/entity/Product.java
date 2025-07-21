package gift.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 15)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Option> options = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductStatus status;

    protected Product() {
    }

    public Product(String name, Integer price, String imageUrl, ProductStatus status) {
        this(null, name, price, imageUrl, status);
    }

    public Product(Long id, String name, Integer price, String imageUrl, ProductStatus status) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.status = status;
    }

    public void updateNameAndStatus(String name, ProductStatus status) {
        this.name = name;
        changeStatus(status);
    }

    public void updatePrice(Integer price) {
        this.price = price;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void changeStatus(ProductStatus newStatus) {
        this.status = newStatus;
    }

    public void addOption(Option option) {
        options.add(option);
        option.setProduct(this);
    }

    public void removeOption(Option option) {
        options.remove(option);
        option.setProduct(null);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public List<Option> getOptions() {
        return options;
    }
}
