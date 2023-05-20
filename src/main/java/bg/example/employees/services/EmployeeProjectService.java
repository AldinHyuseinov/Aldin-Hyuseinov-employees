package bg.example.employees.services;

import bg.example.employees.models.dto.EmployeeWorkPeriodDTO;
import bg.example.employees.models.entities.EmployeeProject;
import bg.example.employees.repositories.EmployeeProjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class EmployeeProjectService {
    private final EmployeeProjectRepository employeeProjectRepository;

    public void saveData(MultipartFile file) {

        if (employeeProjectRepository.count() > 0) {
            employeeProjectRepository.deleteAll();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line = reader.readLine();

            if (hasHeader(line)) {
                line = reader.readLine();
            }

            while (line != null) {
                String[] fields = line.split(",");

                Long empID = Long.parseLong(fields[0]);
                Long projectID = Long.parseLong(fields[1]);
                LocalDate dateFrom = parseDate(fields[2]);
                String dateToString = fields[3];
                LocalDate dateTo;

                if (dateToString.equals("NULL")) {
                    dateTo = LocalDate.now();
                } else {
                    dateTo = parseDate(dateToString);
                }

                EmployeeProject employeeProject = new EmployeeProject();
                employeeProject.setEmpId(empID);
                employeeProject.setProjectId(projectID);
                employeeProject.setDateFrom(dateFrom);
                employeeProject.setDateTo(dateTo);

                employeeProjectRepository.save(employeeProject);

                line = reader.readLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public List<EmployeeWorkPeriodDTO> getResults() {
        List<Long> distinctProjectIds = employeeProjectRepository.findAllDistinctProjectIds();
        List<EmployeeWorkPeriodDTO> results = new ArrayList<>();

        for (Long projectId : distinctProjectIds) {
            List<EmployeeProject> employeeProjects = employeeProjectRepository.findAllByProjectId(projectId);

            if (employeeProjects.size() > 1) {

                for (int i = 0; i < employeeProjects.size(); i++) {

                    for (int j = i + 1; j < employeeProjects.size(); j++) {
                        EmployeeProject firstEmpProject = employeeProjects.get(i);
                        EmployeeProject secEmpProject = employeeProjects.get(j);

                        LocalDate firstStartDate = firstEmpProject.getDateFrom();
                        LocalDate firstEndDate = firstEmpProject.getDateTo();

                        LocalDate secStartDate = secEmpProject.getDateFrom();
                        LocalDate secEndDate = secEmpProject.getDateTo();

                        long workPeriodDays = 0;

                        if (!firstStartDate.isAfter(secEndDate) && !secStartDate.isAfter(firstEndDate)) {
                            LocalDate overlapStartDate = firstStartDate.isAfter(secStartDate) ? firstStartDate : secStartDate;
                            LocalDate overlapEndDate = firstEndDate.isBefore(secEndDate) ? firstEndDate : secEndDate;

                            workPeriodDays = ChronoUnit.DAYS.between(overlapStartDate, overlapEndDate);
                        }

                        EmployeeWorkPeriodDTO result = new EmployeeWorkPeriodDTO();
                        result.setEmpId1(firstEmpProject.getEmpId());
                        result.setEmpId2(secEmpProject.getEmpId());
                        result.setProjectId(projectId);
                        result.setDuration(workPeriodDays);

                        results.add(result);
                    }
                }
            }
        }

        return results.stream().sorted(Comparator.comparingLong(EmployeeWorkPeriodDTO::getDuration).reversed())
                .collect(Collectors.toList());
    }

    private boolean hasHeader(String line) {
        return line != null && line.contains("EmpID,ProjectID,DateFrom,DateTo");
    }

    private LocalDate parseDate(String dateString) {
        LocalDate parsedDate = null;

        // Array of potential date formats to try
        String[] dateFormats = {
                "yyyy-MM-dd",
                "MM/dd/yyyy",
                "dd/MM/yyyy",
                "yyyyMMdd",
                "MM/DD/YY",
                "DD/MM/YY",
                "YY/MM/DD",
                "yyyy/MM/dd",
                "dd.MM.yyyy"
        };

        // Attempt to parse the date using each format
        for (String format : dateFormats) {

            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                parsedDate = LocalDate.parse(dateString, formatter);
                break;
            } catch (DateTimeParseException e) {
                // Parsing failed for the current format, try the next one
            }
        }

        if (parsedDate == null) {
            throw new DateTimeException("Unsupported date format, contained in file.");
        }

        return parsedDate;
    }
}
