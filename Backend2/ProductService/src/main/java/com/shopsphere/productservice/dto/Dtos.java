package com.shopsphere.productservice.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/** Request and response shapes. The JPA entities are never sent over HTTP. */
public final class Dtos {

    private Dtos() {}   // nobody should create a Dtos object; it only groups the records

    // ---------- categories ----------

    /** Body of POST and PUT /categories */
    public record CategoryRequest(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 500) String description) {}

    /** A small view of a sub-category, shown inside a category response */
    public record SubCategorySummary(Long id, String name, boolean active) {}

    public record CategoryResponse(Long id, String name, String description, boolean active,
                                   List<SubCategorySummary> subCategories) {}

    // ---------- sub-categories ----------

    /** Body of POST and PUT /subcategories. categoryId is the first dropdown's value. */
    public record SubCategoryRequest(
            @NotNull Long categoryId,
            @NotBlank @Size(max = 100) String name,
            @Size(max = 500) String description) {}

    public record SubCategoryResponse(Long id, String name, String description, boolean active,
                                      Long categoryId, String categoryName) {}

    // ---------- products ----------

    /** Body of POST and PUT /products. subCategoryId is the second dropdown's value. */
    public record ProductRequest(
            @NotNull Long subCategoryId,
            @NotBlank @Size(max = 150) String name,
            @Size(max = 1000) String description,
            @NotNull @DecimalMin("0.01") BigDecimal price,
            @Min(0) int stock,
            @Size(max = 500) String imageUrl) {}

    // id, name and price are what the Cart service reads. The Order service uses the stock endpoints.
    public record ProductResponse(Long id, String name, String description, BigDecimal price, int stock,
                                  boolean active, String imageUrl,
                                  Long subCategoryId, String subCategoryName,
                                  Long categoryId, String categoryName) {}

    // ---------- shared ----------

    /** Body of PUT /{id}/status. Boolean (not boolean), so a missing field is rejected instead of meaning false. */
    public record StatusRequest(@NotNull Boolean active) {}

    /** Body of reduce-stock and restore-stock */
    public record StockRequest(@Min(1) int quantity) {}
}