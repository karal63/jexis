package db.migration;

import com.jexis.jexis_backend.user.infrastructure.PhonePreflight;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V10__canonical_user_phone_numbers extends BaseJavaMigration {
    @Override public void migrate(Context context) throws Exception {
        var connection = context.getConnection();
        try (var statement = connection.createStatement()) {
            statement.execute("LOCK TABLE users IN ACCESS EXCLUSIVE MODE");
            var audit = PhonePreflight.audit(connection);
            audit.requireClean(); // Complete audit before changing any values or constraints.
            statement.execute("ALTER TABLE users DROP CONSTRAINT users_phone_number_key");
            try (var update = connection.prepareStatement("UPDATE users SET phone_number = ? WHERE id = ?")) {
                for (var entry : audit.normalized().entrySet()) {
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
}
