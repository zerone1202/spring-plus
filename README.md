# Spring 플러스 주차 개인 과제

---

## Level 1: 필수 기능

### 1. 코드 개선 퀴즈 - @Transactional의 이해 
- **문제**: 클래스 전체에 `@Transactional(readOnly = true)`가 적용되어 쓰기 작업 불가능.  
- **해결**: `saveTodo()` 메소드에 개별적으로 `@Transactional`을 추가하여 정상 저장 가능하도록 수정.

```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;

    @Transactional // 쓰기 가능 트랜잭션으로 오버라이딩
    public Todo saveTodo(TodoRequest request, User user) {
        Todo todo = new Todo(request.getTitle(), request.getContents(), user);
        return todoRepository.save(todo);
    }
}
```

---

### 2. 코드 추가 퀴즈 - JWT의 이해

- **요구사항**: JWT에 nickname 포함.
- **해결**: User 엔티티에 nickname 필드 추가.
JwtUtil에서 토큰 생성 시 nickname 클레임 포함.
JWT 필터에서 nickname 추출 가능하도록 수정.
- **검증**: 토큰을 Base64 디코딩해 확인한 결과, payload에 nickname 정보가 정상적으로 포함됨.

---

### 3. 코드 개선 퀴즈 -  JPA의 이해
- **요구사항**: weather, modifiedAt(기간) 조건 검색 + 페이징 지원.
- **해결**: @Query와 동적 파라미터(null 시 조건 제외)로 검색 구현.
컨트롤러에서 요청 파라미터를 받아 서비스 계층에 전달.

```java
// 컨트롤러
@GetMapping("/todos")
public ResponseEntity<Page<TodoResponse>> getTodos(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String weather,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
) {
    return ResponseEntity.ok(todoService.getTodos(page, size, weather, startDate, endDate));
}
```
```java
// 레포지토리
@Query("""
        SELECT t FROM Todo t
        LEFT JOIN t.user
        WHERE (:weather IS NULL OR t.weather = :weather)
          AND (:startDate IS NULL OR t.modifiedAt >= :startDate)
          AND (:endDate IS NULL OR t.modifiedAt <= :endDate)
       """)
Page<Todo> searchTodos(
        @Param("weather") String weather,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
);
```
---

### 4. 테스트 코드 퀴즈 - 컨트롤러 테스트의 이해

- **문제**: 존재하지 않는 ID 조회 시 테스트 실패.
- **해결**:
given(...).willThrow(...)로 예외 설정.
status와 code를 BadRequest 기준으로 검증하도록 수정.

```java
.andExpect(status().isBadRequest())
.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
.andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
```
---

### 5. 코드 개선 퀴즈 - AOP의 이해

- **문제**: UserAdminController.changeUserRole() 실행 전 로그가 동작하지 않음.
- **해결**: 포인트컷 대상 및 실행 시점을 수정하여 정상 동작하도록 변경.

```java
// 수정 전
@After("execution(* org.example.expert.domain.user.controller.UserController.getUser(..))")
public void logAfterChangeUserRole(JoinPoint joinPoint) { ... }

// 수정 후
@Before("execution(* org.example.expert.domain.user.controller.UserController.getUser(..))")
public void logBeforeChangeUserRole(JoinPoint joinPoint) {
    System.out.println("[LOG] 실행 전 : " + joinPoint.getSignature().getName());
}
```
---

## Level 2: 필수 기능

### 6. JPA Cascade

- **요구사항**: 할 일 생성 시 생성자가 담당자로 자동 등록.
- **해결**: @OneToMany(cascade = CascadeType.PERSIST) 적용.
Todo 저장 시 Manager도 자동 저장.

---

### 7. N+1

- **문제**: 댓글 조회 시 User 연관 정보로 인한 N+1 발생.
- **해결**: JOIN FETCH 적용하여 User 함께 로딩.

---

### 8. QueryDSL
- **요구사항**: findByIdWithUser를 QueryDSL로 리팩터링.
- **해결**: JPAQueryFactory와 Q-Class 활용.
타입 안정성 확보 및 N+1 방지.

```java
@Repository
public class TodoRepositoryImpl implements TodoQueryRepository {
    private final JPAQueryFactory queryFactory;

    public TodoRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<Todo> findByIdWithUserQueryDSL(Long todoId) {
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
```
---
### 9. Spring Security

- **요구사항**: 기존 Filter + Argument Resolver 인증/인가를 Spring Security로 교체.
- **해결**: SecurityConfig 생성, SecurityFilterChain 등록.
JWT 인증 필터 추가, 세션 비활성화.
authorizeHttpRequests()로 접근 권한 제어.
권한 검증을 Spring Security 표준 방식으로 전환.
- **테스트 코드 수정**: Spring Security 환경에서 API 테스트를 위해 @WithMockUser를 적용하여 인증된 사용자 컨텍스트를 시뮬레이션.
  
```java
@Test
@WithMockUser(username = "testUser", roles = {"USER"})
void testGetTodo_withMockUser() throws Exception {
    mockMvc.perform(get("/todos/1"))
            .andExpect(status().isOk());
}
```
---

## Level 3: 도전 기능

### 10. QueryDSL 을 사용하여 검색 기능 만들기

- **요구사항**: 일정 검색 기능을 QueryDSL로 구현.
제목 키워드 (부분 검색 지원)
생성일 범위 조건
담당자 닉네임 (부분 검색 지원)
최신순 정렬
페이징 처리
반환 데이터: 일정 제목, 담당자 수, 댓글 개수

```java
//컨트롤러
@GetMapping("/search")
public Page<TodoSearchResponse> searchTodos(
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String nickname,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        Pageable pageable
) {
    return todoService.searchTodos(title, nickname, startDate, endDate, pageable);
}
```
```java
//레포지토리
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
```
---

- Level 1: 트랜잭션 오류 해결, JWT 확장(Base64 검증), 동적 검색, 테스트 코드 수정, AOP 수정

- Level 2: Cascade 자동 저장, N+1 해결, QueryDSL 전환, Spring Security 도입 (@WithMockUser 적용)

- Level 3: QueryDSL 기반 검색 기능 추가 (제목/닉네임/기간 조건 + Projections 최적화 + 페이징 처리)

