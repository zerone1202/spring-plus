package org.example.expert.domain.comment.repository;

import org.example.expert.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.todo.id = :todoId")
    List<Comment> findByTodoIdWithUser(@Param("todoId") Long todoId);

    /*
    [기존 코드 N+1문제]
    @Query("SELECT c FROM Comment c JOIN c.user WHERE c.todo.id = :todoId")
    Hibernate:
    select
        c1_0.id,
        c1_0.contents,
        c1_0.created_at,
        c1_0.modified_at,
        c1_0.todo_id,
        c1_0.user_id
    from
        comments c1_0
    where
        c1_0.todo_id=?
Hibernate:
    select
        u1_0.id,
        u1_0.created_at,
        u1_0.email,
        u1_0.modified_at,
        u1_0.nickname,
        u1_0.password,
        u1_0.user_role
    from
        users u1_0
    where
        u1_0.id=?
     */

    /*
    [Fetch Join 적용한 수정 코드 N+1문제 해결]
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.todo.id = :todoId")
    Hibernate:
    select
        c1_0.id,
        c1_0.contents,
        c1_0.created_at,
        c1_0.modified_at,
        c1_0.todo_id,
        c1_0.user_id,
        u1_0.id,
        u1_0.created_at,
        u1_0.email,
        u1_0.modified_at,
        u1_0.nickname,
        u1_0.password,
        u1_0.user_role
    from
        comments c1_0
    join
        users u1_0
            on u1_0.id=c1_0.user_id
    where
        c1_0.todo_id=?
     */
}
