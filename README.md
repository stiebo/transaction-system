# Transaction System

The Transaction System is a web application built with Vaadin and Spring Boot, designed to manage and process financial
transactions efficiently.

## Features

- **Vaadin Frontend with Spring Boot Backend**
- **User Authentication**: Secure login system with role-based access control.
- **Transaction Management**: Create, view, and manage transactions seamlessly.
- **Responsive UI**: Intuitive and responsive user interface built with Vaadin.

## Background

Frauds carry significant financial costs and risks for all stakeholders. So, the presence of an anti-fraud system is a
necessity for any serious e-commerce platform.

The Anti-Fraud System project provides a comprehensive framework for detecting and preventing fraudulent financial
transactions. By integrating role-based access control, RESTful APIs, heuristic validation rules, and adaptive feedback
mechanisms, the system offers a robust solution for financial institutions to safeguard against fraud. Leveraging Spring
Boot and its associated technologies, the project demonstrates best practices in building secure, scalable, and
maintainable applications in the financial sector.

Check out my Github profile: [https://github.com/stiebo](https://github.com/stiebo)

Link to the learning project: [https://hyperskill.org/projects/232](https://hyperskill.org/projects/232)

Check out my learning profile: [https://hyperskill.org/profile/500961738](https://hyperskill.org/profile/500961738)

## Key Components of the Anti-Fraud System

1. **Role-Based Access Control**:

- **User Roles**: The system defines specific roles, including **Administrator**, **Merchant**, and **Support**.

- **Permissions**:

    - **Administrator**: Manages user roles and access rights.

    - **Merchant**: Submits transactions for validation.

    - **Support**: Reviews and provides feedback on transactions.

- This structure ensures that users have access only to functionalities pertinent to their roles, enhancing security and
  operational efficiency.

## Running the Application

Execute the following command:

```bash
./mvnw clean spring-boot:run
```

The application will start, and you can access it at `http://localhost:8080`.

### Using Docker

If you prefer to run the application in a Docker container:

1. **Build the Docker Image**:

   ```bash
   docker build -t transaction-system:latest .
   ```

2. **Run the Docker Container**:

   ```bash
   docker run -p 8080:8080 transaction-system:latest
   ```

The application will be available at `http://localhost:8080`.
