package bg.example.employees.web;

import bg.example.employees.models.dto.EmployeeWorkPeriodDTO;
import bg.example.employees.services.EmployeeProjectService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DateTimeException;
import java.util.List;

@Controller
@AllArgsConstructor(onConstructor_ = @Autowired)
public class DataController {
    private final EmployeeProjectService employeeProjectService;

    @PostMapping("/upload")
    public String uploadCsv(@RequestParam MultipartFile file, RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("noFileSelected", "Please select a file.");

            return "redirect:/";
        }

        if (!file.getContentType().equals("text/csv")) {
            redirectAttributes.addFlashAttribute("incorrectFileFormat", "A CSV file is required.");

            return "redirect:/";
        }

        try {
            employeeProjectService.saveData(file);
        } catch (DateTimeException e) {
            redirectAttributes.addFlashAttribute("unsupportedDateFormat", e.getMessage());

            return "redirect:/";
        }

        return "redirect:/results";
    }

    @GetMapping("/results")
    public String results(Model model) {
        List<EmployeeWorkPeriodDTO> results = employeeProjectService.getResults();

        if (results.isEmpty()) {
            model.addAttribute("noData", true);
        }
        model.addAttribute("results", results);

        return "results";
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public ModelAndView onIOException() {
        return new ModelAndView("file-error");
    }
}
