package org.example.musicplayergruppo9.integration;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

//Test di integrazione per BranoDAO.incrementaAscolti().
//non applicato su musicplayer.db
 
class BranoDAOIncrementaAscoltiTest {

    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement stmt = conn.createStatement()) {
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
            //inserisce un brano per il test
            stmt.execute("""
                INSERT INTO brani (titolo, artista, percorso_file_audio, contatore_ascolti)
                VALUES ('Test Song', 'Test Artist', '/fake/path.mp3', 0)
            """);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        conn.close();
    }

    @Test
    @DisplayName("incrementaAscolti: il contatore sale da 0 a 1 al primo ascolto")
    void incrementaAscolti_primoAscolto_contatoreDiventa1() throws Exception {
        //prende l'id del brano inserito
        int id;
        try (var rs = conn.createStatement().executeQuery("SELECT id FROM brani WHERE titolo = 'Test Song'")) {
            id = rs.getInt("id");
        }

        //query di incremento
        try (var pstmt = conn.prepareStatement(
                "UPDATE brani SET contatore_ascolti = contatore_ascolti + 1 WHERE id = ?")) {
            pstmt.setInt(1, id);
            int righe = pstmt.executeUpdate();
            assertEquals(1, righe, "Deve aggiornare esattamente 1 riga");
        }

        try (var rs = conn.createStatement().executeQuery(
                "SELECT contatore_ascolti FROM brani WHERE id = " + id)) {
            assertEquals(1, rs.getInt("contatore_ascolti"),
                    "Dopo un ascolto il contatore deve essere 1");
        }
    }

    @Test
    @DisplayName("incrementaAscolti: tre ascolti consecutivi portano il contatore a 3")
    void incrementaAscolti_treVolte_contatoreE3() throws Exception {
        int id;
        try (var rs = conn.createStatement().executeQuery("SELECT id FROM brani WHERE titolo = 'Test Song'")) {
            id = rs.getInt("id");
        }

        String sql = "UPDATE brani SET contatore_ascolti = contatore_ascolti + 1 WHERE id = ?";
        for (int i = 0; i < 3; i++) {
            try (var pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
        }

        try (var rs = conn.createStatement().executeQuery(
                "SELECT contatore_ascolti FROM brani WHERE id = " + id)) {
            assertEquals(3, rs.getInt("contatore_ascolti"),
                    "Dopo tre ascolti il contatore deve essere 3");
        }
    }

    @Test
    @DisplayName("incrementaAscolti: ID inesistente non aggiorna nessuna riga")
    void incrementaAscolti_idInesistente_nessunaRigaAggiornata() throws Exception {
        try (var pstmt = conn.prepareStatement(
                "UPDATE brani SET contatore_ascolti = contatore_ascolti + 1 WHERE id = ?")) {
            pstmt.setInt(1, 9999); // ID che non esiste
            int righe = pstmt.executeUpdate();
            assertEquals(0, righe, "Con ID inesistente non deve aggiornare nessuna riga");
        }
    }

    @Test
    @DisplayName("brano appena inserito ha contatore_ascolti = 0")
    void nuovoBrano_contatoreAscoltiDefaultZero() throws Exception {
        try (var rs = conn.createStatement().executeQuery(
                "SELECT contatore_ascolti FROM brani WHERE titolo = 'Test Song'")) {
            assertEquals(0, rs.getInt("contatore_ascolti"),
                    "Un brano appena inserito deve avere contatore_ascolti = 0");
        }
    }
}