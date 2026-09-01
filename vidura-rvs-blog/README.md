# ViduraRvs

A personal blog platform built with Spring Boot + MySQL, so you can publish long-form
articles on technology, science, travel, gaming, food, education, philosophy, ICT,
programming and anything else on your mind — and invite trusted people as verified
admins to publish alongside you.

This project was generated as source code for you to run, debug and extend yourself
in **IntelliJ IDEA**. It does not run inside this online workspace — follow the steps
below on your own machine.

## What you get

- **Public site** — anyone can browse the latest posts, filter by category, and
  search by keyword. Newest posts always show first.
- **Admin accounts** — you (the `SUPER_ADMIN`/owner) can invite other people as
  `ADMIN`s. Admins can write, edit and delete their own posts; you can edit/delete
  any post and revoke admin access at any time.
- **Dashboard** — every admin sees their own post count and view count; you (the
  owner) also see site-wide totals.
- **Rich posts** — cover image upload, one embedded YouTube video per post, tags for
  search, and a content field that supports basic HTML if you want extra formatting.
- **MySQL storage** — all posts, categories, admin accounts and view counts persist
  in a real relational database via Spring Data JPA/Hibernate.

## Tech stack

| Layer      | Choice                                    |
|------------|--------------------------------------------|
| Backend    | Java 17, Spring Boot 3 (Web, Security, Data JPA, Validation) |
| Frontend   | Server-rendered HTML with Thymeleaf, plain CSS (no JS framework) |
| Database   | MySQL 8 |
| Build tool | Maven |

See `ARCHITECTURE.md` for how the code is organized and why (OOP/SOLID notes for
your SE module).

## 1. Prerequisites

- **JDK 17+** (Temurin, OpenJDK, or any distro)
- **Maven** (or use IntelliJ's bundled Maven — no separate install needed)
- **MySQL 8** running locally (MySQL Community Server + MySQL Workbench, or via
  XAMPP/WAMP, or Docker: `docker run --name vidurarvs-mysql -e MYSQL_ROOT_PASSWORD=yourpassword -p 3306:3306 -d mysql:8`)
- **IntelliJ IDEA** (Community edition is enough)
- **Git** installed and a GitHub (or similar) account, for pushing your progress

## 2. Configure the database

1. Start MySQL and create the database (or let the app do it — the connection
   string already includes `createDatabaseIfNotExist=true`).
2. Open `src/main/resources/application.properties` and set your real MySQL
   username/password:

   ```properties
   spring.datasource.username=root
   spring.datasource.password=your-real-password
   ```

3. (Optional but recommended) Change the bootstrap owner account before your
   first run:

   ```properties
   app.bootstrap.super-admin.username=your-username
   app.bootstrap.super-admin.password=SomethingStrong123!
   ```

   This account is created automatically the very first time the app starts
   (only if the `users` table is empty), so this is how you get your own
   login. Change the password again from a real UI later — this project
   doesn't include a self-service password change screen yet, so if you want
   one, that's a great first extension to build yourself.

## 3. Open and run in IntelliJ

1. `File > Open...` and select the `vidura-rvs-blog` folder (this one). IntelliJ
   will detect the `pom.xml` and import it as a Maven project automatically —
   wait for indexing/dependency download to finish.
2. Open `src/main/java/com/vidurarvs/blog/BlogApplication.java`.
3. Click the green ▶ run icon next to `public static void main`, or right-click
   the file and choose **Run 'BlogApplication'**.
4. Watch the "Run" console — on first successful start you'll see a banner with
   your generated owner username, and the app listening on port 8080.
5. Open **http://localhost:8080** in any browser. That's your live blog, running
   entirely on your own machine.
6. Go to **http://localhost:8080/login** and sign in with the owner account from
   `application.properties` to reach `/admin/dashboard` and start writing.

If the run fails with a MySQL connection error, double check MySQL is running
and the credentials in `application.properties` are correct.

## 4. Try the core flows before moving on

- Publish a post from `/admin/posts/new` with a cover image and a YouTube ID.
- Confirm it appears on the home page, under its category, and shows up when
  you search for one of its tags.
- From `/admin/admins`, invite a second admin account, log out, and log back in
  as that admin to confirm they can publish but can't see "Manage admins".
- Confirm an admin can't edit/delete another admin's post (try changing the URL
  to another author's post id — you should get a 403 page, not the edit form).

## 5. Save your progress with Git

From the project root, in IntelliJ's terminal (or your own terminal):

```bash
git init
git add .
git commit -m "Initial commit: ViduraRvs blog platform"
```

Create an empty repository on GitHub (or GitLab/Bitbucket) named e.g.
`vidura-rvs-blog`, **without** a README/license (to avoid merge conflicts with
what you already committed), then:

```bash
git remote add origin https://github.com/<your-username>/vidura-rvs-blog.git
git branch -M main
git push -u origin main
```

From then on, after each meaningful change:

```bash
git add .
git commit -m "Describe what changed"
git push
```

IntelliJ also has this built in under **Git > Commit** and **Git > Push** if you
prefer the UI over the terminal.

## 6. Hosting it on the internet (after you've verified it locally)

Once everything works on `localhost`, you have a few realistic options to put it
on the public internet with a domain name. All of them need the same two things:
a place to run the Spring Boot app, and a MySQL database it can reach.

**Easiest for a student project (free/cheap tiers):**
- [Railway](https://railway.app) or [Render](https://render.com) — connect your
  GitHub repo, they build the `pom.xml` project automatically and give you a
  managed MySQL/Postgres add-on and a public URL. Add a custom domain in their
  dashboard once you own one (Namecheap, Google Domains, etc.).

**More control (still budget-friendly):**
- A small VPS (DigitalOcean, Hetzner, Oracle Cloud free tier). Install Java +
  MySQL there, build the app with `mvn clean package`, run the resulting jar
  from `target/vidura-rvs-blog.jar` with `java -jar vidura-rvs-blog.jar`, and
  point your domain's DNS `A` record at the server's IP. Use a reverse proxy
  (Nginx or Caddy) in front of it for HTTPS via Let's Encrypt.

Whichever you choose, remember to:
- Set real production values for `spring.datasource.*` and the bootstrap admin
  password via environment variables instead of committing them to Git.
- Set `spring.jpa.hibernate.ddl-auto=validate` (not `update`) once your schema
  is stable, and manage schema changes deliberately from then on.

## Project structure

```
src/main/java/com/vidurarvs/blog/
  controller/   HTTP endpoints (public site + admin area)
  service/      business rules (interfaces + impl/ package)
  repository/   Spring Data JPA interfaces
  model/        JPA entities (User, Category, Post, Role)
  dto/          form-backing objects, separate from entities
  security/     Spring Security wiring (login, roles, UserDetails)
  config/       app-wide config + first-run data seeding
  exception/    custom exceptions + friendly error pages
  util/         small stateless helpers (slug generation)
src/main/resources/
  templates/    Thymeleaf HTML views
  static/       CSS, and uploaded images at runtime
  application.properties
```

## Where to go next

Ideas for you to extend as you learn (good candidates for your SE module writeup):
- A self-service "change my password" page.
- Comments on posts (would introduce a new entity + moderation flow).
- Rich-text/Markdown editor instead of a plain textarea.
- Unit tests for `PostServiceImpl`'s ownership rules using Mockito.
- Flyway migrations instead of `ddl-auto=update` before going to production.
