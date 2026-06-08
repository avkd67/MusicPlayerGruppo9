package org.example.musicplayergruppo9.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    // Il database sarà un file chiamato "musicplayer.db" nella radice del progetto
    private static final String URL = "jdbc:sqlite:musicplayer.db";
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(URL);
            inizializzaDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("Driver SQLite non trovato!");
            e.printStackTrace();
            throw new SQLException(e);
        }
    }

    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.getConnection() == null || instance.getConnection().isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Restituisce la connessione attiva al database (Pattern Singleton semplice)
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Crea le tabelle se non esistono ancora nel file .db
     */
    private void inizializzaDatabase() {
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
                "percorso_copertina TEXT, " +
                "preferito INTEGER DEFAULT 0, " +
                "new_release INTEGER DEFAULT 0, " +
                "contatore_ascolti INTEGER DEFAULT 0,"+
                "explicit INTEGER DEFAULT 0" +
                ");";

        String sqlTabellaPlaylist = "CREATE TABLE IF NOT EXISTS playlist (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "copertina TEXT" +
                ");";

        String sqlTabellaPlaylistBrani = "CREATE TABLE IF NOT EXISTS playlist_brani (" +
            "playlist_id INTEGER NOT NULL, " +
            "brano_id INTEGER NOT NULL, " +
            "PRIMARY KEY (playlist_id, brano_id), " +
            "FOREIGN KEY (playlist_id) REFERENCES playlist(id) ON DELETE CASCADE, " +
            "FOREIGN KEY (brano_id) REFERENCES brani(id) ON DELETE CASCADE" +
            ");";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            // attivo i foreign keys per sqlite 
            stmt.execute("PRAGMA foreign_keys = ON;");

            // creazione tabella brani
            stmt.execute(sqlTabellaBrani);
            System.out.println("[Database] Tabella 'brani' verificata/creata con successo.");

            // creazione tabella playlist
            stmt.execute(sqlTabellaPlaylist);
            System.out.println("[Database] Tabella 'playlist' verificata/creata con successo.");
            
            // creazione tabella di relazione playlist-brani
            stmt.execute(sqlTabellaPlaylistBrani);
            System.out.println("[Database] Tabella 'playlist_brani' verificata/creata con successo.");

            //aggiunge contatore_ascolti se manca
            try {
                stmt.execute("ALTER TABLE brani ADD COLUMN contatore_ascolti INTEGER DEFAULT 0");
                System.out.println("[Database] Migrazione: colonna 'contatore_ascolti' aggiunta.");
            } catch (SQLException ignored) {
                //colonna già presente
            }

        } catch (SQLException e) {
            System.err.println("[Database] Errore durante l'inizializzazione delle tabelle:");
            e.printStackTrace();
        }
    }


}