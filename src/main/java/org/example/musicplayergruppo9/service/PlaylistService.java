package org.example.musicplayergruppo9.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

import org.example.musicplayergruppo9.database.DAO.PlaylistBraniDAO;
import org.example.musicplayergruppo9.database.DAO.PlaylistDAO;
import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.Playlist;

public class PlaylistService {

    private PlaylistDAO playlistDAO;
    private PlaylistBraniDAO playlistBraniDAO;

    public PlaylistService() {
        playlistDAO = PlaylistDAO.getInstance();
        playlistBraniDAO = playlistBraniDAO.getInstance();
    }

    public List<Brano> getBraniByPlaylist(Playlist playlist) {
        return playlistBraniDAO.getBraniByPlaylist(playlist);
    }

    public ArrayList<Playlist> getAllPlaylists() {
        return playlistDAO.getAllPlaylists();
    }

    public boolean checkNomePlaylist(Playlist playlist) {
        return playlistDAO.checkNomePlaylist(playlist);
    }

    public boolean salvaPlaylist(Playlist playlist) {
        return playlistDAO.salvaPlaylist(playlist);
    }

    public boolean aggiornaPlaylist(Playlist playlist, Playlist playlistAggiornata) {
        return playlistDAO.aggiornaPlaylist(playlist, playlistAggiornata);
    }
}
