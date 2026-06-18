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

    // singleton
    private static PlaylistBraniDAO instance;

    public static synchronized PlaylistBraniDAO getInstance() {
        if (instance == null) {
            instance = new PlaylistBraniDAO();
        }
        return instance;
    }

    // metodo per ottenere tutte le canzoni associate a una playlist esistente
    public List<Brano> getBraniByPlaylist(Playlist playlist){

        List<Brano> brani = new ArrayList<>();
        String statement = "SELECT b.* FROM brani b " +
                "JOIN playlist_brani pb ON b.id = pb.brano_id " +
                "WHERE pb.playlist_id = ?"+
                "ORDER BY pb.posizione ASC, pb.brano_id ASC";

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

    // metodo per l'aggiunta di un brano ad una playlist
    public boolean aggiungiBranoAPlaylist(Playlist playlist, Brano brano) {
        String sql = "INSERT OR IGNORE INTO playlist_brani (playlist_id, brano_id, posizione) " +
        "VALUES (?, ?, COALESCE((SELECT MAX(posizione) + 1 FROM playlist_brani WHERE playlist_id = ?), 1))";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playlist.getId());
            pstmt.setInt(2, brano.getId());
            pstmt.setInt(3, playlist.getId());
            int righeInteressate = pstmt.executeUpdate();

            return righeInteressate > 0;

        } catch (SQLException e) {
            System.err.println("[PlaylistBraniDAO] Errore durante il salvataggio dell'associazione: Playlist ID " + playlist.getId() + ", Brano ID " + brano.getId());
            e.printStackTrace();
            return false;
        }
    }

    // metodo per la rimozione di un brano da una playlist
    public boolean rimuoviBranoDaPlaylist(Playlist playlist, Brano brano){
        String sql = "DELETE FROM playlist_brani WHERE playlist_id = ? AND brano_id = ?";

        try(Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playlist.getId());
            pstmt.setInt(2, brano.getId());

            int righeInteressate = pstmt.executeUpdate();
            return righeInteressate > 0;

        } catch (SQLException e) {
            System.out.println("[PlaylistBraniDAO] Errore durante la rimozione del brano: " + brano.getTitolo());
            e.printStackTrace();
        }
        return false;
    }

    // metodo per ottenere tutte le playlist che contengono uno specifico brano per l'undo
    public List<Playlist> getPlaylistsByBrano(Brano brano) {
        List<Playlist> playlists = new ArrayList<>();
        String sql = "SELECT p.* FROM playlist p " +
                "JOIN playlist_brani pb ON p.id = pb.playlist_id " +
                "WHERE pb.brano_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, brano.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Playlist playlist = new Playlist(rs.getString("nome"), rs.getString("copertina"));
                    playlist.setId(rs.getInt("id"));
                    playlists.add(playlist);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PlaylistBraniDAO] Errore durante il recupero delle playlist per il brano: " + e.getMessage());
            e.printStackTrace();
        }
        return playlists;
    }
}
