package org.example.musicplayergruppo9.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        playlistBraniDAO = PlaylistBraniDAO.getInstance();
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

    public boolean salvaPlaylist(Playlist playlist) throws  IOException {

        String percorsoAssoluto = playlist.getPercorsoCopertina();
        String percorsoRelativo;

        if (percorsoAssoluto != null){
            try {
                Path cartellaCopertine = java.nio.file.Paths.get("AltriFile", "Copertine");

                if (!java.nio.file.Files.exists(cartellaCopertine))
                    Files.createDirectories(cartellaCopertine);

                File copertinaOriginale = new File(percorsoAssoluto);
                Path targetCopertina = cartellaCopertine.resolve(copertinaOriginale.getName());
                Files.copy(copertinaOriginale.toPath(), targetCopertina, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                percorsoRelativo = "AltriFile/Copertine/" + copertinaOriginale.getName();

            }catch (java.io.IOException e) {
                e.printStackTrace();
                throw e;
            }
        }

        return playlistDAO.salvaPlaylist(playlist);
    }

    public boolean aggiornaPlaylist(Playlist playlist, Playlist playlistAggiornata) {
        return playlistDAO.aggiornaPlaylist(playlist, playlistAggiornata);
    }

    public boolean eliminaPlaylist(Playlist playlist) {
        return playlistDAO.eliminaPlaylist(playlist);
    }
}
