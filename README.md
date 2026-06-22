# GearShare

A peer-to-peer equipment rental platform built for the SoftUni **Spring Fundamentals** course (May 2026) individual project. Owners list equipment (cameras, tools, camping gear, etc.); renters browse it, request bookings for specific dates, and leave a review once the rental is complete.

## Tech stack

- **Java** 17
- **Spring Boot** 3.4.0
  - Spring Web (Spring MVC)
  - Spring Data JPA (Hibernate)
  - Spring Security (session-based form login)
  - Spring Boot Validation (Jakarta Bean Validation)
  - Thymeleaf (server-rendered views) + `thymeleaf-extras-springsecurity6`
- **Database**: MariaDB (relational)
- **Migrations**: Flyway
- **Build tool**: Maven
- **Other**: Lombok, BCrypt password hashing

## Domain model

| Entity | Type | Notes |
|---|---|---|
| `User` | technical | Authentication entity (username, email, hashed password, role). |
| `Equipment` | domain | An item listed by an `OWNER`. Has a category, daily price, location, and an availability flag. |
| `Booking` | domain | A rental request made by a `RENTER` for an `Equipment`, over a date range. Carries a calculated total price and a status. |
| `Review` | domain | A rating + comment a `RENTER` leaves for a `COMPLETED` `Booking`. One review per booking. |

**Entity relationship**: `Booking` → `Equipment` (many-to-one) and `Booking` → `User` (many-to-one, as renter); `Equipment` → `User` (many-to-one, as owner); `Review` → `Booking` (one-to-one via a unique foreign key). All relationships are unidirectional, navigated from the "many" side only, per the recommended design practices.

All entities use a `UUID` as their primary key (`GenerationType.UUID`).

## Roles & access control

- **Guests** (not logged in): home page, login, registration, and browsing equipment (list + details) — read-only.
- **Logged-in users**: everything else.
  - **OWNER**: create/edit/delete/toggle their own equipment listings; approve, reject, or complete booking requests for their equipment.
  - **RENTER**: request, edit (while pending), or cancel bookings; leave/edit/delete a review for a completed booking.
- Role and ownership checks are enforced at the service layer in addition to Spring Security's URL-level rules, so a user can never act on another user's resources even if they guess a URL.

## Functionalities (full CRUD, triggered from the UI, POST/PUT/DELETE-backed)

1. **Equipment management** — owners create, edit, delete, and toggle availability of listings.
2. **Booking lifecycle** — renters create, edit (while pending), and cancel booking requests; owners approve, reject, or mark a booking completed.
3. **Reviews** — renters create, edit, and delete a review tied to a completed booking.
4. **Availability toggling** — owners flip a listing between available/unavailable, which immediately affects what renters can book.

Functionality #2 (Booking) implements full CRUD (Create/Read/Update/Delete) on its own entity and is the central flow of the app; #1 and #3 each implement full CRUD on their respective entities as well.

## Data validation & error handling

- Every form (`registerRequest`, `equipmentFormRequest`, `bookingFormRequest`, `reviewFormRequest`) uses Jakarta Bean Validation annotations (`@NotBlank`, `@Size`, `@Email`, `@DecimalMin`, `@Future`, etc.).
- On validation failure, the form is redisplayed with field-level error messages (rendered in red via `.field-error`).
- Business rules that can't be expressed as field annotations — overlapping booking dates, booking your own unavailable equipment, reviewing a booking that isn't completed, duplicate reviews, acting on someone else's resource — are enforced in the service layer and surfaced as custom exceptions:
  - `EntityNotFoundException`
  - `UnauthorizedActionException`
  - `InvalidBookingException`
  - `DuplicateReviewException`

  These are translated to a friendly error page by a `@ControllerAdvice` (`GlobalExceptionHandler`) instead of a raw stack trace.

## Pages

| # | Page | Type |
|---|---|---|
| 1 | Home | Static |
| 2 | Register | Dynamic |
| 3 | Login | Dynamic |
| 4 | Browse equipment | Dynamic |
| 5 | Equipment details | Dynamic |
| 6 | List equipment (create) | Dynamic |
| 7 | Edit equipment | Dynamic |
| 8 | My listings | Dynamic |
| 9 | Request booking | Dynamic |
| 10 | Edit booking | Dynamic |
| 11 | My bookings | Dynamic |
| 12 | Booking requests (owner) | Dynamic |
| 13 | Leave a review | Dynamic |
| 14 | Edit review | Dynamic |

13 of 14 pages are dynamic; only the home page is static.

## Running locally

1. Create a MariaDB database:
   ```sql
   CREATE DATABASE gearshare_db CHARACTER SET utf8mb4;
   ```
2. Set credentials via environment variables (or edit `application.yml` directly):
   ```bash
   export DB_USERNAME=root
   export DB_PASSWORD=your_password
   ```
3. Run the app:
   ```bash
   mvn spring-boot:run
   ```
   Flyway will create the schema automatically on first run.
4. Visit `http://localhost:8080`.

## Running tests

```bash
mvn test
```

Tests run against an in-memory H2 database (MySQL compatibility mode) via the `test` Spring profile, so no external database is required to run the test suite.

## Integrations with other systems

This project does not integrate with any third-party/external systems or APIs; it is a fully self-contained Spring Boot application with its own relational database.
