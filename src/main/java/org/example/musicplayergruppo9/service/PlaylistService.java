package org.example.musicplayergruppo9.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.example.musicplayergruppo9.database.DAO.PlaylistDAO;
import org.example.musicplayergruppo9.model.Playlist;

public class PlaylistService {

    private PlaylistDAO playlistDAO;

    public boolean aggiungiPlaylist(Playlist playlist){
        playlistDAO = PlaylistDAO.getInstance();

        String percorsoCopertinaDefinitiva = playlist.getPercorsoCopertina();
        String percorsoCopertinaRelativa = "";

        try {
            if (percorsoCopertinaDefinitiva != null) {
                Path cartellaCopertine = java.nio.file.Paths.get("AltriFile", "Copertine");
                if (!java.nio.file.Files.exists(cartellaCopertine)) {
                    java.nio.file.Files.createDirectories(cartellaCopertine);
                }

                File fileCopertinaOriginale = new File(percorsoCopertinaDefinitiva);
                Path targetCopertina = cartellaCopertine.resolve(fileCopertinaOriginale.getName());
                Files.copy(fileCopertinaOriginale.toPath(), targetCopertina, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                percorsoCopertinaRelativa = "AltriFile/Copertine/" + fileCopertinaOriginale.getName();            }
        }
        catch (java.io.IOException e) {
            System.out.println("Errore durante la copia del file copertina della playlist");
            e.printStackTrace();
            return false; // Gestione dell'errore di copia del file
        }

        playlist.setPercorsoCopertina(percorsoCopertinaRelativa);

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
