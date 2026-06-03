package itss.group11.frontend.auth;

public final class LoginSession {

    private static String username;
    private static UserRole role;

    private LoginSession() {
    }

    public static void login(String loggedInUsername, UserRole loggedInRole) {
        username = loggedInUsername;
        role = loggedInRole;
    }

    public static void clear() {
        username = null;
        role = null;
    }

    public static boolean isAuthenticated() {
        return role != null;
    }

    public static String getUsername() {
        return username;
    }

    public static UserRole getRole() {
        return role;
    }
}
