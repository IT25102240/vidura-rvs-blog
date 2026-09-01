# Architecture & design notes

Written to double as background for your SE module — a short explanation of how
this codebase is organized and which OOP/SOLID principles show up where.

## Layers

```
Browser
  -> Controller   (HTTP in/out only: read request, call a service, pick a view)
    -> Service    (business rules: publishing, search, ownership, view counts)
      -> Repository (Spring Data JPA - talks to MySQL)
        -> Entity   (Post, User, Category - what gets persisted)
```

Each layer only talks to the one directly below it. Controllers never touch
repositories directly, and services never build HTML or read HTTP request
objects. This is the classic layered/MVC split, and it's also what makes the
SOLID points below possible.

## Where each SOLID principle shows up

**Single Responsibility Principle** — `AdminPostController` only translates
HTTP requests into service calls and picks a template; it has no idea *how*
"only the author can edit their own post" is enforced. That rule lives in one
place: `PostServiceImpl.requireOwnerOrSuperAdmin(...)`. If that rule ever
changes, you edit exactly one method.

**Open/Closed Principle** — `PostService` and `UserService` are interfaces.
`PostServiceImpl` is *one* implementation, but nothing in a controller depends
on that class directly. You could add a `CachingPostService implements
PostService` that wraps the real one and adds an in-memory cache, wire it up
in a `@Configuration` class, and no controller code would need to change.

**Liskov Substitution Principle** — `CustomUserPrincipal` fully implements
Spring Security's `UserDetails` contract (it doesn't throw
`UnsupportedOperationException` anywhere or secretly break assumptions like
`isEnabled()`/`isAccountNonLocked()` tracking whether the admin was revoked).
Anywhere Spring Security expects a `UserDetails`, this class works correctly
as a drop-in.

**Interface Segregation Principle** — there's no single fat `BlogService`
interface with 30 methods. `PostService`, `CategoryService` and `UserService`
are separate, small, and each is about one kind of thing. A class that only
needs categories (like `GlobalModelAttributes`, which builds the nav bar)
depends only on `CategoryService` — it isn't forced to depend on post or user
methods it never calls.

**Dependency Inversion Principle** — every controller and service depends on
interfaces (`PostService`, `UserService`, `CategoryService`, `PostRepository`,
...) via constructor injection, never on `new SomeImpl()`. Spring wires the
concrete implementations at startup. This is also why unit-testing a
controller or service in isolation (with Mockito mocks of its dependencies) is
straightforward — see the "Where to go next" list in `README.md`.

## Data model

- `User` — a verified contributor. `role` is `SUPER_ADMIN` (you, the owner) or
  `ADMIN` (someone you invited). There is intentionally no "visitor account" —
  reading the blog never requires a login.
- `Category` — a topic bucket (Technology, Science, Travel, ...), seeded once
  on first run by `DataInitializer`.
- `Post` — one article: title, slug, summary, content, optional cover image
  path, optional YouTube video id, comma-separated tags, its `Category`, its
  author `User`, a `viewCount`, and `published`/timestamps.

## Why MySQL instead of plain text files

The brief mentioned either. MySQL was chosen because the product needs three
things a flat text file can't give you cleanly: (1) safe concurrent writes
when more than one admin publishes at the same time, (2) fast filtering and
sorting ("latest first", "only this category", keyword search across five
fields) without hand-rolled file parsing, and (3) referential integrity
(a post always points at a real category and a real author). The repository
layer (`PostRepository`, etc.) is still just an interface, so if you want to
explore a file-backed store as a learning exercise, you can implement a
second class that satisfies the same interface without touching any service
or controller code — that's the Open/Closed principle in practice.

## Security model

- Spring Security intercepts every request. Public paths (`/`, `/post/**`,
  `/category/**`, `/search`, static assets) are open to everyone.
  `/admin/**` requires `ADMIN` or `SUPER_ADMIN`; `/admin/admins/**`
  (inviting/revoking admins) requires `SUPER_ADMIN` specifically.
- Passwords are hashed with BCrypt (`PasswordEncoder` bean) — the raw password
  is never stored or logged.
- Ownership checks (an `ADMIN` editing only their own posts) are enforced a
  second time inside `PostServiceImpl`, not just via URL access rules — so
  even if a future controller forgets to check, the service layer still
  protects the data.
