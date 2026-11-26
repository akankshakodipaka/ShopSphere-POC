package com.shopsphere.productservice.service;
import java.math.BigDecimal;
import java.util.List;

import com.shopsphere.productservice.exception.ProductException;
import com.shopsphere.productservice.models.Product;
import com.shopsphere.productservice.models.SubCategory;
import com.shopsphere.productservice.repository.ProductRepository;
import com.shopsphere.productservice.repository.SubCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shopsphere.productservice.dto.Dtos.ProductRequest;
import com.shopsphere.productservice.dto.Dtos.ProductResponse;
/** Rules for products: create under an active sub-category, search, soft delete, and stock for the Order service. */
@Service
@Transactional
public class ProductService {

    // "no upper limit" for the search query
    private static final BigDecimal NO_MAX_PRICE = new BigDecimal("999999999");

    private final ProductRepository products;
    private final SubCategoryRepository subCategories;

    public ProductService(ProductRepository products, SubCategoryRepository subCategories) {
        this.products = products;
        this.subCategories = subCategories;
    }

    // ---------- admin: create, change, switch on or off ----------

    public ProductResponse create(ProductRequest request) {
        SubCategory sub = activeSubCategory(request.subCategoryId());
        Product product = new Product(request.name().trim(), request.description(), request.price(),
                request.stock(), request.imageUrl(), sub);
        return toResponse(products.save(product));
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = find(id);
        if (!product.getSubCategory().getId().equals(request.subCategoryId())) {   // moved
            product.setSubCategory(activeSubCategory(request.subCategoryId()));
        }
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setImageUrl(request.imageUrl());
        return toResponse(products.save(product));
    }

    /** Activate or deactivate (soft delete). It cannot be activated under an inactive sub-category or category. */
    public ProductResponse setActive(Long id, boolean active) {
        Product product = find(id);
        if (active && !(product.getSubCategory().isActive()
                && product.getSubCategory().getCategory().isActive())) {
            throw new ProductException(HttpStatus.CONFLICT, "Activate its sub-category and category first");
        }
        product.setActive(active);
        return toResponse(products.save(product));
    }

    /** Every product including inactive ones, for the admin screen. */
    @Transactional(readOnly = true)
    public List<ProductResponse> listAll() {
        return products.findAllWithParents().stream().map(this::toResponse).toList();
    }

    // ---------- customers: browse and search ----------

    /** Visible products only. Any filter may be null, and null becomes the "no filter" value. */
    @Transactional(readOnly = true)
    public List<ProductResponse> search(String name, Long categoryId, Long subCategoryId,
                                        BigDecimal minPrice, BigDecimal maxPrice) {
        return products.search(
                        name == null ? "" : name.trim(),
                        categoryId == null ? 0L : categoryId,
                        subCategoryId == null ? 0L : subCategoryId,
                        minPrice == null ? BigDecimal.ZERO : minPrice,
                        maxPrice == null ? NO_MAX_PRICE : maxPrice)
                .stream().map(this::toResponse).toList();
    }

    /** One product. Hidden (inactive) products answer 404, as if they did not exist. */
    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        Product product = find(id);
        if (!product.isVisible()) {
            throw new ProductException(HttpStatus.NOT_FOUND, "Product " + id + " not found");
        }
        return toResponse(product);
    }

    // ---------- stock, called by the Order service ----------

    /** Takes stock in one atomic step. 404 if the product does not exist, 409 if there is not enough. */
    public void reduceStock(Long id, int quantity) {
        if (products.reduceStock(id, quantity) == 1) {
            return;
        }
        // the update changed nothing: find out why
        if (!products.existsById(id)) {
            throw new ProductException(HttpStatus.NOT_FOUND, "Product " + id + " not found");
        }
        throw new ProductException(HttpStatus.CONFLICT, "Not enough stock for product " + id);
    }

    /** Puts stock back (order failed or was cancelled). */
    public void restoreStock(Long id, int quantity) {
        if (products.restoreStock(id, quantity) == 0) {
            throw new ProductException(HttpStatus.NOT_FOUND, "Product " + id + " not found");
        }
    }

    // ---------- helpers ----------

    private Product find(Long id) {
        return products.findById(id)
                .orElseThrow(() -> new ProductException(HttpStatus.NOT_FOUND, "Product " + id + " not found"));
    }

    // the sub-category must exist (404) and it and its category must be active (409)
    private SubCategory activeSubCategory(Long subCategoryId) {
        SubCategory sub = subCategories.findById(subCategoryId)
                .orElseThrow(() -> new ProductException(HttpStatus.NOT_FOUND,
                        "Sub-category " + subCategoryId + " not found"));
        if (!sub.isActive() || !sub.getCategory().isActive()) {
            throw new ProductException(HttpStatus.CONFLICT, "Sub-category '" + sub.getName() + "' is inactive");
        }
        return sub;
    }

    private ProductResponse toResponse(Product p) {
        SubCategory s = p.getSubCategory();
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStock(),
                p.isActive(), p.getImageUrl(),
                s.getId(), s.getName(), s.getCategory().getId(), s.getCategory().getName());
    }
}
