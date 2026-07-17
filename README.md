# Exercise Organizer API

Backend REST API for the **Exercise Organizer Android application**.

The API provides structured exercise and category data for the mobile application, including sports categories, category hierarchy, and exercise information.

Built with **Kotlin, Spring Boot, PostgreSQL and JPA**

---

## API Endpoints

**http://localhost:8080/swagger-ui/index.html** - Swagger UI <br>
**http://localhost:8080/v3/api-docs** - OpenAPI specification

**GET /sports** - Returns top-level sport categories. <br>
Example response:
```
[
{
"categoryId": 1,
"name": "Gym",
"iconUrl": "gym.png",
"parentCategoryId": null
}
]
```

**GET /categories** - Returns child(non-sport) categories. <br>
Example response:
```
[
  {
    "categoryId": 12,
    "name": "Chest",
    "iconUrl": null,
    "parentCategoryId": 1
  }
]
```

**GET /exercises** - Returns available exercises. <br>
Example response:
```
[
  {
    "exerciseId": 1,
    "name": "Push Up",
    "instruction": "1. Place hands shoulder width apart 2. Lower you body...",
    "imageUrl": "pushup.png",
    "categoryId": 12
  }
]
```

---


## Tech Stack

### Backend

| Technology | Purpose |
|---|---|
| Kotlin | Programming language |
| Spring Boot | Backend framework |
| Spring Web | REST API |
| Spring Data JPA | Database access |
| Hibernate | ORM |
| PostgreSQL | Database |
| Gradle | Build system |
| SpringDoc OpenAPI | API documentation |

---

# Architecture
Controller > Service > Repository > Database

**Controller Layer**
- HTTP endpoints
- Request handling
- Returning API responses

Available endpoints: <br>
GET /sports <br>
GET /categories <br>
GET /exercises

**Service Layer** <br>
Contains business logic and converts database entities into API DTOs.

**Repository Layer** <br>
Database access is handled using Spring Data JPA.
