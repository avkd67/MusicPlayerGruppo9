package org.example.musicplayergruppo9.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.service.PlayerService;

import java.io.File;

public class PlayerController {

    @FXML private ImageView imgCopertina;
    @FXML private Label lblTitolo;
    @FXML private Label lblArtista;
    @FXML private Button btnPlayPause;
    @FXML private Label lblTempo;
    @FXML private Slider sliderProgresso;

    private static PlayerService playerService;

    private Brano branoInRiproduzione;

    @FXML
    public void initialize() {
        if (playerService == null) {
            playerService = new PlayerService();
        }

        // Questo va fatto sempre per ricollegare l'interfaccia grafica aggiornata
        playerService.setCallbacks(
                testoBottone -> btnPlayPause.setText(testoBottone),
                tempoAttuale -> aggiornaUIProgresso(tempoAttuale),
                () -> sliderProgresso.setMax(playerService.getTotalDuration().toSeconds())
        );
    }

    public void setBrano(Brano brano) {
        this.branoInRiproduzione = brano;
        lblTitolo.setText(brano.getTitolo());
        lblArtista.setText(brano.getArtista());

        File fileCopertina = brano.getCopertina();
        if (fileCopertina != null && fileCopertina.exists()) {
            imgCopertina.setImage(new Image(fileCopertina.toURI().toString()));
        } else {
            imgCopertina.setImage(null);
        }

        playerService.loadTrack(brano);
    }

    @FXML
    public void togglePlayPause() {
        playerService.togglePlayPause();
    }

    private void aggiornaUIProgresso(Duration currentTime) {

        int minuti = (int) currentTime.toMinutes();
        int secondi = (int) currentTime.toSeconds() % 60;
        lblTempo.setText(String.format("%02d:%02d", minuti, secondi));
    }

    @FXML
    public void skipSong() { System.out.println("Pulsante skip cliccato!"); }

    @FXML
    public void skipPlaylist() { System.out.println("Pulsante skipPlaylist cliccato!"); }

    @FXML
    public void loopSong() { System.out.println("Pulsante loop cliccato!"); }
}