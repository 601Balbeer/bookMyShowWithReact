# 🎬 Book My Show (BMS) - Core Backend Application

A movie theatre management backend built with Spring Boot, PostgreSQL, and JPA. It supports user registration and login, theatre and hall management, show scheduling, public show and seat-status lookups, customer bookings with pending seat holds, and a separate simulated payment confirmation API. Payments are demo-only and do not move money.

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Database Schema](#database-schema)
- [API Endpoints](#api-endpoints)
- [Authentication and Authorization](#authentication-and-authorization)
- [Setup Instructions](#setup-instructions)
- [Testing with Postman](#testing-with-postman)
- [Key Features](#key-features)
- [Data Models](#data-models)
- [Common Queries](#common-queries)

## 🎯 Overview

BMS Core is a backend application for:
- Public customer registration and login; theater-owner accounts require trusted provisioning/approval
- Theatre and hall management
- Show scheduling and browsing
- Public theatre/location, show, and seat-status browsing
- Customer seat booking followed by a separate payment-confirmation request

Payments are simulated locally: the API records a successful demo payment only when the submitted amount exactly matches the booking total. It does not connect to PhonePe or move money.

### Who Can Do What

- **Public users:** Register and log in. Browse theatres by city/location, view shows by theatre or hall, and inspect each seat's availability without a token.
- **Theatre owners/managers:** Approved/provisioned owner accounts log in to get an owner-authorized JWT, then create theatres, configure halls and seat types/prices, and schedule shows.
- **Customers:** Register as `CUSTOMER`, log in to get a customer-authorized JWT, create a pending booking, and pay the calculated total through the payment API. A mismatched amount fails and leaves the booking pending. Customers can cancel a confirmed booking for a simulated refund and can view only their own payment records.

Send a protected request's token in `Authorization: Bearer <token>`. Public seat-status responses show `NOT_BOOKED`, `PENDING` (held for an unpaid booking), or `BOOKED`. The API returns seat-by-seat status; clients can count `NOT_BOOKED` seats to display the number currently available.

## 🛠 Tech Stack

- **Framework:** Spring Boot 3.5.x
- **Language:** Java 17
- **Database:** PostgreSQL
- **ORM:** Spring Data JPA / Hibernate
- **Security:** Spring Security with JWT bearer authentication
- **Build Tool:** Maven
- **API Testing:** Postman

## 🏗 Architecture

### Inheritance Strategy

The application uses JPA's `JOINED` inheritance strategy with a base `GlobalRecord` entity that provides:
- `sysId` (UUID) - Primary key
- `id` (String) - Unique identifier
- `createdAt` / `updatedAt` - Timestamps
- `createdBy` / `updatedBy` - Audit fields

Entities extending `GlobalRecord` use separate joined tables keyed by `sys_id`.

### Entity Relationships

```text
User
├── Customer
│   └── Booking
│       ├── BookedSeat ── belongs to ── Show
│       └── Payment (pending until simulated payment API succeeds)
└── TheatreOwner
    └── Theater
        └── Hall
            └── Show

Booking.successPayment ── optional reference to the successful Payment
```

Each `BookedSeat` links one seat on a `Show` to its `Booking`. The simulated `Payment` links to its `Booking`, and `Booking.successPayment` references that record.

## 💾 Database Schema

### Core Tables

#### global_records
Base table for entities extending `GlobalRecord`.
```text
- sys_id (UUID, PK)
- id (String, Unique)
- created_at / updated_at (Timestamp)
- created_by / updated_by (String)
```

#### users
Stores common user information.
```text
- sys_id (UUID, PK, FK -> global_records)
- full_name, email, contact_number, password hash
- dtype (Customer or TheatreOwner discriminator)
```

#### customers
Customers use the common account fields; bookings link to the customer row.
```text
- sys_id (UUID, PK, FK -> users)
- date_of_birth (Date)
```

#### theatre-owners
Theatre-owner-specific fields.
```text
- sys_id (UUID, PK, FK -> users)
- business_name
- pan_card
- business_registration_id
- gst_number
```

For an existing PostgreSQL database, run `src/main/resources/db/manual/user-profile-cleanup-postgresql.sql` once to remove the unused user-location and customer rewards columns, add customer `date_of_birth`, and add owner `business_name`. Back up the database before applying this schema cleanup. Theater locations remain in the `locations` table and continue to be linked to theaters.

#### theaters
Theatre information.
```text
- sys_id (UUID, PK, FK -> global_records)
- theater_name
- location_sys_id (UUID, FK -> locations)
- owner_sys_id (UUID, FK -> users)
```

#### halls
Hall/screen information within a theatre.
```text
- sys_id (UUID, PK, FK -> global_records)
- hall_name
- seat_count
- theater_sys_id (UUID, FK -> theaters)
```

#### hall-row-mappings
Seat row configurations and pricing.
```text
- sys_id (UUID, PK, FK -> global_records)
- hall_sys_id (UUID, FK -> halls)
- seat_type (SeatType enum)
- row_range (String)
- seat_count (String)
- base_price (Double)
- base_price_currency (Currency enum)
```

#### shows
Movie/show scheduling information.
```text
- sys_id (UUID, PK, FK -> global_records)
- show_name, short_description, artists
- show_start_time / show_end_time (Timestamp)
- show_start_time_in_seconds / show_end_time_in_seconds (Long)
- hall_sys_id (UUID, FK -> halls)
```

#### booked_seats
Seats associated with a booking and show.
```text
- sys_id (UUID, PK, FK -> global_records)
- seat_id (String)
- show_sys_id (UUID, FK -> shows)
```

#### payments
Payment attempt associated with a booking. Booking creation writes it with `PENDING`; the separate demo payment API marks it `SUCCESS` and confirms the booking. No gateway is called.
```text
- sys_id (UUID, PK, FK -> global_records)
- amount (Double)
- payment_time (LocalDateTime)
- transaction_id (String)
- payment_status (PaymentStatus enum: PENDING, SUCCESS, FAILED, REFUND)
- booking_sys_id (UUID, FK -> bookings)
```

#### locations
Address information.
```text
- sys_id (UUID, PK, FK -> global_records)
- address_line1, address_line2, address_line3
- country, state, city
- zip_code (Integer)
- lat, log (Integer)
```

## 🔌 API Endpoints

All endpoints use the base URL `http://localhost:8080` by default. JSON request bodies use `Content-Type: application/json`.

### Complete Endpoint Index

| Method | Endpoint | Access |
|---|---|---|
| `POST` | `/api/v1/users/user/register` | Public; customer accounts only |
| `POST` | `/api/v1/users/login` | Public |
| `POST` | `/api/v1/admin/theater-owners` | `ADMIN` JWT; provisions a theater-owner account |
| `POST` | `/api/v1/theatre/create` | `THEATER_OWNER` JWT; owner is taken from token |
| `GET` | `/api/v1/theatre/get?city={city}` | Public |
| `GET` | `/api/v1/theatre/get/all` | `THEATER_OWNER` JWT; returns authenticated owner's theaters |
| `POST` | `/api/v1/hall/register?theaterId={theaterSysId}` | `THEATER_OWNER` JWT; theater must belong to owner |
| `GET` | `/api/v1/hall/shows/all?hallId={hallSysId}` | Public |
| `POST` | `/api/v1/show/create?hallId={hallSysId}` | `THEATER_OWNER` JWT; hall must belong to owner |
| `GET` | `/api/v1/show/get/{theatreSysId}` | Public |
| `GET` | `/api/v1/show/seat-status/{showSysId}` | Public |
| `POST` | `/api/v1/bookings` | `CUSTOMER` JWT |
| `POST` | `/api/v1/payments/booking/{bookingSysId}/pay` | `CUSTOMER` JWT; booking owner only |
| `GET` | `/api/v1/payments/{paymentSysId}` | `CUSTOMER` JWT; booking owner only |
| `POST` | `/api/v1/bookings/{bookingSysId}/cancel` | `CUSTOMER` JWT; booking owner only |

The following sections give request examples and describe the responses and effects for these endpoints.
Successful create/register endpoints return `201 Created`; successful login, lookup, payment, and cancellation endpoints return `200 OK`, unless noted otherwise.
In endpoint examples, text inside `{...}` is a placeholder. Replace it with the actual value and do not type the curly braces into Postman. For example, use `?sysId=2c011121-9bd6-489a-81e1-76680927dc9b`, not `?sysId={2c011121-9bd6-489a-81e1-76680927dc9b}`.

### User Management

#### Register User (Customer or Theatre Owner) - Public
```http
POST /api/v1/users/user/register
Content-Type: application/json
```

```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "your-password",
  "contactNumber": "+919876543210",
  "dateOfBirth": "1998-04-21",
  "userType": "CUSTOMER"
}
```

Public registration accepts only `userType: CUSTOMER` and returns `403 Forbidden` for `THEATER_OWNER`; owner roles cannot be self-assigned. It returns `201 Created` with a safe account response DTO that excludes the password and owner business identifiers. Customer date of birth is stored as profile data; age restrictions are not currently enforced.

#### Bootstrap the Admin Account

Set `BMS_ADMIN_EMAIL` and `BMS_ADMIN_PASSWORD` in the application's run configuration before starting the app. `com.bms.bms_core.schedulers.SystemScheduler` listens for `ApplicationReadyEvent` and creates the admin account if that email is not already registered. The event listener runs at each startup, but the database check prevents duplicate account creation. The password must be at least 12 characters and is stored as a BCrypt hash. The app does not reset an existing admin password. Do not commit these credentials.

Log in through the normal login endpoint with that email and password. The login response includes an `admin` profile and an `ADMIN` JWT.

#### Provision a Theatre Owner (Admin JWT required)
```http
POST /api/v1/admin/theater-owners
Authorization: Bearer <admin-token>
Content-Type: application/json
```

```json
{
  "fullName": "Taylor Owner",
  "email": "owner@example.com",
  "password": "owner-password",
  "contactNumber": "+919876543211",
  "businessName": "PVR Cinemas",
  "panCard": "ABCDE1234F",
  "businessRegistrationId": "BUS-12345",
  "gstNumber": "27ABCDE1234F1Z5"
}
```

The endpoint creates an owner account only for an authenticated admin and returns a safe profile response without PAN, GST, or registration ID. The owner can then log in normally to receive a `THEATER_OWNER` JWT.

#### Login - Public
```http
POST /api/v1/users/login
Content-Type: application/json
```

```json
{
  "email": "john@example.com",
  "password": "your-password"
}
```

A successful login response includes a JWT in `token`.

Example response shape:
```json
{
  "token": "<signed-jwt>",
  "theatreOwner": null,
  "admin": null,
  "customer": {
    "sysId": "123e4567-e89b-12d3-a456-426614174000",
    "id": "CUSTOMER-example-id",
    "fullName": "John Doe",
    "email": "john@example.com",
    "contactNumber": "+919876543210",
    "userType": "CUSTOMER",
    "dateOfBirth": "1998-04-21"
  }
}
```

Login returns safe account DTOs; PAN, GST, and business registration identifiers are never included. For an owner login, `theatreOwner` contains safe profile fields and `customer` is `null`.

### Theatre Management

#### Create Theatre - Theatre owner JWT required
```http
POST /api/v1/theatre/create
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "theaterName": "PVR Cinemas",
  "location": {
    "addressLine1": "Phoenix Mall",
    "city": "Mumbai",
    "state": "Maharashtra",
    "country": "India",
    "zipCode": 400001,
    "lat": 19,
    "log": 72
  }
}
```

Returns `201 Created` with the created theater, including its generated identifiers and associated location.

#### Get Theatres by City - Public
```http
GET /api/v1/theatre/get?city={cityName}
```

Returns `200 OK` with a JSON array of theaters in that city, including their location data.

#### Get My Theatres - Theatre owner JWT required
```http
GET /api/v1/theatre/get/all
Authorization: Bearer <token>
```

Returns `200 OK` with a JSON array of theaters owned by the authenticated owner. The owner is identified from the JWT; no owner UUID is sent.

### Hall Management

#### Register Hall - Theatre owner JWT required
```http
POST /api/v1/hall/register?theaterId={theatre_sys_id}
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "hallName": "Audi 1",
  "seatCount": 150,
  "rowMappingList": [
    {
      "seatType": "REGULAR",
      "rowRange": "A-E",
      "seatCount": "50",
      "basePrice": 150.0,
      "basePriceCurrency": "INR"
    },
    {
      "seatType": "PREMIUM",
      "rowRange": "F-H",
      "seatCount": "60",
      "basePrice": 250.0,
      "basePriceCurrency": "INR"
    },
    {
      "seatType": "RECLINER",
      "rowRange": "I-J",
      "seatCount": "40",
      "basePrice": 400.0,
      "basePriceCurrency": "INR"
    }
  ]
}
```

Returns `201 Created` with the created hall and its generated identifier.

#### Get Shows by Hall - Public
```http
GET /api/v1/hall/shows/all?hallId={hall_sys_id}
```

Returns `200 OK` with a JSON array of shows scheduled in the hall.

### Show Management

#### Create Show - Theatre owner JWT required
```http
POST /api/v1/show/create?hallId={hall_sys_id}
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "showName": "Avengers: Endgame",
  "shortDescription": "The epic conclusion to the Infinity Saga",
  "artists": "Robert Downey Jr., Chris Evans, Scarlett Johansson",
  "showStartTime": "2026-10-15T18:00:00",
  "showEndTime": "2026-10-15T21:00:00"
}
```

Returns `201 Created` with the created show.

#### Get Shows by Theatre - Public
```http
GET /api/v1/show/get/{theatreSysId}
```

Returns `200 OK` with a JSON array of shows scheduled at the theater.

#### Get Seat Status for a Show - Public
```http
GET /api/v1/show/seat-status/{showSysId}
```

Returns `200 OK` with a JSON array containing each seat's `seatNumber`, `seatStatus`, `showId`, `theaterId`, `hallId`, and `seatType`.

### Booking and Demo Payment - Customer JWT required

Booking and payment use two API requests. Creating a booking checks availability, reserves the selected seats, calculates the price, and saves both the booking and a payment attempt as `PENDING`. The customer submits the amount to the payment API. An exact match simulates success and confirms the booking; an amount mismatch records `FAILED` and leaves the booking pending for retry. The demo payment endpoint does not contact PhonePe or transfer money.

### Booking Management - Customer JWT required

#### Create a Booking
```http
POST /api/v1/bookings
Authorization: Bearer <customer-token>
Content-Type: application/json
```

Request JSON:
```json
{
  "showSysId": "123e4567-e89b-12d3-a456-426614174000",
  "seatCounts": [
    { "seatType": "PREMIUM", "count": 2 },
    { "seatType": "GOLD", "count": 1 }
  ]
}
```

The authenticated customer is used as the booking owner. Send the number of seats needed for each type configured in that hall. The server selects available seats of those types and calculates the total from the hall's configured prices; callers do not submit seat IDs or an amount. This request saves the selected seats and a `PENDING` booking/payment. The seats are held and shown as `PENDING` in seat-status results while payment is pending.

Example response JSON:
```json
{
  "bookingSysId": "123e4567-e89b-12d3-a456-426614174000",
  "showSysId": "123e4567-e89b-12d3-a456-426614174001",
  "seats": [
    { "seatId": "A-1", "seatType": "PREMIUM", "price": 250.0 },
    { "seatId": "A-2", "seatType": "PREMIUM", "price": 250.0 },
    { "seatId": "E-1", "seatType": "GOLD", "price": 300.0 }
  ],
  "amount": 800.0,
  "status": "PENDING",
  "payment": {
    "sysId": "987fcdeb-51a2-43f7-b123-456789abcdef",
    "bookingSysId": "123e4567-e89b-12d3-a456-426614174000",
    "amount": 800.0,
    "attemptedAmount": null,
    "paymentTime": null,
    "transactionId": null,
    "paymentStatus": "PENDING"
  }
}
```

#### Pay for a Pending Booking (Simulated)
```http
POST /api/v1/payments/booking/{bookingSysId}/pay
Authorization: Bearer <customer-token>
Content-Type: application/json
```

Submit the amount being paid:
```json
{
  "amount": 800.0
}
```

The server compares this with the booking total. An exact match records `SUCCESS` and changes the booking status to `CONFIRMED`. A smaller or larger amount records `FAILED`; the booking stays `PENDING`, and the customer can retry with the correct amount. This is a local simulation; it does not call PhonePe or charge money. Only the customer who owns the booking can pay it.

Example successful response:
```json
{
  "sysId": "987fcdeb-51a2-43f7-b123-456789abcdef",
  "bookingSysId": "123e4567-e89b-12d3-a456-426614174000",
  "amount": 800.0,
  "attemptedAmount": 800.0,
  "paymentTime": "2026-10-13T18:30:00",
  "transactionId": "DEMO-PAYMENT-generated-id",
  "paymentStatus": "SUCCESS"
}
```

#### Example Failed Payment Response
```http
POST /api/v1/payments/booking/{bookingSysId}/pay
Authorization: Bearer <customer-token>
Content-Type: application/json
```

For a booking total of `800.0`, submitting `700.0` returns:
```json
{
  "sysId": "987fcdeb-51a2-43f7-b123-456789abcdef",
  "bookingSysId": "123e4567-e89b-12d3-a456-426614174000",
  "amount": 800.0,
  "attemptedAmount": 700.0,
  "paymentTime": "2026-10-13T18:30:00",
  "transactionId": "DEMO-FAILED-generated-id",
  "paymentStatus": "FAILED"
}
```

The booking stays `PENDING` and the seats remain held. Retry the same `/pay` endpoint with the correct total; a new payment attempt is created.

#### Cancel a Confirmed Booking and Record a Refund
```http
POST /api/v1/bookings/{bookingSysId}/cancel
Authorization: Bearer <customer-token>
```

Cancelling a confirmed booking sets its status to `CANCELLED`, marks its payment `REFUND`, and releases the seats. Only the booking owner can cancel it. The refund is recorded in the database; no money is transferred. Cancellation currently accepts confirmed bookings only.

Example response:
```json
{
  "bookingSysId": "123e4567-e89b-12d3-a456-426614174000",
  "bookingStatus": "CANCELLED",
  "paymentStatus": "REFUND",
  "message": "Booking cancelled and demo refund recorded. No money was transferred."
}
```

#### Get a Payment Record
```http
GET /api/v1/payments/{paymentSysId}
Authorization: Bearer <customer-token>
```

Returns the payment JSON shown above, including `amount`, `attemptedAmount`, `paymentTime`, `transactionId`, and `paymentStatus`. The API returns only payment records whose booking belongs to the authenticated customer.

## 🔐 Authentication and Authorization

- Registration (`POST /api/v1/users/user/register`) and login (`POST /api/v1/users/login`) are public.
- Login returns a signed JWT. Send it on protected requests as `Authorization: Bearer <token>`.
- Admin accounts are bootstrapped from `BMS_ADMIN_EMAIL` and `BMS_ADMIN_PASSWORD`; only `ADMIN` tokens can provision theater-owner accounts.
- Public registration creates customers only. Theater-owner accounts are created through `POST /api/v1/admin/theater-owners`.
- Tokens are stateless and expire after the configured duration (one hour by default).
- Theatre, hall, and show creation require the `THEATER_OWNER` role. The owner identity comes from the JWT, so clients do not send an owner UUID.
- GET browsing endpoints for theatres by city, shows, halls' shows, and seat status are public.
- `GET /api/v1/theatre/get/all` requires a `THEATER_OWNER` JWT and returns only that authenticated owner's theaters.
- Owner endpoints verify both the authenticated owner's identity and resource ownership: halls can only be added to that owner's theaters, and shows can only be created in halls under that owner's theaters.
- Booking and payment endpoints require a `CUSTOMER` JWT. Payment reads, payment attempts, and cancellations enforce booking ownership. Payment failures and refunds are simulated database status changes; no real transaction or refund is performed.

The payment API is a learning-project simulation. A caller-provided amount is compared with the server-calculated total, but there is no payment gateway to verify money received. Do not use this simulation to collect real payments.

## 🚀 Setup Instructions

### Prerequisites
- Java 17 or higher
- PostgreSQL
- Maven (or use the included Maven Wrapper)
- Postman (optional, for API testing)

### Database Setup

Create a PostgreSQL database (the current `application.properties` uses `data` by default):
```sql
CREATE DATABASE data;
```

Set `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password` in `src/main/resources/application.properties` for your local PostgreSQL instance. Do not commit personal database credentials.

Set `JWT_SECRET` to a private random secret for your environment. The fallback in `application.properties` is for development only. `JWT_EXPIRATION_MS` can set the token lifetime in milliseconds; it defaults to `3600000`. `SERVER_PORT` can override the default port `8080`.

### Build and Run

From the project root:

**Windows PowerShell:**
```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

**macOS/Linux:**
```bash
./mvnw clean package
./mvnw spring-boot:run
```

The application starts at `http://localhost:8080` unless `SERVER_PORT` is set.

## 🧪 Testing with Postman

### Import Collection
1. Import `BMS_Postman_Collection.json`.
2. Import `BMS_Postman_Environment.json`.
3. Set `base_url` to `http://localhost:8080`.
4. Configure `BMS_ADMIN_EMAIL` and `BMS_ADMIN_PASSWORD`, restart the app, and log in as the bootstrap admin.
5. Use the admin token to provision a theater owner, then log in as that owner and copy the response's `token`.
6. For theatre, hall, and show creation requests, open **Authorization**, select **Bearer Token**, and paste the owner token.

See [POSTMAN_GUIDE.md](POSTMAN_GUIDE.md) for collection-specific instructions. The collection may need to be updated with login and bearer-token steps if those requests are not yet included.

## ✨ Key Features

### 1. User Management
- Public customer registration and admin-provisioned theatre-owner accounts
- Password-based login
- JWT authentication and role-based access for owner actions

### 2. Theatre & Hall Management
- Theatre creation
- Hall configuration with row mappings, seat types, and prices

### 3. Show Scheduling
- Hall-specific show assignments
- Show timing and overlapping-time checks

### 4. Seat Status Browsing
- Public seat status lookup by show
- Seats show as `NOT_BOOKED`, `PENDING` (held while awaiting payment), or `BOOKED`
- Available-seat totals can be counted from the public seat-status response

### 5. Booking and Payment
- Customers request seats by type and quantity; the server selects available seats and calculates the price
- Booking creation holds seats and saves both booking and payment attempt as `PENDING`
- A separate customer-owned payment API simulates success and then confirms the booking
- The demo payment does not call PhonePe or move money

### 6. Audit Fields
- Shared record timestamps and identifiers through `GlobalRecord`

## 📊 Data Models

### Enums and How the APIs Use Them

Enums are values used by API requests, responses, or stored model fields; they are not standalone API operations. The following enum types and values exist in `src/main/java/com/bms/bms_core/enums`:

Persisted enum fields use their names in the database (for example, `REGULAR` or `PENDING`) rather than ordinal numbers. If your PostgreSQL database already contains numeric enum values, run `src/main/resources/db/manual/enum-name-migration-postgresql.sql` once before restarting the updated application. This converts existing values to their matching enum names.

#### UserType
- `CUSTOMER`
- `THEATER_OWNER`
- `ADMIN`

`CUSTOMER` is the only type accepted by public registration. `THEATER_OWNER` accounts are provisioned by an admin, and `ADMIN` is created by the startup bootstrap configuration.

#### SeatType
- `REGULAR`
- `PREMIUM`
- `VIP`
- `RECLINER`
- `SILVER`
- `GOLD`

Used in hall row/price configuration and in the customer's booking `seatCounts` request.

#### Currency
- `INR`
- `DOLLAR`
- `EURO`

Used as the configured currency on a hall row mapping. The application does not perform currency conversion.

#### SeatStatus
- `BOOKED`
- `NOT_BOOKED`
- `PENDING`

Returned by the public seat-status API. `PENDING` means the seat is held by a booking awaiting payment.

#### PaymentStatus
- `PENDING` - Payment is pending
- `SUCCESS` - Payment succeeded
- `FAILED` - The amount submitted to pay did not match the booking total
- `REFUND` - A confirmed booking was cancelled and a demo refund was recorded

Booking creation uses `PENDING`; the pay API sets `SUCCESS` only when the submitted amount exactly matches the booking total, otherwise it sets `FAILED`. Cancelling a confirmed booking sets the payment to `REFUND`. These are demo database updates, not gateway transactions.

#### BookingStatus
- `PENDING`
- `CONFIRMED`
- `CANCELLED`

Booking creation uses `PENDING`; an exact payment amount changes it to `CONFIRMED`. Cancelling a confirmed booking changes it to `CANCELLED` and records a simulated refund.

## 🔍 Common Queries

### Get Theatres by City
```sql
SELECT t.sys_id, t.theater_name, l.city, l.address_line1
FROM theaters t
JOIN locations l ON t.location_sys_id = l.sys_id
WHERE LOWER(l.city) = LOWER('Mumbai');
```

### Get Halls with Seat Configuration
```sql
SELECT h.hall_name, h.seat_count, hrm.seat_type, hrm.row_range, hrm.base_price
FROM halls h
JOIN "hall-row-mappings" hrm ON hrm.hall_sys_id = h.sys_id
WHERE h.theater_sys_id = 'theatre-uuid-here'
ORDER BY h.hall_name, hrm.seat_type;
```

### Get Shows with Theatre and Hall Details
```sql
SELECT s.show_name, s.show_start_time, s.show_end_time,
       h.hall_name, t.theater_name, l.city
FROM shows s
JOIN halls h ON s.hall_sys_id = h.sys_id
JOIN theaters t ON h.theater_sys_id = t.sys_id
JOIN locations l ON t.location_sys_id = l.sys_id
WHERE s.show_start_time > NOW()
ORDER BY s.show_start_time;
```

### Check Recorded Seats for a Show
```sql
SELECT bs.seat_id, s.show_name, s.show_start_time
FROM booked_seats bs
JOIN shows s ON bs.show_sys_id = s.sys_id
WHERE s.sys_id = 'show-uuid-here'
ORDER BY bs.seat_id;
```

## 📝 Notes

### Important Column Names
Due to JPA naming conventions:
- Foreign keys commonly use the `_sys_id` suffix (e.g., `owner_sys_id`, `hall_sys_id`).
- Timestamp fields use snake_case (e.g., `created_at`, `updated_at`).
- For native queries involving inherited entities, join through `global_records` when needed by the mapped schema.

## 🤝 Contributing

1. Follow the existing code structure.
2. Document implemented endpoints in this README.
3. Keep the Postman collection aligned with the API.

## 📄 License

This project is part of a learning exercise for building a movie theatre management and booking backend.
