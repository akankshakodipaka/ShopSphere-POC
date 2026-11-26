package com.shopsphere.productservice.service;
import java.util.Comparator;
import java.util.List;
import com.shopsphere.productservice.exception.ProductException;
import com.shopsphere.productservice.models.Category;
import com.shopsphere.productservice.models.SubCategory;
import com.shopsphere.productservice.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shopsphere.productservice.dto.Dtos.CategoryRequest;
import com.shopsphere.productservice.dto.Dtos.CategoryResponse;
import com.shopsphere.productservice.dto.Dtos.SubCategorySummary;

/** Rules for categories. Categories come first: sub-categories and products hang under them. */
@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categories;

    public CategoryService(CategoryRepository categories) {
        this.categories = categories;
    }

    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();
        if (categories.existsByNameIgnoreCase(name)) {
            throw new ProductException(HttpStatus.CONFLICT, "Category '" + name + "' already exists");
        }
        return toResponse(categories.save(new Category(name, request.description())));
    }

    /** All categories with their sub-categories (handy for dropdowns). activeOnly hides deactivated ones. */
    @Transactional(readOnly = true)
    public List<CategoryResponse> list(boolean activeOnly) {
        List<Category> found = activeOnly ? categories.findByActiveTrueOrderByNameAsc()
                : categories.findAllByOrderByNameAsc();
        return found.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(Long id) {
        return toResponse(find(id));
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = find(id);
        String name = request.name().trim();
        if (categories.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ProductException(HttpStatus.CONFLICT, "Category '" + name + "' already exists");
        }
        category.setName(name);
        category.setDescription(request.description());
        return toResponse(categories.save(category));
    }

    /**
     * Activate or deactivate (soft delete). Deactivating a category also deactivates its
     * sub-categories, so nothing under it can be bought. Activating does not bring them back.
     */
    public CategoryResponse setActive(Long id, boolean active) {
        Category category = find(id);
        category.setActive(active);
        if (!active) {
            category.getSubCategories().forEach(s -> s.setActive(false));
        }
        return toResponse(categories.save(category));
    }

    // ---- helpers ----

    private Category find(Long id) {
        return categories.findById(id)
                .orElseThrow(() -> new ProductException(HttpStatus.NOT_FOUND, "Category " + id + " not found"));
    }

    // entity -> response, kept private inside the service
    private CategoryResponse toResponse(Category c) {
        List<SubCategorySummary> subs = c.getSubCategories().stream()
                .sorted(Comparator.comparing(SubCategory::getName, String.CASE_INSENSITIVE_ORDER))
                .map(s -> new SubCategorySummary(s.getId(), s.getName(), s.isActive()))
                .toList();
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(), c.isActive(), subs);
    }
}
