package com.example.taskmanager.taskmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taskmanager.taskmanager.dto.TaskDTO;
import com.example.taskmanager.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.taskmanager.model.Priority;
import com.example.taskmanager.taskmanager.model.Status;
import com.example.taskmanager.taskmanager.model.Task;
import com.example.taskmanager.taskmanager.model.User;
import com.example.taskmanager.taskmanager.repository.TaskRepository;
import com.example.taskmanager.taskmanager.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskManagerScheduler taskScheduler;

    @Transactional
    public TaskDTO createTask(String username, TaskDTO taskDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.getStatus() != null ? taskDTO.getStatus() : Status.PENDING);
        task.setPriority(taskDTO.getPriority() != null ? taskDTO.getPriority() : Priority.MEDIUM);
        task.setUser(user);

        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "#taskId")
    public TaskDTO getTaskById(Long taskId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Task not found with id: " + taskId);
        }

        return convertToDTO(task);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tasks-by-user", key = "{#username, #page, #size, #status, #priority}")
    public Page<TaskDTO> getTasks(String username, int page, int size, Status status, Priority priority,
            String sortBy) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Sort sort = Sort.by(sortBy != null ? sortBy : "createdAt").descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Task> taskPage;

        if (status != null && priority != null) {
            taskPage = taskRepository.findByUserAndStatusAndPriority(user, status, priority, pageable);
        } else if (status != null) {
            taskPage = taskRepository.findByUserAndStatus(user, status, pageable);
        } else if (priority != null) {
            taskPage = taskRepository.findByUserAndPriority(user, priority, pageable);
        } else {
            taskPage = taskRepository.findByUser(user, pageable);
        }

        return taskPage.map(this::convertToDTO);

    }

    @Transactional
    @CacheEvict(value = { "tasks", "tasks-by-user" }, allEntries = true)
    public TaskDTO updateTask(Long taskId, TaskDTO taskDTO, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Task not found with id: " + taskId);
        }

        if (taskDTO.getTitle() != null) {
            task.setTitle(taskDTO.getTitle());
        }

        if (taskDTO.getDescription() != null) {
            task.setDescription(taskDTO.getDescription());
        }

        if (taskDTO.getStatus() != null) {
            task.setStatus(taskDTO.getStatus());
        }

        if (taskDTO.getPriority() != null) {
            task.setPriority(taskDTO.getPriority());
        }

        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);

    }

    @Transactional
    @CacheEvict(value = { "tasks", "tasks-by-user" }, allEntries = true)
    public void deleteTask(Long taskId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Task not found with id: " + taskId);
        }

        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getPrioritizedTasks(String username, int limit) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<Task> prioritizedTasks = taskScheduler.getNextBatchOfTasks(user, limit);

        return prioritizedTasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        BeanUtils.copyProperties(task, dto);
        return dto;
    }
}
