package com.shopsphere.productservice.repository;
import java.math.BigDecimal;
import java.util.List;

import com.shopsphere.productservice.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Customer search: only visible products (product, sub-category and category all active).
     * Every filter is always supplied, so the service passes "no filter" values:
     * name "" matches everything, categoryId or subCategoryId 0 means any,
     * and the prices cover the full range.
     */
    @Query("""
            select p from Product p
              join fetch p.subCategory s
              join fetch s.category c
            where p.active = true and s.active = true and c.active = true
              and lower(p.name) like lower(concat('%', :name, '%'))
              and (:categoryId = 0L or c.id = :categoryId)
              and (:subCategoryId = 0L or s.id = :subCategoryId)
              and p.price >= :minPrice and p.price <= :maxPrice
            order by p.name
            """)
    List<Product> search(@Param("name") String name,
                         @Param("categoryId") Long categoryId,
                         @Param("subCategoryId") Long subCategoryId,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice);

    /** Admin list: every product, including inactive ones. */
    @Query("""
            select p from Product p
              join fetch p.subCategory s
              join fetch s.category c
            order by p.name
            """)
    List<Product> findAllWithParents();

    /**
     * Takes stock in ONE atomic statement: the check and the subtraction happen together,
     * so two orders for the last item cannot both succeed.
     * Returns 1 if it worked, 0 if the product is missing, inactive or short of stock.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Product p set p.stock = p.stock - :quantity "
            + "where p.id = :id and p.active = true and p.stock >= :quantity")
    int reduceStock(@Param("id") Long id, @Param("quantity") int quantity);

    /** Puts stock back. Returns 1 if the product exists, 0 if not. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Product p set p.stock = p.stock + :quantity where p.id = :id")
    int restoreStock(@Param("id") Long id, @Param("quantity") int quantity);
}
