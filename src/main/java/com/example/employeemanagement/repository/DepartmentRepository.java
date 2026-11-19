package com.example.employeemanagement.repository;

import java.util.List;

import com.example.employeemanagement.model.Department;
import com.example.employeemanagement.dto.DepartmentStatistics;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Query("""
            select new com.example.employeemanagement.dto.DepartmentStatistics(d.id, d.name, count(e))
            from Department d left join Employee e on e.department = d
            group by d.id, d.name
            order by d.name, d.id
            """)
    List<DepartmentStatistics> countEmployeesByDepartment();
}
