package com.teamtask.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectProgressResponse {

    private Long projectId;
    private String projectName;
    private long totalTasks;
    private long completedTasks;
    private double progressPercentage;
}