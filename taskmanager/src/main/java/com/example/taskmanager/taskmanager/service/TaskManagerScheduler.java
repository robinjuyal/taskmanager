package com.example.taskmanager.taskmanager.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.example.taskmanager.taskmanager.model.Status;
import com.example.taskmanager.taskmanager.model.Task;
import com.example.taskmanager.taskmanager.model.User;
import com.example.taskmanager.taskmanager.repository.TaskRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskManagerScheduler {
    
    private final TaskRepository taskRepository;
    
    public List<Task> getOptimizedTaskList(User user) {
        // Fetch all pending tasks for the user
        List<Task> pendingTasks = taskRepository.findByUserAndStatus(user, Status.PENDING);
        
        // Create a priority queue (min-heap) based on task priority score
        // Higher score = higher priority
        PriorityQueue<Task> taskQueue = new PriorityQueue<>(
                Comparator.comparingInt(Task::calculatePriorityScore).reversed());
        
        // Add all pending tasks to the queue
        taskQueue.addAll(pendingTasks);
        
        // Extract tasks in priority order
        List<Task> optimizedTasks = new ArrayList<>(pendingTasks.size());
        while (!taskQueue.isEmpty()) {
            optimizedTasks.add(taskQueue.poll());
        }
        
        return optimizedTasks;
    }
    
    // Method to get next N highest priority tasks
    public List<Task> getNextBatchOfTasks(User user, int batchSize) {
        List<Task> optimizedTasks = getOptimizedTaskList(user);
        int size = Math.min(batchSize, optimizedTasks.size());
        return optimizedTasks.subList(0, size);
    }
    
    // Calculate approximate completion time based on priority
    public String estimateCompletionTime(Task task) {
        int priorityValue = task.calculatePriorityScore();
        
        if (priorityValue > 150) {
            return "Today";
        } else if (priorityValue > 100) {
            return "Tomorrow";
        } else if (priorityValue > 50) {
            return "This week";
        } else {
            return "Next week";
        }
    }
}
