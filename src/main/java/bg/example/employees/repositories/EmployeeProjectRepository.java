package bg.example.employees.repositories;

import bg.example.employees.models.entities.EmployeeProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeProjectRepository extends JpaRepository<EmployeeProject, Long> {
    @Query("SELECT DISTINCT ep.projectId FROM EmployeeProject ep")
    List<Long> findAllDistinctProjectIds();

    List<EmployeeProject> findAllByProjectId(Long projectId);
}
