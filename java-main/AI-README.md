# AI-README —— 给 AI 助手看的速读说明

> 本文件由 AI 生成并维护。**下次打开这个目录，先读这里，不要重新全盘扫描、不要重复踩坑。**
> 最后更新：2026-10-12

---

## 0. 一句话概括

`java-main` 下是一套配套的前后端项目：

| 项目 | 位置 | 技术栈 | 端口 |
|---|---|---|---|
| 后端 `springBootTest` | `.\springBootTest` | Spring Boot 3.4.5 + MyBatis-Plus 3.5.9 + MySQL 8 | 8081 |
| 前端 `第五人格` | `.\第五人格` | Vue 3 + Vite 5 | 5173 |

### ⚠️⚠️ 桌面上有**两份** `第五人格`，别搞混

| 位置 | 状态 |
|---|---|
| `C:\Users\Administrator\Desktop\java-main\java-main\第五人格` | 内层，**一直是完整版**（api/ + stores/ + Home.vue + 路由守卫） |
| `C:\Users\Administrator\Desktop\java-main\第五人格` | 外层，之前是**旧纯演示版**（没有 api/，提交只弹 Toast）；**2026-10-12 已同步成完整版**，两份 `src/` 现在内容一致 |

用户口述路径时经常说的是**外层**那份（`java-main\第五人格`），但配套后端通常是内层 `java-main\java-main\springBootTest`。
**动手前先用 `diff -r` 确认改的是哪一份**，改错了等于白干。
（另：`C:\Users\Administrator\Desktop\java-main\java-main.rar` 是打包出去的压缩包，通常不用管。）

业务：第五人格（祭司主题）登录 / 注册。后端有**三套并列的入口**，读的是同一张 `t_user` 表：

### A. REST 接口（给前端 Vue 用，无状态 JWT）

| 接口 | 说明 | 需要令牌 |
|---|---|---|
| `POST /api/auth/register` | 注册，参数 `nickname` + `password` | 否 |
| `POST /api/auth/login` | 登录，返回 JWT 令牌 + 用户信息 | 否 |
| `GET /api/user/me` | 当前登录用户信息 | **是** |
**注册不需要手机号、不需要短信验证码**（已按用户要求移除，改动在 2026-10-09）。

### B. 服务器端页面流程（Thymeleaf + Session，2026-10-12 新增）

| 地址 | 说明 |
|---|---|
| `GET /` | 首页，带「登录 / 注册」入口 |
| `GET /register` · `POST /register` | 注册页；成功 **302 → /login** |
| `GET /login` · `POST /login` | 登录页；成功 **302 → /home** |
| `GET /home` | 用户主页，**需登录**，未登录被拦截器 **302 → /login?redirect=...** |
| `POST /logout` | 退出，**302 → /login?logout** |
| 任意 404/500 | 渲染 `templates/error.html`（主题错误页） |

导航图（细节见 §9）：

```
  /  ──注册──▶ /register ──成功 302──▶ /login ──成功 302──▶ /home ──退出 302──▶ /login
   ▲                                                          │
   └──────────────────── 未登录时被拦截器 302 ◀───────────────┘
```

### C. 教学演示页（和 A/B 都无关，密码写死 `123`）

`GET /user/to_login` → `templates/user/login.html` → `POST /user/login` → `templates/user/main.html`

> ⚠️ 后端自带的欢迎页 `static/index.html` 仍然在，但被 `PageController` 的 `GET /` 顶掉了，
> 只能通过 `/index.html` 显式访问（实测确认：`GET /` 返回 Thymeleaf 首页，`GET /index.html` 返回旧欢迎页）。

### ⚠️ `springBootTest.rar` 是「改造前」的旧版本，不要用它覆盖代码
rar 里还留着完整的手机号 + 短信验证码实现：
- `src/main/java/…/service/CodeService.java` —— 内存版 6 位验证码（5 分钟 TTL，`ConcurrentHashMap`，用后即删）
- `POST /api/auth/send-code?phone=xxx` —— 演示环境直接把验证码返回给前端
- `dto/RegisterRequest.java` —— 带 `phone`（`^1[3-9]\d{9}$`）和 `code`（6 位）两个字段

已用「127 个文件的清单比对」确认：**当前工作目录里这些都已经删干净了**（`CodeService.java` 整个文件不存在）。
用 rar 覆盖 = 把删掉的功能装回来。

其它：根目录 `README.md` 是给人看的快速开始；`11.txt`（内容 `54411561`）是无关的垃圾文件。

---

## 1. 本机环境事实（已实测，不用再查）

| 项 | 值 |
|---|---|
| JDK | Zulu **21.0.10**，`C:\Program Files\Zulu\zulu-21`（`JAVA_HOME` 已设置） |
| Maven | **没有安装 `mvn` 命令**。可用：`%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd`，或项目自带 `.\springBootTest\mvnw.cmd` |
| MySQL | 8.0，Windows 服务名 **`MySQL80`**，启动类型 = **自动**，端口 3306，账号 `root` / 密码 `123456` |
| MySQL 客户端 | `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe` |
| Node | 系统 **v20.11.0**，`D:\Program Files\nodejs`；WorkBuddy 自带 v22 在 `C:\Users\Administrator\.workbuddy\binaries\node\versions\22.22.2-2` |
| 数据库 | `idv_auth`，表 `t_user`（id / nickname / password / status / created_at / updated_at，**没有 phone**） |
| 同机其它库 | `ruoyi`（别的项目在用，**不要动 MySQL 服务、不要 drop 其它库**） |

> ⚠️ 这台电脑 **每次开机都会重置**：数据库 `idv_auth` 会消失、`node_modules` 和 `.m2` 缓存可能回到快照状态。
> 所以每次开机都要重跑一遍：建库建表 → 起后端 → 起前端。

---

## 2. 每次开机怎么启动（推荐直接双击脚本）

脚本全在 `java-main\` 根目录，**GBK 编码**（`chcp 936`，中文不会乱码）：

| 脚本 | 作用 |
|---|---|
| `启动全部.bat` | 起 MySQL 服务 → 建库建表 → 分别在新窗口起后端和前端（**最常用**） |
| `停止全部.bat` | 停后端 + 停前端（不动 MySQL80） |
| `初始化数据库.bat` | 只建库建表（`springBootTest\sql\init.sql`） |
| `启动后端.bat` / `停止后端.bat` | 单独起 / 停后端（8081，窗口标题 `IDV-BACKEND`） |
| `启动前端.bat` / `停止前端.bat` | 单独起 / 停前端（5173，窗口标题 `IDV-FRONTEND`） |

访问地址：后端 <http://localhost:8081> ｜ 前端 <http://localhost:5173>

---

## 3. AI 自己在命令行里要起服务时（关键，别踩）

### 3.1 必须先清掉这两个环境变量！
本机（沙箱）注入了 `SERVER__PORT=51126` / `SERVER__HOST=127.0.0.1`，
Spring Boot 会把它当成 `server.port`，导致 **绑到随机端口** 并报
`Web server failed to start. Port 51126 was already in use`，跟 `application-dev.properties` 里的 8081 无关。

```bash
unset SERVER__PORT SERVER__HOST
```

### 3.2 起后端（bash 里）
```bash
cd "C:/Users/Administrator/Desktop/java-main/java-main/springBootTest"
unset SERVER__PORT SERVER__HOST
export JAVA_HOME="C:/Program Files/Zulu/zulu-21"

# 方式 A：已经有 fat jar 就直接跑（最快，约 2 秒）
"$JAVA_HOME/bin/java" -jar target/springBootTest-0.0.1-SNAPSHOT.jar

# 方式 B：用本地缓存的 Maven 编译运行（改了 Java 代码用这个）
#   ⚠️ 必须用 cygpath -m 把 /c/... 转成 C:/...，否则 java 报
#      「找不到或无法加载主类 org.codehaus.plexus.classworlds.launcher.Launcher」
#      （JVM 是原生程序，不认识 MSYS 的 /c/Users/... 这种路径）
M=$(cygpath -m "$HOME/.m2/wrapper/dists/apache-maven-3.9.9-bin/4nf9hui3q3djbarqar9g711ggc/apache-maven-3.9.9")
"$JAVA_HOME/bin/java" -classpath "$M/boot/plexus-classworlds-2.8.0.jar" \
  "-Dclassworlds.conf=$M/bin/m2.conf" "-Dmaven.home=$M" \
  "-Dmaven.multiModuleProjectDirectory=C:/Users/Administrator/Desktop/java-main/java-main/springBootTest" \
  org.codehaus.plexus.classworlds.launcher.Launcher -B -DskipTests package
```
> 直接执行 `$M/bin/mvn` 会报 `找不到或无法加载主类 …Launcher`（MinGW 路径处理问题），**别用**，要么用上面的 java 直调，要么用 `mvn.cmd`。

> **改 Thymeleaf 模板时的提速技巧**：不用重新打包，启动时把模板/静态资源指向源码目录即可热加载。
> ```bash
> "$JAVA_HOME/bin/java" -jar target/springBootTest-0.0.1-SNAPSHOT.jar \
>   --spring.thymeleaf.cache=false \
>   --spring.thymeleaf.prefix=file:///C:/Users/Administrator/Desktop/java-main/java-main/springBootTest/src/main/resources/templates/ \
>   --spring.web.resources.static-locations=file:///C:/Users/Administrator/Desktop/java-main/java-main/springBootTest/src/main/resources/static/,classpath:/static/
> ```
> 改完 Java 代码仍然要重新 `package`。

### 3.3 起前端（bash 里）
```bash
cd "C:/Users/Administrator/Desktop/java-main/第五人格"
[ -d node_modules ] || npm install
npm run dev        # 监听 5173
```

### 3.4 建库（bash 里）
```bash
"/c/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe" -uroot -p123456 \
  --default-character-set=utf8mb4 < "C:/Users/Administrator/Desktop/java-main/java-main/springBootTest/sql/init.sql"
```
> ⚠️ 路径是 `java-main\java-main\springBootTest\...`（**嵌套两层同名目录**，容易写成只写一层）。

---

## 4. 沙箱 / 工具坑（避免浪费时间）

1. **`cmd.exe`、`sc.exe`、`reg.exe`、`wmic.exe` 在 Bash 里被安全策略禁用** → 不能用 bash 执行 `.bat`；`.bat` 只能让用户双击。要读服务启动类型就用 Python 的 `winreg`。
2. `tasklist` 在 Git Bash 里要 `export MSYS_NO_PATHCONV=1`，否则 `/FI` 会被当成路径转换而报无效参数。
3. `net start` **可以用**（用于查服务/启动 MySQL80）。
4. 端口被占时用 `netstat -ano | grep ":8081" | grep LISTENING` 找 PID，再 `taskkill` 杀掉（写法见第 8 条）。
5. 沙箱里 `curl` 走代理，连接失败会打印 `upstream connect failed`（不是项目问题）。**测本机服务建议统一加 `--noproxy '*'`**，否则可能被代理拦掉。
6. `~/.m2` 里 **只有 mybatis-plus 3.5.1 / mysql-connector-j 8.0.33**（这是 Spring Boot 2 时代的版本，**不能拿来替换 pom 里的 3.5.9 / 9.1.0**）。所以 **离线构建 `-o` 一定失败**，必须联网下载这两个依赖。
7. `~/.m2/settings.xml` 里 `aliyunmaven` 的地址是旧的 `http://maven.aliyun.com/nexus/content/groups/public/`（会 301 重定向），实测 **Maven 能跟随重定向、可以正常下载**；另外里面 `aliyunmaven` 这个 id 重复出现、`nexus-163` 的 163 镜像已停服，属于历史遗留，没坏就先别动（`ruoyi` 等项目共用）。
8. **`taskkill` 的两种写法别混**：设了 `export MSYS_NO_PATHCONV=1` 就用 `/F /PID <pid>`（单斜杠）；没设就用 `//F //PID <pid>`。混用会报「无效参数/选项 - '//F'」。
9. **`curl -o /tmp/x.html` 写不出文件**（但不会报错，只是文件不存在）。Git Bash 的 `/tmp`、`/c/...` 是虚拟路径，而 `curl.exe` 是原生程序。要拿 Windows 真实路径：
   ```bash
   OUT=$(cygpath -m "$(mktemp -d)")     # 得到 C:/Users/ADMINI~1/AppData/Local/Temp/tmp.xxx
   curl -s --noproxy '*' -o "$OUT/a.html" http://localhost:8081/login
   ```
   同样地，任何传给原生程序的路径参数都要先过一遍 `cygpath -m`（第 3.2 节的 Maven 就是这个坑）。
10. `mktemp -d` 出来的目录在系统 Temp 下，跑完记得不用管；但**别用它当项目输出目录**。

---

## 5. 项目结构速查

### 后端 `springBootTest`
```
pom.xml                                  Spring Boot 3.4.5, java 17(用 21 编译), mybatis-plus-spring-boot3-starter 3.5.9, jjwt 0.12.6
mvnw / mvnw.cmd / .mvn/wrapper/…         Maven Wrapper（distributionType=only-script, Maven 3.9.9）
sql/init.sql                             建库 idv_auth + 建表 t_user（无 phone 列）
log/xx.log                               应用日志
src/main/java/com/fenglin/springboottest/
  SpringBootTestApplication.java        启动类，@MapperScan("com.fenglin.springboottest.mapper")
  controller/AuthController.java        POST /api/auth/register、POST /api/auth/login
  controller/ApiUserController.java     GET /api/user/me（受保护，靠拦截器注入的 userId）
  controller/UserController.java        Thymeleaf 演示页（另一套逻辑，密码固定 123）
  web/PageController.java               【新】服务器端页面流程：/ /register /login /home /logout
  web/PageAuthInterceptor.java          【新】拦 /home，未登录 302 到 /login?redirect=...
  web/form/RegisterForm.java            【新】注册表单（nickname/password/confirmPassword + 校验注解）
  web/form/LoginForm.java               【新】登录表单（nickname/password）
  service/UserService.java              注册查重 + 登录校验 + BCrypt + 签发令牌
                                        （register / authenticate 两个方法被 REST 与页面流程共用）
  util/JwtUtil.java                     JWT 签发/解析（HS256，密钥来自 jwt.secret）
  interceptor/JwtInterceptor.java       校验 Authorization: Bearer，写 request attribute
  dto/RegisterRequest.java              nickname + password
  dto/LoginRequest.java                 nickname + password
  dto/LoginResponse.java                token / tokenType / expiresIn / user
  dto/UserVO.java                       对外用户信息（永远不含密码）
  entity/User.java                      @TableName("t_user")
  mapper/UserMapper.java                BaseMapper<User>
  config/WebMvcConfig.java              注册两个拦截器：JwtInterceptor(/api/**)、PageAuthInterceptor(/home)
  config/CorsConfig.java                放行 5173 / 4173 / 5174 的 /api/**
  config/MyMetaObjectHandler.java       自动填充 created_at / updated_at
  exception/GlobalExceptionHandler.java 统一返回 {code,message,data}
                                        ⚠️ @RestControllerAdvice(basePackages="…controller")，
                                        只作用于 REST 接口，别让它管到 web 包（页面会二次失败）
  common/Result.java, BizException.java
src/main/resources/
  application.properties                profiles.active=dev + jwt.secret / jwt.expire-minutes
                                        + server.servlet.session.tracking-modes=cookie（去掉 ;jsessionid=）
  application-dev.properties            8081 + MySQL 连接(mybatis-plus SQL 日志已开)
  application-test.properties           8080
  application-prod.properties           8082
  static/index.html                     后端自带的欢迎页（引用了 images/1.svg）；已被 GET / 顶掉，
                                        只能用 /index.html 访问
  static/images/1.svg                   占位演示图（SVG，可直接编辑）
  static/css/site.css                   【新】页面流程的主题样式（深蓝夜空 + 鎏金，和前端配色一致）
  templates/site/fragments.html         【新】公共片段：head / 星空背景 / 页脚
  templates/site/index.html             【新】首页
  templates/site/register.html          【新】注册页（错误回显用 th:errors）
  templates/site/login.html             【新】登录页（flash 提示 + ?logout / ?expired / redirect 横幅）
  templates/site/home.html              【新】用户主页（需登录）
  templates/error.html                  【新】主题错误页（404/500 时浏览器看到的就是它）
  templates/user/login.html             演示登录页，密码固定 123，会显示 ${msg}
  templates/user/main.html              登录成功页（原来缺失，已补）
```

### 前端 `第五人格`
```
src/api/http.js          统一请求封装：拼 API_BASE、自动带 Bearer 令牌、判 body.code、401 自动清登录态
src/api/auth.js          register / login / fetchMe 三个接口函数
src/stores/auth.js       登录态（token + user，内存响应式 + localStorage 双存）
src/views/Login.vue      账号 + 密码，登录成功后跳 /home（支持 ?redirect= 回跳）
src/views/Register.vue   昵称 + 密码 + 确认密码，注册成功后跳 /login?nickname=（预填昵称，不自动登录）
src/views/Home.vue       登录后的用户中心，可校验登录态 / 退出
src/components/          DoorPortal.vue / NightSky.vue / Toast.vue
src/composables/useToast.js
src/router/index.js      / → /home；/home 需登录（meta.requiresAuth），/login|/register 已登录则跳走
dist/                    构建产物（vite build），dist/index.html 需用 http 服务打开，直接双击打不开
```

---

## 6. 已做过的改动记录（2026-10-09）

### 6.1 移除「手机号 + 短信验证码」
- `Register.vue` 去掉手机号/验证码输入与 `1[3-9]\d{9}`、`\d{6}` 校验与 60 秒倒计时；`Login.vue` 由「账号/手机号」改回「账号」；`style.css` 删掉残留 `.code-btn`；**重新 `vite build` 刷新了 dist**。
- 后端同步：`User.java` 删 `phone` 字段与 getter/setter；`sql/init.sql` 建表去掉 `phone` 列和 `uk_phone` 唯一索引（文件末尾留了旧库升级用的注释版 `ALTER TABLE`）。

### 6.2 工程化
- `target/` 里原来的 jar 是**普通 jar（33KB，无 Main-Class）**，`java -jar` 会失败；已重新 `package`，现在是约 30MB 带 `BOOT-INF` 的 Spring Boot 可执行 jar。
- 新增 7 个 GBK 编码的启停脚本（见第 2 节）+ 本文件。
- `springBootTest` 的 `application.properties`：中文注释恢复，并修掉 `logging.pattern.file=logging.pattern.console=...` 的重复前缀。

### 6.3 打通前后端 + 补齐缺失资源
- `第五人格/src/views/Register.vue` 的 `onSubmit` 改为 **真正调用** `POST http://localhost:8081/api/auth/register`（`API_BASE` 可用 `.env` 的 `VITE_API_BASE` 覆盖），并处理：409 昵称重复→挂在昵称下、400 参数错误、后端连不上→提示先启动 8081；提交中按钮禁用。已重新 `vite build`。
- 新增 `templates/user/main.html`（原来 `UserController#login` 返回 `user/main` 会 500）。
- `templates/user/login.html` 补上 `${msg}` 显示（原来密码错误没有任何提示）+ 演示密码说明；顺手修掉重复的 XML 标签。
- 新增 `static/images/1.svg` 占位图（原来 `index.html` 引用的 `1.jpg` 不存在），并把 `index.html` 的 `src` 与重复 `</head>` 一并修正。

### 6.4 实测结论（都跑过）
- 后端 8081：`/`、`/images/1.svg`(200, image/svg+xml)、`/user/to_login`、`/user/login`（错密码显示「密码错误」、对密码渲染 `user/main`）、`/api/auth/register`（200 / 409 / 400）全部正常，数据能落库。测试数据每次跑完都已清空。
- CORS 预检 `OPTIONS /api/auth/register`（Origin: `http://localhost:5173`）返回正确的 `Access-Control-Allow-*`。

## 7. 已知待办 / 不一致（用户可自选是否修）

1. `application.properties` 里 `logging.level.com.fengling.springboot=warn` 少了个 `test`（正确是 `com.fenglin.springboottest`），这条配置实际不生效。
   **故意没改**：`UserController#test()` 是演示各级别日志的接口，改了会看不到 info/debug/trace 输出。
2. `UserController#login` 是硬编码密码 `123` 的教学用演示，和 `/api/auth/login` 那套（BCrypt + t_user + JWT）**是两套互不相干的逻辑**，别混淆。
   **2026-10-12 明确保留未动**（用户选了「原 UserController 教学演示保持不动」）。
3. `GlobalExceptionHandler` 从 2026-10-12 起加了 `basePackages = "com.fenglin.springboottest.controller"`，**只管 REST 接口**。
   所以：`/api/**` 出错仍然返回 `{"code":...}`（HTTP 200，行为没变，前端 `http.js` 照样按 `body.code` 判断）；
   而 `web` 包下的页面出错会走正常的 HTML 错误页，不会再出现「Result 序列化成 text/html 失败」的二次异常。
   ⚠️ 用 curl / Postman 测 `/api/**` 时别只看状态码，会以为「怎么都是 200」。
4. `jwt.secret` 现在写在 `application.properties` 里（默认值仅供本机开发）。真要上线必须换成环境变量注入，并改掉这个仓库里的默认值。
5. 前端没有做令牌自动续期，令牌过期（默认 120 分钟）后第一次请求会收到 401，`http.js` 会自动清掉本地登录态，用户需要重新登录。
6. 前端登录页的「微信 / QQ / 网易」三个按钮仍是纯演示，点了只弹提示。
7. **`UserController#to_index` 指向 `redirect:/index.jsp`，但这个项目没有 JSP，访问 `/user/to_index` 会 404。**
   正确写法应该是 `redirect:/`。**故意没改**（同在「教学演示保持不动」范围内）。要修就一行的事。
8. 页面流程用 Session、REST 接口用 JWT，两套登录态**互不共享**：在后端页面上登录了，前端 SPA 不会自动登录（反之亦然）。
   账号本身是共用的（同一张 `t_user` 表），所以在页面注册的账号能直接拿去前端登录。
9. 页面流程目前没有「记住我 / 忘记密码 / 修改昵称」，退出也只是 `session.invalidate()`。
10. `static/index.html`（后端自带欢迎页）已经访问不到了 —— 根路径被 `PageController#index()` 接管，只能走 `/index.html`。

---

## 8. 登录 / JWT 实现要点（2026-10-09 新增）

### 8.1 后端
- 依赖：`io.jsonwebtoken:jjwt-api / jjwt-impl / jjwt-jackson`，版本 **0.12.6**（0.12 的新 API：`Jwts.builder().subject()…signWith(key, Jwts.SIG.HS256)`、`Jwts.parser().verifyWith(key).build().parseSignedClaims(t)`）。
- `JwtUtil`：构造时校验 `jwt.secret` 不少于 32 字节（HS256 要求），不合格**直接启动失败**；载荷 `sub` = 用户 id、`nickname` = 昵称。
  > 坑：`.signWith(key)` 不指定算法时，jjwt 会**按密钥长度自动挑算法**（57 字节的密钥会被抬成 HS384）。已显式写死 `Jwts.SIG.HS256`，别再把算法参数删掉。
- `JwtInterceptor`：从 `Authorization` 头取令牌，前缀 `Bearer` **大小写不敏感**；OPTIONS 直接放行；校验通过把 `loginUserId` / `loginUserNickname` 塞进 request attribute。
- `WebMvcConfig`：`addPathPatterns("/api/**")` + `excludePathPatterns("/api/auth/register","/api/auth/login")`。**新增公开接口记得加到这里**，否则会被拦。
- 拦截器里 `throw new BizException(401, …)` 能被 `@RestControllerAdvice` 正常捕获（DispatcherServlet 会走 `processHandlerException`），实测返回 `{"code":401,…}`，且 CORS 响应头也在（`CorsInterceptor` 排在拦截器链最前面）。
- 登录失败提示统一为「账号或密码错误」，**不区分账号不存在 / 密码错误**，避免被用来枚举账号。

### 8.2 前端
- `src/api/http.js` 是唯一发请求的地方：`API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8081'`；`ApiError.code = -1` 表示压根连不上后端；收到 `code === 401` 会 `clearAuth()` 再抛错。
- 登录态存 `localStorage` 的 `idv_token` / `idv_user`，`router.beforeEach` 靠 `hasToken()` 判断（只判有没有，不验签，验签在后端）。
- 注册成功**不自动登录**，直接跳 `/login?nickname=昵称`（账号已预填，输入密码即可进入），与后端页面流程行为一致。
- 登录支持「记住我」：勾选 → 令牌存 localStorage 且后端签发 7 天长效 JWT（expiresIn=604800）；不勾 → sessionStorage + 2 小时令牌（7200）。

### 8.3 实测结论（2026-10-09，全部跑过）
| 用例 | 期望 | 结果 |
|---|---|---|
| 正确密码登录 | 返回 token（header `{"alg":"HS256"}`）、`expiresIn=7200` | ✓ |
| 错误密码 / 不存在的账号 | `code=401`「账号或密码错误」 | ✓ |
| 空参数登录 | `code=400`「账号不能为空」 | ✓ |
| 不带令牌访问 `/api/user/me` | `code=401`「未登录，请先登录」 | ✓ |
| 带令牌访问 `/api/user/me` | `code=200` + 用户信息 | ✓ |
| 伪造令牌 / 换密钥签的令牌 | `code=401`「登录状态已失效」 | ✓ |
| 同密钥但已过期的令牌（Python 手工构造） | `code=401` | ✓ |
| `Authorization: bearer xxx`（小写前缀） | 正常放行 | ✓ |
| CORS 预检（Origin 5173 / 4173） | 返回正确的 `Access-Control-Allow-*` | ✓ |
| 401 响应带 Origin 时 | 仍带 `Access-Control-Allow-Origin` | ✓ |

---

## 9. 服务器端页面流程（2026-10-12 新增）

### 9.1 它是干什么的

后端原来只有「REST 接口」和「密码写死 123 的教学演示页」两套。本次补上第三套：
**一套完整的、能真正走通的服务器端多页面流程**，用 Thymeleaf + Session 实现，
数据落真实 `t_user` 表（复用 `UserService` 的 BCrypt 逻辑），重点是**跳转链路完整**。

### 9.2 跳转地图（全部实测通过）

| 操作 | 结果 |
|---|---|
| `GET /` | 200 首页（登录 / 注册入口） |
| `POST /register` 成功 | **302 → `/login`**（并 flash 提示「注册成功」+ 预填昵称） |
| `POST /register` 昵称重复 | 200 原地返回，昵称框下显示「该昵称已被注册」，输入内容保留 |
| `POST /register` 两次密码不一致 | 200 原地返回，确认密码框下显示提示 |
| `POST /register` 弱密码 | 200 原地返回，「密码需 8-20 位且同时包含字母和数字」 |
| `POST /login` 成功 | **302 → `/home`**（或回 `?redirect=` 指定的页面） |
| `POST /login` 密码错 / 账号被禁用 | 200 原地返回，密码框下显示「账号或密码错误」/「该账号已被禁用，请联系管理员」 |
| `GET /home` 未登录 | **302 → `/login?redirect=%2Fhome`**（由 `PageAuthInterceptor` 发出） |
| `GET /home` 已登录 | 200，显示 id / 昵称 / 状态 / 注册时间 |
| `POST /logout` | 销毁 session，**302 → `/login?logout`** |
| `POST /login` 且 `redirect=//evil.com` | 回落 `/home`（`safeRedirect()` 挡开放重定向） |
| 访问不存在的路径（浏览器 Accept） | 404 + `templates/error.html` 主题错误页 |

### 9.3 实现要点 / 踩过的坑

1. **`#fields` 系列表达式必须写在 `<form th:object="...">` 内部**。
   写在表单外面会抛 `Could not bind form errors using expression "global"` → 整页 500。
   （`th:errors="*{xxx}"` 同理，但它是字段级、放在 `.field` 里天然就在表单内。）
2. **`@RestControllerAdvice` 会连页面 Controller 一起管**。原来的 `GlobalExceptionHandler` 是全局无差别的，
   页面抛异常时它会试图把 `Result` 序列化成 `text/html`，二次失败、日志里刷
   `No converter for [class ...Result] with preset Content-Type 'text/html;charset=UTF-8'`。
   → 已加 `basePackages = "com.fenglin.springboottest.controller"` 限定范围。
3. **`GET /` 的优先级**：`RequestMappingHandlerMapping`（order 0）排在
   `WelcomePageHandlerMapping`（order 2）前面，所以 Controller 里的 `@GetMapping("/")`
   会**顶掉** `static/index.html` 欢迎页。欢迎页没删，只是要显式走 `/index.html` 才能看到。
4. **`;jsessionid=` 问题**：注册/登录会创建 session，如果客户端没带 Cookie，
   Tomcat 默认会做 URL 重写，重定向地址变成 `/login;jsessionid=XXXX`。
   → 已在 `application.properties` 加 `server.servlet.session.tracking-modes=cookie` 关掉。
5. **登录成功要换 session**（防会话固定攻击）：先 `old.invalidate()` 再 `request.getSession(true)`。
6. **别在 `session.invalidate()` 之后用 `RedirectAttributes` 的 flash 属性**（依赖 session 存）。
   所以注册后用 flash 传昵称，而退出用查询串 `?logout` + 模板里 `th:if="${param.containsKey('logout')}"`
   （注意：`param.xxx` 是 `String[]`，用 `containsKey` 判断比直接取值更稳）。
7. `th:errors` 在**没有 BindingResult 的 GET 请求**下不会报错（实测 `/login`、`/register` 首次打开都是 200），
   只要确保它在 `th:object` 表单里即可。
8. 表单对象 `web/form/*Form.java` 与 REST 的 `dto/*Request.java` **故意分开**：
   页面需要「确认密码」这个跨字段校验，注解表达不了，在 `PageController#doRegister` 里手动
   `binding.rejectValue("confirmPassword", ...)` 挂到字段上（比 `@AssertTrue` 的全局错误更好展示）。

### 9.4 前后端两条链路的关系

```
前端 Vue (5173)  ──fetch + JWT ──▶  /api/auth/**  /api/user/me     ← 无状态
后端 Thymeleaf   ──form + Session──▶  /register /login /home       ← 有状态
                      └────── 共用 UserService + t_user + BCrypt ──────┘
```
登录态不互通，**账号互通**。实测：在页面上注册的账号，用 `POST /api/auth/login` 能直接拿到 token。

