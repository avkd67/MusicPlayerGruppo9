package org.example.musicplayergruppo9.database.DAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.example.musicplayergruppo9.database.DatabaseConnection;
import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.Playlist;

public class PlaylistBraniDAO {
    // metodo per salvare l'associazione tra playlist e brano
    public boolean salvaPlaylistBrano(int playlistId, int branoId) {
        String sql = "INSERT INTO playlist_brani (playlist_id, brano_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playlistId);
            pstmt.setInt(2, branoId);
            int righeInteressate = pstmt.executeUpdate();
            
            return righeInteressate > 0;

        } catch (SQLException e) {
            System.err.println("[PlaylistBraniDAO] Errore durante il salvataggio dell'associazione: Playlist ID " + playlistId + ", Brano ID " + branoId);
            e.printStackTrace();
            return false;
        }
    }

    // metodo per ottenere tutte le canzoni associate a una playlist esistente
    public List<Brano> getBraniByPlaylist(Playlist playlist){

        List<Brano> brani = new ArrayList<>();
        String statement = "SELECT b.* FROM brani b " +
                "JOIN playlist_brani pb ON b.id = pb.brano_id " +
                "WHERE pb.playlist_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(statement)) {

            pstmt.setInt(1, playlist.getId());
            try (ResultSet rs = pstmt.executeQuery()){

                // ricreo i brani dalle info presenti nella risposta del database
                while(rs.next()){
                    Brano brano = new Brano();

                    brano.setId(rs.getInt("id"));
                    brano.setTitolo(rs.getString("titolo"));    
                    brano.setArtista(rs.getString("artista"));
                    brano.setGenere(rs.getString("genere"));

                    // gestione della data di rilascio
                    brano.setDataRilascio(rs.getInt("data_rilascio"));

                    brano.setDurata(rs.getInt("durata"));
                    brano.setPercorsoFileAudio(rs.getString("percorso_file_audio"));
                    brano.setEstensione(rs.getString("estensione"));
                    brano.setPercorsoCopertina(rs.getString("percorso_copertina"));

                    brani.add(brano);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PlaylistBraniDAO] Errore durante il recupero dei brani: " + e.getMessage());
            e.printStackTrace();
        }
        return brani;
    }
}
