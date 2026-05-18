package com.teamtask.dto.response;

import com.teamtask.enums.TaskPriority;
import com.teamtask.enums.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskPriority priority;
    private TaskStatus status;
    private LocalDate dueDate;
    private boolean overdue;
    private String projectName;
    private String assignedTo;
    private String createdBy;
    private LocalDateTime createdAt;
}