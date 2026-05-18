package com.teamtask.service;

import com.teamtask.dto.request.ProjectRequest;
import com.teamtask.dto.response.ProjectResponse;
import com.teamtask.entity.Project;
import com.teamtask.entity.User;
import com.teamtask.enums.Role;
import com.teamtask.repository.ProjectRepository;
import com.teamtask.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public ProjectResponse createProject(ProjectRequest request, String email) {
        User user = getCurrentUser(email);

        if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admin can create projects");
        }

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(user)
                .build();

        project.getMembers().add(user);

        return mapToResponse(projectRepository.save(project));
    }

    public List<ProjectResponse> getProjects(String email) {
        User user = getCurrentUser(email);

        List<Project> projects;

        if (user.getRole() == Role.ADMIN) {
            projects = projectRepository.findAll();
        } else {
            projects = projectRepository.findByMembersContaining(user);
        }

        return projects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(Long id, String email) {
        User user = getCurrentUser(email);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (user.getRole() != Role.ADMIN && !project.getMembers().contains(user)) {
            throw new AccessDeniedException("You cannot access this project");
        }

        return mapToResponse(project);
    }

    public ProjectResponse updateProject(Long id, ProjectRequest request, String email) {
        User user = getCurrentUser(email);

        if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admin can update projects");
        }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        return mapToResponse(projectRepository.save(project));
    }

    public void deleteProject(Long id, String email) {
        User user = getCurrentUser(email);

        if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admin can delete projects");
        }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        projectRepository.delete(project);
    }

    public ProjectResponse addMember(Long projectId, Long userId, String email) {
        User currentUser = getCurrentUser(email);

        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admin can add members");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User member = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        project.getMembers().add(member);

        return mapToResponse(projectRepository.save(project));
    }

    private ProjectResponse mapToResponse(Project project) {
        Set<String> memberNames = project.getMembers()
                .stream()
                .map(User::getFullName)
                .collect(Collectors.toSet());

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .createdBy(project.getCreatedBy().getFullName())
                .createdAt(project.getCreatedAt())
                .members(memberNames)
                .build();
    }
}