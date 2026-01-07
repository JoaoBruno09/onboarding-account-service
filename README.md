## 🧩 Account Service

The Account Service is a core backend microservice responsible for managing user accounts within a distributed, service-oriented system. It encapsulates all account-related concerns including user registration and account lifecycle operations. Designed with scalability, security, and loose coupling in mind, the service acts as a foundational building block that other services rely on for identity and account data.

The Account Service follows a stateless, RESTful architecture and is intended to operate independently while integrating seamlessly into a broader microservices ecosystem.

## 🔍 Key Features

- User account creation and lifecycle management
- Secure credential storage and account authentication support
- Account profile retrieval and updates
- Account status management (active, suspended, deleted)
- Clear separation of concerns from authentication and authorization consumers

## 🔗 API Endpoints
- POST /accounts – Create a new user account
- PUT /accounts/{accountNumber} - Select type account
- PUT /accounts/{accountNumber}/card - Select account card
- DELETE /accounts/{accountNumber}/card/{cardNumber} - Delete account card
- PUT /accounts/{accountNumber}/netbanco - Select online banking
- PUT /accounts/{accountNumber}/moveNextPhase - Move to next phase

## 👨‍💻 Technologies

<div style="display: inline_block"><br>
<img align="center" alt="Java" height="40" width="40" src="https://github.com/devicons/devicon/blob/master/icons/java/java-original.svg">
<img align="center" alt="Spring" height="40" width="40" src="https://github.com/devicons/devicon/blob/master/icons/spring/spring-original.svg">
<img align="center" alt="Docker" height="40" width="40" src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/docker/docker-original.svg" />
<img align="center" alt="PostgreSQL" height="40" width="40" src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/postgresql/postgresql-original.svg" />
</div>

## 📂 Repository Structure

The repository is organized as follows:

- `boot`: Module that includes the application startup.
- `services/src/main/java/com/bank/onboarding/accountservice/services`: Contains services and their implementation.
- `web/src/main/java/com/bank/onboarding/accountservice/controllers`: Contains all the controllers of the application.

## 📋 Prerequisites

- Java 17+
- Maven
- Docker
- PostgreSQL database instance (local or containerized)

## 🌟 Additional Resources

- [Master's dissertation](http://hdl.handle.net/10400.22/26586)
