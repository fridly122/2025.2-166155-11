package itss.group11.frontend.auth;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public enum UserRole {
    SALES(
            "sales",
            "Bộ phận bán hàng",
            AppFeature.CREATE_REQUEST,
            EnumSet.of(AppFeature.CREATE_REQUEST)
    ),
    INTERNATIONAL_ORDER(
            "order",
            "Bộ phận đặt hàng quốc tế",
            AppFeature.CLASSIFICATION,
            EnumSet.of(AppFeature.CLASSIFICATION, AppFeature.SHIPPING, AppFeature.ALLOCATION)
    ),
    SITE(
            "site",
            "Site nhập khẩu",
            AppFeature.INVENTORY,
            EnumSet.of(AppFeature.INVENTORY)
    ),
    WAREHOUSE(
            "warehouse",
            "Bộ phận quản lý kho",
            AppFeature.RECONCILIATION,
            EnumSet.of(AppFeature.RECONCILIATION)
    );

    private final String username;
    private final String displayName;
    private final AppFeature defaultFeature;
    private final Set<AppFeature> allowedFeatures;

    UserRole(
            String username,
            String displayName,
            AppFeature defaultFeature,
            Set<AppFeature> allowedFeatures
    ) {
        this.username = username;
        this.displayName = displayName;
        this.defaultFeature = defaultFeature;
        this.allowedFeatures = EnumSet.copyOf(allowedFeatures);
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public AppFeature getDefaultFeature() {
        return defaultFeature;
    }

    public boolean canAccess(AppFeature feature) {
        return allowedFeatures.contains(feature);
    }

    public static Optional<UserRole> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        String normalizedUsername = username.trim().toLowerCase();
        for (UserRole role : values()) {
            if (role.username.equals(normalizedUsername)) {
                return Optional.of(role);
            }
        }

        return Optional.empty();
    }
}
