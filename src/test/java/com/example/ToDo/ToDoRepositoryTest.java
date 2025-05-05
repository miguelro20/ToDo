package com.example.ToDo;

import com.example.ToDo.entities.ToDo;
import com.example.ToDo.repository.ToDoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ToDoRepositoryTest {

    @Autowired
    private ToDoRepository toDoRepository;

    private LocalDate today;
    private LocalDate tomorrow;
    private LocalDate yesterday;

    @BeforeEach
    void setup() {
        // Clear the repository
        toDoRepository.deleteAll();

        // Set up dates
        today = LocalDate.now();
        tomorrow = today.plusDays(1);
        yesterday = today.minusDays(1);

        // Create test data
        List<ToDo> todos = Arrays.asList(
            new ToDo("High Priority Task", "Important task", "High", false, tomorrow, null, today),
            new ToDo("Medium Priority Task", "Regular task", "Medium", true, yesterday, today, yesterday),
            new ToDo("Low Priority Task", "Less important task", "Low", false, tomorrow, null, today),
            new ToDo("Completed High Task", "Completed important task", "High", true, yesterday, today, yesterday),
            new ToDo("Another Medium Task", "Another regular task", "Medium", false, tomorrow, null, today)
        );

        // Save all todos
        toDoRepository.saveAll(todos);
    }

    @Test
    void testFindByFiltersWithNameFilter() {
        // Test filtering by name
        Page<ToDo> result = toDoRepository.findByFilters(
                "Medium",
                null,
                null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"))
        );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(todo -> todo.getName().contains("Medium"));
    }

    @Test
    void testFindByFiltersWithStatusFilter() {
        // Test filtering by status (completed tasks)
        Page<ToDo> result = toDoRepository.findByFilters(
                null,
                true,
                null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"))
        );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(todo -> todo.isStatus());
    }

    @Test
    void testFindByFiltersWithPriorityFilter() {
        // Test filtering by priority
        Page<ToDo> result = toDoRepository.findByFilters(
                null,
                null,
                "High",
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"))
        );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(todo -> todo.getPriority().equals("High"));
    }

    @Test
    void testFindByFiltersWithCombinedFilters() {
        // Test combined filters
        Page<ToDo> result = toDoRepository.findByFilters(
                "Task",
                false,
                "Medium",
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"))
        );

        assertThat(result.getContent()).hasSize(1);
        ToDo todo = result.getContent().get(0);
        assertThat(todo.getName()).contains("Medium");
        assertThat(todo.isStatus()).isFalse();
        assertThat(todo.getPriority()).isEqualTo("Medium");
    }

    @Test
    void testSortingByDueDateAscending() {
        // Test sorting by due date ascending
        Page<ToDo> result = toDoRepository.findByFilters(
                null,
                null,
                null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "dueDate"))
        );

        assertThat(result.getContent()).hasSize(5);
        // The first tasks should have earlier due dates (yesterday)
        assertThat(result.getContent().get(0).getDueDate()).isEqualTo(yesterday);
        assertThat(result.getContent().get(1).getDueDate()).isEqualTo(yesterday);
    }

    @Test
    void testSortingByDueDateDescending() {
        // Test sorting by due date descending
        Page<ToDo> result = toDoRepository.findByFilters(
                null,
                null,
                null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "dueDate"))
        );

        assertThat(result.getContent()).hasSize(5);
        // The first tasks should have later due dates (tomorrow)
        assertThat(result.getContent().get(0).getDueDate()).isEqualTo(tomorrow);
        assertThat(result.getContent().get(1).getDueDate()).isEqualTo(tomorrow);
        assertThat(result.getContent().get(2).getDueDate()).isEqualTo(tomorrow);
    }

    @Test
    void testSortingByPriorityAndName() {
        // Test sorting by multiple fields
        Page<ToDo> result = toDoRepository.findByFilters(
                null,
                null,
                null,
                PageRequest.of(0, 10, Sort.by(
                        Sort.Order.desc("priority"),
                        Sort.Order.asc("name")))
        );

        assertThat(result.getContent()).hasSize(5);
        
        // In lexicographical order with descending sort:
        // "Medium" comes before "Low" which comes before "High" (M > L > H)
        // So the order should be: Medium -> Low -> High
        // This is because lexicographically, 'M' comes after 'L' which comes after 'H'
        
        // First should be "Medium" priority items when sorting by priority desc
        assertThat(result.getContent().get(0).getPriority()).isEqualTo("Medium");
        assertThat(result.getContent().get(1).getPriority()).isEqualTo("Medium");
        
        // Within the same priority, names should be in alphabetical order
        if (result.getContent().get(0).getPriority().equals(result.getContent().get(1).getPriority())) {
            assertThat(result.getContent().get(0).getName().compareTo(result.getContent().get(1).getName()) <= 0).isTrue();
        }
        
        // Verify that all items are sorted by priority first (Medium -> Low -> High)
        List<String> priorities = result.getContent().stream()
                .map(ToDo::getPriority)
                .toList();
                
        // Check that all Medium priorities come before Low priorities
        int lastMediumIndex = -1;
        int firstLowIndex = -1;
        int lastLowIndex = -1;
        int firstHighIndex = -1;
        
        for (int i = 0; i < priorities.size(); i++) {
            if (priorities.get(i).equals("Medium")) {
                lastMediumIndex = i;
            } else if (priorities.get(i).equals("Low") && firstLowIndex == -1) {
                firstLowIndex = i;
            } else if (priorities.get(i).equals("Low")) {
                lastLowIndex = i;
            } else if (priorities.get(i).equals("High") && firstHighIndex == -1) {
                firstHighIndex = i;
            }
        }
        
        if (lastMediumIndex != -1 && firstLowIndex != -1) {
            assertThat(lastMediumIndex).isLessThan(firstLowIndex);
        }
        
        if (lastLowIndex != -1 && firstHighIndex != -1) {
            assertThat(lastLowIndex).isLessThan(firstHighIndex);
        }
    }

    @Test
    void testPagination() {
        // Test pagination
        int pageSize = 2;
        
        Page<ToDo> page1 = toDoRepository.findByFilters(
                null,
                null,
                null,
                PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, "id"))
        );
        
        Page<ToDo> page2 = toDoRepository.findByFilters(
                null,
                null,
                null,
                PageRequest.of(1, pageSize, Sort.by(Sort.Direction.ASC, "id"))
        );
        
        Page<ToDo> page3 = toDoRepository.findByFilters(
                null,
                null,
                null,
                PageRequest.of(2, pageSize, Sort.by(Sort.Direction.ASC, "id"))
        );

        assertThat(page1.getContent()).hasSize(2);
        assertThat(page2.getContent()).hasSize(2);
        assertThat(page3.getContent()).hasSize(1);
        
        // Ensure no overlap between pages
        List<Long> idsPage1 = page1.getContent().stream().map(ToDo::getId).toList();
        List<Long> idsPage2 = page2.getContent().stream().map(ToDo::getId).toList();
        
        assertThat(idsPage1).doesNotContainAnyElementsOf(idsPage2);
    }
} 