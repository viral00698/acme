package com.acme.salary.organization.repository;

import com.acme.salary.organization.entity.JobTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobTitleRepository extends JpaRepository<JobTitle, Long> {
    List<JobTitle> findByDepartmentId(Long departmentId);
}
