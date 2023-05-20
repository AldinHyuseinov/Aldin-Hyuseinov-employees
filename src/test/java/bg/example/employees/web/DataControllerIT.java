package bg.example.employees.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DataControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void uploadCsvWithValidCsvFileReturnsRedirectToResults() throws Exception {
        MockMultipartFile csvFile = new MockMultipartFile("file", "test.csv",
                "text/csv", "143,12,2013-11-01,2014-01-05\n218,10,2012-05-16,NULL".getBytes());

        mockMvc.perform(multipart("/upload").file(csvFile))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/results"))
                .andExpect(flash().attributeCount(0));
    }

    @Test
    void uploadInvalidCsvRedirects() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile("file", "test.txt",
                "text/plain", "invalid".getBytes());

        mockMvc.perform(multipart("/upload").file(invalidFile))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("incorrectFileFormat"));
    }

    @Test
    void uploadCsvWithEmptyFileReturnsRedirectWithErrorFlashAttribute() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/upload").file(emptyFile))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("noFileSelected"));
    }

    @Test
    void uploadCsvWithUnsupportedDateFormatReturnsRedirectWithErrorFlashAttribute() throws Exception {
        MockMultipartFile csvFile = new MockMultipartFile("file", "test.csv",
                "text/csv", "143,12,Jan 11 2013,2023/05/19".getBytes());

        mockMvc.perform(multipart("/upload").file(csvFile))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("unsupportedDateFormat"));
    }

    @Test
    void resultsWithNoDataReturnsResultsViewWithNoDataAttribute() throws Exception {
        mockMvc.perform(get("/results"))
                .andExpect(status().isOk())
                .andExpect(view().name("results"))
                .andExpect(model().attributeExists("noData"))
                .andExpect(model().attribute("noData", true));
    }
}
