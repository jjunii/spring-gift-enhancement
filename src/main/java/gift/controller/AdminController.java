package gift.controller;

import gift.dto.ProductRequestDto;
import gift.entity.ProductStatus;
import gift.exception.ProductNotFoundException;
import gift.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
public class AdminController {

    private final ProductService productService;

    public AdminController(ProductService productService) {
        this.productService = productService;
    }

    // 상품 목록 조회
    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.findAllProducts());
        model.addAttribute("allStatuses", ProductStatus.values());
        return "admin/products/list";
    }

    // 상품 단건 조회
    @GetMapping("/{id}")
    public String detailProduct(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("product", productService.findProduct(id));
            model.addAttribute("allStatuses", ProductStatus.values());
        } catch (ProductNotFoundException ex) {
            model.addAttribute("errorMessage", "상품을 찾을 수 없습니다.");
        }
        return "admin/products/detail";
    }

    // 상품 추가
    @GetMapping("/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new ProductRequestDto("", 0, ""));
        return "admin/products/new";
    }

    @PostMapping
    public String addProduct(
            @Valid @ModelAttribute("product") ProductRequestDto productRequestDto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "admin/products/new";
        }

        productService.saveProduct(productRequestDto);
        return "redirect:/admin/products";
    }

    // 상품 수정
    @GetMapping("/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("product", productService.findProduct(id));
            model.addAttribute("productId", id);
        } catch (ProductNotFoundException ex) {
            model.addAttribute("errorMessage", "상품을 찾을 수 없습니다.");
        }
        return "admin/products/edit";
    }

    @PutMapping("/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute("product") ProductRequestDto productRequestDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("productId", id);
            return "admin/products/edit";
        }

        try {
            productService.updateProduct(id, productRequestDto);
        } catch (ProductNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "상품을 찾을 수 없습니다.");
        }
        return "redirect:/admin/products";
    }

    // 상품 삭제
    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
        } catch (ProductNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "상품을 찾을 수 없습니다.");
        }
        return "redirect:/admin/products";
    }

    // 상태 수정
    @PatchMapping("/{id}/status")
    public String updateProductStatus(@PathVariable Long id,
            @RequestParam ProductStatus status,
            RedirectAttributes redirectAttributes) {
        try {
            productService.updateProductStatus(id, status);
        } catch (ProductNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "상품을 찾을 수 없습니다.");
        }
        return "redirect:/admin/products";
    }

}
