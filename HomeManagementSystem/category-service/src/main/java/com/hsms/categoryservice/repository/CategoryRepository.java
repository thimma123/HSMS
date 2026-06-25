package com.hsms.categoryservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hsms.categoryservice.entity.ServiceCategory;

public interface CategoryRepository extends JpaRepository<ServiceCategory, Long>{
	
	List<ServiceCategory> findByActiveTrue(); // this generates: select * from category_tbl where active = true;
	
	boolean existsByCategoryName(String categoryName);


}