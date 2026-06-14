package org.example.musicplayergruppo9.integration;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

// Test di integrazione per PlaylistBraniDAO e PlaylistDAO.
// non applicato su musicplayer.db

public class Playlist_PlaylistBraniDAOTest {

    private Connection conn;

    // setUp iniziale prima di ogni test
    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");

        // per l'attivazione delle foreign keys che sono di default disabilitate
        try (Statement pragmaStmt = conn.createStatement()) {
            pragmaStmt.execute("PRAGMA foreign_keys = ON");
        }

        try (Statement stmt = conn.createStatement()) {

            // creazione tabella brani
            stmt.execute("""
                CREATE TABLE brani (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    titolo TEXT NOT NULL,
                    artista TEXT NOT NULL,
                    genere TEXT,
                    data_rilascio INTEGER,
                    durata INTEGER,
                    percorso_file_audio TEXT NOT NULL,
                    estensione TEXT,
                    percorso_copertina TEXT,
                    preferito INTEGER DEFAULT 0,
                    new_release INTEGER DEFAULT 0,
                    contatore_ascolti INTEGER DEFAULT 0,
                    explicit INTEGER DEFAULT 0
                )
            """);

            // creazione tabella playlist
            stmt.execute("""
                CREATE TABLE playlist (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL,
                    copertina TEXT
                )
            """);

            // creazione tabella di associazione playlist_brani
            stmt.execute("""
                CREATE TABLE playlist_brani (
                    playlist_id INTEGER NOT NULL,
                    brano_id INTEGER NOT NULL,
                    PRIMARY KEY (playlist_id, brano_id),
                    FOREIGN KEY (playlist_id) REFERENCES playlist(id) ON DELETE CASCADE,
                    FOREIGN KEY (brano_id) REFERENCES brani(id) ON DELETE CASCADE
                )
            """);

            // inserimento dati iniziali
            stmt.execute("""
                INSERT INTO brani (titolo, artista, percorso_file_audio)
                VALUES ('Bad Romance', 'Lady Gaga', 'path')
            """);

            stmt.execute("""
                INSERT INTO playlist (nome) VALUES ('Playlist 1')
            """);
        }
    }

    // chiude la connessione alla fine di ogni test
    @AfterEach
    void tearDown() throws Exception {
        conn.close();
    }

    // getIDs
    private int getBranoId() throws Exception {
        try (var rs = conn.createStatement().executeQuery(
                "SELECT id FROM brani WHERE titolo = 'Bad Romance'")) {
            return rs.getInt("id");
        }
    }

    private int getPlaylistId() throws Exception {
        try (var rs = conn.createStatement().executeQuery(
                "SELECT id FROM playlist WHERE nome = 'Playlist 1'")) {
            return rs.getInt("id");
        }
    }

    // test per l'aggiunta del brano alla playlist, controlla l'avvenuto inserimento in playlist_brani
    @Test
    @DisplayName("aggiungiBranoAPlaylist: il brano viene correttamente associato alla playlist")
    void aggiungiBranoAPlaylist() throws Exception {
        int playlistId = getPlaylistId();
        int branoId = getBranoId();

        try (var pstmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO playlist_brani (playlist_id, brano_id) VALUES (?, ?)")) {
            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, branoId);
            int righe = pstmt.executeUpdate();
            assertEquals(1, righe, "deve inserire esattamente 1 riga");
        }

        // verifica che l'associazione esista
        try (var pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM playlist_brani WHERE playlist_id = ? AND brano_id = ?")) {
            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, branoId);
            try (var rs = pstmt.executeQuery()) {
                assertEquals(1, rs.getInt(1), "L'associazione playlist-brano deve esistere");
            }
        }
    }

    @Test
    @DisplayName("aggiungiBranoAPlaylist: inserimento duplicato non crea una seconda riga (INSERT OR IGNORE)")
    void aggiungiDuplicato() throws Exception {
        int playlistId = getPlaylistId();
        int branoId = getBranoId();

        String sql = "INSERT OR IGNORE INTO playlist_brani (playlist_id, brano_id) VALUES (?, ?)";

        // primo inserimento
        try (var pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, branoId);
            pstmt.executeUpdate();
        }

        // (duplicato)
        try (var pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, branoId);
            int righe = pstmt.executeUpdate();
            assertEquals(0, righe, "Il duplicato non deve inserire nuove righe");
        }

        // verifica che ci sia ancora solo 1 riga
        try (var pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM playlist_brani WHERE playlist_id = ? AND brano_id = ?")) {
            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, branoId);
            try (var rs = pstmt.executeQuery()) {
                assertEquals(1, rs.getInt(1), "Deve esserci esattamente 1 associazione, non 2");
            }
        }
    }

    @Test
    @DisplayName("rimuoviBranoDaPlaylist: il brano viene correttamente rimosso dalla playlist")
    void rimuoviBrano() throws Exception {
        int playlistId = getPlaylistId();
        int branoId = getBranoId();

        // aggiunge il brano
        try (var pstmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO playlist_brani (playlist_id, brano_id) VALUES (?, ?)")) {
            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, branoId);
            pstmt.executeUpdate();
        }

        // lo rimuove
        try (var pstmt = conn.prepareStatement(
                "DELETE FROM playlist_brani WHERE playlist_id = ? AND brano_id = ?")) {
            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, branoId);
            int righe = pstmt.executeUpdate();
            assertEquals(1, righe, "Deve eliminare esattamente 1 riga");
        }

        // verifica che l'associazione non esista più
        try (var pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM playlist_brani WHERE playlist_id = ? AND brano_id = ?")) {
            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, branoId);
            try (var rs = pstmt.executeQuery()) {
                assertEquals(0, rs.getInt(1), "L'associazione playlist-brano non deve più esistere");
            }
        }
    }

    @Test
    @DisplayName("rimuoviBranoDaPlaylist: rimozione di un brano non presente non aggiorna nessuna riga")
    void rimuoviBranoNonPresente() throws Exception {
        int playlistId = getPlaylistId();

        try (var pstmt = conn.prepareStatement(
                "DELETE FROM playlist_brani WHERE playlist_id = ? AND brano_id = ?")) {
            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, 9999); // ID brano inesistente
            int righe = pstmt.executeUpdate();
            assertEquals(0, righe, "Nessuna riga deve essere eliminata per un brano non presente");
        }
    }

    // eliminaione playlist
    @Test
    @DisplayName("eliminaPlaylist: la playlist viene rimossa dalla tabella playlist")
    void eliminaPlaylist() throws Exception {
        int playlistId = getPlaylistId();

        try (var pstmt = conn.prepareStatement(
                "DELETE FROM playlist WHERE id = ?")) {
            pstmt.setInt(1, playlistId);
            int righe = pstmt.executeUpdate();
            assertEquals(1, righe, "Deve eliminare esattamente 1 playlist");
        }

        // verifica che la playlist non esista più
        try (var pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM playlist WHERE id = ?")) {
            pstmt.setInt(1, playlistId);
            try (var rs = pstmt.executeQuery()) {
                assertEquals(0, rs.getInt(1), "La playlist non deve più esistere nel DB");
            }
        }
    }

    // controllo sulle associazioni della playlist, che vengano eliminate dal CASCADE
    @Test
    @DisplayName("eliminaPlaylist: l'eliminazione rimuove a cascata le associazioni in playlist_brani")
    void eliminaPlaylistCascata() throws Exception {
        int playlistId = getPlaylistId();
        int branoId = getBranoId();

        // aggiunge il brano alla playlist
        try (var pstmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO playlist_brani (playlist_id, brano_id) VALUES (?, ?)")) {
            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, branoId);
            pstmt.executeUpdate();
        }

        // elimina la playlist
        try (var pstmt = conn.prepareStatement("DELETE FROM playlist WHERE id = ?")) {
            pstmt.setInt(1, playlistId);
            pstmt.executeUpdate();
        }

        // verifica sull'eliminazione a cascata
        try (var pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM playlist_brani WHERE playlist_id = ?")) {
            pstmt.setInt(1, playlistId);
            try (var rs = pstmt.executeQuery()) {
                assertEquals(0, rs.getInt(1),
                        "Le associazioni playlist-brani devono essere eliminate a cascata");
            }
        }
    }

    // controlla, come da criterio di accettazione,
    // che il brano rimanga nel db dopo la cancellazione della playlist
    @Test
    @DisplayName("eliminaPlaylist: il brano rimane nel DB dopo l'eliminazione della playlist")
    void eliminaPlaylistCheckSuBrani() throws Exception {
        int playlistId = getPlaylistId();
        int branoId = getBranoId();

        // associa ed elimina la playlist
        try (var pstmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO playlist_brani (playlist_id, brano_id) VALUES (?, ?)")) {
            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, branoId);
            pstmt.executeUpdate();
        }

        try (var pstmt = conn.prepareStatement("DELETE FROM playlist WHERE id = ?")) {
            pstmt.setInt(1, playlistId);
            pstmt.executeUpdate();
        }

        // il brano deve essere ancora presente nella tabella brani
        try (var pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM brani WHERE id = ?")) {
            pstmt.setInt(1, branoId);
            try (var rs = pstmt.executeQuery()) {
                assertEquals(1, rs.getInt(1),
                        "il brano non deve essere eliminato quando si elimina la playlist");
            }
        }
    }

    // aggiunta di un brano alla playlist preferiti (controlla che i brani settati come preferiti siano correttamente considerati)
    @Test
    @DisplayName("Aggiungi Preferito: il brano viene settato come preferito (flag a 1)")
    void aggiungiBranoPreferiti() throws Exception {
        int branoId = getBranoId();

        // simula l'azione del DAO di aggiungere ai preferiti aggiornando la colonna
        try (var pstmt = conn.prepareStatement("UPDATE brani SET preferito = 1 WHERE id = ?")) {
            pstmt.setInt(1, branoId);
            int righe = pstmt.executeUpdate();
            assertEquals(1, righe, "Deve aggiornare esattamente 1 brano");
        }

        // verifica che il db abbia registrato il cambiamento
        try (var pstmt = conn.prepareStatement("SELECT preferito FROM brani WHERE id = ?")) {
            pstmt.setInt(1, branoId);
            try (var rs = pstmt.executeQuery()) {
                assertEquals(1, rs.getInt("preferito"), "il flag preferito del brano deve essere 1");
            }
        }
    }

    // aggiunta di un secondo brano ai preferiti
    @Test
    @DisplayName("Aggiungi Secondo Preferito: a playlist esistente devo poter popolare la playlist")
    void aggiuntaSecondoBranoPreferiit() throws Exception {
        int branoId1 = getBranoId();

        // inserisco un secondo brano nel DB
        try (var stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO brani (titolo, artista, percorso_file_audio) VALUES ('Toxic', 'Britney Spears', 'path2')");
        }

        // recupero l'id del brano appena inserito
        int branoId2;
        try (var rs = conn.createStatement().executeQuery("SELECT id FROM brani WHERE titolo = 'Toxic'")) {
            branoId2 = rs.getInt("id");
        }

        // aggiunta di entrambi i brani ai preferiti
        try (var pstmt = conn.prepareStatement("UPDATE brani SET preferito = 1 WHERE id IN (?, ?)")) {
            pstmt.setInt(1, branoId1);
            pstmt.setInt(2, branoId2);
            pstmt.executeUpdate();
        }

        // la playlist preferiti conterà esattamente 2 brani se la query restituisce 2
        try (var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM brani WHERE preferito = 1")) {
            assertEquals(2, rs.getInt(1), "ci devono essere esattamente 2 brani con flag preferito = 1 nel db");
        }
    }

    // eliminazione di un brano dalla playlist preferiti
    @Test
    @DisplayName("Elimina Brano Preferito: a playlist esistente devo poter rimuovere il brano dai preferiti (flag a 0)")
    void eliminaBranoPreferito() throws Exception {
        int branoId = getBranoId();

        // mettiamo nel db un brano nei preferiti
        try (var pstmt = conn.prepareStatement("UPDATE brani SET preferito = 1 WHERE id = ?")) {
            pstmt.setInt(1, branoId);
            pstmt.executeUpdate();
        }

        // rimuovo il brano dai preferiti (riportando il flag a 0)
        try (var pstmt = conn.prepareStatement("UPDATE brani SET preferito = 0 WHERE id = ?")) {
            pstmt.setInt(1, branoId);
            int righe = pstmt.executeUpdate();
            assertEquals(1, righe, "deve aggiornare esattamente 1 brano");
        }

        // il db deve tornare allo stato originale
        try (var pstmt = conn.prepareStatement("SELECT preferito FROM brani WHERE id = ?")) {
            pstmt.setInt(1, branoId);
            try (var rs = pstmt.executeQuery()) {
                assertEquals(0, rs.getInt("preferito"), "il flag preferito deve essere tornato a 0");
            }
        }
    }
}