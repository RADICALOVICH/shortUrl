package vp.shorturl.app;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


import vp.shorturl.config.AppConfig;
import vp.shorturl.core.*;
import vp.shorturl.infra.InMemoryShortLinkRepository;
import vp.shorturl.infra.InMemoryUserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        AppConfig config = new AppConfig();

        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        InMemoryShortLinkRepository shortLinkRepository = new InMemoryShortLinkRepository();

        UserService userService = new UserService(userRepository);
        ShortLinkService shortLinkService = new ShortLinkService(shortLinkRepository);

        Scanner scanner = new Scanner(System.in);
        User currentUser = null;

        while (true){
            shortLinkService.removeExpiredLinks();
            printMenu();
            String option = scanner.nextLine();
            switch(option){
                case "1" -> currentUser = handleCreateUser(userService);
                case "2" -> currentUser = handleLogin(userService, scanner);
                case "3" -> handleCreateShortLink(shortLinkService,config.getDefaultMaxUsages(), config.getDefaultTtlHours(), currentUser, scanner);
                case "4" -> handleOpenShortLink(shortLinkService, scanner);
                case "5" -> handleListMyLinks(shortLinkService, currentUser);
                case "6" -> handleUpdateMaxUsages(currentUser,shortLinkService, scanner);
                case "7" -> handleUpdateExpiration(currentUser, shortLinkService, scanner);
                case "8" -> handleDeleteShortLink(currentUser, shortLinkService, scanner);
                case "9", "h", "help" -> printHelp(scanner);
                case "0" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Unknown command. Please try again.");
            }

        }

    }

    public static void handleUpdateExpiration(User currentUser, ShortLinkService shortLinkService, Scanner scanner) {
        if (currentUser == null) {
            System.out.println("You must be logged in to update link expiration.");
            return;
        }

        System.out.print("Enter short link ID: ");
        String shortId = scanner.nextLine().trim();

        System.out.print("Enter new expiration in hours from now (integer > 0): ");
        int hours;
        try {
            hours = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format.");
            return;
        }

        if (hours <= 0) {
            System.out.println("Expiration hours must be greater than 0.");
            return;
        }

        LocalDateTime newExpiration = LocalDateTime.now().plusHours(hours);

        try {
            shortLinkService.updateExpiration(currentUser.getUuid(), shortId, newExpiration);
            System.out.println("Expiration updated successfully.");
        } catch (IllegalStateException e) {
            System.out.println("Failed to update expiration: " + e.getMessage());
        }
    }


    private static void printMenu() {
        System.out.println("==== Short URL Service ====");
        System.out.println("1. Create new user");
        System.out.println("2. Login with UUID");
        System.out.println("3. Create short link");
        System.out.println("4. Open short link");
        System.out.println("5. List my links");
        System.out.println("6. Update link max usages count");
        System.out.println("7. Update link expiration time");
        System.out.println("8. Delete link");
        System.out.println("9. Help / Usage examples");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private static void printHelp(Scanner scanner) {
        System.out.println("\n=== Help & Usage Examples ===");
        System.out.println();
        System.out.println("QUICK START:");
        System.out.println("1. Create a new user (option 1) - you'll get a UUID, save it!");
        System.out.println("2. Login with your UUID (option 2)");
        System.out.println("3. Create a short link (option 3)");
        System.out.println("4. Share the short ID or open it (option 4)");
        System.out.println();
        System.out.println("DETAILED EXAMPLES:");
        System.out.println();
        System.out.println("Creating a user:");
        System.out.println("  > Choose option 1");
        System.out.println("  > Save the displayed UUID (e.g., 550e8400-e29b-41d4-a716-446655440000)");
        System.out.println();
        System.out.println("Logging in:");
        System.out.println("  > Choose option 2");
        System.out.println("  > Enter your UUID: 550e8400-e29b-41d4-a716-446655440000");
        System.out.println();
        System.out.println("Creating a short link:");
        System.out.println("  > Choose option 3 (must be logged in)");
        System.out.println("  > Enter URL: https://example.com");
        System.out.println("  > You'll get a short ID (e.g., aB3dEf9K)");
        AppConfig config = new AppConfig();
        System.out.println("  > Default: " + config.getDefaultMaxUsages() + " max usages, " +
            config.getDefaultTtlHours() + " hours TTL");
        System.out.println();
        System.out.println("Opening a link:");
        System.out.println("  > Choose option 4");
        System.out.println("  > Enter short ID: aB3dEf9K");
        System.out.println("  > Link opens in your browser (if supported)");
        System.out.println();
        System.out.println("Updating max usages:");
        System.out.println("  > Choose option 6 (must be logged in)");
        System.out.println("  > Enter short ID: aB3dEf9K");
        System.out.println("  > Enter new max usages: 10");
        System.out.println();
        System.out.println("Updating expiration:");
        System.out.println("  > Choose option 7 (must be logged in)");
        System.out.println("  > Enter short ID: aB3dEf9K");
        System.out.println("  > Enter hours from now: 48");
        System.out.println();
        System.out.println("IMPORTANT NOTES:");
        System.out.println("  • Links expire after TTL (time-to-live)");
        System.out.println("  • Links stop working after reaching max usages");
        System.out.println("  • Only the link owner can update/delete their links");
        System.out.println("  • Expired links are automatically removed");
        System.out.println("  • URLs must start with http:// or https://");
        System.out.println();
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }

    private static User handleCreateUser(UserService userService) {
        User user = userService.createUser();
        System.out.println("New user created. Your UUID is: " + user.getUuid());
        System.out.println("Please save this UUID. You will need it to login later.");
        return user;
    }

    private static User handleLogin(UserService userService, Scanner scanner) {
        System.out.print("Enter your UUID: ");
        String input = scanner.nextLine();

        UUID uuid;
        try {
            uuid = UUID.fromString(input);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format. Please try again.");
            return null;
        }

        Optional<User> user = userService.findById(uuid);

        if (user.isPresent()) {
            System.out.println("Logged in as user: " + user.get().getUuid());
            return user.get();
        } else {
            System.out.println("User not found.");
            return null;
        }
    }

    public static void handleCreateShortLink(ShortLinkService shortLinkService, int defaultMaxUsages, int defaultTTLHours, User currentUser, Scanner scanner) {
        if (currentUser == null) {
            System.out.println("You must be logged in to create a short link.");
            return;
        }

        System.out.println("Enter original URL:");
        String url = scanner.nextLine();

        if (!isValidUrl(url)) {
            System.out.println("Invalid URL. Please enter a valid http/https URL.");
            return;
        }

        ShortLink link = shortLinkService.createShortLink(
                currentUser.getUuid(),
                url,
                defaultMaxUsages,
                defaultTTLHours
        );

        System.out.println("Short link created. ID: " + link.getShortId());
    }

    private static void handleOpenShortLink(ShortLinkService shortLinkService, Scanner scanner) {
        System.out.println("Enter short link ID: ");
        String shortId = scanner.nextLine().trim();
        try {
            String url = shortLinkService.openShortLink(shortId);
            System.out.println("Opening: " + url);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                System.out.println("Desktop browsing is not supported on this system.");
            }
        } catch (IllegalStateException e) {
            System.out.println("Cannot open link: " + e.getMessage());
        } catch (IOException | URISyntaxException e) {
            System.out.println("Failed to open in browser: " + e.getMessage());
        }
    }

    private static void handleListMyLinks(ShortLinkService shortLinkService, User currentUser){
        if (currentUser == null) {
            System.out.println("You must be logged in to view your links");
            return;
        }

        var links = shortLinkService.getLinksByOwner(currentUser.getUuid());

        if (links.isEmpty()){
            System.out.println("You don't have any links yet.");
            return;
        }
        System.out.println("Your links: ");
        for (ShortLink link : links) {
            System.out.println("- " + link.getShortId());
        }

    }

    public static void handleUpdateMaxUsages(User currentUser, ShortLinkService shortLinkService, Scanner scanner) {
        if (currentUser == null) {
            System.out.println("You must be logged in to update max usages of the link.");
            return;
        }

        System.out.print("Enter short link ID: ");
        String shortId = scanner.nextLine().trim();

        System.out.print("Enter new max usages (integer > 0): ");
        int maxUsages;
        try {
            maxUsages = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format.");
            return;
        }

        if (maxUsages <= 0) {
            System.out.println("Max usages must be greater than 0.");
            return;
        }

        try {
            shortLinkService.updateMaxUsages(currentUser.getUuid(), shortId, maxUsages);
            System.out.println("Max usages for the link updated successfully.");
        } catch (IllegalStateException e) {
            System.out.println("Failed to update max usages: " + e.getMessage());
        }
    }

    public static void handleDeleteShortLink(User currentUser, ShortLinkService shortLinkService, Scanner scanner) {
        if (currentUser == null) {
            System.out.println("You must be logged in to delete a link.");
            return;
        }

        System.out.print("Enter short link ID: ");
        String shortId = scanner.nextLine().trim();

        if (shortId.isEmpty()) {
            System.out.println("Short link ID cannot be empty.");
            return;
        }

        try {
            shortLinkService.deleteShortLink(currentUser.getUuid(), shortId);
            System.out.println("Link deleted successfully.");
        } catch (IllegalStateException e) {
            System.out.println("Failed to delete link: " + e.getMessage());
        }
    }



    private static boolean isValidUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            return scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"));
        } catch (URISyntaxException e) {
            return false;
        }
    }

}
