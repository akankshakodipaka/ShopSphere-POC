package com.shopsphere.productservice.controller;
import com.shopsphere.productservice.service.SubCategoryService;
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
import com.shopsphere.productservice.dto.Dtos.SubCategoryRequest;
import com.shopsphere.productservice.dto.Dtos.StatusRequest;
import com.shopsphere.productservice.dto.Dtos.SubCategoryResponse;

/** Reading is public. Creating, changing and switching on or off needs ADMIN or SUPER_ADMIN. */
@RestController
@RequestMapping("/v1")
public class SubCategoryController {

    private static final String ADMIN = "hasAnyRole('ADMIN','SUPER_ADMIN')";

    private final SubCategoryService service;

    public SubCategoryController(SubCategoryService service) {
        this.service = service;
    }

    // POST /subcategories   body: {"categoryId":1,"name":"Mobiles","description":"..."}
    @PostMapping("/createSubCategory")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ADMIN)
    public SubCategoryResponse create(@Valid @RequestBody SubCategoryRequest request) {
        return service.create(request);
    }

    // GET /subcategories?categoryId=1&activeOnly=true : the second dropdown, filled after a category is chosen
    @GetMapping("/getSubCategoryDetails")
    public List<SubCategoryResponse> list(@RequestParam(required = false) Long categoryId,
                                          @RequestParam(defaultValue = "false") boolean activeOnly) {
        return service.list(categoryId, activeOnly);
    }

    // GET /subcategories/1
    @GetMapping("/getSubCategoryDetailsById/{id}")
    public SubCategoryResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    // PUT /subcategories/1   body: same as create
    @PutMapping("/updateSubCategoriesById/{id}")
    @PreAuthorize(ADMIN)
    public SubCategoryResponse update(@PathVariable Long id, @Valid @RequestBody SubCategoryRequest request) {
        return service.update(id, request);
    }

    // PUT /subcategories/1/status   body: {"active":false}
    @PutMapping("/updateSubCategoriesStatusById/{id}")
    @PreAuthorize(ADMIN)
    public SubCategoryResponse setStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return service.setActive(id, request.active());
    }
}