package com.vislupus.mindmath.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.vislupus.mindmath.config.AppConfig
import java.io.File

/**
 * The app's SQLite database. It lives in the private app sandbox:
 *   <app files dir>/<AppConfig.DATABASE_DIR>/<AppConfig.DATABASE_NAME>
 *
 * When the schema changes, raise VERSION and add a migration in [onUpgrade]
 * so that existing user data survives app updates.
 */
class Database(context: Context) : SQLiteOpenHelper(context, databaseFile(context).absolutePath, null, VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // User preferences as simple key/value pairs.
        db.execSQL("CREATE TABLE settings (key TEXT PRIMARY KEY NOT NULL, value TEXT NOT NULL)")

        // One row per practice session.
        db.execSQL(
            """
            CREATE TABLE sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                mode TEXT NOT NULL,
                method_id TEXT,
                level INTEGER NOT NULL,
                param INTEGER NOT NULL,
                day TEXT NOT NULL,
                started_at INTEGER NOT NULL,
                ended_at INTEGER,
                total INTEGER NOT NULL DEFAULT 0,
                correct INTEGER NOT NULL DEFAULT 0,
                duration_ms INTEGER NOT NULL DEFAULT 0,
                xp INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )

        // One row per answered (or skipped) problem.
        db.execSQL(
            """
            CREATE TABLE attempts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id INTEGER NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
                method_id TEXT NOT NULL,
                level INTEGER NOT NULL,
                operands TEXT NOT NULL,
                question TEXT NOT NULL,
                correct_answer TEXT NOT NULL,
                user_answer TEXT NOT NULL,
                is_correct INTEGER NOT NULL,
                time_ms INTEGER NOT NULL,
                hint_used INTEGER NOT NULL,
                xp INTEGER NOT NULL,
                day TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX idx_attempts_day ON attempts(day)")
        db.execSQL("CREATE INDEX idx_attempts_method ON attempts(method_id, operands)")

        // Lessons the user has opened.
        db.execSQL("CREATE TABLE lessons (method_id TEXT PRIMARY KEY NOT NULL, viewed_at INTEGER NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Version 1 is the first schema. Future migrations go here, e.g.:
        // if (oldVersion < 2) db.execSQL("ALTER TABLE attempts ADD COLUMN ...")
    }

    companion object {
        private const val VERSION = 1

        fun databaseFile(context: Context): File {
            val dir = File(context.filesDir, AppConfig.DATABASE_DIR)
            if (!dir.exists()) dir.mkdirs()
            return File(dir, AppConfig.DATABASE_NAME)
        }
    }
}
