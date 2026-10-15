package com.acme.salary.employee.repository;

import com.acme.salary.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findByEmail(String email);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByEmail(String email);

    long countByStatus(String status);

    @Query("SELECT e FROM Employee e " +
           "JOIN FETCH e.department " +
           "JOIN FETCH e.country " +
           "JOIN FETCH e.jobTitle " +
           "WHERE e.id = :id")
    Optional<Employee> findByIdWithDetails(@Param("id") Long id);
}
