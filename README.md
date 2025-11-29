# Short URL Service

A command-line application for creating and managing short URLs with expiration times and usage limits. This service allows users to create shortened links that automatically expire after a specified time or after reaching a maximum usage count.

## Features

- **User Management**: Create users and authenticate using UUID
- **Short Link Creation**: Generate unique 8-character short IDs for long URLs
- **Expiration Control**: Set time-to-live (TTL) for links with automatic cleanup
- **Usage Limits**: Configure maximum number of times a link can be accessed
- **Link Management**: Update expiration times, usage limits, and delete links
- **Auto-cleanup**: Expired links are automatically removed
- **Browser Integration**: Opens shortened links directly in your default browser

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Installation

1. Clone the repository:
```bash
git clone https://github.com/RADICALOVICH/shortUrl.git
cd shortUrl
```

2. Build the project:
```bash
mvn clean compile
```

## Configuration

Default settings can be configured in `src/main/resources/application.properties`:

```properties
shorturl.defaultTtlHours=24
shorturl.defaultMaxUsages=5
```

- `shorturl.defaultTtlHours`: Default expiration time in hours (default: 24)
- `shorturl.defaultMaxUsages`: Default maximum usage count (default: 5)

## Usage

### Running the Application

```bash
mvn exec:java -Dexec.mainClass="vp.shorturl.app.Main"
```

Or compile and run:
```bash
mvn compile
java -cp target/classes vp.shorturl.app.Main
```

### Menu Options

1. **Create new user** - Generates a new user with a unique UUID
2. **Login with UUID** - Authenticate using your saved UUID
3. **Create short link** - Create a new shortened link (requires login)
4. **Open short link** - Access a shortened link by its ID
5. **List my links** - View all links created by the current user
6. **Update link max usages count** - Change the usage limit for a link
7. **Update link expiration time** - Extend or modify the expiration time
8. **Delete link** - Remove a link permanently
0. **Exit** - Quit the application

### Example Workflow

1. Create a new user and save the generated UUID
2. Login with your UUID
3. Create a short link by providing a valid HTTP/HTTPS URL
4. Use the generated short ID to access your link
5. Manage your links (update, delete) as needed

## Project Structure

```
src/
├── main/
│   ├── java/vp/shorturl/
│   │   ├── app/          # Application entry point and CLI
│   │   ├── config/       # Configuration management
│   │   ├── core/         # Domain models and business logic
│   │   └── infra/        # Infrastructure (in-memory repositories)
│   └── resources/        # Configuration files
└── test/                 # Unit and integration tests
```

## Architecture

The project follows a clean architecture pattern:

- **Core**: Domain entities (`ShortLink`, `User`) and business logic (`ShortLinkService`, `UserService`)
- **Infrastructure**: In-memory implementations of repositories
- **App**: Command-line interface and application configuration

## Testing

Run all tests:
```bash
mvn test
```

## Technical Details

- **Short ID Generation**: 8-character alphanumeric strings (a-z, A-Z, 0-9)
- **Storage**: In-memory (data is lost when the application exits)
- **Link Validation**: Only HTTP and HTTPS URLs are accepted
- **Expiration**: Links are checked and removed automatically on each menu interaction

## License

This project is provided as-is for educational purposes.

