package org.example.musicplayergruppo9.pattern.command;

import org.example.musicplayergruppo9.database.DAO.PlaylistDAO;
import org.example.musicplayergruppo9.model.Playlist;
import org.example.musicplayergruppo9.utilities.FXutilities;

public class AggiungiPlaylistCommand implements Command {

    private final Playlist playlist;
    private final PlaylistDAO playlistDAO;

    public AggiungiPlaylistCommand(Playlist playlist, PlaylistDAO playlistDAO) {
        this.playlist = playlist;
        this.playlistDAO = playlistDAO;
    }

    @Override
    public boolean execute() {
        return playlistDAO.salvaPlaylist(playlist); // Assicurati del nome corretto del metodo nel DAO
    }

    @Override
    public void undo() {
        boolean eliminata = playlistDAO.eliminaPlaylist(playlist);
        if (eliminata) {
            FXutilities.mostraAlertSuccesso("Annullamento", "Creazione playlist annullata.");
        }
    }
}