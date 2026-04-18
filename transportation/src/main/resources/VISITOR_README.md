# 群众（Visitor）登录功能使用说明

## 一、功能概述

新增了"群众"登录身份，该身份具有以下权限：
- ✅ 综合信息查看
- ✅ 个人信息修改
- ❌ 其他管理权限（无）

## 二、数据库初始化

### 方法 1：使用 schema.sql（推荐）

schema.sql 已自动包含 visitor 表的创建语句和初始数据：

```sql
-- 表结构
CREATE TABLE IF NOT EXISTS `visitor`(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(50),
    avatar VARCHAR(255),
    status TINYINT DEFAULT 0 COMMENT '0:启用，1:禁用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 初始账户
INSERT INTO `visitor` (username, password, status) 
VALUES ('visitor', 'e10adc3949ba59abbe56e057f20f883e', 0);
```

### 方法 2：手动执行 SQL

执行以下 SQL 脚本初始化：

```bash
# 在 MySQL 中执行
mysql -u root -p transportation < src/main/resources/init_visitor_quick.sql
```

### 方法 3：直接运行 SQL 命令

```sql
-- 1. 创建表
CREATE TABLE IF NOT EXISTS `visitor`(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(50),
    avatar VARCHAR(255),
    status TINYINT DEFAULT 0 COMMENT '0:启用，1:禁用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. 插入初始账户
INSERT INTO `visitor` (username, password, status) 
VALUES ('visitor', 'e10adc3949ba59abbe56e057f20f883e', 0);
```

## 三、账户信息

| 项目 | 值 |
|------|-----|
| **用户名** | visitor |
| **密码** | 123456 |
| **角色** | 群众 |
| **权限** | 综合信息查看、个人信息修改 |

## 四、登录方式

### 1. 前端登录

1. 打开登录页面
2. 选择"账号密码登录"标签
3. 在身份选择区域点击"群众"按钮
4. 输入用户名：`visitor`
5. 输入密码：`123456`
6. 点击"登录"

### 2. 登录界面截图说明

登录界面现在有三个身份选项：
- 🔵 普通用户
- 🔵 管理员
- 🔵 群众（新增）

## 五、后端 API 接口

### 5.1 群众登录
```http
POST /api/visitor/login
Content-Type: application/json

{
  "username": "visitor",
  "password": "123456"
}
```

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 5.2 获取群众信息
```http
GET /api/visitor/info
Authorization: Bearer {token}
```

### 5.3 更新群众信息
```http
PUT /api/visitor/info
Authorization: Bearer {token}
Content-Type: application/json

{
  "phone": "13800138000",
  "email": "visitor@example.com",
  "avatar": "http://example.com/avatar.jpg"
}
```

### 5.4 修改密码
```http
PATCH /api/visitor/password
Authorization: Bearer {token}
Content-Type: application/json

{
  "oldPassword": "123456",
  "newPassword": "newpassword123",
  "confirmPassword": "newpassword123"
}
```

### 5.5 退出登录
```http
POST /api/visitor/logout
Authorization: Bearer {token}
```

## 六、权限说明

### 群众角色权限

| 功能模块 | 权限 | 说明 |
|---------|------|------|
| 综合信息查看 | ✅ 有 | 可以查看交通态势、设备状态等公共信息 |
| 个人信息修改 | ✅ 有 | 可以修改自己的手机号、邮箱、头像等 |
| 密码修改 | ✅ 有 | 可以修改自己的登录密码 |
| 设备管理 | ❌ 无 | 无法访问设备管理功能 |
| 事件处理 | ❌ 无 | 无法访问事件处理功能 |
| 用户管理 | ❌ 无 | 无法访问用户管理功能 |
| 系统设置 | ❌ 无 | 无法访问系统设置功能 |

## 七、技术实现

### 后端文件清单

```
transportation/src/main/java/org/example/transportation/
├── controller/
│   └── VisitorController.java          # 群众相关 API 接口
├── service/
│   ├── VisitorService.java             # 群众业务接口
│   └── impl/
│       └── VisitorServiceImpl.java     # 群众业务实现
├── repository/
│   └── VisitorRepository.java          # 群众数据访问层
└── dox/visitor/
    ├── Visitor.java                    # 群众实体类
    ├── VisitorLoginDTO.java            # 登录 DTO
    ├── VisitorRegisterDTO.java         # 注册 DTO
    ├── VisitorUpdateDTO.java           # 更新 DTO
    └── VisitorVO.java                  # 群众 VO
```

### 前端文件修改

```
Smart-transportation-web/src/
├── api/
│   └── user.js                         # 新增 visitor 相关 API
├── store/
│   └── modules/
│       └── user.js                     # 新增 isVisitor 状态管理
└── views/
    └── login/
        └── index.vue                   # 新增群众登录选项
```

## 八、常见问题

### Q1: 登录提示"密码错误"？
**A:** 检查数据库中 visitor 账户的密码是否为 MD5 加密的 `e10adc3949ba59abbe56e057f20f883e`

### Q2: 登录提示"用户不存在"？
**A:** 执行初始化 SQL 脚本创建 visitor 表和账户

### Q3: 登录后无法访问某些页面？
**A:** 群众角色只有查看权限，这是正常现象

### Q4: 如何创建更多群众账户？
**A:** 
1. 通过前端注册功能（如果开启）
2. 直接在数据库插入：
```sql
INSERT INTO `visitor` (username, password, phone, email, status) 
VALUES ('newvisitor', 'e10adc3949ba59abbe56e057f20f883e', '13800138000', 'visitor@example.com', 0);
```

## 九、安全建议

1. **修改默认密码**：首次登录后立即修改默认密码
2. **启用账户审核**：建议开启群众账户注册审核机制
3. **限制访问频率**：对登录接口增加限流保护
4. **日志审计**：记录群众账户的操作日志

## 十、下一步

如需为群众角色添加更多功能，请修改：
1. 后端：VisitorController.java 添加新的 API 接口
2. 前端：修改路由配置，添加群众可访问的页面
3. 数据库：根据需要扩展 visitor 表字段

---

**创建时间**: 2026-04-07  
**版本**: v1.0  
**作者**: 系统自动生成
