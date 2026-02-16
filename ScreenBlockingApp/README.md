# Screen-Blocking App: Multi-Platform Solution

This project provides the foundation for building a screen-blocking application for both iOS (Apple) and Android platforms, supported by a central Java Spring Boot backend.

## Architecture

The solution consists of three main components:

1.  **Backend API (Spring Boot + MongoDB)**:
    *   Manages user configurations and blocked app lists.
    *   Stores and aggregates screen time usage statistics.
    *   Provides RESTful endpoints for mobile clients to sync data.
    *   Location: `./backend`

2.  **iOS Client (Swift)**:
    *   Uses Apple's **Screen Time API** (FamilyControls, ManagedSettings, DeviceActivity).
    *   Implements native app shielding (blocking) without requiring background processes.
    *   Location: `./mobile/ios`

3.  **Android Client (Kotlin)**:
    *   Uses **Accessibility Services** for real-time app blocking.
    *   Uses **UsageStatsManager** for monitoring screen time.
    *   Location: `./mobile/android`

## Getting Started

### Backend Setup

1.  Navigate to the `backend` directory.
2.  Ensure you have Java 17 and Maven installed.
3.  Configure your MongoDB connection in `src/main/resources/application.properties`.
4.  Run the application:
    ```bash
    mvn spring-boot:run
    ```

### Mobile Integration

Refer to the platform-specific guides in the `mobile` directory:
- [iOS Integration Guide](./mobile/ios/IntegrationGuide.md)
- [Android Integration Guide](./mobile/android/IntegrationGuide.md)

## API Endpoints

- `GET /api/blocked-apps/{userId}`: Retrieve the list of blocked apps for a user.
- `POST /api/blocked-apps`: Add a new app to the block list.
- `PUT /api/blocked-apps/{id}`: Update an existing block rule.
- `DELETE /api/blocked-apps/{id}`: Remove an app from the block list.

## Security Considerations

*   **OAuth2/JWT**: Implement secure authentication for mobile clients.
*   **Data Privacy**: Screen time data is highly sensitive; ensure all data is encrypted and handled according to GDPR/CCPA regulations.
*   **Platform Permissions**: Both iOS and Android require explicit user consent and specific entitlements (especially iOS) to access Screen Time data.
