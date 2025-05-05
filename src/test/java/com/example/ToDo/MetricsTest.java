package com.example.ToDo;

import com.example.ToDo.entities.Metrics;
import com.example.ToDo.entities.ToDo;
import com.example.ToDo.repository.ToDoRepository;
import com.example.ToDo.services.ToDoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MetricsTest {

    @Autowired
    private ToDoService toDoService;

    @Autowired
    private ToDoRepository toDoRepository;

    private LocalDate today;
    private LocalDate yesterday;
    private LocalDate twoDaysAgo;
    private LocalDate threeDaysAgo;
    private LocalDate fourDaysAgo;
    private LocalDate fiveDaysAgo;

    @BeforeEach
    void setup() {
        // Clear the repository
        toDoRepository.deleteAll();

        // Set up dates
        today = LocalDate.now();
        yesterday = today.minusDays(1);
        twoDaysAgo = today.minusDays(2);
        threeDaysAgo = today.minusDays(3);
        fourDaysAgo = today.minusDays(4);
        fiveDaysAgo = today.minusDays(5);
    }

    @Test
    void testMetricsWithMixedPriorities() {
        // Create tasks of different priorities with different completion times
        List<ToDo> todos = Arrays.asList(
            // High priority tasks (5 days, 3 days)
            new ToDo("High Task 1", "Important task 1", "High", true, fiveDaysAgo, today, fiveDaysAgo), // 5 days to complete
            new ToDo("High Task 2", "Important task 2", "High", true, fourDaysAgo, yesterday, fourDaysAgo), // 3 days to complete
            
            // Medium priority tasks (4 days, 2 days)
            new ToDo("Medium Task 1", "Regular task 1", "Medium", true, fourDaysAgo, today, fourDaysAgo), // 4 days to complete
            new ToDo("Medium Task 2", "Regular task 2", "Medium", true, threeDaysAgo, yesterday, threeDaysAgo), // 2 days to complete
            
            // Low priority tasks (3 days, 1 day)
            new ToDo("Low Task 1", "Less important task 1", "Low", true, threeDaysAgo, today, threeDaysAgo), // 3 days to complete
            new ToDo("Low Task 2", "Less important task 2", "Low", true, twoDaysAgo, yesterday, twoDaysAgo)  // 1 day to complete
        );

        toDoRepository.saveAll(todos);
        
        Metrics metrics = toDoService.getMetrics();
        
        // 5+3+4+2+3+1 = 18 days total, divided by 6 tasks = 3 days average
        assertThat(metrics.getTotalAverage()).isEqualTo(3);
        
        // 5+3 = 8 days for high priority, divided by 2 tasks = 4 days average
        assertThat(metrics.getHighAverage()).isEqualTo(4);
        
        // 4+2 = 6 days for medium priority, divided by 2 tasks = 3 days average
        assertThat(metrics.getMediumAverage()).isEqualTo(3);
        
        // 3+1 = 4 days for low priority, divided by 2 tasks = 2 days average
        assertThat(metrics.getLowAverage()).isEqualTo(2);
    }

    @Test
    void testMetricsWithOnlyHighPriority() {
        List<ToDo> todos = Arrays.asList(
            new ToDo("High Task 1", "Important task 1", "High", true, fiveDaysAgo, today, fiveDaysAgo), // 5 days to complete
            new ToDo("High Task 2", "Important task 2", "High", true, fourDaysAgo, yesterday, fourDaysAgo), // 3 days to complete
            new ToDo("High Task 3", "Important task 3", "High", true, threeDaysAgo, today, threeDaysAgo) // 3 days to complete
        );

        toDoRepository.saveAll(todos);
        
        Metrics metrics = toDoService.getMetrics();
        
        // 5+3+3 = 11 days total, divided by 3 tasks = 3.67 days, but it's truncated to 3 (long type)
        assertThat(metrics.getTotalAverage()).isEqualTo(3);
        
        // 11 days for high priority, divided by 3 tasks = 3.67 days, truncated to 3
        assertThat(metrics.getHighAverage()).isEqualTo(3);
        
        // No medium priority tasks
        assertThat(metrics.getMediumAverage()).isEqualTo(0);
        
        // No low priority tasks
        assertThat(metrics.getLowAverage()).isEqualTo(0);
    }

    @Test
    void testMetricsWithNoCompletedTasks() {
        // Create tasks that are not completed (status=false)
        List<ToDo> todos = Arrays.asList(
            new ToDo("High Task", "Important task", "High", false, tomorrow(), null, today),
            new ToDo("Medium Task", "Regular task", "Medium", false, tomorrow(), null, today),
            new ToDo("Low Task", "Less important task", "Low", false, tomorrow(), null, today)
        );

        toDoRepository.saveAll(todos);
        
        Metrics metrics = toDoService.getMetrics();
        
        // No completed tasks, so all averages should be 0
        assertThat(metrics.getTotalAverage()).isEqualTo(0);
        assertThat(metrics.getHighAverage()).isEqualTo(0);
        assertThat(metrics.getMediumAverage()).isEqualTo(0);
        assertThat(metrics.getLowAverage()).isEqualTo(0);
    }

    @Test
    void testMetricsWithSameDayCompletion() {
        // Create tasks that are completed on the same day they are created
        List<ToDo> todos = Arrays.asList(
            new ToDo("High Task", "Important task", "High", true, today, today, today), // 0 days to complete
            new ToDo("Medium Task", "Regular task", "Medium", true, today, today, today), // 0 days to complete
            new ToDo("Low Task", "Less important task", "Low", true, today, today, today) // 0 days to complete
        );

        toDoRepository.saveAll(todos);
        
        Metrics metrics = toDoService.getMetrics();
        
        // All tasks completed on same day, so all averages should be 0
        assertThat(metrics.getTotalAverage()).isEqualTo(0);
        assertThat(metrics.getHighAverage()).isEqualTo(0);
        assertThat(metrics.getMediumAverage()).isEqualTo(0);
        assertThat(metrics.getLowAverage()).isEqualTo(0);
    }

    @Test
    void testMetricsWithEmptyRepository() {
        // No tasks in repository
        Metrics metrics = toDoService.getMetrics();
        
        // No tasks, so all averages should be 0
        assertThat(metrics.getTotalAverage()).isEqualTo(0);
        assertThat(metrics.getHighAverage()).isEqualTo(0);
        assertThat(metrics.getMediumAverage()).isEqualTo(0);
        assertThat(metrics.getLowAverage()).isEqualTo(0);
    }

    // Helper method to get tomorrow's date
    private LocalDate tomorrow() {
        return LocalDate.now().plusDays(1);
    }
} 