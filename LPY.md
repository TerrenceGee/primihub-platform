# 令牌云协作节点

## 版本记录

| 版本号  | 版本号   | 分支            | deploy分支 | web镜像                                                    | application镜像                      | node镜像                                                                      |
|------|-------|---------------|----------|----------------------------------------------------------|------------------------------------|-----------------------------------------------------------------------------|
| 0926 | 09-26 | feature/lpy_A |          | registry.cn-beijing.aliyuncs.com/primihub/primihub-web:6 | 192.168.99.10/primihub/privacy:316 | registry.cn-beijing.aliyuncs.com/primihub/primihub-node:2024-05-23-18-44-42 |

## sql

```sql
ALTER TABLE privacy.data_exam_task
    ADD COLUMN raw_file_path VARCHAR(256) DEFAULT '';
```