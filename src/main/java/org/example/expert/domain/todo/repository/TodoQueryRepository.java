package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

// Custom Repository 인터페이스
public interface TodoQueryRepository {
    Optional<Todo> findByIdWithUserQueryDSL(Long todoId);

    Page<TodoSearchResponse> searchTodoWithUserQueryDSL(
            String title,
            String nickname,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );
}
