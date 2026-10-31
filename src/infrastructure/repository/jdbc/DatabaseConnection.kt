package infrastructure.repository.jdbc

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object DatabaseConnection {
    private const val URL = "jdbc:h2:./data/app_db"
    private const val USER = "sa"
    private const val PASSWORD = ""

    fun getConnection(): Connection {
        return DriverManager.getConnection(URL, USER, PASSWORD)
    }
}
