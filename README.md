# Description

A task management system that Store tasks with title, description, status (pending/completed), priority (low/medium/high) with redis cache integration

## Setup Instructions 

- Open the taskmanager to any code editor and run the project . Note - your computer must have Springboot already installed with proper setup.
- Database is handled by Postgres therefore it should be installed on your pc
- If properly done your Springboot application will start to run on port 8080 .

## Features 

- Backend: RESTful APIs with CRUD operations
- Database: Postgres for storing user data


### Application.peroperites file Springboot :-

```

spring.application.name=taskmanager
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://localhost:5432/taskmanager
spring.datasource.username=postgres
spring.datasource.password=your password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.default_schema=taskmanager
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

logging.level.org.springframework.security.config.annotation.authentication.configuration.InitializeUserDetailsBeanManagerConfigurer=ERROR
# JWT Configuration
jwt.secret=veryLongAndSecureRandomKeyForJwtSigning1234567890
jwt.expiration=86400000

# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379

# Application specific settings
app.task.page-size=10


```

## API documentation :-
```

 > POST "http://localhost:8080/api/auth/register" > - register users

 > POST "http://localhost:8080/api/auth/login" > - login a user

 > POST "http://localhost:8080/api/tasks" > - create task

 > GET "http://localhost:8080/api/tasks?page=0&size=10" >  - get all tasks

 > GET "http://localhost:8080/api/tasks?priority=HIGH" >  - get all tasks with priority filter

```


### Rest Controller :-

```
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    
    private final TaskService taskService;
    
    @Value("${app.task.page-size:10}")
    private int defaultPageSize;
    
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(
            @RequestBody TaskDTO taskDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        TaskDTO createdTask = taskService.createTask(userDetails.getUsername(), taskDTO);
        return ResponseEntity.created(URI.create("/api/tasks/" + createdTask.getId()))
                .body(createdTask);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.getTaskById(id, userDetails.getUsername()));
    }
    
    @GetMapping
    public ResponseEntity<Page<TaskDTO>> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) String sortBy,
            @AuthenticationPrincipal UserDetails userDetails) {
        int pageSize = size != null ? size : defaultPageSize;
        return ResponseEntity.ok(taskService.getTasks(
                userDetails.getUsername(), page, pageSize, status, priority, sortBy));
    }
    
    @GetMapping("/prioritized")
    public ResponseEntity<List<TaskDTO>> getPrioritizedTasks(
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.getPrioritizedTasks(userDetails.getUsername(), limit));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Long id,
            @RequestBody TaskDTO taskDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.updateTask(id, taskDTO, userDetails.getUsername()));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        taskService.deleteTask(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}


```

### Global Exception Handling :-
```
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFoundException(
            UsernameNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "Invalid username or password");
        
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Object> handleJwtException(
            JwtException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "Invalid JWT token");
        
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        body.put("errors", errors);
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "An unexpected error occurred");
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```


# Author 
## Robin Juyal | robinjuyal29@gmail.com | 9548933347



