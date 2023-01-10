import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import ru.ivk1800.diff.database.DiffDatabase
import java.io.File

object DriverFactory {
    fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("java.io.tmpdir"), "DiffDatabaseDev.db")
        val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}")

        if (!databasePath.exists()) {
            DiffDatabase.Schema.create(driver)
        }
        return driver
    }
}
