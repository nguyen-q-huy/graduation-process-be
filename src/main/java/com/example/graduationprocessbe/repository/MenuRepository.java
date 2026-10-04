package com.example.graduationprocessbe.repository;

import com.example.graduationprocessbe.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, String> {
    Optional<Menu> findByCode(String code);
    boolean existsByCode(String code);
    List<Menu> findByActiveTrueOrderBySortOrderAsc();
    List<Menu> findByParentIdIsNullAndActiveTrueOrderBySortOrderAsc();
    List<Menu> findByParentIdAndActiveTrueOrderBySortOrderAsc(String parentId);
}
