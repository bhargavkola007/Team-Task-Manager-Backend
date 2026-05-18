package com.teamtask.repository;

import com.teamtask.entity.Project;
import com.teamtask.entity.Task;
import com.teamtask.entity.User;
import com.teamtask.enums.TaskPriority;
import com.teamtask.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAssignedTo(User user);

    List<Task> findByProject(Project project);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByPriority(TaskPriority priority);

    List<Task> findByProjectAndAssignedTo(Project project, User assignedTo);

    List<Task> findByProjectAndStatus(Project project, TaskStatus status);

    List<Task> findByProjectAndPriority(Project project, TaskPriority priority);

    long countByStatus(TaskStatus status);

    long countByDueDateBeforeAndStatusNot(LocalDate date, TaskStatus status);

    List<Task> findTop5ByOrderByCreatedAtDesc();

    List<Task> findTop5ByAssignedToOrderByCreatedAtDesc(User user);

    long countByAssignedTo(User user);

    long countByAssignedToAndStatus(User user, TaskStatus status);

    long countByAssignedToAndDueDateBeforeAndStatusNot(
            User user,
            LocalDate date,
            TaskStatus status
    );
}