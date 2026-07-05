# 🔗 URL Shortener API

A RESTful URL Shortener built using **Java 17**, **Spring Boot 3**, **Spring Data JPA**, and **MySQL**.

The application allows users to generate short URLs, create custom aliases, redirect to original URLs, track click counts, and manage URL mappings through REST APIs.

---

# Features

- Shorten long URLs
- Custom short aliases
- Automatic unique short code generation
- Redirect using short URL
- Track click count
- View URL statistics
- Delete shortened URLs
- Global Exception Handling
- Input Validation
- Layered Architecture

---

# Tech Stack

| Technology | Used |
|------------|------|
| Java | 17 |
| Spring Boot | 3.x |
| Spring Web | REST APIs |
| Spring Data JPA | Database Operations |
| MySQL | Database |
| Maven | Dependency Management |

---

# Project Structure

```
src
├── main
│   ├── java
│   │   └── com
│   │       └── neeraj
│   │           └── urlshortener
│   │               ├── controller
│   │               ├── dto
│   │               ├── entity
│   │               ├── exception
│   │               ├── repository
│   │               ├── service
│   │               ├── util
│   │               └── UrlShortenerApplication.java
│   │
│   └── resources
│       └── application.properties
│
└── test
```

---

# Database Configuration

Create a MySQL database.

```sql
CREATE DATABASE urlshortener;
```

Update `application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/urlshortener
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

# Run the Project

## Clone Repository

```bash
git clone https://github.com/YOUR_USERNAME/url-shortener.git
```

Move into the project directory.

```bash
cd url-shortener
```

Run the application.

```bash
mvn spring-boot:run
```

Application starts on

```
http://localhost:8080
```

---

# API Endpoints

## 1. Create Short URL

**POST**

```
/shorten
```

### Request

```json
{
    "url":"https://www.google.com",
    "customAlias":"google"
}
```

### Response

```json
{
    "shortCode":"google",
    "shortUrl":"http://localhost:8080/google",
    "originalUrl":"https://www.google.com",
    "clickCount":0
}
```

---

## 2. Redirect

**GET**

```
/{shortCode}
```

Example

```
GET /google
```

Redirects to

```
https://www.google.com
```

---

## 3. URL Statistics

**GET**

```
/api/urls/{shortCode}/stats
```

Example

```
GET /api/urls/google/stats
```

Returns

- Original URL
- Short URL
- Click Count
- Created Time
- Last Accessed Time

---

## 4. Get All URLs

**GET**

```
/api/urls
```

Returns all shortened URLs.

---

## 5. Delete URL

**DELETE**

```
/api/urls/{shortCode}
```

Deletes the specified short URL.

---

# Error Responses

Example

```json
{
    "status":404,
    "message":"Short URL not found",
    "timestamp":"2026-07-05T11:30:00"
}
```

---

# Design Decisions

### Unique Short Code

Random 6-character alphanumeric codes are generated using `SecureRandom`. Before saving, the application checks if the generated code already exists.

---

### Custom Alias

Users can optionally provide their own alias.

If the alias already exists, the API returns **409 Conflict**.

---

### Click Tracking

Every successful redirect increases the click count and updates the last accessed timestamp.

---

### Validation

The application validates

- Valid URL format
- Alias length
- Duplicate aliases

---

### Exception Handling

A global exception handler returns consistent JSON responses for all API errors.

---

# Future Improvements

- User Authentication
- URL Expiration
- QR Code Generation
- Analytics Dashboard
- Rate Limiting
- Redis Caching

---

# Author

**Neeraj Kumar B**

Backend Developer | Java | Spring Boot | MySQL

GitHub:
https://github.com/YOUR_USERNAME