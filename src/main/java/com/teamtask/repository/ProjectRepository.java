package com.teamtask.repository;

import com.teamtask.entity.Project;
import com.teamtask.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByMembersContaining(User user);

    List<Project> findByCreatedBy(User user);

    long countByMembersContaining(User user);
}