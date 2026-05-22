package com.example.bitirmeapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [EmaEntryEntity::class, HamPasifKayit::class, KullaniciEntity::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun emaDao(): EmaDao
    abstract fun hamPasifDao(): HamPasifDao
    abstract fun kullaniciDao(): KullaniciDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // v3->v4: ham_pasif_kayit tablosunu ekler, eski kayıtlar durur.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS ham_pasif_kayit (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        timestamp INTEGER NOT NULL,
                        tip TEXT NOT NULL,
                        deger REAL NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        // v4->v5: ema_entries'e forecasting + mobilMeta kolonları.
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ema_entries ADD COLUMN forecasting TEXT")
                db.execSQL("ALTER TABLE ema_entries ADD COLUMN mobilMeta TEXT")
            }
        }

        // v5->v6: kullanicilar tablosu (email unique).
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS kullanicilar (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        ad TEXT NOT NULL,
                        email TEXT NOT NULL,
                        sifreHash TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_kullanicilar_email ON kullanicilar (email)"
                )
            }
        }

        // v6->v7: kullanicilar'a yas, cinsiyet, onayTarihi kolonları.
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE kullanicilar ADD COLUMN yas INTEGER")
                db.execSQL("ALTER TABLE kullanicilar ADD COLUMN cinsiyet TEXT")
                db.execSQL("ALTER TABLE kullanicilar ADD COLUMN onayTarihi INTEGER")
            }
        }

        // v7->v8: ema_entries'e psikolojikAnaliz (JSON) kolonu. Backend'in
        // yeni "psikolojik_analiz" alanı için, eski kayıtlarda null kalır.
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ema_entries ADD COLUMN psikolojikAnaliz TEXT")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nabz.db"
                )
                    .addMigrations(
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
