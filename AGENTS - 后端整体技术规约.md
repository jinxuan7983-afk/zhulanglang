# 后端开发规范

本规范基于根目录 `AGENTS.md`，所有接口契约、响应格式、分页约定遵循总体规范。 适用于 `backend/` 目录下的 Spring Boot 后端工程。

## 1. 技术栈与版本

| 类别   | 技术                          | 版本      |
| ---- | --------------------------- |---------|
| 框架   | Spring Boot                 | 3.2.5   |
| Java | OpenJDK                     | 17      |
| ORM  | MyBatis Plus                | 3.5.7   |
| 数据库  | MySQL                       | 8.0+    |
| 连接池  | HikariCP                    | 默认      |
| 认证   | JWT (jjwt)                  | 0.12.5  |
| 文档   | SpringDoc OpenAPI (Swagger) | 2.5.0   |
| 工具库  | Lombok                      | 1.18.30 |

## 2. 项目结构

采用 **按模块垂直切片（Package by Feature）** 结构，将业务模块作为顶级包，内部自行分层。

```
backend/kinghunt-portal-service
├── pom.xml
├── src/main/java/cn/turman/kinghunt/portal/
│   ├── Application.java
│   │
│   ├── common/            # 全局、跨模块通用
│   │   ├── annotation/    # 全局通用自定义注解
│   │   ├── config/        # 全局配置（WebMvc、CORS、Swagger、MyBatis-Plus等）
│   │   ├── constant/      # 全局跨模块通用常量
│   │   ├── context/       # 全局通用上下文
│   │   ├── dto/           # 全局跨模块通用 DTO（ApiResult、PageInfo等）
│   │   ├── enums/         # 全局跨模块通用枚举
│   │   ├── exception/     # 全局异常处理器、自定义异常
│   │   ├── interceptor/   # 全局拦截器（认证、日志等）
│   │   ├── param/         # 全局跨模块通用 Param        
│   │   └── utils/         # 全局跨模块通用工具类
│   │
│   └── facade/            # Facade层，凡对外提供服务接口，均应在该目录下做封装
│   └── rpc/               # RPC层，凡使用外部第三方接口，均应在该目录下做封装
│   │
│   ├── customerx/         # 客户中心模块
│   │   ├── common/        # 模块内共用
│   │   │   ├── constant/  # 模块内常量
│   │   │   ├── dto/       # 模块内出参（XxxDTO）
│   │   │   ├── enums/     # 模块内枚举
│   │   │   └── param/     # 模块内入参（XxxParam、XxxCondition）
│   │   ├── controller/    # 模块内REST接口
│   │   ├── dal/
│   │   │   ├── dao/       # 模块内MyBatis Plus Mapper（XxxDAO）
│   │   │   └── entity/    # 模块内数据实体（XxxDO）
│   │   ├── service/       # 模块内业务接口
│   │   │   └── impl/      # 模块内业务实现
│   │
│   ├── documentx/         # 资料中心模块（结构同上）
│   ├── productx/          # 产品中心模块（结构同上）
│   ├── orderx/            # 订单中心模块（结构同上）
│   ├── portalx/           # 综合门户模块（结构同上）
│   ├── userx/             # 用户中心模块（结构同上）
│   │
└── src/main/resources/
    ├── application.yml
    ├── application-dev.yml
    ├── logback-dev.xml
    ├── mapper/            # MyBatis XML映射文件（按模块分包）
        ├── customerx/         
        ├── orderx/ 
        └── ...
```

每个包/目录的使用说明，详见路径下的package-info.java描述。

项目跟进业务模块分组，每个模块一般以`模块名+x`命名，如用户模块，命名为`userx`。

模块及命名如下：

| 模块     | 命名        | 说明                        |
| ------ | --------- | ------------------------- |
| 用户中心模块 | userx     | 包含用户、机构、角色、权限的管理          |
| 客户中心模块 | customerx | 门户注册客户与企业认证管理             |
| 资料中心模块 | documentx | 产品资料包、批次资料等资料管理           |
| 产品中心模块 | productx  | 产品管理                      |
| 订单中心模块 | orderx    | 购物车、订单管理                  |
| 综合门户模块 | portalx   | 企业网站前台，包括企业介绍、内容管理、各模块入口等     |

**重要说明**：
- 用户中心（`userx`）管理后台账号，客户中心（`customerx`）管理门户注册用户，二者在 `service`/`dal` 层可能共用逻辑。
- 每个业务模块的 `web`、`service`、`dal`、`dto`、`param` 等均放在该模块顶级包下，实现**高内聚**。
- `common` 仅存放被 **≥2 个模块** 共享的类；模块私有内容禁止放入 `common`。

## 3. 统一响应与分页（遵循总体规范）

### 3.1 响应格式

所有接口返回 `cn.turman.kinghunt.portal.common.dto.ApiResult<T>`，示例：

```java
public class ApiResult<T> implements Serializable {
    private Boolean success;                              //必填，是否成功
    private T data;                                       //返回结果
    private String code;                                  //失败必填，错误码
    private String message;                               //失败必填，错误提示文案
    private Long timestamp = System.currentTimeMillis();  //返回结果时间戳
}
```

成功响应：

```json
{
  "success": true,
  "code":"000000",
  "message": "",
  "data": { ... }
  ...
}
```

失败响应：

```json
{
  "success": false,
  "code":"400001",
  "message": "参数错误：名称不能为空",
  "data": null
  ...
}
```

### 3.2 分页格式

列表接口使用 `cn.turman.kinghunt.portal.common.dto.PageInfo<T>`：

```java
public class PageInfo<T> implements Serializable {
    private long total;    //查询结果总数
    private List<T> data;  //分页数据
}
```

请求参数使用 `cn.turman.kinghunt.portal.common.param.PageParam`及其子类：

```java
public class PageParam implements Serializable {
    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private int pageNum = DEFAULT_PAGE_NUM;    //当前页数，默认第1页
    private int pageSize = DEFAULT_PAGE_SIZE;  //每页条数，默认每页10条
}
```

MyBatis Plus 分页插件配置：

```java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

### 3.3 错误码定义

| code   | 说明            |
| ------ | ------------- |
| 000000 | 成功            |
| 400001 | 参数校验失败        |
| 401001 | 未登录或 Token 失效 |
| 403001 | 无权限操作         |
| 404001 | 资源不存在         |
| 409001 | 数据冲突（如重复创建）   |
| 500001 | 系统内部异常        |

## 4. 分层职责

**各业务模块调用链**：controller → service→ dal  

common为共用

facade为对外提供服务能力，调用service或dal

rpc为调用外部第三方接口服务，由service调用

**跨模块调用**：通过 service 接口依赖，禁止直接依赖其他模块的 dal 或 web 层。

| 层级                 | 职责                                           | 约束                                 |
| ------------------ | -------------------------------------------- | ---------------------------------- |
| **controller** | 接收请求、参数校验（JSR-303）、调用 Service、返回 `ApiResult` | 禁止业务逻辑，禁止直接操作 Mapper               |
| **service**    | 业务逻辑、事务管理、数据组装                               | 必须通过 Mapper 访问数据库，返回 DTO 而非 Entity |
| **dal/dao**    | 数据访问接口，继承 `BaseMapper<T>`                    | 复杂 SQL 写在 `resources/mapper/{模块}` 中 |
| **dal/entity** | 数据库表映射（使用 `@TableName`），命名为XxxDO             | 仅用于持久化，禁止直接返回给前端                   |
| **dto**        | 出参数据                                         | 用于返回给controller层，以及service层数据流转    |
| **param**      | 入参数据（表单、查询条件）        | 不可以作为出参                            |

**命名示例**：

- Controller: `UserController`
- Service: `userx/UserService` / `userx/impl/UserServiceImpl`
- Mapper: `UserMapper`
- Entity: `UserDO`
- DTO（出参）: `UserDTO`
- Param（入参）: `UserParam`（表单）、`UserCondition`（查询条件）

## 5. 数据库规范（MySQL）

- **表名 / 字段名**：小写 + 下划线，如 `user_account`, `created_at`。
  Java DO 字段 camelCase，表字段 snake_case，依赖 MP 下划线映射。

- **索引命名**：`idx_表名_字段名`；唯一索引：`uk_表名_字段名`。

- **基础字段（推荐）**：

  ```sql
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  created_by BIGINT COMMENT '创建人',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by BIGINT COMMENT '更新人',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0 COMMENT '软删除标记'
  ```

- **查询规范**：

  - 禁止 `SELECT *`
  - 一般列表查询优先使用分页，字典、树、下拉、全量启用状态等场景例外。
  - 更新/删除必须带 WHERE 条件

- **软删除**：

  - 对于核心业务表，凡是数据库表定义了deleted字段的，一律使用软删除，即使用`deleted`软删除标记
  - 对于非核心业务表，如用户角色关联表可以直接删除

- **迁移脚本**：所有 DDL 必须存放在项目根目录 [db](../../db)（即 `kinghunt-portal/db`），命名如 `V1__init_schema.sql`，并按版本递增。


## 6. 接口开发约定

### 6.1 接口路径规范

- 基础路径：`/api/{module}/{resource}`
- 示例：`/api/userx/user`、`/api/orderx/cart`
- 更新和删除操作虽然使用 POST 方法，但仍需在 URL 中体现资源 ID（如 `/api/userx/user/{id}/update`），以符合 RESTful 资源定位习惯。

### 6.2 HTTP 方法映射

| 操作          | HTTP 方法 | URL 示例（推荐）                                                          |
| ----------- | ------- | ------------------------------------------------------------------- |
| 详情（单个）      | GET     | `/api/userx/user/{id}`                                              |
| 列表查询        | GET     | `/api/userx/user/list?keyword=...&orgId=...`                        |
| 复杂条件查询 + 分页 | POST    | `/api/userx/user/query`（Body 传 pageNum, pageSize, 筛选条件）             |
| 新增          | POST    | `/api/userx/user/add`                                               |
| 更新          | POST    | `/api/userx/user/{id}/update` 或 `/api/userx/user/update`（Body 含 id） |
| 删除          | POST    | `/api/userx/user/{id}/delete` 或 `/api/userx/user/delete`（Body 含 id） |

> **约束**：Controller 中只能接收 `@GetMapping` 和 `@PostMapping`，禁止使用 `@PutMapping`、`@DeleteMapping`、`@PatchMapping`。

### 6.3 日期格式

- 请求/响应中日期统一为 `yyyy-MM-dd HH:mm:ss`（Java 侧使用 `LocalDateTime`，Jackson 自动格式化）。

### 6.4 参数校验

- 使用 `@Valid` + JSR-303 注解（`@NotNull`, `@Size`, `@Min` 等）。
- 校验失败时抛出 `MethodArgumentNotValidException`，全局异常处理返回 `code=400001`。

## 7. 接口契约

所有接口契约统一维护在项目根目录 [contract/openapi.yaml](../contract/openapi.yaml)，遵循 OpenAPI 3.0 规范。

### 7.1 契约管理规则

- **新增接口前必须先更新契约**：在 `openapi.yaml` 中补充 path、schema 后再编码实现。
- **契约即文档**：前后端均以 `openapi.yaml` 为接口字段、类型、校验规则的唯一来源。
- **同步更新**：接口变更（新增字段、修改校验、废弃接口）必须同步更新契约文件。


### 7.2 Swagger 文档

启动后端服务后，可通过以下地址访问在线文档：

- Swagger UI: `http://localhost:9063/kh-portal/swagger-ui.html`
- OpenAPI JSON: `http://localhost:9063/kh-portal/v3/api-docs`

## 8. 内容安全检测

- 接口：`POST /api/userx/content-security/check`

- **认证要求**：需要登录（携带 `Authorization` 头），后端自动从当前用户获取 userId。

## 9. 文件上传

- 接口：`POST /api/documentx/upload/image`

- 参数：`file`（multipart/form-data）

- 响应：

  ```json

  {
    "success": true,
    "code":"000000",
    "message": "",
    "data": { "url": "http://.../uploads/xxx.jpg" }
    ...
  }
  ```

- 存储路径：由配置 `upload.path` 指定，生产环境使用对象存储（如 OSS）。

- 图片大小限制：单个 ≤ 50MB，类型限制 `image/jpeg`, `image/png`。

## 10. 异常处理与日志

- **全局异常处理器**：统一捕获业务异常、系统异常，返回 `ApiResult`。
- **日志规范**：
  - 使用 SLF4J + Logback。
  - 开发环境 `DEBUG`，生产环境 `INFO`。
  - 关键操作（登录、创建、修改、删除）记录 INFO 日志，包含用户 ID 和操作对象。
  - 禁止输出敏感信息（密码、token、身份证号）。
- **事务管理**：
  - 在 Service 层使用 `@Transactional(rollbackFor = Exception.class)`。
  - 禁止在事务中调用外部 HTTP 接口或操作 Redis。

## 11. 开发流程（与总体规范一致）

1. **澄清需求**：明确接口、数据表、校验规则、验收标准。
2. **分析影响**：涉及的表、索引、API 兼容性。
3. **设计方案**：数据库变更脚本、Service 流程、DTO/Param 定义。
4. **编码实现**：分层实现，并通过单元测试。
5. **自测验证**：使用 Postman / Swagger 测试所有接口，覆盖正常和异常场景。
6. **交付说明**：列出 SQL 脚本、新增/修改的接口、验证命令。

## 12. 严格禁止

- `BeanUtil.copyProperties()`（必须手动赋值，防止字段遗漏）
- 在 Controller 中写业务逻辑或 SQL
- 在循环中查询数据库（改用批量查询 `selectBatchIds` 或 `in` 条件）
- 生成未使用的空方法
- 事务中 `try-catch` 吞掉异常（应抛出并由全局处理器回滚）

## 13. 验证与构建

- 提交前必须执行：

  ```bash
  mvn clean compile
  mvn test
  mvn spring-boot:run   # 实际启动验证
  ```

- 确保所有单元测试通过，覆盖率 ≥ 80%。

- 数据库迁移脚本必须可重复执行（使用 `IF NOT EXISTS` 或版本控制）。

## 14. 参考总体规范

- 前端契约、分页、错误码、数据库变更管理等参见根目录 `AGENTS.md`。
- 本规范与总体规范冲突时，以总体规范为准。
