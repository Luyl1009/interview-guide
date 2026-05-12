# AI 错误模式库 (AI Error Pattern Library)

本文件用于记录在 Vibe Coding 过程中 AI 反复出现的错误或不符合项目规范的代码模式。
每当发现新的重复性错误时，请在此处记录，并定期通过 `bmad-retrospective` 将其转化为 `.lingma/rules/vibe_coding_bmad.md` 中的自查规则。

---

## 📋 错误记录模板

### [编号] 错误名称
- **触发场景**：[例如：生成 React 表单组件时]
- **错误表现**：[例如：AI 引入了 Ant Design 的 `<Form>` 组件]
- **正确做法**：[例如：必须使用 TailwindCSS 类名配合原生 HTML 标签或 Headless UI]
- **状态**：[待处理 / 已加入规则 / 已忽略]
- **关联规则更新**：[指向 `.lingma/rules/vibe_coding_bmad.md` 中对应的检查点]

---

## 📝 当前记录

### 001 Service 层事务边界违规
- **触发场景**：实现涉及 LLM 调用或 S3 上传的业务逻辑时。
- **错误表现**：AI 在 `@Transactional` 方法内直接同步调用外部 API，导致数据库连接占用时间过长。
- **正确做法**：必须将耗时操作剥离到异步 Redis Stream 管道中处理。
- **状态**：✅ 已加入规则
- **关联规则更新**：`.lingma/rules/vibe_coding_bmad.md` -> `Self-Check Protocol` -> `质量护栏自查`

### 002 前端样式规范冲突
- **触发场景**：生成新的 UI 页面或弹窗时。
- **错误表现**：AI 习惯性地引入 `antd` 或编写内联 `style={{}}`。
- **正确做法**：强制使用 TailwindCSS 4.1 工具类，严禁引入其他 UI 库。
- **状态**：✅ 已加入规则
- **关联规则更新**：`.lingma/rules/vibe_coding_bmad.md` -> `Self-Check Protocol` -> `架构与规范自查`

---

## 🔄 迭代流程
1. **发现**：在 Code Review 或测试中发现 AI 犯错。
2. **记录**：按上述模板在本文件中新增一条记录。
3. **回顾**：每两周运行 `bmad-retrospective` 分析本文件。
4. **转化**：将高频错误点加入 `.lingma/rules/vibe_coding_bmad.md` 的自查清单。
5. **标记**：将本文件中对应条目的状态更新为“✅ 已加入规则”。
