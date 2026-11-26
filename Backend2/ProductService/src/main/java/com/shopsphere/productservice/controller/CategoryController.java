package com.shopsphere.productservice.controller;
import com.shopsphere.productservice.dto.Dtos;
import com.shopsphere.productservice.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.shopsphere.productservice.dto.Dtos.CategoryRequest;
import com.shopsphere.productservice.dto.Dtos.CategoryResponse;
import com.shopsphere.productservice.dto.Dtos.StatusRequest;


/** Reading is public. Creating, changing and switching on or off needs ADMIN or SUPER_ADMIN. */
@RestController
@RequestMapping("/v1")
public class CategoryController {

    private static final String ADMIN = "hasAnyRole('ADMIN','SUPER_ADMIN')";

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    // POST /categories   body: {"name":"Electronics","description":"..."}
    @PostMapping("/createCategory")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ADMIN)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return service.create(request);
    }

    // GET /categories?activeOnly=true : every category with its sub-categories (for dropdowns)
    @GetMapping("/getCategoryDetails")
    public List<CategoryResponse> list(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return service.list(activeOnly);
    }

    // GET /categories/1
    @GetMapping("/getCategoriesById/{id}")
    public CategoryResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    // PUT /categories/1   body: {"name":"...","description":"..."}
    @PutMapping("/updateCategoryById/{id}")
    @PreAuthorize(ADMIN)
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return service.update(id, request);
    }

    // PUT /categories/1/status   body: {"active":false}
    @PutMapping("/UpdateCategoryStatusById/{id}")
    @PreAuthorize(ADMIN)
    public CategoryResponse setStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return service.setActive(id, request.active());
    }
}
