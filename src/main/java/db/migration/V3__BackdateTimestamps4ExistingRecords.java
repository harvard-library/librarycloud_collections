package edu.harvard.lib.librarycloud.db;

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;

public class V3__BackdateTimestamps4ExistingRecords implements SpringJdbcMigration {
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
        jdbcTemplate.execute("UPDATE `collection` SET created=now()");
        jdbcTemplate.execute("UPDATE `collection` SET modified=now()");
    }
}
