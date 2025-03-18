package com.example.taskmanager.taskmanager.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taskmanager.taskmanager.model.Priority;
import com.example.taskmanager.taskmanager.model.Status;
import com.example.taskmanager.taskmanager.model.Task;
import com.example.taskmanager.taskmanager.model.User;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByUser(User user, Pageable pageable);
    
    Page<Task> findByUserAndStatus(User user, Status status, Pageable pageable);
    
    Page<Task> findByUserAndPriority(User user, Priority priority, Pageable pageable);
    
    Page<Task> findByUserAndStatusAndPriority(User user, Status status, Priority priority, Pageable pageable);
    
    List<Task> findByUserAndStatus(User user, Status status);
}
