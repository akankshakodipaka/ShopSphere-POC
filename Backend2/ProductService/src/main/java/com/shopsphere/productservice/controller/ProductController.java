package com.shopsphere.productservice.controller;
import com.shopsphere.productservice.service.ProductService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
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
import com.shopsphere.productservice.dto.Dtos.ProductRequest;
import com.shopsphere.productservice.dto.Dtos.ProductResponse;
import com.shopsphere.productservice.dto.Dtos.StockRequest;
import com.shopsphere.productservice.dto.Dtos.StatusRequest;


/**
 * Browsing and searching is public. Managing products needs ADMIN or SUPER_ADMIN.
 * The two stock endpoints are for the Order service and accept any logged-in caller.
 */
@RestController
@RequestMapping("/v1")
public class ProductController {

    private static final String ADMIN = "hasAnyRole('ADMIN','SUPER_ADMIN')";

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    // POST /products   body: {"subCategoryId":1,"name":"Headphones","description":"...","price":2499.00,"stock":24,"imageUrl":"..."}
    @PostMapping("/createProduct")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ADMIN)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return service.create(request);
    }

    // GET /products?name=head&categoryId=1&subCategoryId=2&minPrice=500&maxPrice=3000 : search, every filter optional
    @GetMapping("/getProductDetails")
    public List<ProductResponse> search(@RequestParam(required = false) String name,
                                        @RequestParam(required = false) Long categoryId,
                                        @RequestParam(required = false) Long subCategoryId,
                                        @RequestParam(required = false) BigDecimal minPrice,
                                        @RequestParam(required = false) BigDecimal maxPrice) {
        return service.search(name, categoryId, subCategoryId, minPrice, maxPrice);
    }

    // GET /products/all : admin list including inactive products
    @GetMapping("/getAllProducts")
    @PreAuthorize(ADMIN)
    public List<ProductResponse> listAll() {
        return service.listAll();
    }

    // GET /products/1 : the Cart service calls this for name and price
    @GetMapping("/getProductById/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    // PUT /products/1   body: same as create
    @PutMapping("/updateProductById/{id}")
    @PreAuthorize(ADMIN)
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return service.update(id, request);
    }

    // PUT /products/1/status   body: {"active":false}
    @PutMapping("/updateProductStatusById/{id}")
    @PreAuthorize(ADMIN)
    public ProductResponse setStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return service.setActive(id, request.active());
    }

    // POST /products/1/reduce-stock   body: {"quantity":2} : 204, or 409 if not enough, 404 if unknown
    @PostMapping("/reduceStockById/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reduceStock(@PathVariable Long id, @Valid @RequestBody StockRequest request) {
        service.reduceStock(id, request.quantity());
    }

    // POST /products/1/restore-stock   body: {"quantity":2} : undoes a reduction
    @PostMapping("/restoreStockById/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreStock(@PathVariable Long id, @Valid @RequestBody StockRequest request) {
        service.restoreStock(id, request.quantity());
    }
}
