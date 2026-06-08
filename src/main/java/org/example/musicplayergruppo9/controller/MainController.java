package org.example.musicplayergruppo9.controller;

import java.io.IOException;

import org.example.musicplayergruppo9.model.Brano;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.example.musicplayergruppo9.model.ElementoCoda;
import org.example.musicplayergruppo9.pattern.command.CommandHistory;

public class MainController {

    @FXML
    private BorderPane mainContainer; // Riferimento al layout principale

    @FXML
    private StackPane areaContenuti; // Lo StackPane che fa da schermo per le due view Home e Libreria

    private PlayerController playerController = new PlayerController();
    private boolean playerAperto = false;
    private CommandHistory commandHistory = CommandHistory.getInstance();

    // Metodo collegato al click del tasto "Home" a sinistra
    @FXML
    public void mostraHome() {
        try {
            // Si carica il file fxml della Home (che sarà un AnchorPane o VBox a sé stante)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/musicplayergruppo9/fxml/HomeView.fxml"));
            Node viewHome = loader.load();

            // Inietto il riferimento al MainController nella Home
            org.example.musicplayergruppo9.controller.HomeController homeController = loader.getController();
            if (homeController != null) homeController.setMainController(this);

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

            // INIEZIONE: Passiamo al LibreriaController un riferimento a questo MainController
            LibreriaController libreriaController = loader.getController();
            libreriaController.setMainController(this);

            areaContenuti.getChildren().setAll(viewLibreria);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void play() {
        System.out.println("Pulsante Play cliccato!");
    }

    //task 6.2 lo skip viene delegato a PlayerController
    @FXML
    public void skipSong() {
        //delega al PlayerController
        if (playerController != null) playerController.skipSong();
    }

    @FXML
    public void skipPlaylist() {
        System.out.println("Pulsante skipPlaylist cliccato!");
    }

    //task 7.2 Delega il loop al PlayerController
    @FXML
    public void loopSong() {
        //delega al PlayerController
        if (playerController != null) playerController.loopSong();
    }

    @FXML
    public void unDo() {
        commandHistory.undo();
    }

    //Chiamato quando l'utentne clicca su brano/playlist, 
    // e aggiunge l'elemento alla coda o apre la player
    public void apriPlayer(ElementoCoda elemento) {
        if(!playerAperto){
            try {
                FXMLLoader loader = new FXMLLoader(getClass()
                        .getResource("/org/example/musicplayergruppo9/fxml/PlayerView.fxml"));
                Node playerView = loader.load();

                // PASSA I DATI: Prendi il controller del player e passagli il brano
                playerController = (PlayerController) loader.getController();

                // Inserisce dinamicamente il player nella parte in basso del BorderPane
                mainContainer.setBottom(playerView);
                playerAperto = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //aggiunge in coda
        if (playerController != null) {
            if (elemento instanceof Brano) {
                playerController.setBrano((Brano) elemento);
            } else {
                playerController.aggiungiInCoda(elemento);
            }
        }
    }

    //accetta ancora un Brano direttamente
    public void apriPlayer(Brano branoSelezionato) {
        apriPlayer((ElementoCoda) branoSelezionato);
    }

    public PlayerController getPlayerController() {
        return playerController;
    }

    //per svolgere il task 6.2 
    //aggiunge un elemnto in coda senza interrompere la riproduzione attuale
    //tramite il tasto, mette l'elemento in coda senza riprodurre subito
    public void aggiungiInCoda(ElementoCoda elemento) {
        // Apre il player la prima volta se non è ancora aperto
        if (!playerAperto) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/org/example/musicplayergruppo9/fxml/PlayerView.fxml"));
                Node playerView = loader.load();
                playerController = (PlayerController) loader.getController();
                mainContainer.setBottom(playerView);
                playerAperto = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (playerController != null) {
            playerController.aggiungiInCoda(elemento);
        }
    }

}