# 管理端前端开发规范

本规范基于根目录 `AGENTS.md`，所有接口契约、响应格式、分页约定遵循总体规范。
适用于 `frontend/system/` 目录下的「凯恒生物门户管理系统」前端工程（管理后台）。

> 说明：根目录 `AGENTS.md` 中前端列示的技术栈为 Vue + Pinia，本工程为既有 React/Umi 项目，**以本规范为准**，根目录其余通用原则（最小复杂度、契约先行、分层职责）仍适用。

## 1. 技术栈与版本

| 类别         | 技术                                            | 版本       |
| ---------- | --------------------------------------------- | -------- |
| 框架         | React                                         | 19.x     |
| 脚手架        | @umijs/max（Umi 4）                             | 4.3.x    |
| 构建         | Mako（仅 dev 启用）/ Webpack（build）                | -        |
| 语言         | TypeScript                                    | 5.6.x    |
| UI 组件库     | Ant Design                                    | 5.25.x   |
| Pro 组件     | @ant-design/pro-components / pro-form         | 2.7.x    |
| 样式         | Less Modules + antd-style                     | -        |
| 状态管理       | Umi Model（`useModel`）                         | 内置       |
| 网络请求       | umi-request（基于 fetch）                         | -        |
| 路由         | Umi 配置式路由 (`config/routes.ts`)                | -        |
| 权限         | Umi Access（`src/access.ts`）                   | -        |
| 国际化        | Umi Locale（默认 `zh-CN`）                        | -        |
| 图表         | @ant-design/plots、@antv/l7、echarts            | -        |
| 工具库        | dayjs、lodash、numeral                          | -        |
| 代码检查       | Biome + tsc                                   | -        |
| Git 提交     | Husky + Commitlint（Conventional Commits）      | -        |
| 运行环境       | Node.js                                       | ≥ 20.0.0 |

## 2. 项目结构

采用 **按业务模块垂直切片（Package by Feature）** 结构，与后端 `userx / customerx / documentx / productx / orderx / portalx` 等模块保持对齐，每个业务模块在 `src/pages` 下独立成树。

```
frontend/system
├── config/                       # Umi 构建配置
│   ├── config.ts                 # 主配置（路由、插件、国际化等）
│   ├── config.dev.ts             # 开发环境（含代理、mako）
│   ├── config.pro.ts             # 生产环境（hash、压缩、PUBLIC_PATH）
│   ├── proxy.ts                  # 代理配置
│   ├── routes.ts                 # 路由表（仅出现在此的页面会被编译）
│   ├── defaultSettings.ts        # ProLayout 默认布局/主题/标题
│   └── baseConfig.ts             # 不同环境的 baseURL、upload、iconfont 等
│
├── src/
│   ├── app.tsx                   # 运行时配置（initialState、layout、运行时钩子）
│   ├── access.ts                 # 路由/菜单权限校验函数
│   ├── global.tsx / global.less  # 全局脚本与样式
│   ├── loading.tsx               # 路由级 loading
│   ├── typings.d.ts              # 全局 TS 声明
│   │
│   ├── components/               # 跨模块共享的 UI 组件
│   │   ├── PageHeader/           # 顶栏面包屑
│   │   ├── Footer/               # 页脚
│   │   ├── RightContent/         # 右上角（语言、头像下拉）
│   │   ├── Notice/               # 消息中心入口
│   │   ├── NoticeIcon/           # 消息中心抽屉
│   │   ├── UploadFile/           # 通用上传（含图片/表格列）
│   │   ├── GlobalProTable/       # 全局 ProTable 默认配置
│   │   ├── ConfirmTip/           # 确认提示
│   │   ├── DynamicRedirect/      # 根据权限动态跳转
│   │   ├── NoAuth/               # 403 占位
│   │   ├── NoFound/              # 404 占位
│   │   └── index.ts              # 统一对外导出
│   │
│   ├── models/                   # 全局状态（useModel 消费）
│   │   ├── global.ts             # 当前用户、登录态
│   │   └── layout.ts             # ProLayout 收起、移动端断点
│   │
│   ├── services/                 # 跨模块通用 API
│   │   ├── request.ts            # 请求实例与拦截器封装
│   │   ├── index.ts              # 跨模块共用接口（登录、当前用户、上传、地区等）
│   │   ├── error-code.ts         # HTTP 状态错误码 → 文案
│   │   └── typings.d.ts          # 通用 API 类型
│   │
│   ├── pages/                    # 业务页面（按业务模块组织）
│   │   ├── login/                # 登录页
│   │   ├── exception/            # 403 / 404 / 500
│   │   ├── account/              # 个人账号（设置、安全、通知）
│   │   ├── userx/                # 用户中心模块（与后端 userx 对齐）
│   │   ├── customerx/            # 客户中心模块
│   │   ├── documentx/            # 资料中心模块
│   │   ├── productx/             # 产品中心模块
│   │   ├── orderx/               # 订单中心模块
│   │   └── portalx/              # 综合门户模块
│   │
│   ├── locales/                  # 国际化文案
│   │   ├── zh-CN.ts / en-US.ts   # 语言入口
│   │   ├── zh-CN/                # 按命名空间拆分（menu / pages / account / pwa）
│   │   └── en-US/
│   │
│   ├── assets/                   # 图片、图标等静态资源
│   └── utils/                    # 跨模块工具
│       ├── index.ts              # 通用工具（localStorage、下载、URL 拼接）
│       ├── auth.ts               # 登录态、白名单、权限工具
│       └── routeAccess.ts        # 路由权限映射构建
│
└── package.json
```

**模块命名约定**：业务模块包名与后端保持一致，统一为「模块名+x」（如 `userx`、`orderx`）。新增业务模块时优先在 `src/pages/<模块>x/` 下展开，禁止跨模块直接 import 兄弟模块的 `service.ts` 与内部组件。

### 2.1 单个业务模块内部结构

每个业务功能（feature）按如下结构组织，保证**高内聚**：

```
src/pages/<module>x/<feature>/
├── index.tsx              # 入口/列表页（若为单页直接放在此）
├── service.ts             # 该 feature 的 API 调用（仅本 feature 使用）
├── data.d.ts              # 该 feature 的 TS 类型（接口出/入参、列表项等）
├── style.less             # 页面局部样式（Less Modules）
├── add/        ├── index.tsx       # 新增/编辑/详情子页
│              └── components/      # 子页面级组件（如 XxxModal、XxxForm）
├── detail/     └── index.tsx
└── components/            # 当前 feature 内部共享的组件
```

**层级约束**：

- `service.ts` 只能被本 feature 内的 `*.tsx` 使用，**禁止跨 feature/跨模块互相 import**。
- 真正跨模块共享的接口请放入 `src/services/index.ts`；跨模块共享的组件请放入 `src/components/`。
- `data.d.ts` 中的类型只用于本 feature；通用 DTO/Param 类型放 `src/services/typings.d.ts`。

## 3. 统一响应与分页（对接后端契约）

### 3.1 响应格式

后端统一返回 `ApiResult<T>`：

```typescript
type ApiResult<T> = {
  success: boolean;
  code: string;          // '000000' 表示成功
  message: string;       // 失败提示文案
  data: T | null;
  timestamp: number;
};
```

`src/services/request.ts` 已封装：

- 业务接口通过拦截器抹平响应体，返回 `{ success, code, data, message }`。
- 失败时（`success === false`）若 `showErr` 为真（默认开启），会通过 `message.error(message)` 提示。
- HTTP 状态码异常（4xx/5xx）由 `error-code.ts` 文案兜底。
- 业务码 `401001` / `code === 201` 表示登录失效，自动清理 token 并跳转 `/login`。

**调用示例**：

```typescript
import request from '@/services/request';

export async function queryUserList(data: UserCondition) {
  return request('/api/userx/user/query', {
    method: 'POST',
    data,
  });
}

const { success, data, message: msg } = await queryUserList(condition);
```

### 3.2 分页格式

请求参数遵循 `PageParam`：

```typescript
type PageParam = {
  pageNum: number;   // 默认 1
  pageSize: number;  // 默认 10
  // ...业务筛选字段
};
```

响应结构遵循 `PageInfo<T>`：

```typescript
type PageInfo<T> = {
  total: number;
  data: T[];
};
```

**ProTable 适配**：

```typescript
<ProTable<UserDTO, UserCondition & PageParam>
  request={async (params) => {
    const { current = 1, pageSize = 10, ...rest } = params;
    const { success, data } = await queryUserList({
      pageNum: current,
      pageSize,
      ...rest,
    });
    return {
      success,
      data: data?.data || [],
      total: data?.total || 0,
    };
  }}
/>
```

> ProTable 默认分页字段为 `current/pageSize`，需要在 `request` 中映射为后端的 `pageNum/pageSize`。

### 3.3 错误码

| code   | 处理建议                                  |
| ------ | ------------------------------------- |
| 000000 | 成功，正常消费 `data`                            |
| 400001 | 参数校验失败：通常在表单提交后由后端兜底，提示 `message` 即可  |
| 401001 | 未登录或 Token 失效：清理 token、跳转 `/login`                  |
| 403001 | 无权限：跳 `/exception/403` 或就地提示              |
| 404001 | 资源不存在：跳 `/exception/404` 或就地提示            |
| 409001 | 数据冲突：直接提示后端 `message`                   |
| 500001 | 系统异常：直接提示，必要时上报                           |

> 后端业务码以字符串形式下发，前端比较时使用字符串字面量；HTTP 层错误码（如 502/504）已在 `error-code.ts` 提供默认文案。

## 4. 分层职责

```
config/routes.ts ─┐
                  ▼
src/pages/<feature>/index.tsx (页面) ──► service.ts (API) ──► request.ts ──► 后端
                  │
                  ├──► useModel('global' | 'layout') 全局状态
                  └──► src/components/* 公共组件
```

| 层级                | 职责                                   | 约束                                                          |
| ----------------- | ------------------------------------ | ----------------------------------------------------------- |
| **routes.ts**     | 路由表、布局开关、菜单可见性、权限标识                  | 配置式路由，未在此声明的页面**不会被编译**                                     |
| **pages**         | 业务页面：UI 组装、表单交互、本地状态、调用 service      | 不直接调用 `request`；页面级筛选/弹窗状态用 `useState`；只在需要时引入全局状态           |
| **service.ts**    | 单个 feature 的 API 封装                  | 仅由本 feature 调用；统一返回 `request` 的结果，不在此层做业务处理                  |
| **services/**     | 跨模块通用 API、`request` 实例与拦截器           | 业务模块通用接口（登录、当前用户、上传、地区、字典）放在 `src/services/index.ts`        |
| **models/**       | 全局状态（当前用户、权限、布局、字典等）                 | 通过 `useModel('xxx')` 消费；页面/组件级状态禁止上提                        |
| **components/**   | 跨模块复用的展示/交互组件                        | 不依赖具体业务接口；与业务相关的「场景化组件」放回 feature 内部 `components/`         |
| **utils/**        | 与业务无关的纯函数（格式化、URL、localStorage）      | 禁止依赖 React/Umi 上下文；禁止在此调用接口                                 |
| **locales/**      | 国际化文案                                | 按命名空间拆分；新增 key 必须 zh-CN / en-US 同步                          |

**跨模块调用**：仅允许通过 `src/components` 或 `src/services` 暴露的能力调用，**禁止**直接 `import` 其他模块 `pages/<other>/` 下的内部组件或 `service.ts`。

## 5. 命名规范

### 5.1 标识符命名

| 标识符               | 规范                              | 示例                                                       |
| ----------------- | ------------------------------- | -------------------------------------------------------- |
| 变量 / 函数           | camelCase                       | `userInfo`、`fetchUserList()`、`handleSubmit()`             |
| React Hook        | camelCase + `use` 前缀            | `useLayout`、`useUserPermissions`                          |
| React 组件          | PascalCase                      | `UserModal`、`HeaderDropdown`                              |
| 类型 / 接口           | PascalCase                      | `UserDTO`、`UserCondition`、`PageInfo<T>`                   |
| 枚举                | 枚举名 PascalCase / 值小写字符串          | `type UserStatus = 'enabled' \| 'disabled'`               |
| 模块级常量             | UPPER_SNAKE_CASE                | `MOBILE_BREAKPOINT`、`MAX_UPLOAD_SIZE`、`DATE_FORMAT`        |

> 函数内部的局部常量（如解构出的 `const { id } = props`）保持 camelCase；只有「在整个模块乃至应用范围内不可变、对外暴露的字面量配置」才使用 UPPER_SNAKE_CASE。

### 5.2 文件与资源命名

| 类型               | 命名                                | 示例                                         |
| ---------------- | --------------------------------- | ------------------------------------------ |
| 组件文件             | PascalCase + `.tsx`               | `UserModal.tsx`、`PageHeader/index.tsx`     |
| 页面入口             | `index.tsx`                       | `pages/userx/user/index.tsx`               |
| Hook / 模型        | camelCase，文件名小写                   | `useLayout`、`models/global.ts`             |
| API 文件           | `service.ts` / `services/index.ts`| -                                          |
| 类型定义             | `data.d.ts` / `XxxDTO` / `XxxParam` | `UserDTO`、`UserCondition`、`UserParam`     |
| 样式               | `*.less` / `*.style.ts`           | `index.less`、`style.style.ts`              |
| 路由路径             | kebab-case                        | `/userx/role-permission`                   |
| 权限 code          | 大写 + `::` 分隔                      | `USERX::USER::QUERY`                       |
| 国际化 key          | 点分小写命名空间                          | `pages.login.submit`、`account.menu.base`   |

**DTO / Param 命名**与后端保持一致：

- 出参（接口返回）：`XxxDTO`
- 表单入参：`XxxParam`
- 查询条件：`XxxCondition`

## 6. 接口调用约定

### 6.1 路径与方法

完全遵循总体规范 `AGENTS.md` §5 与后端 §7.2：

- 上下文路径：`/kh-portal`（由后端 nginx 或 `baseConfig.baseURL` 提供）。
- 仅使用 `GET` 与 `POST`，**禁止** `PUT/DELETE/PATCH`。

| 操作        | 方法   | URL 示例（前端调用）                                  |
| --------- | ---- | ---------------------------------------------- |
| 单条详情      | GET  | `/api/userx/user/{id}`                         |
| 简单列表      | GET  | `/api/userx/user/list?keyword=...`             |
| 分页+复杂条件查询 | POST | `/api/userx/user/query`                        |
| 新增        | POST | `/api/userx/user/add`                          |
| 更新        | POST | `/api/userx/user/{id}/update` 或 Body 含 `id`    |
| 删除        | POST | `/api/userx/user/{id}/delete` 或 Body 含 `id`    |

### 6.2 字段与日期

- 请求/响应字段一律 **camelCase**。
- 日期统一字符串 `yyyy-MM-dd HH:mm:ss`；前端使用 `dayjs` 处理，在表单提交前格式化。
- 枚举值统一**小写字符串**（如 `enabled`、`disabled`），**禁止**用数字或大写串。

### 6.3 Header 约定

每个请求自动携带（由 `request.ts` 拦截器注入）：

| Header            | 取值                                | 备注                                  |
| ----------------- | --------------------------------- | ----------------------------------- |
| `Authorization`   | `Bearer ${token}`                 | 登录后写入 localStorage `authorization` |
| `X-Lang`          | `zh_CN` / `en_US`                 | 与 Umi `getLocale()` 同步              |

> 新增 `X-Lang` Header 的对接位置：`src/services/request.ts` 的请求拦截器；当后端要求多语种时与后端同步开关。

### 6.4 请求/响应封装的可选项（`request.ts`）

| 参数              | 用途                              |
| --------------- | ------------------------------- |
| `showErr`       | 失败时是否自动 `message.error`（默认 true）|
| `hasBlob`       | 文件流响应（导出/下载）                    |
| `hasFromData`   | `multipart/form-data` 提交        |
| `hasCustom`     | 透传原始响应，由调用方自行处理                 |
| `hasCancelToken`| 启用相同接口短时间内自动取消                  |
| `useBaseUrl`    | 强制使用 `baseConfig.baseURL`       |

## 7. 认证与授权

- 登录接口：`POST /api/userx/auth/login`，成功后将 token 写入 `localStorage('authorization')`。
- 全局态：`src/app.tsx` 的 `getInitialState` 在应用启动时校验登录态、拉取当前用户与权限树。
- 路由白名单：`src/utils/auth.ts` 中 `whiteList`，含 `/login`、`/exception/*`。
- 权限校验：`src/access.ts`
  - `accessRoute`：路由级，比对 `permissions` 与 `route.permission`。
  - `canShowInMenu`：菜单可见性，受 `hiddenMenuCodes` 影响。
- 路由配置示例：

  ```typescript
  {
    name: 'user',
    path: '/userx/user',
    component: './userx/user',
    access: 'accessRoute',
    permission: 'USERX::USER',
  }
  ```

- **退出登录**：调用 `logOut()`（`src/services/request.ts`）统一清理 token、`userInfo` 并跳 `/login`。

## 8. 国际化

- 默认语种：`zh-CN`；当前已配置 `zh-CN`、`en-US`。
- 入口：`src/locales/{lang}.ts`，按命名空间拆分到子目录（`menu` / `pages` / `account` / `pwa`）。
- 使用：

  ```typescript
  import { useIntl } from '@umijs/max';

  const intl = useIntl();
  intl.formatMessage({ id: 'pages.login.submit', defaultMessage: '登录' });
  ```

- **新增/修改 key 必须同时维护 zh-CN 与 en-US**；不再使用的 key 必须同步删除。
- 路由 `name` 字段会自动读取 `menu.<name>` 文案，无需手动包 `formatMessage`。

## 9. 全局布局与 ProLayout

- 默认布局来自 `config/defaultSettings.ts`：侧栏深色、品牌色 `#1890ff`、标题「凯恒生物门户管理系统」。
- 用户保存的布局偏好写入 `localStorage('app-settings')`，初始化时与默认值合并。
- 大屏看板等需要全屏显示的页面在 `app.tsx` 中按路径前缀关闭头部/侧栏（参考 `/databoard`、`/equipmentboard` 处理）。
- 移动端断点：`src/models/layout.ts` 中 `MOBILE_BREAKPOINT = 768`；通过 `useModel('layout')` 获取 `isMobile`、`collapsed`。

## 10. 路由与代码组织

- **唯一路由源**：`config/routes.ts`，未在此声明的页面不会被打包。
- 路由 `component` 使用相对路径（如 `./userx/user`）或别名（`@/components/DynamicRedirect`）。
- 通过 `permission` 字段绑定权限码，通过 `access` 指定校验函数；菜单 `name` 字段对应 `menu.ts` 中文案。
- 不要在路由组件外做副作用（避免编译期出错）。

## 11. 文件上传

- 通用上传接口：`POST /api/documentx/upload/image`，由 `src/services/index.ts` 中的 `sysFileUpload` / `uploadRichTextImage` 包装。
- 单文件 ≤ 50MB；图片仅支持 `image/jpeg`、`image/png`。
- UI 统一使用 `src/components/UploadFile`：
  - `UploadFile`：通用附件，含 ProForm 适配。
  - `UploadImage`：单/多图，含预览。
  - `TableFileCell` / `TableImageCell`：用于表格列内展示附件。

## 12. 状态管理约定

- **页面级状态**（筛选、弹窗、loading）：组件内 `useState` / `useRef`。
- **跨页面/全局状态**：注册到 `src/models/` 并通过 `useModel('xxx')` 消费。
  - 已有：`global`（当前用户）、`layout`（侧栏/移动端）、`business`（业务字典等，按需扩展）。
- **避免**：将仅在单页面用到的状态上提至全局；通过 props 透传超过 2 层时优先考虑模型或 Context。

## 13. 编码规范

### 13.1 函数与组件

- **推荐使用函数式组件 + Hooks**。
- **优先使用箭头函数**定义组件、回调与工具函数；仅当需要函数提升或写原型方法时才使用 `function` 声明。
- **单个函数体保持在 50 行以内**：超出时按职责拆为子函数；单个组件文件超过 300 行优先拆 `components/` 子目录。
- **每个函数职责单一**：渲染、数据加工、副作用分离；表单提交、接口调用等异步流程独立成 `handleXxx` 函数，不要塞进 JSX。
- **合理使用性能优化 API**（按需，不要无脑包）：
  - `React.memo`：纯展示组件、props 变化频率低但被频繁重渲染时使用。
  - `useMemo`：依赖稳定且计算成本较高（大数组运算、深层映射）时使用；轻量计算无需包裹。
  - `useCallback`：作为 prop 透传给 `React.memo` 子组件或作为 `useEffect`/`useMemo` 依赖项时使用。
  - 优化前先确认存在可观察的重渲染问题，避免引入额外心智负担。

### 13.2 TypeScript 类型

- **所有新增代码必须显式声明类型**：函数参数、返回值、组件 props、`useState` 初始值复杂时的泛型都不可省略。
- **导出函数 / 组件 props 必须先定义 `interface` 或 `type` 再使用**，并放入对应的 `data.d.ts`；跨模块复用上提到 `src/services/typings.d.ts`。
- **减少 `any` 的使用**：接收外部不确定数据用 `unknown` + 类型守卫；第三方库无类型时本地补 `*.d.ts` 声明。
- 联合类型优先于宽松字符串，例如状态字段使用 `'enabled' \| 'disabled'` 而非 `string`。

### 13.3 错误处理

- **所有异步调用必须考虑失败分支**：`await` 后判断 `success`；自行 `try/catch` 时禁止吞错，至少 `message.error` 或抛给上层。
- 业务接口的 `success === false` 已由 `src/services/request.ts` 拦截器统一弹错，页面只需处理业务侧分支（如登录失败跳转、表单字段定位）。
- React 渲染期的可恢复异常使用 `ErrorBoundary`（位于 `src/components/`）包裹，避免局部异常导致整页白屏。

### 13.4 代码风格

- **变量声明**：默认 `const`，需重赋值时才用 `let`；**禁止 `var`**。
- **相等判断**：一律使用 `===` / `!==`，**禁止 `==` / `!=`**；判空允许使用 `value == null`（同时覆盖 `null` 与 `undefined`），但需加注释说明。
- **字符串拼接**：使用模板字符串，禁止 `+` 拼接多个变量。
- **对象 / 数组**：解构获取属性；不可变更新使用展开运算或 `lodash` 工具，避免直接 mutate state。
- **注释**：仅解释**为什么**、**风险点**、**非显而易见的取舍**；不写「这是 xxx」、「// 调用接口」这类描述性注释。

## 14. 开发流程（与总体规范一致）

1. **澄清需求**：确认页面、对应后端接口、字段、校验规则、验收标准。
2. **核对契约**：阅读 `contract/openapi.yaml` 或后端 Controller，确认入参、出参、分页字段；若缺失则**先补契约**。
3. **设计方案**：明确路由、新增/复用组件、`service.ts` 中的接口、`data.d.ts` 中的类型、是否需要新增 i18n key。
4. **编码实现**：按 page → service → 全局组件复用 的顺序推进。
5. **自测验证**：覆盖正常 / 异常（接口失败、无权限、空数据）/ 移动端断点。
6. **代码质量**：执行 `npm run lint` 与 `npm run tsc`，通过后再提交。
7. **交付说明**：列出新增/修改的路由、组件、接口、i18n key，附验证步骤。

## 15. 严格禁止

**模块边界与分层**

- **禁止**直接调用 `fetch` / `axios` / `window.fetch`，统一使用 `@/services/request`。
- **禁止**在 `src/pages/<a>/...` 中 `import` `src/pages/<b>/...` 的 `service.ts` 或内部组件（破坏模块边界）。
- **禁止**跨 feature 共用 `data.d.ts`；通用类型上提到 `src/services/typings.d.ts`。
- **禁止**在 `utils/` 中调用接口或依赖 React Hook / Umi 运行时。
- **禁止**在 Controller 风格的 `service.ts` 中写业务逻辑（拼装、过滤、判空），业务逻辑放页面或 model。
- **禁止**绕过 `routes.ts` 添加页面（不会被编译）。
- **禁止**直接修改 `src/.umi/` 下生成产物。

**代码质量**

- **禁止** `console.log` / `console.info` / `console.debug` 等调试日志出现在提交代码中。
- **禁止** `var`，统一使用 `const` / `let`。
- **禁止** `==` / `!=`，使用 `===` / `!==`。
- **禁止**滥用 `any`、`as any`、`@ts-ignore`；类型不确定时使用 `unknown` + 类型守卫，确需绕过类型检查使用 `@ts-expect-error` 并注释原因。
- **禁止**单个函数超过 50 行、单个组件文件超过 300 行仍不拆分。
- **禁止**保留无引用的 i18n key、无引用的样式类、注释掉的死代码；新增 key 必须 zh-CN/en-US 同步。
- **禁止**在循环内调用接口；改为后端批量接口或前端 `Promise.all`。

## 16. 验证与构建

提交前必须执行：

```bash
npm run lint          # Biome + tsc 静态检查
npm run tsc           # 仅类型检查
npm run start:dev     # 启动 dev 环境（连接 dev 后端）
npm run start:test    # 启动 dev 服务（连接 test 后端）
npm run build         # 生产构建（验证打包通过）
```

- 提交信息遵循 Conventional Commits（由 Husky + Commitlint 校验）。
- 生产部署 `publicPath` 由 `config/config.pro.ts` 中 `BASE_PATH` / `PUBLIC_PATH` 控制，发布前与运维确认部署路径。
- 接口代理：`config/proxy.ts`，目标地址由 `config/baseConfig.ts` 中 `baseURL` 配置；不同环境通过 `REACT_APP_ENV=dev|test|pre` 切换。
