package com.shopsphere.productservice.models;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/** Top level of the catalog, for example Electronics. Has many sub-categories. */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    // inactive categories are hidden from customers and cannot receive new sub-categories
    @Column(nullable = false)
    private boolean active = true;

    // mappedBy = the "category" field in SubCategory owns the relationship
    @OneToMany(mappedBy = "category")
    private List<SubCategory> subCategories = new ArrayList<>();

    protected Category() {}   // JPA needs a no-argument constructor

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<SubCategory> getSubCategories() { return subCategories; }
}
