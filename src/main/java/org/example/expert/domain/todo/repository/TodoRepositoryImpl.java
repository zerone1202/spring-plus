package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class TodoRepositoryImpl implements TodoQueryRepository {
    private final JPAQueryFactory queryFactory;

    public TodoRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<Todo> findByIdWithUserQueryDSL(Long todoId) {
        // QueryDSL 코드
        QTodo todo = QTodo.todo;

        Todo result = queryFactory
                .select(todo)
                .from(todo)
                .leftJoin(todo.user)
                .fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchResponse> searchTodoWithUserQueryDSL(
            String title,
            String nickname,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        QTodo todo = QTodo.todo;
        QUser user = QUser.user;

        List<TodoSearchResponse> results = queryFactory
                .select(Projections.constructor(
                        TodoSearchResponse.class,
                        todo.title,
                        user.countDistinct(),
                        todo.comments.size().longValue()
                ))
                .from(todo)
                .leftJoin(todo.user, user)
                .where(
                        title != null ? todo.title.contains(title) : null,
                        nickname != null ? user.nickname.contains(nickname) : null,
                        startDate != null && endDate != null
                                ? todo.createdAt.between(startDate, endDate)
                                : null
                        )
                .groupBy(todo.id)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(todo.count())
                .from(todo)
                .fetchOne();

        return PageableExecutionUtils.getPage(results, pageable, () -> total);
    }
}

/*
[QueryDSL로 N+1문제 해결]
Hibernate:
    select
        t1_0.id,
        t1_0.contents,
        t1_0.created_at,
        t1_0.modified_at,
        t1_0.title,
        t1_0.user_id,
        u1_0.id,
        u1_0.created_at,
        u1_0.email,
        u1_0.modified_at,
        u1_0.nickname,
        u1_0.password,
        u1_0.user_role,
        t1_0.weather
    from
        todos t1_0
    left join
        users u1_0
            on u1_0.id=t1_0.user_id
    where
        t1_0.id=?
 */
