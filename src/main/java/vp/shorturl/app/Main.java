package vp.shorturl.app;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;


import vp.shorturl.core.*;
import vp.shorturl.infra.InMemoryShortLinkRepository;
import vp.shorturl.infra.InMemoryUserRepository;


import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        InMemoryShortLinkRepository shortLinkRepository = new InMemoryShortLinkRepository();

        UserService userService = new UserService(userRepository);
        ShortLinkService shortLinkService = new ShortLinkService(shortLinkRepository);

        Scanner scanner = new Scanner(System.in);
        User currentUser = null;

        while (true){
            printMenu();
            String option = scanner.nextLine();
            switch(option){
                case "1" -> currentUser = handleCreateUser(userService);
                case "2" -> currentUser = handleLogin(userService, scanner);
                case "3" -> handleCreateShortLink(shortLinkService, currentUser, scanner);
                case "4" -> handleOpenShortLink(shortLinkService, scanner);
                case "5" -> handleListMyLinks(shortLinkService,currentUser,scanner);
                case "0" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Unknown command. Please try again.");
            }

        }

    }

    private static void printMenu() {
        System.out.println("==== Short URL Service ====");
        System.out.println("1. Create new user");
        System.out.println("2. Login with UUID");
        System.out.println("3. Create short link");
        System.out.println("4. Open short link");
        System.out.println("5. List my links");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
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

    public static void handleCreateShortLink(ShortLinkService shortLinkService, User currentUser, Scanner scanner) {
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

        System.out.println("Enter max usages (integer > 0):");
        String maxUsagesInput = scanner.nextLine();
        int maxUsages;
        try {
            maxUsages = Integer.parseInt(maxUsagesInput);
            if (maxUsages <= 0) {
                System.out.println("Max usages must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number for max usages.");
            return;
        }

        System.out.println("Enter expiration time in hours (integer > 0):");
        String expirationInput = scanner.nextLine();
        int expirationHours;
        try {
            expirationHours = Integer.parseInt(expirationInput);
            if (expirationHours <= 0) {
                System.out.println("Expiration hours must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number for expiration hours.");
            return;
        }

        ShortLink link = shortLinkService.createShortLink(
                currentUser.getUuid(),
                url,
                maxUsages,
                expirationHours
        );

        System.out.println("Short link created. ID: " + link.getShortId());
    }

    private static void handleOpenShortLink(ShortLinkService shortLinkService, Scanner scanner) {
        System.out.println("Enter shorl link ID: ");
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

    private static void handleListMyLinks(ShortLinkService shortLinkService, User currentUser, Scanner scanner){
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