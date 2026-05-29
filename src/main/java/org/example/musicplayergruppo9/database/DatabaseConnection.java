package org.example.musicplayergruppo9.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
public class DatabaseConnection {
    // Il database sarà un file chiamato "musicplayer.db" nella radice del progetto
    private static final String URL = "jdbc:sqlite:musicplayer.db";
    private static Connection connection = null;

    /**
     * Restituisce la connessione attiva al database (Pattern Singleton semplice)
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {

                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(URL);

                inizializzaDatabase();
            } catch (ClassNotFoundException e) {
                System.err.println("Driver SQLite non trovato!");
                e.printStackTrace();
                throw new SQLException(e);
            }
        }
        return connection;
    }

    /**
     * Crea le tabelle se non esistono ancora nel file .db
     */
    private static void inizializzaDatabase() {
        // Query per creare la tabella dei brani
        String sqlTabellaBrani = "CREATE TABLE IF NOT EXISTS brani (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "titolo TEXT NOT NULL, " +
                "artista TEXT NOT NULL, " +
                "genere TEXT, " +
                "data_rilascio INTEGER, " +
                "durata INTEGER, " +
                "percorso_file_audio TEXT NOT NULL, " +
                "estensione TEXT, " +
                "percorso_copertina TEXT" +
                ");";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(sqlTabellaBrani);
            System.out.println("[Database] Tabella 'brani' verificata/creata con successo.");

        } catch (SQLException e) {
            System.err.println("[Database] Errore durante l'inizializzazione delle tabelle:");
            e.printStackTrace();
        }
    }
}
