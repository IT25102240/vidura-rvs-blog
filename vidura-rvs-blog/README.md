# ViduraRvs

A personal blog platform built with **Spring Boot + MySQL** for publishing long-form articles
on any topic — and inviting trusted people as admins to publish alongside you.

**Live at:** `https://vidurarvs.duckdns.org` *(after deployment)*

---

## What you get

| Feature | Details |
|---|---|
| 🌐 **Public site** | Browse, filter by category, search by keyword |
| ✍️ **Quill rich-text editor** | Bold, headings H1-H3, italic, lists, code blocks, links |
| 🖼️ **Up to 5 images per post** | Gallery + click-to-open lightbox |
| 📺 **YouTube smart embed** | Paste any YouTube URL, short link, embed code, or bare ID |
| 💾 **Auto-save drafts** | Saves every 4 s to localStorage; restored on return |
| 👁️ **Hide/Show toggle** | Publish or draft posts without opening the edit form |
| 🏷️ **Category management** | Owner can add/delete categories from the admin panel |
| 👤 **Author profiles** | Public page at `/author/{username}` with bio + photo |
| 📊 **Dashboard** | Personal stats; owner sees site-wide totals |
| 🔒 **Role-based security** | SUPER_ADMIN owns everything; ADMIN manages own posts |

---

## Tech stack

| Layer | Choice |
|---|---|
| Backend | Java 21 (bytecode) / JDK 24 runtime, Spring Boot 3.2.5 |
| Frontend | Thymeleaf server-rendered HTML, custom dark CSS, Quill.js |
| Database | MySQL 8 (utf8mb4) |
| Build | Maven 3.8.5, maven-compiler-plugin 3.13.0 |
| Lombok | 1.18.38 (Java 24 compatible) |
| Hosting | Oracle Cloud Always Free Arm A1, Nginx, Let's Encrypt |

See [`ARCHITECTURE.md`](ARCHITECTURE.md) for the full layered architecture and SOLID analysis.

---

## 1. Prerequisites (local dev)

- **JDK 21+** (Java 24 also works — bytecode targets Java 21)
- **Maven** (or use IntelliJ's bundled Maven)
- **MySQL 8** running locally
- **IntelliJ IDEA** (Community edition is enough)
- **Git**

---

## 2. Configure the database

Create the database and a dedicated user:

```sql
CREATE DATABASE vidurarvs_blog
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER 'vidura_blog'@'localhost' IDENTIFIED BY '<strong-password>';
GRANT ALL PRIVILEGES ON vidurarvs_blog.* TO 'vidura_blog'@'localhost';
FLUSH PRIVILEGES;
```

Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.username=vidura_blog
spring.datasource.password=<strong-password>

app.bootstrap.super-admin.username=your-username
app.bootstrap.super-admin.password=SomethingStrong123!
```

The super-admin account is created automatically on **first start only** (if the `users` table
is empty). Change the password from the admin panel afterwards.

---

## 3. Run the DB migration (if upgrading from an older version)

If you already have an existing `vidurarvs_blog` database, run this once:

```bash
# From MySQL:
source src/main/resources/db-migration.sql
```

This adds `profile_picture_path`, `bio` to the `users` table and creates the `post_images`
table. It is safe to run multiple times (uses `IF NOT EXISTS`).

---

## 4. Open and run in IntelliJ

1. `File → Open` → select the `vidura-rvs-blog` folder. IntelliJ imports it as a Maven project.
2. Open `BlogApplication.java` → click ▶ Run.
3. Open **http://localhost:8080** in your browser.
4. Go to **http://localhost:8080/login** to sign in as owner.

---

## 5. Admin features walkthrough

### Writing a post

1. Go to **Posts → Write new post**.
2. Use the **Quill editor** toolbar — bold (Ctrl+B), Heading 2, bullet list, code block.
3. Upload up to **5 images** — drag or click the image slots. First image = cover.
4. Paste any YouTube URL into the YouTube field (full URL, short link, or embed code).
5. Click **Save Draft** or **Publish**.

Auto-save kicks in every 4 seconds. If you close the tab accidentally, reopen the new-post
form and click **Restore draft** when prompted.

### Managing categories

Go to **🏷️ Categories** in the admin nav.

- **All admins** can view the category list.
- **Owner (SUPER_ADMIN) only** can add new categories or delete unused ones.
- Categories with posts assigned **cannot be deleted** — reassign or delete those posts first.

### Profile page

Go to **👤 My Profile** — upload your photo and write a short bio.
Your public author page will be available at `/author/{your-username}`.

---

## 6. Project structure

```
src/main/java/com/vidurarvs/blog/
  controller/   HTTP endpoints (public site + admin area + categories)
  service/      Business rules — interfaces + impl/ package
  repository/   Spring Data JPA interfaces
  model/        JPA entities — User, Category, Post, PostImage, Role
  dto/          Form-backing objects (separate from entities)
  security/     Spring Security — login, roles, UserDetails
  config/       App config + first-run data seeding (categories + super-admin)
  exception/    Custom exceptions + friendly error pages
  util/         SlugUtils (slug generation)

src/main/resources/
  templates/    Thymeleaf HTML views (public + admin)
  static/       CSS, JS (editor.js, main.js), img/, uploads/
  application.properties
  db-migration.sql
```

---

## 7. Category list (as of September 2026)

Technology · Science · Programming · Information & Communication Technology · Gaming ·
Travel · Food · Education · Philosophy · Society & Opinion · **Movies** · **TV Series** ·
**Web Development** · **Visual Arts** · **AI** · **Creativity** · **Music** · **Skills** ·
**Cartoons** · **Dramas** · **Culture**

New categories can be added at any time from **Admin → 🏷️ Categories** without redeploying.

---

## 8. Save progress with Git

```bash
git add .
git commit -m "Describe what changed"
git push
```

---

## 9. Deploy to Oracle VM

See [`PROJECT_REPORT.md`](PROJECT_REPORT.md) — Section 11 for the full 16-step Oracle VM
deployment guide (DuckDNS + Nginx + Let's Encrypt HTTPS).

Quick summary:
```bash
# On Oracle VM:
sudo git pull
sudo -u vidurarvs mvn clean package -DskipTests
sudo systemctl restart vidurarvs
```

---

## 10. Ideas for extension

- Self-service password change screen.
- Comments on posts (new entity + moderation flow).
- Unit tests for `PostServiceImpl` ownership rules (JUnit 5 + Mockito).
- Flyway migrations instead of `ddl-auto=update` before production.
- Image resizing on upload (Thumbnailator library).
- Post scheduling (publish at a future date/time).
