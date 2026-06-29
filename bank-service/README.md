# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/4.1.0/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.1.0/gradle-plugin/packaging-oci-image.html)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

### Technology Stack & Dependencies
Language/Runtime: Java 17
Framework Core: Spring Boot 4.1.0
Gradle: 9.5.1

### 🚀 Running the Application
The main entry point to the backend service is the BankAccountHandlingApiApplication class. You can launch it directly from your IDE (e.g., IntelliJ IDEA) or run it from the command line using Maven:
`mvn spring-boot:run`

### 🔑 Automated Demo Data Seeding & Authentication
Once the application initializes, a specialized data bootstrap component will execute automatically:
.../bank-service/src/main/java/com/swedbank/account/application/infrastructure/bootstrap/DemoDataSeeder.java

1. What it does: This script pre-populates the in-memory database with realistic test user details, multiple bank accounts, and an extensive history of credit and debit transactions.

2. Developer JWT Token: For ease of testing, a valid test JWT token will be printed directly to the developer console/terminal output during this startup sequence.

💡 Important Demo Step: Copy the printed JWT token string from the backend service terminal and paste it into your Frontend (FE) Auth Interceptor to instantly bypass the authentication gate and browse the live dashboard views.

### 🧪 Running the Test Suite
The repository includes comprehensive integration test coverage to ensure data calculations and ledger states are perfectly intact.

Via IDE (IntelliJ IDEA): Navigate to the src/test/java directory, right-click the root test package, and select Run 'All Tests'.

Via Terminal CLI: Execute the standard Maven test lifecycle goal:
`mvn test`
