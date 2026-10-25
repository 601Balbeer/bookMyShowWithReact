# BMS Core API: Postman Guide

This guide documents the HTTP endpoints currently implemented by this Spring Boot project. The API base URL used below is `http://localhost:8080`.

## Before you start

1. Start PostgreSQL and the Spring Boot application.
2. In Postman, create or select an environment with `base_url` set to `http://localhost:8080`.
3. The startup initializer creates the first admin account from `security.bootstrap-admin.email` and `security.bootstrap-admin.password` when configured and when that account does not already exist. Keep those credentials local and private. The password is hashed before it is stored in the database.
4. Send JSON requests with `Content-Type: application/json`.

The app is stateless and uses JWT. For protected requests, choose **Authorization → Bearer Token** in Postman and paste the token returned by login. Do not send owner IDs in the request body: the authenticated owner's identity comes from the JWT. Theater and hall IDs are still needed to identify which parent resource a hall or show belongs to.

## Authentication and roles

| Operation | Access |
|---|---|
| Customer registration and login | Public |
| Create a theater-owner account | Admin JWT |
| Create theater, hall, or show | Theater-owner JWT; ownership is checked by the API |
| Browse theaters, shows, halls, and seat status | Public GET endpoints, except owner's theater list |
| Create/cancel booking, pay, and read payment attempt | Customer JWT; booking/payment ownership is checked |

Login returns a JWT for the account's role (`CUSTOMER`, `THEATER_OWNER`, or `ADMIN`). Use the token returned for the role required by the next operation.

## Recommended request sequence

1. Register a customer, or log in to an existing account.
2. An admin logs in and creates a theater-owner account.
3. The theater owner logs in, creates a theater, then creates halls and shows under their own resources.
4. A customer browses shows and calls the seat-status endpoint.
5. The customer creates a booking by show ID and requested seat-type counts. The API allocates available seats, calculates the amount from the hall's configured seat prices, and creates both a `PENDING` booking and `PENDING` payment.
6. The customer calls the payment endpoint with the booking ID and amount. This is a local demo payment simulation: an exact amount succeeds and confirms the booking; a different amount fails and leaves the booking pending. No real payment provider is contacted.
7. A customer can cancel their own confirmed booking. Cancellation records a demo refund and marks the booking `CANCELLED`.

## Endpoints

### 1. Register a customer

`POST {{base_url}}/api/v1/users/user/register` — public.

```json
{
  "fullName": "Simran Kaur",
  "email": "simran@example.com",
  "password": "UseAUniquePassword123!",
  "contactNumber": "+919876543210",
  "dateOfBirth": "2000-05-20",
  "userType": "CUSTOMER"
}
```

`userType` must be `CUSTOMER` for public registration. A successful response is `201 Created` with a safe user profile; it does not contain the password.

### 2. Log in and get a JWT

`POST {{base_url}}/api/v1/users/login` — public.

```json
{
  "email": "simran@example.com",
  "password": "UseAUniquePassword123!"
}
```

Copy the `token` from the response. In Postman, set **Authorization → Bearer Token** for subsequent requests. Login returns the profile matching the account type along with the token.

### 3. Create a theater-owner account

`POST {{base_url}}/api/v1/admin/theater-owners` — requires an `ADMIN` JWT.

```json
{
  "fullName": "Rajesh Kumar",
  "email": "owner@example.com",
  "password": "UseAnotherUniquePassword123!",
  "contactNumber": "+919876543211",
  "businessName": "PVR Cinemas",
  "panCard": "ABCDE1234F",
  "businessRegistrationId": "BIZ12345",
  "gstNumber": "22AAAAA1234A1Z5",
  "userType": "THEATER_OWNER"
}
```

This endpoint is admin-only. Sensitive business identifiers are accepted for account creation but are not included in the user response DTO. Log in using the new owner's email and password to get a `THEATER_OWNER` JWT.

### 4. Create a theater

`POST {{base_url}}/api/v1/theatre/create` — requires a `THEATER_OWNER` JWT. The owner is derived from the token; do not pass an owner ID.

```json
{
  "theaterName": "PVR Phoenix Mall",
  "location": {
    "addressLine1": "Phoenix Marketcity Mall",
    "addressLine2": "LBS Marg, Kurla West",
    "addressLine3": "Near Kurla Station",
    "country": "India",
    "state": "Maharashtra",
    "city": "Mumbai",
    "zipCode": 400070,
    "lat": 19,
    "log": 72
  }
}
```

Save the returned theater's `sysId`; pass it as `theaterId` when registering a hall.

### 5. Browse theaters

`GET {{base_url}}/api/v1/theatre/get?city=Mumbai` — public. Returns theaters in the requested city.

`GET {{base_url}}/api/v1/theatre/get/all` — requires a `THEATER_OWNER` JWT. Returns only the authenticated owner's theaters.

### 6. Register a hall

`POST {{base_url}}/api/v1/hall/register?theaterId={{theater_sys_id}}` — requires a `THEATER_OWNER` JWT. The API verifies that the theater belongs to that owner.

```json
{
  "hallName": "Audi 1",
  "seatCount": 150,
  "rowMappingList": [
    {
      "seatType": "REGULAR",
      "rowRange": "A-F",
      "seatCount": "10",
      "basePrice": 150.0,
      "basePriceCurrency": "INR"
    },
    {
      "seatType": "PREMIUM",
      "rowRange": "G-J",
      "seatCount": "10",
      "basePrice": 250.0,
      "basePriceCurrency": "INR"
    },
    {
      "seatType": "RECLINER",
      "rowRange": "K-L",
      "seatCount": "10",
      "basePrice": 400.0,
      "basePriceCurrency": "INR"
    }
  ]
}
```

Each `rowRange` is inclusive; `seatCount` in a row mapping is the number of seats per row. `seatCount` on the hall is its total capacity.

### 7. Create a show

`POST {{base_url}}/api/v1/show/create?hallId={{hall_sys_id}}` — requires a `THEATER_OWNER` JWT. The API verifies that the hall belongs to the authenticated owner's theater.

```json
{
  "showName": "Example Movie",
  "shortDescription": "An example screening",
  "artists": "Actor One, Actor Two",
  "showStartTime": "2026-10-20T18:00:00",
  "showEndTime": "2026-10-20T20:30:00"
}
```

Use ISO local date-time format `YYYY-MM-DDTHH:mm:ss`; end time must follow start time and must not overlap another show in the same hall.

### 8. Browse shows and seat availability

`GET {{base_url}}/api/v1/show/get/{{theater_sys_id}}` — public; lists shows in the theater.

`GET {{base_url}}/api/v1/hall/shows/all?hallId={{hall_sys_id}}` — public; lists shows in the hall.

`GET {{base_url}}/api/v1/show/seat-status/{{show_sys_id}}` — public; returns each seat's number, seat type, and status (`NOT_BOOKED`, `PENDING`, or `BOOKED`), with hall/theater/show IDs.

### 9. Create a pending booking

`POST {{base_url}}/api/v1/bookings` — requires a `CUSTOMER` JWT.

```json
{
  "showSysId": "{{show_sys_id}}",
  "seatCounts": [
    { "seatType": "REGULAR", "count": 2 },
    { "seatType": "PREMIUM", "count": 1 }
  ]
}
```

The API picks available seats of the requested types and calculates the total using the configured price for each seat type. The response contains `bookingSysId`, allocated seats, amount, a `PENDING` booking status, and a pending payment attempt. Save `bookingSysId` and the returned payment `sysId` for the next steps. The booking customer's identity comes from the JWT.

### 10. Pay for a booking (simulated)

`POST {{base_url}}/api/v1/payments/booking/{{booking_sys_id}}/pay` — requires the same owning customer's `CUSTOMER` JWT.

```json
{
  "amount": 550.0
}
```

Replace `550.0` with the exact amount returned by booking. The project currently simulates payment locally:

- Exact amount: payment becomes `SUCCESS`, booking becomes `CONFIRMED`.
- Different amount: payment attempt becomes `FAILED`, booking stays `PENDING`; retry with the exact amount.

No real payment gateway or money transfer is involved. A customer can pay only their own booking.

### 11. Read a payment attempt

`GET {{base_url}}/api/v1/payments/{{payment_sys_id}}` — requires the owning customer's `CUSTOMER` JWT. Returns payment status and amount; a customer cannot read another customer's payment.

### 12. Cancel a confirmed booking

`POST {{base_url}}/api/v1/bookings/{{booking_sys_id}}/cancel` — requires the owning customer's `CUSTOMER` JWT; only confirmed, successfully paid bookings can be cancelled.

No request body is required. The response reports booking status `CANCELLED` and payment status `REFUND`. The refund is simulated and recorded in the database; the booking's seats are released for availability.

## Enum values used by the API

- `UserType`: `CUSTOMER`, `THEATER_OWNER`, `ADMIN`
- `SeatType`: `REGULAR`, `PREMIUM`, `VIP`, `RECLINER`, `SILVER`, `GOLD` (a type must also have a matching row mapping and configured price in the hall)
- `Currency`: `INR`, `DOLLAR`, `EURO`
- `BookingStatus`: `PENDING`, `CONFIRMED`, `CANCELLED`
- `PaymentStatus`: `PENDING`, `SUCCESS`, `FAILED`, `REFUND`
- `SeatStatus`: `NOT_BOOKED`, `PENDING`, `BOOKED`

Enums are serialized using their names. Use the exact uppercase values shown.

## Common response and error codes

- `200 OK`: successful login, lookup, payment, or cancellation.
- `201 Created`: customer/owner registration, theater/hall/show creation, or booking creation.
- `400 Bad Request`: malformed JSON, invalid enum, missing fields, or invalid values.
- `401 Unauthorized`: missing, expired, or invalid JWT; or invalid login credentials.
- `403 Forbidden`: role mismatch or attempt to manage another owner's resource or another customer's booking/payment.
- `404 Not Found`: referenced resource does not exist.
- `409 Conflict`: requested seats unavailable, overlapping show, invalid booking/payment state, or duplicate account.

## Troubleshooting

- **No token in the response:** send the login request to `/api/v1/users/login`; registration does not log in automatically. Check Postman's response panel and confirm the request returned `200`.
- **401 on a protected request:** set Authorization to **Bearer Token** and paste the complete `token` value from login. Log in again if it has expired.
- **403 Forbidden:** use the correct role's token. Owners can manage only their own theaters, halls, and shows; customers can access only their own bookings and payments.
- **400 Bad Request:** check JSON syntax, required fields, enum spelling, numeric count/amount values, and date-time format.
- **409 Conflict on booking:** the show may not have enough free seats for the requested seat types.
- **PostgreSQL says “too many clients already”:** stop duplicate app runs and disconnect unused database clients, then reconnect. Review connection pooling if it continues.

