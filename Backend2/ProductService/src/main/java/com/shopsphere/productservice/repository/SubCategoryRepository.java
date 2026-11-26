package com.shopsphere.productservice.repository;
import java.util.List;
import com.shopsphere.productservice.models.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {

    List<SubCategory> findAllByOrderByNameAsc();

    // the second dropdown: sub-categories of the chosen category
    List<SubCategory> findByCategoryIdOrderByNameAsc(Long categoryId);

    // a sub-category name only has to be unique inside its own category
    boolean existsByCategoryIdAndNameIgnoreCase(Long categoryId, String name);

    boolean existsByCategoryIdAndNameIgnoreCaseAndIdNot(Long categoryId, String name, Long id);
}
