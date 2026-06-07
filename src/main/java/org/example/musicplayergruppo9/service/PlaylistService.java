package org.example.musicplayergruppo9.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

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
        return playlistBraniDAO.getInstance().getBraniByPlaylist(playlist);
    }

    public ArrayList<Playlist> getAllPlaylists() {
        return playlistDAO.getInstance().getAllPlaylists();
    }

    public boolean checkNomePlaylist(Playlist playlist) {
        return playlistDAO.getInstance().checkNomePlaylist(playlist);
    }

    public boolean salvaPlaylist(Playlist playlist) throws  IOException {
        // prima del salvataggio, rendo il percorso relativo e non più assoluto!
        String percorsoAssoluto = playlist.getPercorsoCopertina();
        String percorsoRelativo;

        if (percorsoAssoluto != null && !percorsoAssoluto.startsWith("AltriFile")){
            try {
                Path cartellaCopertine = java.nio.file.Paths.get("AltriFile", "Copertine");

                if (!java.nio.file.Files.exists(cartellaCopertine))
                    Files.createDirectories(cartellaCopertine);

                File copertinaOriginale = new File(percorsoAssoluto);
                Path targetCopertina = cartellaCopertine.resolve(copertinaOriginale.getName());
                Files.copy(copertinaOriginale.toPath(), targetCopertina, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                percorsoRelativo = "AltriFile/Copertine/" + copertinaOriginale.getName();
                playlist.setPercorsoCopertina(percorsoRelativo);

            }catch (java.io.IOException e) {
                e.printStackTrace();
                throw e;
            }
        }

        return playlistDAO.getInstance().salvaPlaylist(playlist);
    }

    // salva la playlist nel db e le associa tutti i brani presenti nella lista
    public boolean salvaPlaylistConBrani(Playlist playlist, ArrayList<Brano> brani) {
        try {
            if(!salvaPlaylist(playlist)){
                return false;
            }

            for(Brano brano : brani){
                if(!this.aggiungiBranoAPlaylist(playlist, brano))
                    return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean aggiornaPlaylist(Playlist playlist, Playlist playlistAggiornata) throws IOException {
        // se il percorso della copertina è stato cambiato, devo provvedere a renderlo relativo!

        if(!Objects.equals(playlist.getPercorsoCopertina(), playlistAggiornata.getPercorsoCopertina())){
            if (playlistAggiornata.getPercorsoCopertina() != null){
                try {
                    Path cartellaCopertine = java.nio.file.Paths.get("AltriFile", "Copertine");

                    if(!java.nio.file.Files.exists(cartellaCopertine))
                        Files.createDirectories(cartellaCopertine); // teoricamente c'è per forza essendoci una modifica
                                                                    // ma non si sa mai!
                    File copertinaOriginale = new File(playlistAggiornata.getPercorsoCopertina());
                    Path targetCopertina = cartellaCopertine.resolve(copertinaOriginale.getName());
                    Files.copy(copertinaOriginale.toPath(), targetCopertina, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    String percorsoRelativo = "AltriFile/Copertine/" + copertinaOriginale.getName();
                    playlistAggiornata.setPercorsoCopertina(percorsoRelativo);

                } catch(java.io.IOException e){
                    e.printStackTrace();
                    throw e;
                }
            }
        }
        return PlaylistDAO.getInstance().aggiornaPlaylist(playlist, playlistAggiornata);
    }

    public boolean eliminaPlaylist(Playlist playlist) {
        return playlistDAO.getInstance().eliminaPlaylist(playlist);
    }

    public boolean aggiungiBranoAPlaylist(Playlist playlist, Brano brano) {
        return playlistBraniDAO.getInstance().aggiungiBranoAPlaylist(playlist, brano);
    }

    public boolean eliminaBranoDaPlaylist(Playlist playlist, Brano brano) {
        return playlistBraniDAO.getInstance().rimuoviBranoDaPlaylist(playlist, brano);
    }
}
