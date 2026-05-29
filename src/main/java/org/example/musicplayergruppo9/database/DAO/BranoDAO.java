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
        String sql = "INSERT INTO brani (titolo, artista, genere, data_rilascio, durata, " +
                "percorso_file_audio, estensione, percorso_copertina) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, brano.getTitolo());
            pstmt.setString(2, brano.getArtista());
            pstmt.setString(3, brano.getGenere());

            // Gestione della data di rilascio
            if (brano.getDataRilascio() != null) {
                pstmt.setLong(4, brano.getDataRilascio().getTime());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            pstmt.setInt(5, brano.getDurata());
            pstmt.setString(6, brano.getPercorsoFileAudio());
            pstmt.setString(7, brano.getEstensione());
            pstmt.setString(8, brano.getPercorsoCopertina());

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

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                long timestamp = rs.getLong("data_rilascio");
                java.util.Date data = (timestamp != 0) ? new java.util.Date(timestamp) : null;

                // Uso il costruttore completo
                Brano brano = new Brano(
                        rs.getInt("id"),
                        rs.getString("titolo"),
                        rs.getString("artista"),
                        rs.getString("genere"),
                        data,
                        rs.getInt("durata"),
                        rs.getString("percorso_file_audio"),
                        rs.getString("estensione"),
                        rs.getString("percorso_copertina")
                );
                listaBrani.add(brano);
            }

        } catch (SQLException e) {
            System.err.println("[BranoDAO] Errore durante il recupero dei brani.");
            e.printStackTrace();
        }
        return listaBrani;
    }

}