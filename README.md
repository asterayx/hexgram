# 易经六爻 · 铜钱摇卦

基于 Cloudflare Pages + Workers 的在线易经占卦应用。前端摇卦，后端 AI 解卦。

## 项目结构

```
yijing-cf/
├── frontend/
│   └── index.html          # 完整前端（单文件，响应式）
├── worker/
│   ├── worker.js           # Cloudflare Worker（AI 解卦后端）
│   └── wrangler.toml       # Worker 配置
└── README.md
```

## 功能特性

- 🪙 **铜钱摇卦**：模拟三枚铜钱，随机产生少阳(7)、少阴(8)、老阳(9)、老阴(6)
- 🔄 **变卦支持**：自动识别动爻，计算变卦
- 🤖 **AI 解卦**：支持 Claude / GPT / OpenRouter 等多种 LLM
- 📱 **响应式设计**：完美适配手机端和桌面端
- ⚙️ **灵活配置**：前端可配置后端地址，无后端时使用本地解卦引擎
- 🔒 **API Key 安全**：Key 存储在 Worker 环境变量中，不暴露给前端

## 部署步骤

### 方式一：Cloudflare Dashboard（推荐，无需命令行）

#### 1. 部署前端（Cloudflare Pages）

1. 登录 [Cloudflare Dashboard](https://dash.cloudflare.com)
2. 进入 **Workers & Pages** → **Create** → **Pages** → **Upload assets**
3. 项目名称填 `yijing`（或任意名称）
4. 上传 `frontend/` 文件夹（只含 index.html）
5. 点击 Deploy
6. 得到地址如 `https://yijing.pages.dev`

#### 2. 部署后端（Cloudflare Workers）

1. 进入 **Workers & Pages** → **Create** → **Worker**
2. 名称填 `yijing-api`
3. 点击 Deploy，然后 **Edit Code**
4. 将 `worker/worker.js` 的内容粘贴进去，保存部署
5. 进入 Worker 的 **Settings** → **Variables and Secrets**
6. 添加以下环境变量：

| 变量名 | 值 | 说明 |
|--------|------|------|
| `ANTHROPIC_API_KEY` | `sk-ant-xxx...` | Claude API Key（加密） |
| `ALLOWED_ORIGIN` | `https://yijing.pages.dev` | 前端域名（CORS） |
| `LLM_PROVIDER` | `anthropic` | 可选：anthropic / openai / openrouter |
| `ANTHROPIC_MODEL` | `claude-sonnet-4-20250514` | 可选，默认 claude-sonnet-4 |

7. 得到 Worker 地址如 `https://yijing-api.your-domain.workers.dev`

#### 3. 配置前端连接后端

打开前端页面 → 点击右下角 ⚙ 齿轮 → 填入 Worker 地址 → 保存

### 方式二：Wrangler CLI

```bash
# 安装 wrangler
npm install -g wrangler

# 部署 Worker
cd worker
wrangler login
wrangler deploy

# 设置密钥
wrangler secret put ANTHROPIC_API_KEY

# 部署前端
cd ../frontend
wrangler pages deploy . --project-name=yijing
```

## LLM Provider 配置

### Claude（默认）

```
LLM_PROVIDER = anthropic
ANTHROPIC_API_KEY = sk-ant-xxx...
ANTHROPIC_MODEL = claude-sonnet-4-20250514  (可选)
```

### OpenAI / 兼容 API

```
LLM_PROVIDER = openai
OPENAI_API_KEY = sk-xxx...
OPENAI_MODEL = gpt-4o  (可选)
OPENAI_BASE_URL = https://api.openai.com/v1  (可选，可改为其他兼容API)
```

### OpenRouter

```
LLM_PROVIDER = openrouter
OPENROUTER_API_KEY = sk-or-xxx...
OPENROUTER_MODEL = anthropic/claude-sonnet-4-20250514  (可选)
```

## 无后端模式

前端内置了本地解卦引擎，不配置后端地址时自动使用。本地引擎包含：
- 五行生克分析
- 体用关系判断
- 变卦趋势分析
- 基础爻辞解读

配置 AI 后端后可获得更深入、更个性化的解读。

## 自定义域名

1. 在 Cloudflare Pages 项目中添加 Custom Domain（如 `yijing.example.com`）
2. 在 Worker 中添加 Custom Domain（如 `api.yijing.example.com`）
3. 更新 `ALLOWED_ORIGIN` 为新域名

## 技术栈

- **前端**：原生 HTML/CSS/JS，零依赖，单文件
- **后端**：Cloudflare Worker（V8 引擎，边缘部署）
- **AI**：Claude / GPT / OpenRouter（可切换）
- **部署**：Cloudflare Pages + Workers（免费额度足够个人使用）

## 免费额度

Cloudflare 免费计划包含：
- Workers：每天 100,000 次请求
- Pages：无限静态站点
- 足够个人和小型使用场景
