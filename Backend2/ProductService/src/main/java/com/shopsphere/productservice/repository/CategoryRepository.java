package com.shopsphere.productservice.repository;
import java.util.List;

import com.shopsphere.productservice.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // all categories, sorted by name
    List<Category> findAllByOrderByNameAsc();

    // only active categories, sorted by name
    List<Category> findByActiveTrueOrderByNameAsc();

    // duplicate checks, ignoring upper or lower case
    boolean existsByNameIgnoreCase(String name);

    // same check for an update: ignore the category being edited
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
