# API 接口文档

> 请后端开发人员在完成每个接口后，在此文档中填写对应的接口信息。
> 填写格式参考下方示例。

---

## 接口填写示例

```
### [接口名称]
- **URL**: `GET /api/example`
- **功能描述**: 简要说明接口作用
- **请求参数**:
  | 参数名 | 类型 | 必填 | 说明 |
  |:---|:---|:---:|:---|
  | id | Long | 是 | 用户ID |
- **请求体** (JSON):
  ```json
  {
    "field": "value"
  }
  ```
- **响应示例** (JSON):
  ```json
  {
    "code": 200,
    "data": {},
    "msg": "success"
  }
  ```
```

---

## 1. 用户相关接口 (User)

> 由 **后端-内容管理组** 填写

### 1.1 用户注册

- **URL**: `POST /user/register`
- **功能描述**: 注册新用户，成功后自动返回登录凭证
- **请求参数**: 无
- **请求体** (JSON):
  ```json
  {
    "username": "newuser",
    "password": "123456"
  }
  ```
- **响应示例** (JSON):
  ```json
  {
    "code": 200,
    "data": {
      "token": "eyJhbGciOiJIUzI1NiJ9...",
      "userId": 4,
      "username": "newuser"
    },
    "msg": "success"
  }
  ```

### 1.2 用户登录

- **URL**: `POST /user/login`
- **功能描述**: 用户登录，返回 JWT Token
- **请求参数**: 无
- **请求体** (JSON):
  ```json
  {
    "username": "alice",
    "password": "123456"
  }
  ```
- **响应示例** (JSON):
  ```json
  {
    "code": 200,
    "data": {
      "token": "eyJhbGciOiJIUzI1NiJ9...",
      "userId": 1,
      "username": "alice"
    },
    "msg": "success"
  }
  ```

### 1.3 用户注销

- **URL**: `POST /user/logout`
- **功能描述**: 用户注销（JWT 无状态，客户端清除 Token 即可）
- **请求头**: `Authorization: Bearer <token>`
- **请求参数**: 无
- **请求体**: 无
- **响应示例** (JSON):
  ```json
  {
    "code": 200,
    "data": null,
    "msg": "success"
  }
  ```

---

## 2. 视频推荐相关接口 (Feed)

> 由 **后端-推荐流组** 填写

### 2.1 获取推荐视频流

- **URL**: `GET /video/feed`
- **功能描述**:
- **请求参数**:
- **请求体**:
- **响应示例**:

### 2.2 视频上下滑动

- **URL**: `GET /video/next` / `GET /video/prev`
- **功能描述**:
- **请求参数**:
- **请求体**:
- **响应示例**:

### 2.3 点赞 / 取消点赞

- **URL**: `POST /video/like` / `POST /video/unlike`
- **功能描述**:
- **请求参数**:
- **请求体**:
- **响应示例**:

---

## 3. 视频管理相关接口 (Video)

> 由 **后端-内容管理组** 填写

### 3.1 发布视频

- **URL**: `POST /video/publish`
- **功能描述**: 上传并发布视频（需登录）
- **请求头**: `Authorization: Bearer <token>`
- **请求参数** (multipart/form-data):
  | 参数名 | 类型 | 必填 | 说明 |
  |:---|:---|:---:|:---|
  | file | File | 是 | 视频文件（mp4/mov/avi/mkv/webm/flv，最大50MB） |
  | title | String | 是 | 视频标题 |
- **请求体**: multipart/form-data
- **响应示例** (JSON):
  ```json
  {
    "code": 200,
    "data": {
      "id": 4,
      "userId": 1,
      "videoUrl": "/uploads/a1b2c3d4.mp4",
      "title": "我的新视频",
      "likeCount": 0,
      "createTime": "2026-06-16T12:00:00"
    },
    "msg": "success"
  }
  ```

### 3.2 我的视频列表

- **URL**: `GET /video/my`
- **功能描述**: 分页查询当前登录用户发布的视频（需登录）
- **请求头**: `Authorization: Bearer <token>`
- **请求参数**:
  | 参数名 | 类型 | 必填 | 说明 |
  |:---|:---|:---:|:---|
  | page | int | 否 | 页码，默认 1 |
  | size | int | 否 | 每页条数，默认 10，最大 50 |
- **请求体**: 无
- **响应示例** (JSON):
  ```json
  {
    "code": 200,
    "data": {
      "records": [
        {
          "id": 1,
          "userId": 1,
          "videoUrl": "service/video/sample1.mp4",
          "title": "测试视频1 - 风景航拍",
          "likeCount": 5,
          "createTime": "2026-06-16T12:00:00"
        }
      ],
      "total": 1,
      "page": 1,
      "size": 10,
      "pages": 1
    },
    "msg": "success"
  }
  ```

### 3.3 删除视频

- **URL**: `DELETE /video/delete/{id}`
- **功能描述**: 删除指定视频，仅视频发布者可操作（需登录）
- **请求头**: `Authorization: Bearer <token>`
- **请求参数**:
  | 参数名 | 类型 | 必填 | 说明 |
  |:---|:---|:---:|:---|
  | id | Long | 是 | 视频 ID（路径参数） |
- **请求体**: 无
- **响应示例** (JSON):
  ```json
  {
    "code": 200,
    "data": null,
    "msg": "success"
  }
  ```
