package com.jexis.jexis_backend.user.infrastructure;
import com.jexis.jexis_backend.user.domain.ProfileInputNormalizer;
import com.jexis.jexis_backend.user.domain.ProfileValidationException;
import java.sql.*;
import java.util.*;
/** Read-only audit shared by migration and CLI. Reports user IDs, never phone values. */
public final class PhonePreflight {
    private PhonePreflight() {}
    public record Audit(Map<UUID, String> normalized, List<String> problems) {
        public void requireClean() { if (!problems.isEmpty()) throw new IllegalStateException("Phone cleanup required: " + String.join("; ", problems)); }
    }
    public static Audit audit(Connection connection) throws SQLException {
        Map<UUID, String> normalized = new LinkedHashMap<>();
        Map<String, List<UUID>> owners = new LinkedHashMap<>();
        List<String> problems = new ArrayList<>();
        try (var statement = connection.createStatement(); var rows = statement.executeQuery("SELECT id, phone_number FROM users ORDER BY id")) {
            while (rows.next()) {
                UUID id = UUID.fromString(rows.getString(1));
                try {
                    String phone = ProfileInputNormalizer.phone(rows.getString(2));
                    normalized.put(id, phone);
                    if (phone != null) owners.computeIfAbsent(phone, ignored -> new ArrayList<>()).add(id);
                } catch (ProfileValidationException ex) { problems.add("invalid international phone for user " + id); }
            }
        }
        owners.values().stream().filter(ids -> ids.size() > 1).forEach(ids -> problems.add("duplicate canonical phone for users " + ids));
        return new Audit(normalized, problems);
    }
    public static void main(String[] args) throws Exception {
        String url = Objects.requireNonNull(System.getenv("TEST_DATABASE_URL"), "Set TEST_DATABASE_URL to the database to audit");
        try (var connection = DriverManager.getConnection(url, System.getenv().getOrDefault("TEST_DATABASE_USERNAME", "postgres"), System.getenv().getOrDefault("TEST_DATABASE_PASSWORD", ""))) {
            connection.setReadOnly(true);
            connection.setAutoCommit(false);
            try {
                var audit = audit(connection);
                audit.requireClean();
                System.out.println("Phone preflight passed for " + audit.normalized().size() + " users.");
            } finally { connection.rollback(); }
        }
    }
}
