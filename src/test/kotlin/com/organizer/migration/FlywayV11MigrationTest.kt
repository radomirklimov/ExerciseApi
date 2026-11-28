package com.organizer.migration

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import java.sql.Connection
import java.sql.SQLException
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

/**
 * Repeatable PostgreSQL migration verification. Set SYNC_AUDIT_JDBC_URL,
 * SYNC_AUDIT_DB_USER and SYNC_AUDIT_DB_PASSWORD to run it against a disposable
 * PostgreSQL database. The test is skipped when those variables are absent so
 * ordinary CI runs do not require a local database.
 */
class FlywayV11MigrationTest {

    private fun dataSource(): PGSimpleDataSource? {
        val url = System.getenv("SYNC_AUDIT_JDBC_URL") ?: return null
        val user = System.getenv("SYNC_AUDIT_DB_USER") ?: return null
        val password = System.getenv("SYNC_AUDIT_DB_PASSWORD") ?: return null
        return PGSimpleDataSource().apply {
            setUrl(url)
            setUser(user)
            setPassword(password)
        }
    }

    private fun resetSchema(ds: PGSimpleDataSource) {
        ds.connection.use { c ->
            c.createStatement().use { s ->
                s.execute("DROP SCHEMA public CASCADE")
                s.execute("CREATE SCHEMA public")
            }
        }
    }

    private fun flyway(ds: PGSimpleDataSource, target: String): Flyway =
        Flyway.configure()
            .dataSource(ds)
            .locations("classpath:db/migration")
            .target(MigrationVersion.fromVersion(target))
            .load()

    private fun currentVersion(ds: PGSimpleDataSource): String? = ds.connection.use { c ->
        c.createStatement().use { s ->
            s.executeQuery(
                "SELECT version FROM flyway_schema_history WHERE success = true ORDER BY installed_rank DESC LIMIT 1"
            ).use { rs -> if (rs.next()) rs.getString(1) else null }
        }
    }

    private fun insertUsers(ds: PGSimpleDataSource, ids: List<UUID>) {
        val now = Timestamp.from(Instant.now())
        ds.connection.use { c ->
            c.prepareStatement(
                """INSERT INTO app_user (id, firebase_uid, email, display_name, created_at, updated_at, last_login_at)
                   VALUES (?, ?, ?, ?, ?, ?, ?)"""
            ).use { ps ->
                ids.forEachIndexed { index, id ->
                    ps.setObject(1, id)
                    ps.setString(2, "uid-$index")
                    ps.setString(3, "user$index@example.com")
                    ps.setString(4, "User $index")
                    ps.setTimestamp(5, now)
                    ps.setTimestamp(6, now)
                    ps.setTimestamp(7, now)
                    ps.executeUpdate()
                }
            }
        }
    }

    private fun insertLegacyRecords(ds: PGSimpleDataSource, userIds: List<UUID>): List<String> {
        val now = Timestamp.from(Instant.now())
        val clientIds = listOf(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
        )
        val entityTypes = listOf("WORKOUT", "CUSTOM_EXERCISE", "WORKOUT_EXERCISE", "WORKOUT")
        ds.connection.use { c ->
            c.prepareStatement(
                """INSERT INTO user_data_record
                   (id, user_id, entity_type, client_id, payload, created_at, updated_at, deleted_at, version)
                   VALUES (?, ?, ?, ?, ?, ?, ?, NULL, 1)"""
            ).use { ps ->
                clientIds.forEachIndexed { index, clientId ->
                    ps.setObject(1, UUID.randomUUID())
                    ps.setObject(2, userIds[if (index == 3) 1 else 0])
                    ps.setString(3, entityTypes[index])
                    ps.setString(4, clientId)
                    ps.setString(5, """{"name":"legacy-$index"}""")
                    ps.setTimestamp(6, now)
                    ps.setTimestamp(7, now)
                    ps.executeUpdate()
                }
            }
        }
        return clientIds
    }

    private fun rowCount(ds: PGSimpleDataSource, table: String): Int = ds.connection.use { c ->
        c.createStatement().use { s ->
            s.executeQuery("SELECT COUNT(*) FROM $table").use { rs -> if (rs.next()) rs.getInt(1) else 0 }
        }
    }

    private fun syncIds(ds: PGSimpleDataSource): Map<String, String?> = ds.connection.use { c ->
        c.createStatement().use { s ->
            s.executeQuery("SELECT client_id, sync_id FROM user_data_record").use { rs ->
                val result = mutableMapOf<String, String?>()
                while (rs.next()) result[rs.getString(1)] = rs.getString(2)
                result
            }
        }
    }

    @Test
    fun `V10 to V11 migration preserves data and enforces identity`() {
        val ds = dataSource()
        assumeTrue(ds != null, "PostgreSQL audit environment variables are not set")
        ds!!

        resetSchema(ds)
        flyway(ds, "10").migrate()
        assertEquals("10", currentVersion(ds))

        val userIds = listOf(UUID.randomUUID(), UUID.randomUUID())
        insertUsers(ds, userIds)
        val clientIds = insertLegacyRecords(ds, userIds)

        // Simulate legacy sequence state: one user has an explicit sequence row.
        ds.connection.use { c ->
            c.prepareStatement("INSERT INTO user_sync_sequence (user_id, next_sequence) VALUES (?, ?)").use { ps ->
                ps.setObject(1, userIds[0])
                ps.setLong(2, 5)
                ps.executeUpdate()
            }
        }

        val usersBefore = rowCount(ds, "app_user")
        val recordsBefore = rowCount(ds, "user_data_record")

        flyway(ds, "11").migrate()
        assertEquals("11", currentVersion(ds))

        assertEquals(usersBefore, rowCount(ds, "app_user"))
        assertEquals(recordsBefore, rowCount(ds, "user_data_record"))

        val ids = syncIds(ds)
        assertEquals(clientIds.toSet(), ids.keys)
        assertTrue(ids.values.all { it != null }, "sync_id must be populated")
        clientIds.forEach { assertEquals(it, ids[it]) }

        val nullable = ds.connection.use { c ->
            c.createStatement().use { s ->
                s.executeQuery(
                    "SELECT is_nullable FROM information_schema.columns WHERE table_name = 'user_data_record' AND column_name = 'sync_id'"
                ).use { rs -> if (rs.next()) rs.getString(1) else error("column not found") }
            }
        }
        assertEquals("NO", nullable, "sync_id must be NOT NULL in PostgreSQL")

        val constraintName = ds.connection.use { c ->
            c.createStatement().use { s ->
                s.executeQuery(
                    "SELECT conname FROM pg_constraint WHERE conrelid = 'user_data_record'::regclass AND contype = 'u' AND conname = 'uk_user_data_record_user_type_sync'"
                ).use { rs -> if (rs.next()) rs.getString(1) else null }
            }
        }
        assertEquals("uk_user_data_record_user_type_sync", constraintName)

        val sequences = ds.connection.use { c ->
            c.createStatement().use { s ->
                s.executeQuery("SELECT user_id, next_sequence FROM user_sync_sequence").use { rs ->
                    val result = mutableMapOf<UUID, Long>()
                    while (rs.next()) result[rs.getObject(1, UUID::class.java)] = rs.getLong(2)
                    result
                }
            }
        }
        assertEquals(2, sequences.size)
        assertEquals(5L, sequences[userIds[0]])
        assertEquals(1L, sequences[userIds[1]])

        // Duplicate logical identity must be rejected.
        val duplicateClientId = UUID.randomUUID().toString()
        val duplicateError = assertThrows<SQLException> {
            ds.connection.use { c ->
                c.prepareStatement(
                    """INSERT INTO user_data_record
                       (id, user_id, entity_type, client_id, sync_id, created_at, updated_at, version)
                       VALUES (?, ?, 'WORKOUT', ?, ?, ?, ?, 1)"""
                ).use { ps ->
                    ps.setObject(1, UUID.randomUUID())
                    ps.setObject(2, userIds[0])
                    ps.setString(3, duplicateClientId)
                    ps.setString(4, clientIds[0])
                    ps.setTimestamp(5, Timestamp.from(Instant.now()))
                    ps.setTimestamp(6, Timestamp.from(Instant.now()))
                    ps.executeUpdate()
                }
            }
        }
        assertTrue(duplicateError.message.orEmpty().contains("uk_user_data_record_user_type_sync"))

        // NULL sync_id must be rejected.
        val nullError = assertThrows<SQLException> {
            ds.connection.use { c ->
                c.prepareStatement(
                    """INSERT INTO user_data_record
                       (id, user_id, entity_type, client_id, sync_id, created_at, updated_at, version)
                       VALUES (?, ?, 'WORKOUT', ?, NULL, ?, ?, 1)"""
                ).use { ps ->
                    ps.setObject(1, UUID.randomUUID())
                    ps.setObject(2, userIds[0])
                    ps.setString(3, UUID.randomUUID().toString())
                    ps.setTimestamp(4, Timestamp.from(Instant.now()))
                    ps.setTimestamp(5, Timestamp.from(Instant.now()))
                    ps.executeUpdate()
                }
            }
        }
        assertTrue(nullError.message.orEmpty().contains("sync_id", ignoreCase = true))
    }

    @Test
    fun `fresh database migrates through V11`() {
        val ds = dataSource()
        assumeTrue(ds != null, "PostgreSQL audit environment variables are not set")
        ds!!

        resetSchema(ds)
        flyway(ds, "11").migrate()
        assertEquals("11", currentVersion(ds))
        assertTrue(rowCount(ds, "flyway_schema_history") >= 11)
        assertEquals(0, rowCount(ds, "user_data_record"))
    }
}