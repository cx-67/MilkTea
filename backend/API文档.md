MilkyTea 后端 API 接口文档
基本信息
- Base URL: http://localhost:8080
- API 版本: v1
- 认证方式: JWT Bearer Token
- 项目描述: 奶茶消费记录与统计系统（注册/登录、个人信息、品牌、消费记录、统计）
说明:
- 需要认证的接口请在请求头添加 Authorization: Bearer <token>。
- 日期采用 ISO-8601 格式: YYYY-MM-DD。
- 本地开发使用 H2 文件数据库，数据持久化存储在 ./data/milkytea.mv.db，重启后数据保留。
端点概览
认证 (/api/auth)
- 用户注册
- 用户登录
用户 (/api/user) 🔒
- 获取用户信息
- 更新用户信息
- 修改用户名
- 修改密码
品牌 (/api/brands)
- 获取所有品牌（公开）
- 获取品牌详情（公开）
- 创建品牌（需登录）
- 删除品牌（需登录）
奶茶记录 (/api/records) 🔒
- 创建记录
- 获取记录列表（支持分页、筛选）
- 获取记录详情
- 更新记录
- 删除记录
统计 (/api/statistics) 🔒
- 获取基础统计（近期杯数、天数、总金额）
- 获取品牌统计（各品牌消费统计）
- 获取趋势分析（消费频率和金额趋势）
- 获取日历月度数据
注: 🔒 表示需要认证

---
认证相关接口
用户注册
- 接口: POST /api/auth/register
- 描述: 创建新用户账号
- 请求头:
  - Content-Type: application/json
- 请求体:
{"username": "zhangsan","phone": "13800138000","password": "password123"}
- 字段说明:
  - username(string, 必填): 3-25 个字符，唯一
  - phone(string, 必填): 11 位中国大陆手机号，唯一，需匹配 ^1[3-9]\d{9}$
  - password(string, 必填): 6-25 个字符，需包含字母与数字（如 Abc123）
- 成功响应(200):
{"token": "<jwt>","type": "Bearer","userId": 1,"username": "zhangsan","phone": "13800138000"}
- 失败响应(示例):
  - 400 验证失败 / 用户名或手机号已存在 / 密码不符合复杂度要求
用户登录
- 接口: POST /api/auth/login
- 描述: 使用手机号和密码登录
- 请求头:
  - Content-Type: application/json
- 请求体:
{"phone": "13800138000","password": "password123"}
- 成功响应(200): 与注册成功响应相同
- 失败响应(401): {"status":401,"message":"手机号或密码错误"}
说明: 登录成功返回的 token 有效期 7 天，请在所有需认证接口的请求头中携带 Authorization: Bearer <token>。

---
用户相关接口 (需认证)
获取用户信息
- 接口: GET /api/user/profile
- 描述: 获取当前登录用户的个人信息
- 请求头:
  - Authorization: Bearer <token>
- 成功响应(200):
{"id": 1,"username": "zhangsan","phone": "13800138000","avatar": "https://example.com/avatar.jpg","createdAt": "2025-11-18T10:00:00","updatedAt": "2025-11-18T14:30:00"}
更新用户信息
- 接口: PUT /api/user/profile
- 描述: 更新当前登录用户的个人信息
- 请求头:
  - Authorization: Bearer <token>
  - Content-Type: application/json
- 请求体:
{"phone": "13900139000","avatar": "data:image/png;base64,..."}
- 字段说明:
  - phone(string, 可选): 11 位手机号，修改时需唯一且匹配 ^1[3-9]\d{9}$
  - avatar(string, 可选): 头像(支持 Base64 或 URL)
  - 说明: 手机号可修改；若被占用将返回 400
- 成功响应(200): 返回最新用户信息（同"获取用户信息"）
- 失败响应(400): {"message":"手机号已被使用"}
修改用户名
- 接口: PATCH /api/user/username
- 描述: 修改当前登录用户的用户名（右上角头像下拉的“修改用户名”）
- 请求头:
  - Authorization: Bearer <token>
  - Content-Type: application/json
- 请求体:
{"newUsername": "new_name_123"}
- 规则:
  - newUsername: 3-25 个字符，只能包含字母、数字、下划线，需唯一
- 成功响应(200): 返回更新后的用户信息（同“获取用户信息”）
- 失败响应:
  - 400: 用户名不合法或已存在
  - 401: 未认证
修改密码
- 接口: POST /api/user/change-password
- 描述: 修改当前登录用户的密码
- 请求头:
  - Authorization: Bearer <token>
  - Content-Type: application/json
- 请求体:
{"oldPassword": "oldPassword123","newPassword": "newPassword456"}
- 成功响应(200): 无响应体
- 失败响应(400): {"message":"旧密码不正确"}

---
品牌相关接口
说明: GET 列表与详情对未登录用户开放；创建与删除需认证。
获取所有品牌（公开）
- 接口: GET /api/brands
- 描述: 获取所有奶茶品牌列表
- 成功响应(200):
[{"id": 1,"name": "喜茶","description": "灵感之茶","logoUrl": "https://example.com/logo1.png","createdAt": "2025-11-18T10:00:00","updatedAt": "2025-11-18T10:00:00"}]
获取品牌详情（公开）
- 接口: GET /api/brands/{id}
- 描述: 根据 ID 获取品牌详情
- 成功响应(200): 同上单项结构
- 失败响应(404): {"message":"品牌不存在"}
创建品牌（需认证）
- 接口: POST /api/brands
- 请求头: Authorization: Bearer <token>, Content-Type: application/json
- 请求体:
{"name": "古茗","description": "高性价比","logoUrl": "https://example.com/guming.png"}
- 成功响应(201): 返回新建品牌信息
- 失败响应(400): {"message":"品牌名称已存在"}
删除品牌（需认证）
- 接口: DELETE /api/brands/{id}
- 成功响应(204): 无响应体
- 失败响应(404): {"message":"品牌不存在"}

---
奶茶记录相关接口 (需认证)
记录字段说明:
- brandId(Long): 品牌 ID（必填）
- category(String): 品类（必填）
- sweetness(String): 甜度（必填）
- iceLevel(String): 冰度（必填）
- price(Number): 价格，>=0（必填）
- rating(Integer): 评分 0-10（必填）
- comment(String): 评语（选填）
- consumeDate(String): 消费日期，YYYY-MM-DD（选填，默认今天）
创建记录
- 接口: POST /api/records
- 请求头: Authorization: Bearer <token>, Content-Type: application/json
- 请求体:
{"brandId": 1,"category": "波霸奶茶","sweetness": "半糖","iceLevel": "少冰","price": 18.00,"rating": 8,"comment": "很好喝","consumeDate": "2025-11-18"}
- 成功响应(201):
{"id": 10,"brandId": 1,"brandName": "喜茶","category": "波霸奶茶","sweetness": "半糖","iceLevel": "少冰","price": 18.0,"rating": 8,"comment": "很好喝","consumeDate": "2025-11-18","createdAt": "2025-11-18T14:40:00","updatedAt": "2025-11-18T14:40:00"}
- 失败响应(400): 品牌不存在/字段验证失败
获取记录列表
- 接口: GET /api/records
- 描述: 获取当前用户的记录（按消费日期降序），支持按日期/品牌/品类筛选
- 查询参数(可选):
  - date(YYYY-MM-DD): 指定日期的记录
  - startDate(YYYY-MM-DD): 起始日期（含）
  - endDate(YYYY-MM-DD): 结束日期（含）
  - brandId(Long): 品牌 ID
  - category(String): 品类关键词
  - page(int), size(int): 分页参数（默认不分页）
- 成功响应(200): RecordResponse[]（同上结构数组）
示例:
GET /api/records?date=2025-11-18
GET /api/records?startDate=2025-11-01&endDate=2025-11-30&brandId=1
获取记录详情
- 接口: GET /api/records/{id}
- 成功响应(200): RecordResponse
- 失败响应: 404 记录不存在；403 无权访问
更新记录
- 接口: PUT /api/records/{id}
- 请求头: Authorization: Bearer <token>, Content-Type: application/json
- 请求体: 与创建相同字段，均为选填；只更新提供的字段
- 成功响应(200): RecordResponse
- 失败响应: 404 记录不存在；403 无权修改
删除记录
- 接口: DELETE /api/records/{id}
- 成功响应(204): 无响应体
- 失败响应: 404 记录不存在；403 无权删除

---
统计相关接口 (需认证)
获取基础统计
- 接口: GET /api/statistics/summary
- 描述: 返回统计周期内的基础统计信息
- 查询参数(选填):
  - startDate(YYYY-MM-DD): 开始日期，默认 T-30 天
  - endDate(YYYY-MM-DD): 结束日期，默认今天
- 成功响应(200):
{"totalCups": 12,"totalDays": 8,"totalAmount": 216.00,"averagePrice": 18.00,"maxPrice": 28.00,"minPrice": 10.00,"averageRating": 8.2,"startDate": "2025-10-18","endDate": "2025-11-18"}
获取品牌统计
- 接口: GET /api/statistics/brands
- 描述: 返回统计周期内各品牌的消费统计
- 查询参数(选填): 同“基础统计”
- 成功响应(200):
{"statistics": [{"brandId": 1,"brandName": "喜茶","count": 6,"amount": 108.00,"averageRating": 8.3,"percentage": 50.0}],"startDate": "2025-10-18","endDate": "2025-11-18"}
获取趋势分析
- 接口: GET /api/statistics/trends
- 描述: 返回指定时间段内每天/每周/每月的杯数与金额趋势
- 查询参数(选填):
  - startDate(YYYY-MM-DD): 开始日期，默认 T-30 天
  - endDate(YYYY-MM-DD): 结束日期，默认今天
  - groupBy(enum): 统计粒度，day|week|month，默认 day
- 成功响应(200):
{"groupBy": "day","series": {"cups": [{ "date": "2025-11-01", "value": 1 },{ "date": "2025-11-02", "value": 0 }],"amount": [{ "date": "2025-11-01", "value": 18.00 },{ "date": "2025-11-02", "value": 0.00 }]},"startDate": "2025-10-18","endDate": "2025-11-18"}
获取日历月度数据
- 接口: GET /api/statistics/calendar/{year}/{month}
- 描述: 获取指定年月的日历消费数据
- 路径参数:
  - year(int): 年份，如 2025
  - month(int): 月份，1-12
- 成功响应(200):
{"year": 2025,"month": 11,"days": [{ "date": "2025-11-01", "count": 1, "amount": 18.00, "hasConsumption": true },{ "date": "2025-11-02", "count": 0, "amount": 0.00, "hasConsumption": false }],"totalCups": 12,"totalAmount": 216.00,"consumeDays": 8}

---
认证说明
获取 Token
- 通过“注册”或“登录”接口获取 token
- Token 有效期(默认): 7 天（604800000 毫秒）
使用 Token
- 在请求头添加:
Authorization: Bearer <your_token>

---
错误响应约定
统一错误结构:
{"status": 400,"message": "错误信息","timestamp": "2025-11-18T14:30:00","errors": { "field": "校验错误描述" }}
常见状态码:
- 200: 请求成功
- 201: 创建成功
- 204: 无内容（删除成功）
- 400: 参数/业务错误
- 401: 未授权（缺少/无效 Token）
- 403: 无权限
- 404: 资源不存在
- 500: 服务器内部错误

---
开发辅助
Swagger UI
- 访问: http://localhost:8080/swagger-ui.html
H2 数据库控制台
- 访问: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:file:./data/milkytea
- Username: sa
- Password: 空

---
cURL 测试示例
注册用户
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "phone": "13800138000",
    "password": "test123456"
  }'
登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123456"
  }'
创建品牌（需替换 TOKEN）
curl -X POST http://localhost:8080/api/brands \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "喜茶",
    "description": "灵感之茶"
  }'
创建记录（需替换 TOKEN 与 brandId）
curl -X POST http://localhost:8080/api/records \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "brandId": 1,
    "category": "波霸奶茶",
    "sweetness": "半糖",
    "iceLevel": "少冰",
    "price": 18.00,
    "rating": 8
  }'
获取本月日历数据（需替换 TOKEN）
curl -X GET "http://localhost:8080/api/statistics/calendar/2025/11" \
  -H "Authorization: Bearer TOKEN"
{"status": 400,"message": "用户名已存在","timestamp": "2025-11-18T14:30:00","errors": {}}

---
用户登录
接口: POST /api/auth/login
描述: 使用用户名和密码登录
请求头:
Content-Type: application/json
请求体:
{"username": "zhangsan","password": "password123"}
字段说明:
暂时无法在飞书文档外展示此内容
成功响应 (200 OK):
{"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...","type": "Bearer","userId": 1,"username": "zhangsan","phone": "13800138000"}
错误响应:
401 Unauthorized - 用户名或密码错误:
{"status": 401,"message": "用户名或密码错误","timestamp": "2025-11-18T14:30:00","errors": {}}

---
用户相关接口
注意: 以下接口需要在请求头中携带 JWT Token
获取用户信息
接口: GET /api/user/profile
描述: 获取当前登录用户的个人信息
请求头:
Authorization: Bearer <token>
请求参数: 无
成功响应 (200 OK):
{"id": 1,"username": "zhangsan","phone": "13800138000","avatar": "https://example.com/avatar.jpg","createdAt": "2025-11-18T10:00:00","updatedAt": "2025-11-18T14:30:00"}
错误响应:
404 Not Found - 用户不存在:
{"status": 404,"message": "用户不存在","timestamp": "2025-11-18T14:30:00","errors": {}}

---
更新用户信息
接口: PUT /api/user/profile
描述: 更新当前登录用户的个人信息
请求头:
Authorization: Bearer <token>
Content-Type: application/json
请求体:
{"phone": "13900139000","avatar": "https://example.com/new-avatar.jpg"}
字段说明:
暂时无法在飞书文档外展示此内容
成功响应 (200 OK):
{"id": 1,"username": "zhangsan","phone": "13900139000","avatar": "https://example.com/new-avatar.jpg","createdAt": "2025-11-18T10:00:00","updatedAt": "2025-11-18T14:35:00"}
错误响应:
400 Bad Request - 手机号已被使用:
{"status": 400,"message": "手机号已被使用","timestamp": "2025-11-18T14:30:00","errors": {}}

---
修改密码
接口: POST /api/user/change-password
描述: 修改当前登录用户的密码
请求头:
Authorization: Bearer <token>
Content-Type: application/json
请求体:
{"oldPassword": "oldPassword123","newPassword": "newPassword456"}
字段说明:
暂时无法在飞书文档外展示此内容
成功响应 (200 OK):
(无响应体)
错误响应:
400 Bad Request - 旧密码不正确:
{"status": 400,"message": "旧密码不正确","timestamp": "2025-11-18T14:30:00","errors": {}}

---
认证说明
获取 Token
1. 调用注册或登录接口获取 JWT Token
2. Token 有效期为 7 天（604800000 毫秒）
使用 Token
在需要认证的接口请求头中添加:
Authorization: Bearer <your_token>
示例:
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEsImVtYWlsIjoiemhhbmdzYW5AZXhhbXBsZS5jb20iLCJzdWIiOiJ6aGFuZ3NhbiIsImlhdCI6MTYzNzIxNDAwMCwiZXhwIjoxNjM3ODE4ODAwfQ.abc123

---
错误码说明
暂时无法在飞书文档外展示此内容

---
Swagger UI
访问 Swagger UI 查看交互式 API 文档:
http://localhost:8080/swagger-ui.html

---
H2 数据库控制台
在开发环境中，可以访问 H2 数据库控制台查看数据:
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:milkytea
Username: sa
Password: (留空)

---
测试示例
使用 cURL
注册用户
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "phone": "13800138000",
    "password": "test123456"
  }'
登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123456"
  }'
获取用户信息
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
更新用户信息
curl -X PUT http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13900139000",
    "avatar": "https://example.com/new-avatar.jpg"
  }'
修改密码
curl -X POST http://localhost:8080/api/user/change-password \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "oldPassword": "test123456",
    "newPassword": "newpassword123"
  }'

---
技术栈
- 框架: Spring Boot 2.7.18
- Java 版本: Java 8
- 数据库: H2 (开发环境内存数据库)
- 安全: Spring Security + JWT
- ORM: Spring Data JPA + Hibernate
- API 文档: SpringDoc OpenAPI 3

---
注意事项
1. 密码安全: 所有密码均使用 BCrypt 加密存储
2. CORS: 当前配置允许所有来源，生产环境请修改为特定域名
3. 数据库: 当前使用内存数据库 H2，重启后数据会丢失。生产环境请配置 MySQL/PostgreSQL
4. Token 过期: Token 有效期为 7 天，过期后需重新登录
5. 字段验证: 所有输入字段都有验证，违反验证规则会返回 400 错误