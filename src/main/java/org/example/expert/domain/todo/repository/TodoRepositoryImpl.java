package org.example.expert.domain.todo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
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
