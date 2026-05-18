package com.teamtask.service;

import com.teamtask.dto.request.TaskRequest;
import com.teamtask.dto.request.TaskStatusUpdateRequest;
import com.teamtask.dto.response.TaskResponse;
import com.teamtask.entity.Project;
import com.teamtask.entity.Task;
import com.teamtask.entity.User;
import com.teamtask.enums.Role;
import com.teamtask.enums.TaskPriority;
import com.teamtask.enums.TaskStatus;
import com.teamtask.repository.ProjectRepository;
import com.teamtask.repository.TaskRepository;
import com.teamtask.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public TaskResponse createTask(TaskRequest request, String email) {
        User currentUser = getCurrentUser(email);

        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admin can create tasks");
        }

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User assignedUser = userRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new RuntimeException("Assigned user not found"));

        if (!project.getMembers().contains(assignedUser)) {
            throw new RuntimeException("Assigned user is not a member of this project");
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(request.getStatus() == null ? TaskStatus.TODO : request.getStatus())
                .dueDate(request.getDueDate())
                .project(project)
                .assignedTo(assignedUser)
                .createdBy(currentUser)
                .build();

        return mapToResponse(taskRepository.save(task));
    }

    public List<TaskResponse> getTasks(
            String email,
            TaskStatus status,
            TaskPriority priority,
            Long projectId,
            Long assigneeId
    ) {
        User currentUser = getCurrentUser(email);

        List<Task> tasks;

        if (currentUser.getRole() == Role.ADMIN) {
            tasks = taskRepository.findAll();
        } else {
            tasks = taskRepository.findByAssignedTo(currentUser);
        }

        return tasks.stream()
                .filter(task -> status == null || task.getStatus() == status)
                .filter(task -> priority == null || task.getPriority() == priority)
                .filter(task -> projectId == null || task.getProject().getId().equals(projectId))
                .filter(task -> assigneeId == null || task.getAssignedTo().getId().equals(assigneeId))
                .map(this::mapToResponse)
                .toList();
    }

    public TaskResponse getTaskById(Long id, String email) {
        User currentUser = getCurrentUser(email);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (currentUser.getRole() != Role.ADMIN &&
                !task.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You cannot access this task");
        }

        return mapToResponse(task);
    }

    public TaskResponse updateTask(Long id, TaskRequest request, String email) {
        User currentUser = getCurrentUser(email);

        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admin can update tasks");
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User assignedUser = userRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new RuntimeException("Assigned user not found"));

        if (!project.getMembers().contains(assignedUser)) {
            throw new RuntimeException("Assigned user is not a member of this project");
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus() == null ? task.getStatus() : request.getStatus());
        task.setDueDate(request.getDueDate());
        task.setProject(project);
        task.setAssignedTo(assignedUser);

        return mapToResponse(taskRepository.save(task));
    }

    public void deleteTask(Long id, String email) {
        User currentUser = getCurrentUser(email);

        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admin can delete tasks");
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        taskRepository.delete(task);
    }

    public TaskResponse updateTaskStatus(
            Long id,
            TaskStatusUpdateRequest request,
            String email
    ) {
        User currentUser = getCurrentUser(email);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (currentUser.getRole() != Role.ADMIN &&
                !task.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can update only your assigned task status");
        }

        task.setStatus(request.getStatus());

        return mapToResponse(taskRepository.save(task));
    }

    private TaskResponse mapToResponse(Task task) {
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