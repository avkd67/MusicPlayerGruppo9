package org.example.musicplayergruppo9.controller;

import java.io.File;
import java.io.IOException;

import org.example.musicplayergruppo9.model.Playlist;
import org.example.musicplayergruppo9.service.PlaylistService;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class CreaPlaylistController {

    @FXML private TextField nomePlaylist;
    @FXML private ImageView imgCopertina;

    private PlaylistService playlistService;

    private String percorsoCopertinaSelezionata = null;

    @FXML
    public void initialize() {
        playlistService = new PlaylistService();
    }

    // selezione dell'immagine della copertina della playlist
    @FXML
    private void onSfogliaCopertina() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona l'immagine di copertina");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Immagini (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg") //grazie .ExtensionFilter
        );

        Stage stage = (Stage) imgCopertina.getScene().getWindow();
        File fileSelezionato = fileChooser.showOpenDialog(stage);

        if (fileSelezionato != null) {
            percorsoCopertinaSelezionata = fileSelezionato.getAbsolutePath();
            // Carica l'immagine nell'anteprima grafica ImageView
            imgCopertina.setImage(new Image(fileSelezionato.toURI().toString()));
        }
    }

    @FXML
    private void onAnnulla() {
        chiudiFinestra();
    }

    @FXML
    private void onOk() {

        if(!nomePlaylist.getText().isBlank()){
            Playlist playlist = new Playlist(nomePlaylist.getText(), percorsoCopertinaSelezionata);

            if(!playlistService.checkNomePlaylist(playlist)){
                mostraAlertErrore("Errore", "Nome playlist già esistente", "Esiste già una playlist con questo nome. Scegliere un nome diverso.");
                return;
            }

            try {
                if(playlistService.salvaPlaylist(playlist)){
                    chiudiFinestra();
                }
                else {
                    System.err.println("Errore: la playlist non è stata salvata. Controllare il nome della playlist");

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            mostraAlertErrore("Errore", "Nome vuoto", "Il nome della playlist non può essere lasciato in bianco.");
        }
    }

    private void chiudiFinestra() {
        // Recupera lo stage corrente partendo da uno qualsiasi dei nodi grafici e lo chiude
        Stage stage = (Stage) nomePlaylist.getScene().getWindow();
        stage.close();
    }

    private void mostraAlertErrore(String titolo, String header, String contenuto) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(header);
        alert.setContentText(contenuto);
        alert.showAndWait();
    }
}
