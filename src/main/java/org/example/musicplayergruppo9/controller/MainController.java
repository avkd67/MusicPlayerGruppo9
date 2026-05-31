package org.example.musicplayergruppo9.controller;

import java.io.IOException;

import org.example.musicplayergruppo9.model.Brano;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML
    private BorderPane mainContainer; // Riferimento al layout principale

    @FXML
    private StackPane areaContenuti; // Lo StackPane che fa da schermo per le due view Home e Libreria

    // Metodo collegato al click del tasto "Home" a sinistra
    @FXML
    public void mostraHome() {
        try {
            // Si carica il file fxml della Home (che sarà un AnchorPane o VBox a sé stante)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/musicplayergruppo9/fxml/HomeView.fxml"));
            Node viewHome = loader.load();

            // Si svuota l'area contenuti e inserisci la nuova view
            areaContenuti.getChildren().setAll(viewHome);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // inizializzazione della home
    @FXML
    public void initialize() {
        mostraHome(); // Mostra la home all'avvio dell'app
    }

    // Metodo collegato al click del tasto "Libreria" a sinistra
    @FXML
    public void mostraLibreria() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/musicplayergruppo9/fxml/LibreriaView.fxml"));
            Node viewLibreria = loader.load();

            areaContenuti.getChildren().setAll(viewLibreria);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void play() {
        System.out.println("Pulsante Play cliccato!");
    }

    @FXML
    public void skipSong() {
        System.out.println("Pulsante skip cliccato!");
    }

    @FXML
    public void skipPlaylist() {
        System.out.println("Pulsante skipPlaylist cliccato!");
    }

    @FXML
    public void loopSong() {
        System.out.println("Pulsante loop cliccato!");
    }

    @FXML
    public void unDo() {
        System.out.println("Pulsante unDo cliccato!");
    }

    public void apriPlayer(Brano branoSelezionato) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/musicplayergruppo9/fxml/PlayerView.fxml"));
            Node playerView = loader.load();

            // Inserisce dinamicamente il player nella parte in basso del BorderPane
            mainContainer.setBottom(playerView);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}