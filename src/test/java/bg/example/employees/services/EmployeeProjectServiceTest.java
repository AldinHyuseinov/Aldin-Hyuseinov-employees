package bg.example.employees.services;

import bg.example.employees.models.dto.EmployeeWorkPeriodDTO;
import bg.example.employees.models.entities.EmployeeProject;
import bg.example.employees.repositories.EmployeeProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeProjectServiceTest {
    @Mock
    private EmployeeProjectRepository mockEmployeeProjectRepository;

    private EmployeeProjectService employeeProjectService;

    @BeforeEach
    void setup() {
        employeeProjectService = new EmployeeProjectService(mockEmployeeProjectRepository);
    }

    @Test
    void saveDataShouldSaveEmployeeProjects() {
        MockMultipartFile file = new MockMultipartFile("data", "test.csv",
                "text/csv", "143,12,2013-11-01,2014-01-05\n218,10,2012-05-16,NULL".getBytes());

        employeeProjectService.saveData(file);

        verify(mockEmployeeProjectRepository, times(2)).save(any());

        file = new MockMultipartFile("data", "test.csv",
                "text/csv", "13,13,13.12.2013,20230301\n14,13,12.12.2012,05.04.2023".getBytes());

        when(mockEmployeeProjectRepository.count()).thenReturn(2L);

        employeeProjectService.saveData(file);

        verify(mockEmployeeProjectRepository).deleteAll();
        verify(mockEmployeeProjectRepository, times(4)).save(any());
    }

    @Test
    void saveDataShouldThrowExceptionWhenUnsupportedDateFormat() {
        MockMultipartFile csvFile = new MockMultipartFile("file", "test.csv",
                "text/csv", "143,12,Jan 11 2013,2023/05/19".getBytes());

        assertThrows(DateTimeException.class, () -> employeeProjectService.saveData(csvFile));
    }

    @Test
    void getResultsShouldReturnSortedEmployeeWorkPeriods() {
        List<EmployeeProject> employeeProjects = List.of(
                createEmployeeProject(143L),
                createEmployeeProject(218L)
        );

        when(mockEmployeeProjectRepository.findAllDistinctProjectIds()).thenReturn(List.of(10L));
        when(mockEmployeeProjectRepository.findAllByProjectId(10L)).thenReturn(employeeProjects);

        List<EmployeeWorkPeriodDTO> results = employeeProjectService.getResults();

        assertEquals(1, results.size());
        EmployeeWorkPeriodDTO result = results.get(0);
        assertEquals(143L, result.getEmpId1());
        assertEquals(218L, result.getEmpId2());
        assertEquals(10L, result.getProjectId());
        assertEquals(365, result.getDuration());
    }

    private EmployeeProject createEmployeeProject(Long empId) {
        EmployeeProject employeeProject = new EmployeeProject();
        employeeProject.setEmpId(empId);
        employeeProject.setProjectId(10L);
        employeeProject.setDateFrom(LocalDate.parse("2013-11-01"));
        employeeProject.setDateTo(LocalDate.parse("2014-11-01"));

        return employeeProject;
    }
}
