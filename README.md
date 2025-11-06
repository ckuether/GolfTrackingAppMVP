# GolfTrackingAppMVP - Golf Tracking App

A modern Kotlin Multiplatform golf tracking application built with Compose Multiplatform, showcasing Clean Architecture, SOLID principles, and advanced Android development patterns. This project demonstrates professional-level multiplatform development skills with a focus on scalable architecture and best practices.

## 🏌️ Project Overview

GolfTrackingAppMVP is a comprehensive golf tracking application that allows golfers to:
- **Track rounds of golf** with real-time location services
- **Record scores** hole by hole with detailed statistics
- **Visualize shot tracking** on interactive Google Maps integration
- **Manage golf equipment** with club selection and shot analysis
- **View comprehensive scorecards** with historical round data

The app provides a seamless experience across Android and iOS platforms while maintaining a shared business logic layer and consistent UI/UX.

> **Demo Note**: This MVP currently uses mock data for demonstration purposes. The app is configured for **Broken Tee Golf Course in Englewood, CO** with simulated user authentication and course data. All data is stored locally using Room Database with no backend integration.

## 🏗️ Architecture Overview

### Clean Architecture Implementation

This project exemplifies **Clean Architecture** principles with clear separation of concerns:

**📱 Presentation Layer**
- Compose Multiplatform UI components
- ViewModels with StateFlow for reactive state management
- Platform-specific implementations using expect/actual pattern

**🔧 Domain Layer** 
- Use Cases encapsulating business logic
- Domain models and entities
- Repository interfaces and business rules

**💾 Data Layer**
- Room Database with KSP for cross-platform persistence
- Repository implementations
- Platform-specific data sources

### Modular Architecture

The project follows a **feature-based modular architecture** where each feature has separate domain and presentation modules:

#### Core Modules
- **`composeApp`** - Main application with navigation, app-level DI, and platform initialization
- **`shared`** - Core business logic, database entities, repositories, and foundational services  
- **`core-ui`** - Design system, shared UI components, theme definitions, and resources

#### Feature Modules (Domain + Presentation)
- **`location:location-domain`** - Location services business logic, use cases, and domain models
- **`location:location-presentation`** - Location UI components, ViewModels, and platform-specific implementations
- **`round-of-golf:round-of-golf-domain`** - Golf round tracking business logic and use cases
- **`round-of-golf:round-of-golf-presentation`** - Golf round UI components and ViewModels

```
Module Dependency Graph:
composeApp
├── shared (database, repositories, core domain)
├── core-ui (design system)  
├── location:location-domain
├── location:location-presentation (depends on location-domain)
├── round-of-golf:round-of-golf-domain
└── round-of-golf:round-of-golf-presentation (depends on round-of-golf-domain)
```

## 🛠️ Technical Stack

### Core Technologies
- **Kotlin 2.2.20** with **Compose Multiplatform 1.9.0**
- **Room Database 2.7.0** with **KSP 2.2.10-2.0.2** for cross-platform data persistence
- **Koin 4.1.1** for dependency injection across all modules
- **Material3** design system with custom theming
- **Google Maps Compose 4.4.1** with **Play Services Maps 18.2.0**
- **Coil 3.3.0** for image loading with SVG support
- **Jetpack Navigation Compose** for routing

### Platform Targets
- **Android**: Min SDK 26, Target/Compile SDK 36, Java 17
- **iOS**: Static frameworks generated for each feature module (iosArm64, iosSimulatorArm64)

## 🎯 SOLID Principles in Action

### Single Responsibility Principle (SRP)
- Each Use Case handles one specific business operation
- ViewModels manage only UI state for their respective screens
- Repository interfaces define single data access contracts

### Open/Closed Principle (OCP)
- Extensible through interfaces and abstract classes
- New features can be added without modifying existing code
- Plugin-based architecture allows for easy feature expansion

### Liskov Substitution Principle (LSP)
- Repository implementations are interchangeable
- Platform-specific implementations follow expect/actual contracts
- Use Cases can work with any repository implementation

### Interface Segregation Principle (ISP)
- Focused interfaces for specific responsibilities
- Clients depend only on methods they actually use
- Granular repository interfaces for different data operations

### Dependency Inversion Principle (DIP)
- High-level modules don't depend on low-level modules
- Abstractions don't depend on details
- Dependency injection ensures loose coupling

## 🏛️ Key Architectural Patterns

### **Use Case Pattern**
Domain logic encapsulated in Use Cases:
- `TrackSingleRoundEventUseCase`
- `SaveScoreCardUseCase` 
- `GetRoundEventsUseCase`

### **Repository Pattern**
Data access abstracted through repositories in shared module with clean interfaces

### **MVVM Pattern**
ViewModels coordinate between Use Cases and UI components using StateFlow for reactive programming

### **Expect/Actual Pattern**
Platform-specific implementations for location services and map integration

## 📊 Data Layer Architecture

### Room Database
- **Entities**: `ScoreCardEntity`, `RoundOfGolfEventEntity`
- **Database version 2** with proper migration handling
- **Schemas exported** to `shared/schemas/` directory
- **Repository pattern** implemented in shared module

### State Management
- **StateFlow** for reactive UI updates
- **Lifecycle-aware** Compose APIs with `collectAsStateWithLifecycle()`
- **Event-driven architecture** with proper event consumption patterns

## 🧪 Testing & Quality

> **Note**: This is currently a **Phase 1 MVP** focused on demonstrating architecture and core functionality. Testing implementation is planned for Phase 2 of development.

The application has been architected with testing in mind, following Clean Architecture principles that enable comprehensive testing strategies:

**Planned Testing Implementation (Phase 2):**
- Unit tests for domain logic and use cases
- Repository testing with test doubles
- ViewModel testing with coroutine test utilities
- UI testing with Compose testing framework
- Integration testing for cross-platform functionality

The modular architecture and dependency injection setup facilitate easy mocking and isolated testing of individual components.

## 🚀 Build and Run

### Android Application
```shell
# Build debug APK
./gradlew :composeApp:assembleDebug

# Install to connected device
./gradlew :composeApp:installDebug

# Run tests
./gradlew :composeApp:testDebugUnitTest
```

### iOS Application
- Open the `iosApp` directory in Xcode
- Build and run from Xcode, or use IDE run configurations
- iOS framework is built automatically when building the iOS app

### Development Commands
```shell
# Clean build
./gradlew clean

# Build all targets
./gradlew build

# Run all tests
./gradlew test
```

## 📱 Demo Videos

### Demo


https://github.com/user-attachments/assets/e525d2ec-53e0-4de4-b4fa-2343abcdd3eb


---

This project demonstrates advanced Kotlin Multiplatform development skills, Clean Architecture implementation, and modern Android development best practices. The codebase serves as a reference for scalable, maintainable, and testable multiplatform applications.
