# ViduraRvs — Architecture

## Layered MVC with Spring Boot

This document describes the code organisation, design decisions, and
OOP/SOLID principles applied in the ViduraRvs blogging platform.

---

## 1. System architecture

```
Browser / Mobile
   |
   v
DuckDNS hostname → Oracle public IP
   |
   v
Nginx :80 / :443  — reverse proxy + SSL termination
   |
   v
Spring Boot Tomcat :8080 on 127.0.0.1  (never public-facing)
   |
   +──→ MySQL :3306 on localhost
   |
   +──→ /var/lib/vidurarvs/uploads/  (image files on disk)
```

---

## 2. Application layers

```
Browser
   ↓
Controller layer
   Translates HTTP requests, validates form input, selects view template.
   Never accesses repositories directly.
   ↓
Service layer
   Business rules: ownership checks, YouTube parsing, image upload,
   slug generation, view counting, auto-save, category management.
   ↓
Repository layer
   Spring Data JPA — join-fetch queries prevent N+1 queries and
   LazyInitializationException when open-in-view is disabled.
   ↓
Entity / Model layer
   User, Post, PostImage, Category, Role
   ↓
MySQL database
```

---

## 3. Package map

| Package | Responsibility |
|---|---|
| `controller/` | HTTP endpoints — one class per functional area |
| `controller/AdminCategoryController` | **NEW** — add/delete categories (SUPER_ADMIN) |
| `controller/AdminPostController` | Create/edit/delete posts, toggle-visibility, autosave |
| `controller/AdminDashboardController` | Dashboard stats |
| `controller/AdminUserController` | Invite/activate/deactivate admins |
| `controller/ProfileController` | `/author/{username}` and `/admin/profile` |
| `controller/HomeController` | Public home, category, search, post-detail |
| `controller/GlobalModelAttributes` | `@ModelAttribute` for nav categories (cached) |
| `service/` | Business rule interfaces |
| `service/impl/` | Concrete implementations |
| `service/CategoryService` | findAll, findBySlug, findById, **create, delete** |
| `service/PostService` | All post business logic including toggleVisibility |
| `service/UserService` | Account management including updateProfile |
| `repository/` | Spring Data JPA interfaces — no business rules |
| `model/` | JPA entities |
| `dto/` | Form-backing objects, separate from entities |
| `security/` | Spring Security — login, roles, BCrypt, CSRF |
| `config/` | App-wide config + first-run seeder |
| `exception/` | Custom exceptions + `GlobalExceptionHandler` |
| `util/` | `SlugUtils` — stateless slug generation |

---

## 4. Entity model

### User

| Field | Notes |
|---|---|
| id, fullName, username, email | Core identity |
| password | BCrypt hash |
| role | `SUPER_ADMIN` or `ADMIN` |
| active | Deactivated admins cannot log in |
| profilePicturePath | Relative path under /uploads/ |
| bio | Author bio — shown on public profile page |
| createdAt | |

### Post

| Field | Notes |
|---|---|
| id, title, slug | Unique URL-safe slug |
| summary | Short description for cards |
| content | Quill-produced HTML (LONGTEXT) |
| images | `List<PostImage>` — up to 5; sortOrder 0 = cover |
| youtubeVideoId | 11-char parsed ID |
| tags | Comma-separated |
| category | ManyToOne |
| author | ManyToOne |
| viewCount, published, createdAt, updatedAt | |

### PostImage

| Field | Notes |
|---|---|
| id | |
| post | ManyToOne (cascade delete) |
| imagePath | UUID-based filename |
| sortOrder | 0 = cover, 1+ = gallery |
| caption | Optional |

### Category

| Field | Notes |
|---|---|
| id | |
| name | Unique (case-insensitive check on create) |
| slug | Auto-generated from name via SlugUtils |

Seeded on first run; new ones can be added from **Admin → Categories** without redeploying.

---

## 5. URL map

### Public

| URL | Controller method |
|---|---|
| `/` | `HomeController.home()` |
| `/category/{slug}` | `HomeController.byCategory()` |
| `/search?q=` | `HomeController.search()` |
| `/post/{slug}` | `HomeController.postDetail()` |
| `/author/{username}` | `ProfileController.publicProfile()` |
| `/about` | `HomeController.about()` |

### Admin

| URL | Method | Role |
|---|---|---|
| `/admin/dashboard` | GET | ADMIN / SUPER_ADMIN |
| `/admin/posts` | GET | ADMIN / SUPER_ADMIN |
| `/admin/posts/new` | GET + POST | ADMIN / SUPER_ADMIN |
| `/admin/posts/{id}/edit` | GET + POST | Author or SUPER_ADMIN |
| `/admin/posts/{id}/toggle-visibility` | POST | Author or SUPER_ADMIN |
| `/admin/posts/{id}/autosave` | POST (AJAX) | Author or SUPER_ADMIN |
| `/admin/posts/{id}/delete` | POST | Author or SUPER_ADMIN |
| `/admin/profile` | GET + POST | ADMIN / SUPER_ADMIN |
| `/admin/categories` | GET | ADMIN / SUPER_ADMIN |
| `/admin/categories/add` | POST | **SUPER_ADMIN only** |
| `/admin/categories/{id}/delete` | POST | **SUPER_ADMIN only** |
| `/admin/admins` | GET | SUPER_ADMIN |
| `/admin/admins/new` | GET + POST | SUPER_ADMIN |
| `/admin/admins/{id}/activate` | POST | SUPER_ADMIN |
| `/admin/admins/{id}/deactivate` | POST | SUPER_ADMIN |

---

## 6. Security design

### Access rules (SecurityConfig)

```
/admin/admins/**                   → SUPER_ADMIN only
/admin/categories/add              → SUPER_ADMIN only
/admin/categories/*/delete         → SUPER_ADMIN only
/admin/**                          → ADMIN or SUPER_ADMIN
/ (public site), /css/**, etc.     → permitAll
```

### Ownership enforcement

Post edit/delete checks happen twice:
1. The edit GET — redirects to 403 if not owner or SUPER_ADMIN.
2. The service layer — throws `ForbiddenActionException` before any DB write.

### Category delete guard

`CategoryServiceImpl.delete()` calls `postRepository.countByCategory()` before
deleting. If any posts (published or draft) still use the category, it throws
`ForbiddenActionException` with a clear message.

### Password storage

BCrypt via Spring Security `BCryptPasswordEncoder`. Passwords are never logged.

### CSRF

`CsrfTokenRequestAttributeHandler` — all admin forms include a hidden `_csrf` token.
The AJAX autosave endpoint reads the token from the page meta tag.

---

## 7. OOP and SOLID

### Single Responsibility (SRP)

| Class | One responsibility |
|---|---|
| `AdminCategoryController` | HTTP for category CRUD — delegates to service |
| `CategoryServiceImpl` | Category business rules (uniqueness, delete guard) |
| `CategoryRepository` | DB queries — no business rules |
| `PostFormDTO` | Form binding — separate from the `Post` entity |

### Open/Closed (OCP)

Service interfaces (`CategoryService`, `PostService`, `UserService`) allow new
implementations (e.g. cached, read-only) without modifying any controller.

### Liskov Substitution (LSP)

`CustomUserPrincipal` implements `UserDetails`. It can substitute for any Spring
Security principal without breaking callers.

### Interface Segregation (ISP)

Three small, focused service interfaces instead of one large `BlogService`.
`CategoryService` only exposes category operations; `PostService` only post operations.

### Dependency Inversion (DIP)

All controllers and services receive dependencies through constructor injection.
No `new` is used for infrastructure or concrete service classes.

---

## 8. Key implementation decisions

### YouTube smart parser

`PostServiceImpl.extractYoutubeId()` accepts all these formats and extracts the
11-character video ID:

```
https://www.youtube.com/watch?v=XXXXXXXXXXX
https://youtu.be/XXXXXXXXXXX
https://www.youtube.com/embed/XXXXXXXXXXX
<iframe ... src="https://www.youtube.com/embed/XXXXXXXXXXX" ...>
XXXXXXXXXXX  (bare 11-char ID)
```

### Auto-save drafts

`editor.js` saves form content to `localStorage` every 4 seconds. On returning
to the form, if a localStorage entry exists for the same post slug/new-post, the
user is prompted to restore. The server also exposes a `/admin/posts/{id}/autosave`
AJAX endpoint for server-side persistence of in-progress edits.

### Category management (new)

- `CategoryService.create(name)` — auto-generates slug, checks duplicate names
  (case-insensitive), evicts `navCategories` cache.
- `CategoryService.delete(id)` — checks `postRepository.countByCategory()` before
  deleting; rejects if count > 0 with a clear error message. Evicts cache.
- The nav cache is evicted on both create and delete so the public category strip
  updates immediately without a server restart.

### N+1 prevention

All paginated post queries use `join fetch p.category join fetch p.author` in JPQL.
`PostImage` uses `FetchType.EAGER` to avoid lazy-load errors in the gallery view.
`spring.jpa.open-in-view=false` is intentional.

### Oracle Free Tier tuning

- HikariCP pool: max 5 connections (prevents OOM on 6 GB ARM VM).
- HTTP compression: enabled (saves bandwidth).
- Spring Cache: nav-category queries cached in ConcurrentMap.
- Tomcat threads: max 50 (balanced for 4 cores, light blog traffic).

---

## 9. Template map

```
templates/
  fragments/layout.html         Shared HTML shell (fonts, nav, footer, lightbox)
  index.html                    Home page — hero + post card grid
  post-detail.html              Full article, gallery, YouTube embed, share buttons
  author-profile.html           Public author profile — photo, bio, post grid
  login.html                    Admin login form
  about.html                    About page
  access-denied.html            403 page
  error.html                    Generic error page
  admin/
    fragments/admin-nav.html   Admin sidebar nav (Dashboard, Posts, Categories, Profile, Admins)
    dashboard.html              Dashboard — stats cards, recent posts, quick actions
    post-form.html              Create/edit post — Quill editor, image upload, YouTube input
    post-list.html              Post list — status badges, Hide/Show toggle
    category-list.html          Category management — add/delete (SUPER_ADMIN forms)
    profile.html                Admin bio + profile photo upload
    admin-form.html             Invite a new admin account
    admin-list.html             List all admin accounts
```
