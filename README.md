# 权限管理系统 - 后端 (auth-system)

基于 Spring Boot 2.7 + MyBatis + MySQL 的权限管理系统后端服务。

## 技术栈

- **Spring Boot 2.7.18** (Java 8)
- **MyBatis** ORM 框架
- **MySQL 8.0** 数据库
- **Druid** 连接池
- **JWT** 认证
- **Hutool** 工具库
- **Lombok**

## 功能特性

- ✅ JWT Token 认证
- ✅ 基于角色的动态菜单权限
- ✅ 按钮级权限标识控制
- ✅ CORS 跨域支持
- ✅ 统一响应格式

## 快速开始

### 1. 初始化数据库

```bash
mysql -u root -p < src/main/resources/db/schema.sql
```

### 2. 修改配置

编辑 `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auth_system?...
    username: root
    password: your_password
```

### 3. 启动项目

**IDEA 方式:**
- 打开项目 → 等待 Maven 依赖下载
- 右键 `AuthApplication.java` → Run

**命令行方式:**
```bash
mvn spring-boot:run
```

### 4. 打包部署

```bash
# WAR 包（部署到 Tomcat）
mvn clean package -P war -DskipTests
# 产物: target/auth-system.war
```

## 默认账号

| 用户名 | 密码 | 角色 | 权限 |
|---|---|---|---|
| admin | admin123 | 超级管理员 | 全部 |
| operator | admin123 | 运营员 | 手机卡+运营+编辑 |
| viewer | admin123 | 查看员 | 仅查看 |

## 项目结构

```
src/main/java/com/example/auth/
├── AuthApplication.java         # 启动类
├── common/Result.java           # 统一响应
├── config/
│   ├── CorsConfig.java          # CORS 配置
│   ├── JwtInterceptor.java      # JWT 拦截器
│   └── WebMvcConfig.java        # MVC 配置
├── controller/
│   ├── AuthController.java      # 认证接口
│   ├── MenuController.java      # 菜单权限接口
│   ├── SysUserController.java   # 用户管理
│   ├── SysRoleController.java   # 角色管理
│   └── SysMenuController.java   # 菜单管理
├── dto/                         # 数据传输对象
├── entity/                      # 实体类
├── mapper/                      # MyBatis Mapper
├── service/                     # 业务层
└── utils/JwtUtil.java           # JWT 工具
```

## API 接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/auth/login | 登录（无需 token） |
| GET | /api/menus | 获取用户菜单树 |
| GET | /api/permissions | 获取用户权限标识列表 |
| GET | /api/sys/users | 用户列表 |
| GET | /api/sys/roles | 角色列表 |
| GET | /api/sys/menus | 全部菜单 |

> 除登录外的所有接口请求头需携带 `Authorization: Bearer <token>`
