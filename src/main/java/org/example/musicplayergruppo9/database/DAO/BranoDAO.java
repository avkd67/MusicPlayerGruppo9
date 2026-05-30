package org.example.musicplayergruppo9.database.DAO;

import org.example.musicplayergruppo9.database.DatabaseConnection;
import org.example.musicplayergruppo9.model.Brano;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BranoDAO {

    /**
     * TASK 1.3: Salva un brano nel database.
     * Riceve un oggetto Brano senza ID, lo inserisce, e imposta l'ID generato automaticamente.
     */
    public boolean salvaBrano(Brano brano) {
        // Corretto il typo: mancava una virgola e uno spazio tra percorso_copertina e preferito
        String sql = "INSERT INTO brani (titolo, artista, genere, data_rilascio, durata, " +
                "percorso_file_audio, estensione, percorso_copertina, preferito, new_release, explicit) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, brano.getTitolo());
            pstmt.setString(2, brano.getArtista());
            pstmt.setString(3, brano.getGenere());

            // Gestione della data di rilascio (ora è l'anno)
            if (brano.getDataRilascio() > 0) {
                pstmt.setInt(4, brano.getDataRilascio());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            pstmt.setInt(5, brano.getDurata());
            pstmt.setString(6, brano.getPercorsoFileAudio());
            pstmt.setString(7, brano.getEstensione());
            pstmt.setString(8, brano.getPercorsoCopertina());
            pstmt.setInt(9,  brano.isPreferito()    ? 1 : 0);
            pstmt.setInt(10, brano.isNewRelease()   ? 1 : 0);
            pstmt.setInt(11, brano.isExplicit()     ? 1 : 0);

            int righeInteressate = pstmt.executeUpdate();

            if (righeInteressate > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        brano.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("[BranoDAO] Errore durante il salvataggio: " + brano.getTitolo());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * TASK 1.2 (Validazione): Verifica se esiste già un brano con lo stesso titolo e artista.
     * Utile per bloccare i duplicati prima del salvataggio nel Controller.
     */
    public boolean esisteOmonimo(String titolo, String artista) {
        String sql = "SELECT COUNT(*) FROM brani WHERE LOWER(titolo) = LOWER(?) AND LOWER(artista) = LOWER(?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, titolo.trim());
            pstmt.setString(2, artista.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("[BranoDAO] Errore durante la verifica dell'omonimo.");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * FONDAMENTA: Recupera tutti i brani dal database per caricarli nel Player (Playlist generale).
     */
    public List<Brano> getTuttiIBrani() {
        List<Brano> listaBrani = new ArrayList<>();
        String sql = "SELECT * FROM brani";
        int annoCorrente = java.time.LocalDate.now().getYear();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Recupero diretto dell'anno come intero
                int anno = rs.getInt("data_rilascio");

                // Ricalcolo dinamico per sapere se è una nuova uscita
                boolean isNewRelease = (anno > 0 && anno == annoCorrente);

                // Uso il costruttore completo aggiornato
                Brano brano = new Brano(
                        rs.getInt("id"),
                        rs.getString("titolo"),
                        rs.getString("artista"),
                        rs.getString("genere"),
                        anno,
                        rs.getInt("durata"),
                        rs.getString("percorso_file_audio"),
                        rs.getString("estensione"),
                        rs.getString("percorso_copertina"),
                        rs.getBoolean("preferito"),
                        isNewRelease,
                        rs.getBoolean("explicit")
                );

                listaBrani.add(brano);
            }

        } catch (SQLException e) {
            System.err.println("[BranoDAO] Errore durante il recupero dei brani.");
            e.printStackTrace();
        }
        return listaBrani;
    }

    public boolean aggiornaPreferito(int id, boolean preferito) {
        String sql = "UPDATE brani SET preferito = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, preferito ? 1 : 0);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**versione cascata, da testare dopo creazione della tabella di collegamento playlist_brani
     * TASK 3.2: Elimina un brano dalla lista generale e a cascata dalle playlist.
     */
    /*public boolean eliminaBrano(int id) {
        // Query 1: Rimuove il brano dalle playlist (assumendo che la tabella si chiamerà playlist_brani)
        String sqlCascataPlaylist = "DELETE FROM playlist_brani WHERE id_brano = ?";
        // Query 2: Rimuove il brano dalla libreria principale
        String sqlLibreria = "DELETE FROM brani WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Disabilitiamo l'autocommit per eseguire entrambe le query in blocco (Transazione)
            conn.setAutoCommit(false); 

            try (PreparedStatement pstmtPlaylist = conn.prepareStatement(sqlCascataPlaylist);
                 PreparedStatement pstmtLibreria = conn.prepareStatement(sqlLibreria)) {
                
                // 1. Eliminazione a cascata (anche se la tabella non esiste ancora, ci prepariamo)
                try {
                    pstmtPlaylist.setInt(1, id);
                    pstmtPlaylist.executeUpdate();
                } catch (SQLException ignored) {
                    // Ignoriamo l'errore per ora se la tabella playlist_brani non è stata ancora creata dai colleghi
                }

                // 2. Eliminazione dalla libreria
                pstmtLibreria.setInt(1, id);
                int righeEliminate = pstmtLibreria.executeUpdate();

                // Confermiamo le modifiche al database
                conn.commit(); 
                
                return righeEliminate > 0;

            } catch (SQLException ex) {
                conn.rollback(); // Se qualcosa va storto, annulliamo tutte le modifiche
                throw ex;
            } finally {
                conn.setAutoCommit(true); // Ripristiniamo il comportamento di default
            }

        } catch (SQLException e) {
            System.err.println("[BranoDAO] Errore durante l'eliminazione del brano con ID: " + id);
            e.printStackTrace();
            return false;
        }
    }*/

    /**
     * TASK 1.4.3: Elimina un brano dal database in base al suo ID.
     */
    public boolean eliminaBrano(int id) {
        String sql = "DELETE FROM brani WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            
            // Se executeUpdate è maggiore di 0, ha eliminato correttamente la riga
            return pstmt.executeUpdate() > 0; 

        } catch (SQLException e) {
            System.err.println("[BranoDAO] Errore durante l'eliminazione del brano con ID: " + id);
            e.printStackTrace();
            return false;
        }
    }    

} 