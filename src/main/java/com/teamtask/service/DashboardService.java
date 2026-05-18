package com.teamtask.service;

import com.teamtask.dto.response.DashboardResponse;
import com.teamtask.dto.response.ProjectProgressResponse;
import com.teamtask.dto.response.TaskResponse;
import com.teamtask.entity.Project;
import com.teamtask.entity.Task;
import com.teamtask.entity.User;
import com.teamtask.enums.Role;
import com.teamtask.enums.TaskStatus;
import com.teamtask.repository.ProjectRepository;
import com.teamtask.repository.TaskRepository;
import com.teamtask.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public DashboardService(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            TaskRepository taskRepository
    ) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    public DashboardResponse getDashboard(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            return getAdminDashboard();
        }

        return getMemberDashboard(user);
    }

    private DashboardResponse getAdminDashboard() {
        long totalProjects = projectRepository.count();
        long totalTasks = taskRepository.count();
        long completedTasks = taskRepository.countByStatus(TaskStatus.COMPLETED);
        long pendingTasks = taskRepository.countByStatus(TaskStatus.TODO);
        long inProgressTasks = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long overdueTasks = taskRepository.countByDueDateBeforeAndStatusNot(
                LocalDate.now(),
                TaskStatus.COMPLETED
        );

        List<TaskResponse> recentTasks = taskRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapTaskToResponse)
                .toList();

        List<ProjectProgressResponse> projectProgress = projectRepository.findAll()
                .stream()
                .map(this::mapProjectProgress)
                .toList();

        return DashboardResponse.builder()
                .totalProjects(totalProjects)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .inProgressTasks(inProgressTasks)
                .overdueTasks(overdueTasks)
                .recentTasks(recentTasks)
                .projectProgress(projectProgress)
                .build();
    }

    private DashboardResponse getMemberDashboard(User user) {
        long totalProjects = projectRepository.countByMembersContaining(user);
        long totalTasks = taskRepository.countByAssignedTo(user);
        long completedTasks = taskRepository.countByAssignedToAndStatus(user, TaskStatus.COMPLETED);
        long pendingTasks = taskRepository.countByAssignedToAndStatus(user, TaskStatus.TODO);
        long inProgressTasks = taskRepository.countByAssignedToAndStatus(user, TaskStatus.IN_PROGRESS);
        long overdueTasks = taskRepository.countByAssignedToAndDueDateBeforeAndStatusNot(
                user,
                LocalDate.now(),
                TaskStatus.COMPLETED
        );

        List<TaskResponse> recentTasks = taskRepository.findTop5ByAssignedToOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapTaskToResponse)
                .toList();

        List<ProjectProgressResponse> projectProgress = projectRepository.findByMembersContaining(user)
                .stream()
                .map(this::mapProjectProgress)
                .toList();

        return DashboardResponse.builder()
                .totalProjects(totalProjects)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .inProgressTasks(inProgressTasks)
                .overdueTasks(overdueTasks)
                .recentTasks(recentTasks)
                .projectProgress(projectProgress)
                .build();
    }

    private ProjectProgressResponse mapProjectProgress(Project project) {
        List<Task> tasks = taskRepository.findByProject(project);

        long totalTasks = tasks.size();

        long completedTasks = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .count();

        double progressPercentage = totalTasks == 0
                ? 0
                : ((double) completedTasks / totalTasks) * 100;

        return ProjectProgressResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .progressPercentage(progressPercentage)
                .build();
    }

    private TaskResponse mapTaskToResponse(Task task) {
        boolean overdue = task.getDueDate() != null
                && task.getDueDate().isBefore(LocalDate.now())
                && task.getStatus() != TaskStatus.COMPLETED;

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .overdue(overdue)
                .projectName(task.getProject().getName())
                .assignedTo(task.getAssignedTo().getFullName())
                .createdBy(task.getCreatedBy().getFullName())
                .createdAt(task.getCreatedAt())
                .build();
    }
}