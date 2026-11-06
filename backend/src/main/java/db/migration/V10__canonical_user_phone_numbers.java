package db.migration;

import com.jexis.jexis_backend.user.domain.ProfileInputNormalizer;
import com.jexis.jexis_backend.user.domain.ProfileValidationException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V10__canonical_user_phone_numbers extends BaseJavaMigration {
    @Override public void migrate(Context context) throws Exception {
        var connection = context.getConnection();
        try (var statement = connection.createStatement()) {
            statement.execute("LOCK TABLE users IN ACCESS EXCLUSIVE MODE");
            var normalized = validateExistingPhones(connection);
            statement.execute("ALTER TABLE users DROP CONSTRAINT users_phone_number_key");
            try (var update = connection.prepareStatement("UPDATE users SET phone_number = ? WHERE id = ?")) {
                for (var entry : normalized.entrySet()) {
                    update.setString(1, entry.getValue());
                    update.setObject(2, entry.getKey());
                    update.addBatch();
                }
                update.executeBatch();
            }
            statement.execute("ALTER TABLE users ADD CONSTRAINT uk_users_phone_number UNIQUE (phone_number)");
            statement.execute("ALTER TABLE users ADD CONSTRAINT ck_users_phone_e164 CHECK (phone_number IS NULL OR phone_number ~ '^\\+[1-9][0-9]{1,14}$')");
        }
    }

    /** Validate every value and detect collisions before changing data or constraints. */
    private Map<UUID, String> validateExistingPhones(Connection connection) throws SQLException {
        Map<UUID, String> normalized = new LinkedHashMap<>();
        Map<String, List<UUID>> owners = new LinkedHashMap<>();
        List<String> problems = new ArrayList<>();
        try (var statement = connection.createStatement();
                var rows = statement.executeQuery("SELECT id, phone_number FROM users ORDER BY id")) {
            while (rows.next()) {
                UUID id = UUID.fromString(rows.getString(1));
                try {
                    String phone = ProfileInputNormalizer.phone(rows.getString(2));
                    normalized.put(id, phone);
                    if (phone != null) {
                        owners.computeIfAbsent(phone, ignored -> new ArrayList<>()).add(id);
                    }
                } catch (ProfileValidationException ex) {
                    problems.add("invalid international phone for user " + id);
                }
            }
        }
        owners.values().stream().filter(ids -> ids.size() > 1)
                .forEach(ids -> problems.add("duplicate canonical phone for users " + ids));
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Phone cleanup required: " + String.join("; ", problems));
        }
        return normalized;
    }

}
