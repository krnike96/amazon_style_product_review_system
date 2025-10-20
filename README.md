# 📦 Product Review System: Amazon-Style Product Review System

## 🌟 Overview

The Product Review System is a robust, full-stack web application designed to replicate the essential feedback mechanisms of major e-commerce platforms like Amazon. Built on the Spring Boot framework, it provides users with a secure way to browse products, submit detailed reviews with star ratings and media, and engage with community feedback via voting and reporting.

The application includes a comprehensive, role-based security layer and an administrative dashboard for efficient content moderation.

## 💻 Tech Stack

This project leverages a modern, stable set of technologies for a secure and scalable solution.

| Category | Technology | Description |
|----------|------------|-------------|
| Backend Core | Spring Boot | The primary framework for building the application, REST APIs, and business logic. |
| Security | Spring Security | Handles authentication (BCrypt) and role-based authorization (ROLE_USER, ROLE_ADMIN). |
| Persistence | Spring Data JPA / Hibernate | ORM layer managing object-relational mapping and data access. |
| Database | MySQL | A robust, production-ready relational database for persistent data storage. |
| Frontend Rendering | Thymeleaf | Server-side templating engine for generating dynamic HTML views. |
| Styling | Tailwind CSS | Utility-first CSS framework ensuring a modern, responsive, and mobile-friendly UI. |

## ✨ Key Features

### 👤 User & Product Flow

| Feature | Description | Access |
|---------|-------------|--------|
| Product Catalog | Browse a catalog of products with aggregated star ratings. | Public / Authenticated |
| Secure Authentication | User registration and login managed by Spring Security. | Public |
| Review Submission | Users submit star ratings (1-5), detailed comments, and can optionally upload an image. | Authenticated |
| Community Voting | Users can mark reviews as "Helpful" (Up-vote/Down-vote logic implemented). | Authenticated |
| Review Reporting | Users can report inappropriate content with predefined reasons (Spam, Offensive, etc.). | Authenticated |

### 🛡️ Administration & Moderation

Admin access is secured and restricted to users with the ROLE_ADMIN authority.

| Dashboard | Path | Core Functionality |
|-----------|------|-------------------|
| Review Moderation | /admin/reviews | View and manage all unapproved reviews. Actions: Approve (publish) or Reject (delete). |
| Report Management | /admin/reports | Handle pending reports submitted by users. Actions: Keep Review (dismiss report) or Delete Review (process report and delete the review). |
| Product Management | /admin/products | CRUD operations (Create, Read, Update, Delete) for all products. |

## 🛠️ Setup and Installation

### Prerequisites

- Java Development Kit (JDK) 17+(Build on JDK21)
- Apache Maven (Used for build and dependency management)
- MySQL Server (Version 8.0+ recommended)

### 1. Clone the Repository

Clone the project from your GitHub repository:

```bash
git clone https://github.com/krnike96/amazon_style_product_review_system.git
cd amazon_style_product_review_system
```

### 2. Configure MySQL Database

Create a new database in your MySQL server (e.g., `product_review_db`).

Update the `src/main/resources/application.properties` file with your database credentials:

```properties
# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/product_review_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password

# Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Local Directory for File Uploads (used for review/product images)
upload.dir=./uploads
```

### 3. Run the Application

Start the Spring Boot application using the Maven wrapper:

```bash
mvn spring-boot:run
```

The application will launch on the default port 8080.

## 🚀 Getting Started and Key Routes

### Initial Access

Open your browser to the application's login page:
```
http://localhost:8080/login
```

### Default User Accounts

For immediate testing, you can use the `/register` page to create accounts, or pre-configure them in your database.

| Username | Role | Initial Use |
|----------|------|-------------|
| admin | ROLE_ADMIN | Moderation and Product Management access. |
| testuser | ROLE_USER | Review submission, voting, and reporting. |

### Core Application Routes

| Path | Description | Access |
|------|-------------|--------|
| /login | User authentication page. | Public |
| /register | New user account creation. | Public |
| /products | Main product list and catalog. | Public (Read) / Authenticated |
| /products/{id} | Product detail page with all submitted reviews. | Public (Read) / Authenticated |
| /admin/reviews | Review Approval/Rejection dashboard. | ROLE_ADMIN |
| /admin/reports | Management of pending user reports. | ROLE_ADMIN |