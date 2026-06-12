package org.example.musicplayergruppo9.pattern.command;

import org.example.musicplayergruppo9.database.DAO.PlaylistBraniDAO;
import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.Playlist;
import org.example.musicplayergruppo9.utilities.FXutilities;

public class AggiungiBranoPlaylistCommand implements Command {

    private final Brano brano;
    private final Playlist playlist;
    private final PlaylistBraniDAO playlistBraniDAO;

    public AggiungiBranoPlaylistCommand(Brano brano, Playlist playlist, PlaylistBraniDAO playlistBraniDAO) {
        this.brano = brano;
        this.playlist = playlist;
        this.playlistBraniDAO = playlistBraniDAO;
    }

    @Override
    public boolean execute() {
        return playlistBraniDAO.aggiungiBranoAPlaylist(playlist, brano);
    }

    @Override
    public void undo() {
        boolean rimosso = playlistBraniDAO.rimuoviBranoDaPlaylist(playlist, brano);
        if (rimosso) {
            FXutilities.mostraAlertSuccesso("Annullamento", "Brano rimosso dalla playlist.");
        }
    }
}