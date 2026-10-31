package infrastructure.repository.jdbc

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object DatabaseConnection {
  private const val URL = "jdbc:h2:./data/app_db"
  private const val USER = "sa"
  private const val PASSWORD = ""

  init {
    try {
      Class.forName("org.h2.Driver")
    } catch (e: ClassNotFoundException) {
      throw RuntimeException("Драйвер H2 не найден. Проверьте папку lib.", e)
    }
  }

  fun getConnection(): Connection {
    return DriverManager.getConnection(URL, USER, PASSWORD)
  }
}
