package org.example.musicplayergruppo9.service;

import org.example.musicplayergruppo9.database.DAO.PlaylistDAO;
import org.example.musicplayergruppo9.model.Playlist;

public class PlaylistService {

    private PlaylistDAO playlistDAO;

    public boolean aggiungiPlaylist(Playlist playlist){
        playlistDAO = PlaylistDAO.getInstance();

        if(playlistDAO.checkNomePlaylist(playlist))
            playlistDAO.salvaPlaylist(playlist);
        else
            return false;
        return true;
    }

    public boolean aggiornaPlaylist(Playlist playlist, Playlist playlistAggiornata){
        playlistDAO = PlaylistDAO.getInstance();

        if(playlistDAO.checkNomePlaylist(playlistAggiornata))
            playlistDAO.aggiornaPlaylist(playlist, playlistAggiornata);
        else
            return false;
        return true;
    }
}
