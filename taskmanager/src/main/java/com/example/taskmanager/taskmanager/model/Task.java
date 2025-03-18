package com.example.taskmanager.taskmanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "tasks")
public class Task implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;
    
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // Method to calculate task score for priority queue
    public int calculatePriorityScore() {
        int priorityValue;
        switch (priority) {
            case HIGH:
                priorityValue = 100;
                break;
            case MEDIUM:
                priorityValue = 50;
                break;
            case LOW:
            default:
                priorityValue = 10;
                break;
        }
        
        // Add recency factor - more recent tasks get higher priority
        long hoursElapsed = createdAt.until(LocalDateTime.now(), java.time.temporal.ChronoUnit.HOURS);
        int recencyBonus = (int) Math.max(0, 100 - hoursElapsed); // Decreases over time
        
        return priorityValue + recencyBonus;
    }
}

