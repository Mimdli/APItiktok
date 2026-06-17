# 前端启动说明

## 1. 先启动后端（必须）

登录/注册依赖后端，**只开前端会报 500**。

1. 配置 `backend/.env`（复制 `.env.example`），**填写 MySQL 密码**：
   ```ini
   DB_PASSWORD=你的MySQL密码
   UPLOAD_PATH=C:/Users/Derek8571/Desktop/api设计与实现/APItiktok/uploads/
   ```
2. 在 MySQL 中执行 `backend/src/main/resources/init.sql`
3. 用 IDEA 打开 `backend`，运行 `App.java`
4. 看到日志 `Tomcat started on port(s): 8080` 表示成功

## 2. 再启动前端

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 http://localhost:5173

## 3. 测试账号

- 用户名：`alice`
- 密码：`123456`

## 4. 常见问题

| 现象 | 原因 | 解决 |
|------|------|------|
| Request failed with status code 500 | 后端没启动 | 先启动 App.java |
| Access denied for user 'root' | DB 密码错 | 改 `.env` 的 `DB_PASSWORD` |
| Unknown database 'tiktok' | 没建库 | 执行 `init.sql` |
