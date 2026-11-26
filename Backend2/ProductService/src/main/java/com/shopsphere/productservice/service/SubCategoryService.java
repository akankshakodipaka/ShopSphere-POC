package com.shopsphere.productservice.service;
import java.util.List;

import com.shopsphere.productservice.exception.ProductException;
import com.shopsphere.productservice.models.Category;
import com.shopsphere.productservice.models.SubCategory;
import com.shopsphere.productservice.repository.CategoryRepository;
import com.shopsphere.productservice.repository.SubCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shopsphere.productservice.dto.Dtos.SubCategoryRequest;
import com.shopsphere.productservice.dto.Dtos.SubCategoryResponse;
/** Rules for sub-categories. Each one belongs to an active category. */
@Service
@Transactional
public class SubCategoryService {

    private final SubCategoryRepository subCategories;
    private final CategoryRepository categories;

    public SubCategoryService(SubCategoryRepository subCategories, CategoryRepository categories) {
        this.subCategories = subCategories;
        this.categories = categories;
    }

    public SubCategoryResponse create(SubCategoryRequest request) {
        Category category = activeCategory(request.categoryId());
        String name = request.name().trim();
        if (subCategories.existsByCategoryIdAndNameIgnoreCase(category.getId(), name)) {
            throw new ProductException(HttpStatus.CONFLICT,
                    "Sub-category '" + name + "' already exists in " + category.getName());
        }
        return toResponse(subCategories.save(new SubCategory(name, request.description(), category)));
    }

    /** All sub-categories, or only those of one category. activeOnly hides deactivated ones. */
    @Transactional(readOnly = true)
    public List<SubCategoryResponse> list(Long categoryId, boolean activeOnly) {
        List<SubCategory> found = categoryId == null
                ? subCategories.findAllByOrderByNameAsc()
                : subCategories.findByCategoryIdOrderByNameAsc(categoryId);
        return found.stream()
                .filter(s -> !activeOnly || s.isActive())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubCategoryResponse get(Long id) {
        return toResponse(find(id));
    }

    public SubCategoryResponse update(Long id, SubCategoryRequest request) {
        SubCategory sub = find(id);
        Category target = sub.getCategory();
        if (!target.getId().equals(request.categoryId())) {   // moved to another category
            target = activeCategory(request.categoryId());
        }
        String name = request.name().trim();
        if (subCategories.existsByCategoryIdAndNameIgnoreCaseAndIdNot(target.getId(), name, id)) {
            throw new ProductException(HttpStatus.CONFLICT,
                    "Sub-category '" + name + "' already exists in " + target.getName());
        }
        sub.setName(name);
        sub.setDescription(request.description());
        sub.setCategory(target);
        return toResponse(subCategories.save(sub));
    }

    /** Activate or deactivate (soft delete). It cannot be activated while its category is inactive. */
    public SubCategoryResponse setActive(Long id, boolean active) {
        SubCategory sub = find(id);
        if (active && !sub.getCategory().isActive()) {
            throw new ProductException(HttpStatus.CONFLICT,
                    "Activate the category '" + sub.getCategory().getName() + "' first");
        }
        sub.setActive(active);
        return toResponse(subCategories.save(sub));
    }

    // ---- helpers ----

    private SubCategory find(Long id) {
        return subCategories.findById(id)
                .orElseThrow(() -> new ProductException(HttpStatus.NOT_FOUND, "Sub-category " + id + " not found"));
    }

    // the category must exist (404) and be active (409)
    private Category activeCategory(Long categoryId) {
        Category category = categories.findById(categoryId)
                .orElseThrow(() -> new ProductException(HttpStatus.NOT_FOUND, "Category " + categoryId + " not found"));
        if (!category.isActive()) {
            throw new ProductException(HttpStatus.CONFLICT, "Category '" + category.getName() + "' is inactive");
        }
        return category;
    }

    private SubCategoryResponse toResponse(SubCategory s) {
        return new SubCategoryResponse(s.getId(), s.getName(), s.getDescription(), s.isActive(),
                s.getCategory().getId(), s.getCategory().getName());
    }
}