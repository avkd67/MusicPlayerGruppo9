package org.example.musicplayergruppo9.database.DAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.example.musicplayergruppo9.database.DatabaseConnection;
import org.example.musicplayergruppo9.model.Playlist;

public class PlaylistDAO {

    // singleton per evitare che più istanze di DAO che si connettano al database
    private static PlaylistDAO instance;

    //metodo per ottenere l'istanza
    public static synchronized PlaylistDAO getInstance() {
        if (instance == null) {
            instance = new PlaylistDAO();
        }
        return instance;
    }
    
    // metodo per salvare la playlist con solo il nome, perché i brani sono aggiunti successivamente
    public boolean salvaPlaylist(Playlist playlist){

        String sql = "INSERT INTO playlist (nome, copertina) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, playlist.getNome());
            pstmt.setString(2, playlist.getPercorsoCopertina());
            int righeInteressate = pstmt.executeUpdate();
            
            // controllo se l'inserimento è avvenuto con successo e recupero l'ID associato
            if (righeInteressate > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        playlist.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("[PlaylistDAO] Errore durante il salvataggio: " + playlist.getNome());
            e.printStackTrace();
            return false;
        }

        return false;
    }

    // metodo per aggiornare i dati di una playlist esistente (SOLO nome e copertina)
    public boolean aggiornaPlaylist(Playlist playlist, Playlist playlistModificata){
        String sql = "UPDATE playlist SET nome = ?, copertina = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
                pstmt.setString(1, playlistModificata.getNome());
                pstmt.setString(2, playlistModificata.getPercorsoCopertina());
                pstmt.setInt(3, playlist.getId());
                pstmt.executeUpdate();
                return pstmt.executeUpdate() > 0;
            }
            catch (SQLException e) {
            System.err.println("[PlaylistDAO] Errore durante l'aggiornamento!");
            e.printStackTrace();
    }
        return false;
    }

    // metodo per controllare la presenza di una playlist con lo stesso nome ed evitare duplicati
    public boolean checkNomePlaylist(Playlist playlist){
        String sql = "SELECT COUNT(*) FROM playlist WHERE nome = ?";

        try(Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            
                pstmt.setString(1, playlist.getNome());

                try(ResultSet rs = pstmt.executeQuery()){
                    if(rs.next()){
                        int count = rs.getInt(1);
                        return count == 0; // se count è uguale a 0, non esiste una playlist con quel nome
                    }
                }
            }
            catch (SQLException e) {
            System.err.println("[PlaylistDAO] Errore durante il controllo del nome della playlist: " + playlist.getNome());
            e.printStackTrace();
        }

        return false;
    }

    // metodo per recuperare tutte le playlist del database
    public ArrayList<Playlist> getAllPlaylists(){
        ArrayList<Playlist> playlists = new ArrayList<>();
        String sql = "SELECT * FROM playlist";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Playlist playlist = new Playlist(rs.getString("nome"), rs.getString("copertina"));
                playlist.setId(rs.getInt("id"));
                playlist.setContatoreAscolti(rs.getInt("contatore_ascolti"));
                playlists.add(playlist);
            }

        } catch (SQLException e) {
            System.err.println("[PlaylistDAO] Errore durante il recupero delle playlist:");
            e.printStackTrace();
        }

        return playlists;
    }

    // metodo per eliminare una playlist
    public boolean eliminaPlaylist(Playlist playlist){

        String sql = "DELETE FROM playlist WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, playlist.getId());

            int righeInteressate = pstmt.executeUpdate();

            if (righeInteressate > 0) {
                return true;
            }

        } catch (SQLException e) {
            System.out.println("[Playlist DAO] Errore durante l'eliminazione della playlist");
            throw new RuntimeException(e);
        }
        return false;
    }

    public boolean incrementaAscolti(int playlistId) {
        String sql = "UPDATE playlist SET contatore_ascolti = contatore_ascolti + 1 WHERE id = ?";
    
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            pstmt.setInt(1, playlistId);
            return pstmt.executeUpdate() > 0;
    
        } catch (SQLException e) {
            System.err.println("[PlaylistDAO] Errore incremento ascolti playlist ID: " + playlistId);
            e.printStackTrace();
            return false;
        }
    }
    
    public ArrayList<Playlist> getPlaylistsPiuRiprodotte(int limite) {
        ArrayList<Playlist> playlists = new ArrayList<>();
        String sql = "SELECT * FROM playlist WHERE contatore_ascolti > 5 ORDER BY contatore_ascolti DESC LIMIT ?";
    
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            pstmt.setInt(1, limite);
    
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Playlist playlist = new Playlist(rs.getString("nome"), rs.getString("copertina"));
                    playlist.setId(rs.getInt("id"));
                    playlist.setContatoreAscolti(rs.getInt("contatore_ascolti"));
                    playlists.add(playlist);
                }
            }
    
        } catch (SQLException e) {
            System.err.println("[PlaylistDAO] Errore recupero playlist piu riprodotte.");
            e.printStackTrace();
        }
    
        return playlists;
    }

}
