package com.vctek.orderservice.repository;

import com.vctek.orderservice.model.TagModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TagRepository extends JpaRepository<TagModel, Long> {
    TagModel findByIdAndCompanyId(Long id, Long companyId);

    Page<TagModel> findAllByCompanyId(Long companyId, Pageable pageable);

    Page<TagModel> findAllByNameContainingAndCompanyId(String name, Long companyId, Pageable pageable);

    List<TagModel> findByCompanyIdAndName(Long companyId, String name);

    @Query(nativeQuery=true,
            value = "SELECT * FROM tag as t JOIN order_has_tag as oht on t.id = oht.tag_id " +
                    "WHERE oht.order_id = ?1")
    List<TagModel> findAllByOrder(Long orderId);
}
