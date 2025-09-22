package org.example.expert.domain.todo.dto.response;

public record TodoSearchResponse(
        // 일정 제목, 담당자 수, 댓글 개수
        String title,
        long managerCount,
        long commentCount
) {
}