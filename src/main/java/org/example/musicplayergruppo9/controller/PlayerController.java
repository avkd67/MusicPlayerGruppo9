package org.example.musicplayergruppo9.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.musicplayergruppo9.model.Brano;

import java.io.File;

public class PlayerController {

    @FXML
    private ImageView imgCopertina;

    @FXML
    private Label lblTitolo;
    @FXML
    private Label lblArtista;


    Brano branoInRiproduzione = new Brano();

    // Metodo per ricevere il brano e aggiornare la UI
    public void setBrano(Brano brano) {
        branoInRiproduzione = brano;
        lblTitolo.setText(brano.getTitolo());
        lblArtista.setText(brano.getArtista());
        File fileCopertina = brano.getCopertina();
        if (fileCopertina != null && fileCopertina.exists()) {
            imgCopertina.setImage(new Image(fileCopertina.toURI().toString()));
        } else {
            imgCopertina.setImage(null);
        }
    }

    @FXML
    public void play() { System.out.println("Pulsante Play cliccato!"); }

    @FXML
    public void skipSong() { System.out.println("Pulsante skip cliccato!"); }

    @FXML
    public void skipPlaylist() { System.out.println("Pulsante skipPlaylist cliccato!"); }

    @FXML
    public void loopSong() { System.out.println("Pulsante loop cliccato!"); }
}