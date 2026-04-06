# 易学三合 - 部署指南

## 项目结构

```
yijing-cf/
├── frontend/                # 前端（部署到 Cloudflare Pages）
│   ├── index.html           # 主页面（三Tab：六爻/八字/黄历）
│   └── classics.js          # 经典文献数据库（高岛易断/黄金策/焦氏易林）
├── worker/                  # 后端（部署到 Cloudflare Workers）
│   ├── worker.js            # AI解卦Worker
│   └── wrangler.toml        # Worker配置
├── engine/                  # 独立引擎模块（供开发参考）
│   ├── najia.js             # 纳甲装卦引擎
│   └── classics.js          # 经典文献数据源
└── README.md
```

---

## 第一步：转移到 Claude Code

### 方法A：直接下载解压

```bash
# 在你的Mac上
cd ~/projects  # 或任意目录
# 把从Claude.ai下载的 yijing-cloudflare.tar.gz 放到这里
tar xzf yijing-cloudflare.tar.gz
cd yijing-cf
```

### 方法B：用 Claude Code 从零创建

```bash
# 打开终端，进入工作目录
cd ~/projects
mkdir yijing-cf && cd yijing-cf

# 启动 Claude Code
claude

# 在 Claude Code 中说：
# "请帮我创建一个 Cloudflare Pages + Workers 的易经占卦应用，
#  包含六爻纳甲排盘、八字排盘、黄历查询三个功能Tab。
#  我有现成的代码文件要导入。"

# 然后把文件内容粘贴进去，或让Claude Code读取解压后的文件
```

### 方法C：在 Claude Code 中直接操作已有文件

```bash
cd ~/projects/yijing-cf
claude

# 在 Claude Code 中说：
# "读取当前目录的项目结构，帮我部署到Cloudflare"
```

---

## 第二步：安装工具

### 安装 Wrangler（Cloudflare CLI）

```bash
# 需要 Node.js >= 16
npm install -g wrangler

# 验证安装
wrangler --version

# 登录 Cloudflare（会打开浏览器）
wrangler login
```

### 如果没有 Node.js

```bash
# macOS
brew install node

# 或使用 nvm
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install 20
nvm use 20
```

---

## 第三步：部署前端（Cloudflare Pages）

### 方式A：Wrangler CLI 部署（推荐）

```bash
cd ~/projects/yijing-cf

# 首次部署：创建项目
wrangler pages project create yijing

# 部署前端文件夹
wrangler pages deploy frontend/ --project-name=yijing

# 输出类似：
# ✨ Deployment complete!
# URL: https://yijing.pages.dev
# 或: https://xxxxx.yijing.pages.dev
```

后续更新代码后，再次运行同一命令即可：
```bash
wrangler pages deploy frontend/ --project-name=yijing
```

### 方式B：Dashboard 手动上传

1. 打开 https://dash.cloudflare.com
2. 左侧菜单 → **Workers & Pages**
3. 点击 **Create** → **Pages** → **Upload assets**
4. 项目名称填 `yijing`
5. 把 `frontend/` 文件夹里的 **两个文件**（index.html + classics.js）拖进去
6. 点击 **Deploy**
7. 得到地址如 `https://yijing.pages.dev`

---

## 第四步：部署后端 Worker（AI解卦）

### 方式A：Wrangler CLI 部署（推荐）

```bash
cd ~/projects/yijing-cf/worker

# 部署 Worker
wrangler deploy

# 输出类似：
# Published yijing-api (xxx.workers.dev)
# https://yijing-api.你的subdomain.workers.dev

# 设置API密钥（加密存储，不会出现在代码中）
wrangler secret put ANTHROPIC_API_KEY
# 粘贴你的 Claude API Key: sk-ant-api03-xxxxx

# 设置允许的前端域名
wrangler secret put ALLOWED_ORIGIN
# 输入: https://yijing.pages.dev

# 可选：设置模型
wrangler secret put ANTHROPIC_MODEL
# 输入: claude-sonnet-4-20250514
```

### 方式B：Dashboard 手动部署

1. 打开 https://dash.cloudflare.com
2. 左侧 → **Workers & Pages** → **Create** → **Worker**
3. 名称填 `yijing-api`，点 Deploy
4. 点 **Edit Code**（快速编辑）
5. 删除默认代码，把 `worker/worker.js` 的全部内容粘贴进去
6. 点 **Save and Deploy**
7. 进入 Worker 的 **Settings** → **Variables and Secrets**
8. 点 **Add** 添加以下变量：

| 变量名 | 类型 | 值 |
|--------|------|------|
| ANTHROPIC_API_KEY | Secret (Encrypt) | sk-ant-api03-xxxxx |
| ALLOWED_ORIGIN | Text | https://yijing.pages.dev |
| LLM_PROVIDER | Text | anthropic |
| ANTHROPIC_MODEL | Text | claude-sonnet-4-20250514 |

9. 得到 Worker 地址如 `https://yijing-api.你的subdomain.workers.dev`

---

## 第五步：连接前端和后端

打开前端网站 → 点右下角 **⚙ 齿轮** → 填入 Worker 地址 → 保存

或者，如果想硬编码（不用每次手动设置）：

在 `frontend/index.html` 中找到 localStorage 相关代码，添加默认值：
```javascript
// 在 Init 部分添加
if (!localStorage.getItem("yj_ep")) {
  localStorage.setItem("yj_ep", "https://yijing-api.你的subdomain.workers.dev");
}
```

---

## 第六步：绑定自定义域名（可选）

### Pages 自定义域名

1. Cloudflare Dashboard → Workers & Pages → yijing 项目
2. **Custom domains** → Add domain
3. 输入如 `yijing.example.com`
4. Cloudflare 会自动配置 DNS（如果域名在 Cloudflare 管理）

### Worker 自定义域名

1. Workers & Pages → yijing-api Worker
2. **Triggers** → Custom Domains → Add
3. 输入如 `api.yijing.example.com`

然后更新 ALLOWED_ORIGIN 为新域名。

---

## 部署验证清单

- [ ] 前端可访问（https://yijing.pages.dev）
- [ ] 三个Tab都能正常切换
- [ ] 六爻：摇卦→排盘→显示经典文献（高岛易断/黄金策/焦氏易林）
- [ ] 八字：输入生日→排盘→显示四柱/十神/大运
- [ ] 黄历：选择日期→显示宜忌/方位/冲煞
- [ ] AI深度解读按钮→调用Worker→返回专业解读
- [ ] ⚙设置面板能保存API配置

---

## 常见问题

### Q: AI解读报 CORS 错误
A: 检查 Worker 的 ALLOWED_ORIGIN 是否设置为前端的完整域名（含 https://）

### Q: AI解读返回 401
A: API Key 可能过期或格式不对。重新执行 `wrangler secret put ANTHROPIC_API_KEY`

### Q: 想用 OpenAI 而不是 Claude
A: 设置环境变量：
```
LLM_PROVIDER = openai
OPENAI_API_KEY = sk-xxxxx
OPENAI_MODEL = gpt-4o
```

### Q: 想用 OpenRouter（可以切换多种模型）
A:
```
LLM_PROVIDER = openrouter
OPENROUTER_API_KEY = sk-or-xxxxx
OPENROUTER_MODEL = anthropic/claude-sonnet-4-20250514
```

### Q: 前端直连API模式（不用Worker）
A: 在⚙设置面板中：
- 方式一（Worker地址）留空
- 方式二填入 Provider / API Key / Model
- Key 存储在浏览器 localStorage 中，仅本机可见

### Q: 更新代码后如何重新部署
A:
```bash
# 更新前端
wrangler pages deploy frontend/ --project-name=yijing

# 更新Worker
cd worker && wrangler deploy
```

---

## Claude Code 中的后续开发建议

在 Claude Code 中可以继续让 AI 帮你：

```
# 扩充焦氏易林数据库
"帮我在 classics.js 的 JIAOSHI 对象中补充更多卦变辞条目"

# 优化系统提示词
"优化六爻解卦的系统提示词，增加对伏神和应期的分析要求"

# 增加农历转换
"给黄历功能增加准确的公历转农历算法"

# 增加真太阳时
"给八字排盘增加经度校正和真太阳时计算"

# 增加神煞系统
"给八字排盘增加天乙贵人、文昌、驿马等神煞的计算"
```
