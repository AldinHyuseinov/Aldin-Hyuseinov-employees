package bg.example.employees.models.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class EmployeeWorkPeriodDTO {
    private Long empId1;

    private Long empId2;

    private Long projectId;

    private Long duration;
}
