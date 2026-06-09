package org.example.musicplayergruppo9.controller;

import java.io.File;
import java.io.IOException;

import org.example.musicplayergruppo9.database.DAO.PlaylistDAO;
import org.example.musicplayergruppo9.model.Playlist;
import org.example.musicplayergruppo9.pattern.command.AggiungiPlaylistCommand;
import org.example.musicplayergruppo9.pattern.command.CommandHistory;
import org.example.musicplayergruppo9.pattern.observer.Observer;
import org.example.musicplayergruppo9.service.PlaylistService;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.musicplayergruppo9.utilities.FXutilities;

public class CreaPlaylistController implements Observer {

    @FXML private TextField nomePlaylist;
    @FXML private ImageView imgCopertina;

    private PlaylistService playlistService;

    private String percorsoCopertinaSelezionata = null;

    @FXML
    public void initialize() {
        playlistService = PlaylistService.getInstance();
    }

    // selezione dell'immagine della copertina della playlist
    @FXML
    private void onSfogliaCopertina() {
        percorsoCopertinaSelezionata = FXutilities.cercaCopertina(imgCopertina);
    }

    @FXML
    private void onAnnulla() {
        FXutilities.chiudiFinestra(nomePlaylist);
    }

    @FXML
    private void onOk() {

        if(!nomePlaylist.getText().isBlank()){
            Playlist playlist = new Playlist(nomePlaylist.getText(), percorsoCopertinaSelezionata);

            if(!playlistService.checkNomePlaylist(playlist)){
                FXutilities.mostraAlertErrore("Errore", "Nome playlist già esistente", "Esiste già una playlist con questo nome. Scegliere un nome diverso.");
                return;
            }

            try {
                PlaylistDAO playlistDAO = PlaylistDAO.getInstance();
                AggiungiPlaylistCommand comandoCrea = new AggiungiPlaylistCommand(playlist, playlistDAO.getInstance());

                boolean successo = CommandHistory.getInstance().execute(comandoCrea);

                if(successo){
                    FXutilities.chiudiFinestra(nomePlaylist);

                    playlistService.notifyObservers();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        else {
            FXutilities.mostraAlertErrore("Errore", "Nome vuoto", "Il nome della playlist non può essere lasciato in bianco.");
        }
    }

    @Override
    public void update() {

    }
}
