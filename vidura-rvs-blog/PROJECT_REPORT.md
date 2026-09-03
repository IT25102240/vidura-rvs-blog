# ViduraRvs Blogging Web Application

## Complete project, deployment, and analysis report

**Project:** ViduraRvs personal blogging platform  
**Technology:** Java 21 bytecode (JDK 24 runtime), Spring Boot 3.2.5, MySQL 8, Thymeleaf, Spring Security, Quill.js, Maven  
**Application package:** `com.vidurarvs.blog`  
**Last updated:** 3 September 2026 (category management added)  
**Purpose:** BSE IT/SO module project demonstrating Java OOP, layered architecture, and SOLID principles

> **Security note:** This report contains no real passwords, private SSH keys, or credentials.
> Replace every `<...>` value on your own machine. Never commit a real `application.properties`
> or `.env` file containing production secrets.

---

## 1. Executive summary

ViduraRvs is a personal blog website where anonymous visitors can read and search articles,
while the owner and invited administrators publish and manage content through a modern
admin panel featuring rich-text editing, image galleries, auto-save drafting, and profile pages.

### Current status at a glance

| Area | Status |
|---|---|
| Spring Boot source project | Complete + upgraded |
| Quill.js WYSIWYG rich-text editor | NEW — Complete |
| Auto-save drafts (localStorage + server-side) | NEW — Complete |
| Up to 5 images per post (gallery + lightbox) | NEW — Complete |
| YouTube smart parser (any URL/embed format) | NEW — Complete |
| One-click Hide/Show toggle | NEW — Complete |
| Admin profile pages (bio + photo upload) | NEW — Complete |
| Public author profile `/author/{username}` | NEW — Complete |
| Social share buttons (X, WhatsApp, link copy) | NEW — Complete |
| Reading progress bar | NEW — Complete |
| Java 24 JDK + Java 21 bytecode fix | Fixed |
| Lombok 1.18.38 (Java 24 compatible) | Fixed |
| Maven clean compile | BUILD SUCCESS |
| Oracle Cloud VM (Ubuntu 24.04 aarch64) | Created and running |
| Oracle Security List (ports 80, 443, 22) | Open |
| Ubuntu firewall UFW | Configured |
| Nginx on Ubuntu | Installed and connected |
| MySQL database + dedicated user on VM | Created |
| GitHub repository (main branch) | Code pushed |
| **DB migration script on VM** | **NEXT REQUIRED STEP** |
| Spring Boot deployed on VM | Pending |
| DuckDNS subdomain | Pending |
| HTTPS with Let's Encrypt | Pending |

---

## 2. Project objective

ViduraRvs is a blogging platform for university students and people interested in technology,
science, and programming.

### Visitor (no login)

- Browse the home page — latest published posts in a card grid.
- Filter posts by category using the navigation strip.
- Search posts by keyword.
- Read full articles with image gallery (click to open lightbox), embedded YouTube video,
  and social share buttons.
- View author public profiles at `/author/{username}`.
- See the About page.

### Admin (invited by owner)

- Log in at `/login`.
- View personal dashboard with post count and total view stats.
- Create and edit posts using the Quill WYSIWYG editor (headings H1–H3, bold, italic,
  underline, lists, blockquote, code block, links).
- Upload up to 5 images per post; first is the cover, rest form a gallery.
- Attach YouTube video by pasting any format: full URL, short link, embed iframe, or bare ID.
- Draft auto-saves every 4 seconds to localStorage; restored via prompt on returning to form.
- Edit and delete only their own posts.
- Toggle post visibility (Published / Draft) without opening the edit form.
- Edit their public profile (bio and profile photo).

### Owner / Super Admin

All admin permissions plus:

- Edit or delete any post by any author.
- Invite new administrators.
- Activate or deactivate invited administrators.
- View site-wide statistics and all posts on dashboard.

---

## 3. Technology decisions

| Layer | Technology | Reason |
|---|---|---|
| Language | Java 21 bytecode / JDK 24 runtime | Spring Boot 3.2.5 ASM supports up to Java 21 class files; JDK 24 used locally |
| Framework | Spring Boot 3.2.5 | MVC, embedded Tomcat, production JAR packaging |
| Rich text editor | Quill.js 2 via CDN | WYSIWYG: headings, bold, italic, lists, code blocks, links |
| Styling | Plain CSS — custom dark navy theme | Inter + Playfair Display fonts; glassmorphism; no framework lock-in |
| Persistence | Spring Data JPA + Hibernate | Entity mapping, join-fetch queries |
| Database | MySQL 8 utf8mb4 | Relational storage with proper charset |
| Build | Maven 3.8.5 + maven-compiler-plugin 3.13.0 | Clean compile with explicit Lombok annotation processing |
| Lombok | 1.18.38 | First version with full Java 24 JDK annotation-processor support |
| Caching | Spring Cache (in-memory ConcurrentMap) | Nav-category query cached to reduce DB calls |
| Hosting | Oracle Cloud Always Free Arm A1 | No monthly cost within Always Free limits |
| OS on VM | Ubuntu 24.04 Minimal aarch64 | Lightweight ARM-compatible |
| Reverse proxy | Nginx | Public 80/443 → Spring Boot 127.0.0.1:8080 |
| DNS | DuckDNS free subdomain | Free subdomain without a paid domain |
| HTTPS | Let's Encrypt via Certbot | Free TLS certificate, auto-renewed every 90 days |

---

## 4. Application architecture

```
Browser / Mobile
   |
   v
DuckDNS hostname → Oracle public IP
   |
   v
Nginx :80 / :443  (reverse proxy, SSL termination)
   |
   v
Spring Boot Tomcat :8080 on 127.0.0.1  (never public-facing)
   |
   +──→ MySQL :3306 on localhost  (never public-facing)
   |
   +──→ /var/lib/vidurarvs/uploads/  (image files)
```

### Layer responsibilities

| Layer | Responsibility |
|---|---|
| Controller | Translates HTTP requests, validates form input, selects view template |
| Service | Business rules: ownership checks, YouTube parsing, image upload, slug gen, auto-save |
| Repository | Spring Data JPA — join-fetch queries prevent N+1 and LazyInitializationException |
| Model/Entity | User, Post, PostImage, Category, Role |
| MySQL | Persistent storage |

Controllers never access repositories directly.
Constructor injection is used throughout — no `new` for dependencies.

---

## 5. Source structure (updated September 2026)

```
src/main/java/com/vidurarvs/blog/
  BlogApplication.java
  config/
    CacheConfig.java              ← NEW: @EnableCaching
    DataInitializer.java          ← Updated: seeds bio + profile photo path
    WebMvcConfig.java             ← Updated: /uploads/** and /static-img/** handlers
  controller/
    AdminDashboardController.java
    AdminPostController.java      ← Updated: toggle-visibility, /autosave endpoint
    AdminUserController.java
    AuthController.java
    GlobalModelAttributes.java   ← Updated: @Cacheable("navCategories")
    HomeController.java
    ProfileController.java        ← NEW: /author/{username}, /admin/profile
  dto/
    AdminAccountFormDTO.java
    PostFormDTO.java              ← Updated: newImages, removeImageIds, youtubeInput
  exception/
    DuplicateResourceException.java
    ForbiddenActionException.java
    GlobalExceptionHandler.java
    ResourceNotFoundException.java
  model/
    Category.java
    Post.java                     ← Updated: images list, youtubeWatchUrl()
    PostImage.java                ← NEW: up to 5 images per post
    Role.java
    User.java                     ← Updated: profilePicturePath, bio
  repository/
    CategoryRepository.java
    PostImageRepository.java      ← NEW
    PostRepository.java
    UserRepository.java
  security/
    CustomUserDetailsService.java
    CustomUserPrincipal.java
    SecurityConfig.java           ← Updated: /author/**, /img/**, /js/** public
  service/
    CategoryService.java
    PostService.java              ← Updated: toggleVisibility(), stats methods
    UserService.java              ← Updated: updateProfile()
    impl/
      CategoryServiceImpl.java
      PostServiceImpl.java        ← Updated: YouTube parser, multi-image, toggle
      UserServiceImpl.java        ← Updated: profile photo upload + ownership checks

src/main/resources/
  application.properties         ← Tuned for Oracle Free Tier
  db-migration.sql               ← NEW: ALTER TABLE users + CREATE post_images
  static/
    css/style.css                 ← Full rewrite: dark navy, glassmorphism
    js/editor.js                  ← NEW: Quill editor + localStorage auto-save
    js/main.js                    ← NEW: lightbox, progress bar, share buttons
    img/vidura-profile.jpg        ← NEW: default profile avatar
    uploads/.gitkeep
  templates/
    about.html
    access-denied.html
    author-profile.html           ← NEW: public author profile
    error.html
    index.html                    ← Updated: hero banner, post grid
    login.html
    post-detail.html              ← Updated: gallery, YouTube embed, share, author card
    fragments/layout.html         ← Updated: Google Fonts, lightbox, progress bar
    admin/
      admin-form.html
      admin-list.html             ← Updated: profile photo thumbnails
      dashboard.html              ← Updated: stats cards, welcome photo
      post-form.html              ← Updated: Quill editor, 5-image upload slots
      post-list.html              ← Updated: status badges, Hide/Show toggle
      profile.html                ← NEW: bio + photo upload form
      fragments/admin-nav.html   ← Updated: My Profile link added
```

---

## 6. Data model (updated)

### User

| Field | Notes |
|---|---|
| id, fullName, username, email | Core identity |
| password | BCrypt hash — never stored or logged as plaintext |
| role | SUPER_ADMIN or ADMIN |
| active | Deactivated admins cannot log in |
| **profilePicturePath** | NEW — relative path under /uploads/ |
| **bio** | NEW — author bio shown on public profile page |
| createdAt | Set on first persist |

### Post

| Field | Notes |
|---|---|
| id, title, slug, summary | Core fields |
| content | Quill-produced HTML (LONGTEXT) |
| coverImagePath | Legacy single-image field (backward compatible) |
| **images** | NEW — List of PostImage (up to 5, sortOrder 0 = cover) |
| youtubeVideoId | 11-char parsed ID |
| tags | Comma-separated free-text |
| category, author | ManyToOne associations |
| viewCount | Incremented on each public page view |
| published | false = draft (hidden from visitors) |
| createdAt, updatedAt | Timestamps |

### PostImage (NEW)

| Field | Notes |
|---|---|
| id | PK |
| post | ManyToOne → Post (cascade delete) |
| imagePath | UUID-based filename under /uploads/ |
| sortOrder | 0 = cover; higher = gallery |
| caption | Optional alt text |

### Category

Seeded automatically on first run:
Technology, Science, Programming, Information & Communication Technology,
Gaming, Travel, Food, Education, Philosophy, Society & Opinion.

---

## 7. Main application URLs (updated)

### Public

| URL | Purpose |
|---|---|
| `/` | Home — latest published posts |
| `/category/{slug}` | Posts in a category |
| `/search?q=keyword` | Keyword search |
| `/post/{slug}` | Full article (gallery, video, share buttons) |
| `/author/{username}` | **NEW** — Public author profile + post grid |
| `/about` | About page |
| `/login` | Admin login form |

### Admin

| URL | Purpose | Required role |
|---|---|---|
| `/admin/dashboard` | Dashboard with stats + quick actions | ADMIN / SUPER_ADMIN |
| `/admin/posts` | Post list with Hide/Show toggle | ADMIN / SUPER_ADMIN |
| `/admin/posts/new` | Create post (Quill editor) | ADMIN / SUPER_ADMIN |
| `/admin/posts/{id}/edit` | Edit post | Author or SUPER_ADMIN |
| `/admin/posts/{id}/toggle-visibility` | **NEW** Quick publish/draft | Author or SUPER_ADMIN |
| `/admin/posts/{id}/autosave` | **NEW** AJAX auto-save endpoint | Author or SUPER_ADMIN |
| `/admin/posts/{id}/delete` | Delete post | Author or SUPER_ADMIN |
| `/admin/profile` | **NEW** Edit bio + profile photo | ADMIN / SUPER_ADMIN |
| `/admin/admins` | Admin account list | SUPER_ADMIN |
| `/admin/admins/new` | Invite a new admin | SUPER_ADMIN |
| `/admin/admins/{id}/activate` | Restore admin access | SUPER_ADMIN |
| `/admin/admins/{id}/deactivate` | Revoke admin access | SUPER_ADMIN |

---

## 8. Important technical fixes made during this upgrade

### Fix 1 — Java 24 JDK / Spring Boot 3.2.5 class file incompatibility

**Error:** `Unsupported class file major version 68` at startup.

**Root cause:** Spring Boot 3.2.5 bundles ASM 9.x (inside spring-core-6.1.6.jar)
which only reads class files up to version 65 (Java 21). Compiling to Java 24
produces class file version 68.

**Fix:** Set `<java.version>21</java.version>` in pom.xml.
- JDK 24 runs the application — backward compatible.
- Bytecode is Java 21 — Spring's ASM reads it correctly.

### Fix 2 — Lombok 1.18.38 for Java 24 annotation processing

**Error:** `java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN`

**Root cause:** Lombok 1.18.32 (Spring Boot 3.2.5 default) uses internal JDK APIs
removed in Java 24. The annotation processor crashed before generating any getters/setters.

**Fix:**
- `<lombok.version>1.18.38</lombok.version>` in pom.xml properties
- Explicit `<version>1.18.38</version>` on the Lombok dependency
- Added `annotationProcessorPaths` to maven-compiler-plugin 3.13.0

### Fix 3 — PostFormDTO explicit getters/setters

**Problem:** Lombok `@Getter/@Setter` on DTOs had annotation-processing ordering
issues during a clean Maven compile, causing many "cannot find symbol" errors.

**Fix:** Rewrote `PostFormDTO` with explicit Java getters/setters (no Lombok on DTOs).

### Fix 4 — PostImage explicit getters/setters

Same issue as PostFormDTO. Rewritten with explicit accessors.

### Fix 5 — CSRF with Thymeleaf forms

All admin forms use `th:action`. Thymeleaf auto-injects the CSRF token.
`CsrfTokenRequestAttributeHandler` is configured for standard form-post compatibility.

### Fix 6 — LazyInitializationException prevention

`spring.jpa.open-in-view=false` is intentional. Repository JPQL queries use
`join fetch` for category and author. `PostImage` uses `FetchType.EAGER` so
gallery images are always available when Thymeleaf renders the post detail.

---

## 9. Current application.properties (production-safe template)

```properties
spring.application.name=ViduraRvs
server.port=${PORT:8080}

# HTTP compression — saves bandwidth on Oracle Free Tier
server.compression.enabled=true
server.compression.mime-types=text/html,text/xml,text/plain,text/css,application/javascript,application/json
server.compression.min-response-size=1024

# Tomcat — tuned for Arm A1 (6 GB RAM, 4 cores)
server.tomcat.threads.max=50
server.tomcat.threads.min-spare=5
server.tomcat.accept-count=50

# MySQL — dedicated user (NOT root)
spring.datasource.url=jdbc:mysql://localhost:3306/vidurarvs_blog?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4
spring.datasource.username=vidura_blog
spring.datasource.password=<your-db-password>
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# HikariCP — small pool to avoid OOM on free-tier VM
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.open-in-view=false

# Thymeleaf
spring.thymeleaf.cache=false

# Uploads — up to 5 images per post
app.upload.dir=uploads
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=25MB

# Bootstrap super-admin (created only if users table is empty)
app.bootstrap.super-admin.full-name=<your full name>
app.bootstrap.super-admin.username=<your username>
app.bootstrap.super-admin.email=<your email>
app.bootstrap.super-admin.password=<your password>

app.pagination.page-size=8
```

Production overrides come from `/etc/vidurarvs/vidurarvs.env` (never committed to Git).

---

## 10. OOP and SOLID analysis

### Encapsulation

Entity fields are private with Lombok-generated or explicit accessors.
Domain behaviour (view count increment, YouTube URL building, image path resolution,
`isSuperAdmin()`, `hasProfilePicture()`) lives in the entity/service boundary — not
scattered through templates.

### Abstraction

`PostService`, `UserService`, `CategoryService` are interfaces. Controllers depend
on interfaces, not concrete implementations. The `@Cacheable` decorator can be applied
to a service without changing any controller.

### Inheritance and framework contracts

`CustomUserPrincipal` implements Spring Security's `UserDetails` contract. This allows
the application's user to work wherever Spring Security expects a `UserDetails`.

### Polymorphism

Controllers depend on service interfaces. A different implementation (e.g. a cached
or file-backed version) could be substituted without rewriting any controller.

### SOLID

| Principle | Implementation |
|---|---|
| **S** Single Responsibility | Controllers → HTTP; Services → business rules; Repositories → DB; DTOs → form binding; Entities → persistence |
| **O** Open/Closed | Service interfaces allow new implementations without modifying consumers |
| **L** Liskov Substitution | `CustomUserPrincipal` substitutes for any `UserDetails` |
| **I** Interface Segregation | Separate `PostService`, `UserService`, `CategoryService` instead of one large interface |
| **D** Dependency Inversion | Constructor injection throughout; no `new` for infrastructure or service classes |

---

## 11. Deployment guide — Oracle VM with DuckDNS subdomain

### What you have already completed

- Oracle Cloud VM created (`vidurarvs-server`, Ubuntu 24.04 aarch64 Arm A1)
- Oracle Security List: ports 80, 443, 22 open
- SSH access working from Windows
- UFW configured: OpenSSH, HTTP, HTTPS allowed
- Nginx installed and connected on Ubuntu
- MySQL database and user already created:

```sql
CREATE DATABASE vidurarvs_blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'vidura_blog'@'localhost' IDENTIFIED BY '<your-password>';
GRANT ALL PRIVILEGES ON vidurarvs_blog.* TO 'vidura_blog'@'localhost';
FLUSH PRIVILEGES;
```

- Code committed and pushed to GitHub (main branch — already done via Antigravity IDE)

---

### STEP 1 — Run the DB migration (required before first run)

The latest upgrade adds `profile_picture_path` and `bio` columns to `users`,
and creates a new `post_images` table. Run this **once** on your Oracle VM.

SSH into the server:
```bash
ssh -i <your-private-key-path> ubuntu@<your-oracle-public-ip>
```

Open MySQL:
```bash
sudo mysql -u vidura_blog -p vidurarvs_blog
```

Paste and run:
```sql
-- Add profile fields to users
ALTER TABLE users
  ADD COLUMN IF NOT EXISTS profile_picture_path VARCHAR(300) NULL,
  ADD COLUMN IF NOT EXISTS bio TEXT NULL;

-- Create post_images table for multi-image support (up to 5 per post)
CREATE TABLE IF NOT EXISTS post_images (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    post_id      BIGINT       NOT NULL,
    image_path   VARCHAR(300) NOT NULL,
    sort_order   INT          NOT NULL DEFAULT 0,
    caption      VARCHAR(200) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_post_images_post
        FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Performance indexes
CREATE INDEX IF NOT EXISTS idx_posts_published_created
    ON posts (published, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_posts_author
    ON posts (author_id);

EXIT;
```

---

### STEP 2 — Create the application Linux user and directories

```bash
# Dedicated non-root service user
sudo adduser --system --group --home /opt/vidurarvs vidurarvs

# Application directory
sudo mkdir -p /opt/vidurarvs

# Persistent upload directory (images survive redeployments)
sudo mkdir -p /var/lib/vidurarvs/uploads
sudo chown -R vidurarvs:vidurarvs /var/lib/vidurarvs
```

---

### STEP 3 — Install Java 21 on the VM

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk
java -version
```

Expected output:
```
openjdk version "21.x.x" ...
```

> **Why Java 21?** The project compiles to Java 21 bytecode (class file version 65).
> Spring Boot 3.2.5's bundled ASM 9.x supports up to version 65.
> Java 21 is the current LTS version and is in the Ubuntu 24.04 repositories.

---

### STEP 4 — Clone the project from GitHub

```bash
cd /opt/vidurarvs
sudo git clone https://github.com/IT25102240/vidura-rvs-blog.git app
sudo chown -R vidurarvs:vidurarvs /opt/vidurarvs/app
```

> Your repository is public, so no SSH key or token is needed for cloning.

---

### STEP 5 — Install Maven

```bash
sudo apt install -y maven
mvn -version
```

---

### STEP 6 — Create the production environment file

This file overrides `application.properties` with production values.
It is stored on the server only — never committed to Git.

```bash
sudo mkdir -p /etc/vidurarvs
sudo nano /etc/vidurarvs/vidurarvs.env
```

Paste the following, replacing every `<...>` with your real values:

```
# Database — matches the MySQL user you already created
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/vidurarvs_blog?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4
SPRING_DATASOURCE_USERNAME=vidura_blog
SPRING_DATASOURCE_PASSWORD=<your-db-password-you-chose>

# JPA
SPRING_JPA_HIBERNATE_DDL_AUTO=update

# Server — bind to localhost only; Nginx handles public access
SERVER_PORT=8080
SERVER_ADDRESS=127.0.0.1

# Thymeleaf — enable cache in production for speed
SPRING_THYMELEAF_CACHE=true

# Uploads — absolute path on the VM
APP_UPLOAD_DIR=/var/lib/vidurarvs/uploads

# Super-admin (only created on first run if users table is empty)
APP_BOOTSTRAP_SUPER_ADMIN_FULL_NAME=Vidura Rammandalagedara
APP_BOOTSTRAP_SUPER_ADMIN_USERNAME=vidura
APP_BOOTSTRAP_SUPER_ADMIN_EMAIL=vidurarvs@gmail.com
APP_BOOTSTRAP_SUPER_ADMIN_PASSWORD=<choose-a-strong-admin-password>

# Pagination
APP_PAGINATION_PAGE_SIZE=8
```

Protect the file so only root can read it:

```bash
sudo chmod 600 /etc/vidurarvs/vidurarvs.env
sudo chown root:root /etc/vidurarvs/vidurarvs.env
```

---

### STEP 7 — Build the JAR on the VM

```bash
cd /opt/vidurarvs/app
sudo -u vidurarvs mvn clean package -DskipTests
```

First run takes 3–5 minutes to download dependencies. Confirm success:

```bash
ls -lh /opt/vidurarvs/app/target/vidura-rvs-blog.jar
```

---

### STEP 8 — Test the app manually (optional but recommended)

```bash
sudo -u vidurarvs env $(sudo cat /etc/vidurarvs/vidurarvs.env | grep -v "^#" | xargs) \
  java -jar /opt/vidurarvs/app/target/vidura-rvs-blog.jar
```

Look for:
```
Started BlogApplication in X.XXX seconds
Tomcat started on port(s): 8080
```

Test from inside the VM:
```bash
curl http://127.0.0.1:8080
```

Stop with `Ctrl+C`, then set up the service.

---

### STEP 9 — Create the systemd service

```bash
sudo nano /etc/systemd/system/vidurarvs.service
```

Paste:
```ini
[Unit]
Description=ViduraRvs Spring Boot Blog
After=network.target mysql.service
Requires=mysql.service

[Service]
User=vidurarvs
Group=vidurarvs
WorkingDirectory=/opt/vidurarvs/app
EnvironmentFile=/etc/vidurarvs/vidurarvs.env
ExecStart=/usr/bin/java \
  -Xms128m \
  -Xmx512m \
  -jar /opt/vidurarvs/app/target/vidura-rvs-blog.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

Enable, start, and verify:

```bash
sudo systemctl daemon-reload
sudo systemctl enable vidurarvs
sudo systemctl start vidurarvs
sudo systemctl status vidurarvs
```

Watch live logs:
```bash
sudo journalctl -u vidurarvs -f
```

You want to see:
```
Started BlogApplication
Tomcat started on port(s): 8080 (http) with context path ''
```

Test:
```bash
curl http://127.0.0.1:8080
```

---

### STEP 10 — Set up the DuckDNS subdomain

1. Go to [https://www.duckdns.org](https://www.duckdns.org)
2. Sign in with Google, GitHub, or Reddit.
3. Create a subdomain — for example: `vidurarvs`
   → You get: `vidurarvs.duckdns.org`
4. In the **current ip** box, enter your Oracle VM public IP.
   (Always check the current IP in Oracle Cloud Console — it may change on reboot.)
5. Click **update ip**.
6. Verify from Windows:
   ```
   nslookup vidurarvs.duckdns.org
   ```
   The returned IP must match your Oracle public IP.

**Keep the IP updated automatically** (run this on the VM):

```bash
mkdir -p ~/duckdns
nano ~/duckdns/duck.sh
```

Paste (replace TOKEN and subdomain with yours from the DuckDNS dashboard):
```bash
#!/bin/bash
echo url="https://www.duckdns.org/update?domains=vidurarvs&token=<YOUR_DUCKDNS_TOKEN>&ip=" \
  | curl -k -o ~/duckdns/duck.log -K -
```

Make executable and schedule:
```bash
chmod 700 ~/duckdns/duck.sh
crontab -e
```

Add this line (runs every 5 minutes):
```
*/5 * * * * ~/duckdns/duck.sh >/dev/null 2>&1
```

---

### STEP 11 — Configure Nginx

Your Nginx is already installed. Create the site configuration:

```bash
sudo nano /etc/nginx/sites-available/vidurarvs
```

Paste (replace `vidurarvs.duckdns.org` with your actual subdomain):

```nginx
server {
    listen 80;
    listen [::]:80;
    server_name vidurarvs.duckdns.org;

    # Allow large multi-image uploads (5 images x 5 MB)
    client_max_body_size 30M;

    location / {
        proxy_pass         http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header   Host              $host;
        proxy_set_header   X-Real-IP         $remote_addr;
        proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto $scheme;
        proxy_read_timeout 60s;
        proxy_send_timeout 60s;
    }
}
```

Enable the site and remove the default:

```bash
sudo ln -s /etc/nginx/sites-available/vidurarvs /etc/nginx/sites-enabled/vidurarvs
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl reload nginx
```

Test in your browser:
```
http://vidurarvs.duckdns.org
```

---

### STEP 12 — Enable HTTPS with Let's Encrypt

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d vidurarvs.duckdns.org
```

When prompted:
- Enter your email address (for renewal notices).
- Choose **option 2 — Redirect HTTP to HTTPS**.

Test HTTPS:
```
https://vidurarvs.duckdns.org
https://vidurarvs.duckdns.org/login
https://vidurarvs.duckdns.org/about
```

Test automatic certificate renewal:
```bash
sudo certbot renew --dry-run
```

---

### STEP 13 — Full post-deployment test checklist

| Test | URL / Action | Expected result |
|---|---|---|
| Home page | `https://vidurarvs.duckdns.org` | Post card grid loads |
| Category filter | `/category/technology` | Filtered posts |
| Keyword search | `/search?q=java` | Search results |
| Admin login | `/login` | Login form, no CSRF error |
| Dashboard | `/admin/dashboard` | Stats cards, welcome photo |
| New post with Quill | Bold text, H2 heading, bullet list | Formatted correctly on detail page |
| Image upload | Upload 3 images in post form | Gallery + lightbox on post detail |
| YouTube URL | Paste `https://youtube.com/watch?v=abc` | Embedded video + Watch on YouTube link |
| Auto-save | Write in editor, close tab, reopen `/admin/posts/new` | "Restore draft?" prompt |
| Hide/Show toggle | Click Hide in post list | Post disappears from home page |
| My Profile | `/admin/profile` | Upload photo + set bio |
| Author page | `/author/vidura` | Profile photo, bio, post grid |
| HTTPS lock | Any page | Browser padlock icon shown |

---

### STEP 14 — Deploy code updates

When you push new changes from your Windows PC:

```bash
# On Windows (in terminal or Antigravity IDE)
git add .
git commit -m "Update blog"
git push
```

On the Oracle VM:

```bash
cd /opt/vidurarvs/app
sudo git pull
sudo -u vidurarvs mvn clean package -DskipTests
sudo systemctl restart vidurarvs
sudo journalctl -u vidurarvs -f
```

---

### STEP 15 — Regular backups

Run these on the VM periodically (or schedule them with cron):

```bash
# Database backup
sudo mysqldump -u vidura_blog -p vidurarvs_blog > ~/vidurarvs_backup_$(date +%Y%m%d).sql

# Upload images backup
sudo tar -czf ~/vidurarvs_uploads_$(date +%Y%m%d).tar.gz /var/lib/vidurarvs/uploads
```

Download backup files to Windows using WinSCP or:
```bash
scp -i <key> ubuntu@<ip>:~/vidurarvs_backup_*.sql .
```

---

### STEP 16 — After first login on production

1. Go to `https://vidurarvs.duckdns.org/login`
2. Log in with the credentials you set in `vidurarvs.env`
3. Go to **My Profile** → upload your real profile photo
4. Go to **Write a post** → create your first published article
5. Test the Quill editor — try headings, bold text, and a bullet list
6. Upload an image and paste a YouTube URL to verify both features

---

## 12. Security boundary

| Port | Accessible from | Purpose |
|---|---|---|
| 22 | Your IP only (restrict in Oracle Security List) | SSH |
| 80 | Public internet | HTTP → redirected to HTTPS by Certbot |
| 443 | Public internet | HTTPS (Nginx + Let's Encrypt) |
| 8080 | 127.0.0.1 only — never public | Spring Boot Tomcat |
| 3306 | 127.0.0.1 only — never public | MySQL |

---

## 13. Limitations and recommended improvements

| # | Limitation | Recommended action |
|---|---|---|
| 1 | `ddl-auto=update` in production | Introduce Flyway migrations, switch to `validate` |
| 2 | Quill HTML trusted (admin-authored only) | Add OWASP Java HTML Sanitizer before opening to more authors |
| 3 | No automated test suite | Add JUnit 5 + Mockito service unit tests |
| 4 | No email invitation workflow | Admin accounts created directly by owner |
| 5 | View counts not deduplicated | Add session/cookie deduplication for accurate analytics |
| 6 | No image resizing on upload | Add Thumbnailator library to resize on save |
| 7 | No monitoring/alerting | Add Spring Actuator + uptime monitoring (UptimeRobot is free) |
| 8 | DuckDNS only — no custom domain | Purchase a `.lk` or `.com` domain later |
| 9 | No password change screen | Add as a future admin feature |
| 10 | No backup automation | Schedule mysqldump + uploads backup via cron |

---

## 14. Final conclusion

ViduraRvs is a complete, production-ready personal blogging platform with:

- **Rich admin UX** — Quill WYSIWYG editor, multi-image gallery, auto-save, profile pages
- **Clean public experience** — dark theme, card grid, lightbox, social sharing, author profiles
- **Proper security** — BCrypt, CSRF protection, role-based access, ownership enforcement
- **Oracle Free Tier optimised** — HTTP compression, HikariCP tuning, Spring Cache
- **Java 24 JDK / Java 21 bytecode** — clean BUILD SUCCESS, runs correctly

### Deployment progress at time of this report

```
Done  — Code complete, upgraded, and pushed to GitHub
Done  — Oracle VM created, SSH working, ports open
Done  — UFW configured, Nginx installed, MySQL DB + user created
Next  — Run db-migration.sql on VM (Step 1 above)
Next  — Clone code, install Java 21 + Maven, build JAR
Next  — Create systemd service, configure DuckDNS
Next  — Configure Nginx site, issue HTTPS certificate via Certbot
Goal  — https://vidurarvs.duckdns.org live and fully tested
```