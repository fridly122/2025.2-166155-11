package itss.group11.controller.chung;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public enum UserRole {
    SALES(
            "sales",
            "Bá»™ pháº­n bÃ¡n hÃ ng",
            AppFeature.CREATE_REQUEST,
            EnumSet.of(AppFeature.CREATE_REQUEST)
    ),
    INTERNATIONAL_ORDER(
            "order",
            "Bá»™ pháº­n Ä‘áº·t hÃ ng quá»‘c táº¿",
            AppFeature.CLASSIFICATION,
            EnumSet.of(AppFeature.CLASSIFICATION, AppFeature.SHIPPING, AppFeature.ALLOCATION)
    ),
    SITE(
            "site",
            "Site nháº­p kháº©u",
            AppFeature.INVENTORY,
            EnumSet.of(AppFeature.INVENTORY)
    ),
    WAREHOUSE(
            "warehouse",
            "Bá»™ pháº­n quáº£n lÃ½ kho",
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

