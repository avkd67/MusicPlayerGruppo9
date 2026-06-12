package org.example.musicplayergruppo9.pattern.command;

import org.example.musicplayergruppo9.database.DAO.PlaylistBraniDAO;
import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.Playlist;
import org.example.musicplayergruppo9.utilities.FXutilities;

public class RimuoviBranoPlaylistCommand implements Command {

    private final Brano brano;
    private final Playlist playlist;
    private final PlaylistBraniDAO playlistBraniDAO;

    public RimuoviBranoPlaylistCommand(Brano brano, Playlist playlist, PlaylistBraniDAO playlistBraniDAO) {
        this.brano = brano;
        this.playlist = playlist;
        this.playlistBraniDAO = playlistBraniDAO;
    }

    @Override
    public boolean execute() {
        return playlistBraniDAO.rimuoviBranoDaPlaylist(playlist, brano);
    }

    @Override
    public void undo() {
        boolean aggiunto = playlistBraniDAO.aggiungiBranoAPlaylist(playlist, brano);
        if (aggiunto) {
            FXutilities.mostraAlertSuccesso("Annullamento", "Brano ripristinato nella playlist.");
        }
    }
}