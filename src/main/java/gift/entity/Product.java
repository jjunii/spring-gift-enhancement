package gift.entity;

public class Product {

    private Long id;
    private String name;
    private Integer price;
    private String imageUrl;
    private ProductStatus status;

    public Product(Long id, String name, Integer price, String imageUrl, ProductStatus status) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.status = status;
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

    public void update(String name, Integer price, String imageUrl, ProductStatus status) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.status = status;
    }
}
