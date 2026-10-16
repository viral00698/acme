package com.acme.salary.employee.repository;

import com.acme.salary.employee.entity.Employee;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class EmployeeSpecification {

    private EmployeeSpecification() {}

    public static Specification<Employee> filterBy(
            String search,
            Long departmentId,
            Long countryId,
            Long jobTitleId,
            String status) {

        return (root, query, cb) -> {
            // Join fetch reference entities to avoid N+1 query issue for list queries.
            // Distinguish data query from count query to prevent duplicate joins and count failures.
            boolean isCountQuery = query != null && (
                    Long.class.equals(query.getResultType()) ||
                    long.class.equals(query.getResultType()) ||
                    Number.class.isAssignableFrom(query.getResultType())
            );

            if (!isCountQuery) {
                root.fetch("department", JoinType.LEFT);
                root.fetch("country", JoinType.LEFT);
                root.fetch("jobTitle", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate codeMatch = cb.like(cb.lower(root.get("employeeCode")), pattern);
                Predicate firstNameMatch = cb.like(cb.lower(root.get("firstName")), pattern);
                Predicate lastNameMatch = cb.like(cb.lower(root.get("lastName")), pattern);
                Predicate emailMatch = cb.like(cb.lower(root.get("email")), pattern);
                predicates.add(cb.or(codeMatch, firstNameMatch, lastNameMatch, emailMatch));
            }

            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }

            if (countryId != null) {
                predicates.add(cb.equal(root.get("country").get("id"), countryId));
            }

            if (jobTitleId != null) {
                predicates.add(cb.equal(root.get("jobTitle").get("id"), jobTitleId));
            }

            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(cb.upper(root.get("status")), status.trim().toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
