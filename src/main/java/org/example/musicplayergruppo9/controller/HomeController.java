package org.example.musicplayergruppo9.controller;

import java.io.IOException;
import java.util.ArrayList;

import org.example.musicplayergruppo9.database.DAO.PlaylistDAO;
import org.example.musicplayergruppo9.model.Playlist;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class HomeController {

    // il flowpane permette la visualizzazione a griglia e va a capo automaticamente
    @FXML
    private FlowPane playlistsFlowPane; 

    private ObservableList<Playlist> playlistsObservableList;
    private PlaylistDAO playlistDAO;

    // segnaposto per il tasto aggiungi playlist
    private final Playlist segnaposto_aggiungi = new Playlist();

    @FXML
    public void initialize() {
        playlistDAO = PlaylistDAO.getInstance();

        // prendo le playlists presenti nel db e ci lego l'observable list
        ArrayList<Playlist> playlistRecuperate = playlistDAO.getAllPlaylists();
        playlistsObservableList = FXCollections.observableArrayList(playlistRecuperate);

        segnaposto_aggiungi.setId(-1);
        playlistsObservableList.add(segnaposto_aggiungi);

        mostraPlaylists();
    }

    private void mostraPlaylists() {
        playlistsFlowPane.getChildren().clear();

        // se la playlist ha id -1 è il segnaposto, altrimenti è una playlist reale con veri dati
        for (Playlist p : playlistsObservableList) {
            if (p.getId() == -1) {
                playlistsFlowPane.getChildren().add(createAddCard());
            } else {
                playlistsFlowPane.getChildren().add(createPlaylistCard(p));
            }
        }
    }

    // Card per una playlist esistente
    private VBox createPlaylistCard(Playlist playlist) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(6));

        // Copertina 
        javafx.scene.Node cover; // inizializzazione generale, per riconoscere cover anche fuori dall'if
        if(playlist.getPercorsoCopertina() == null){

            // quando la copertina non è stata caricata
            Rectangle coverRect = new Rectangle(90, 90);
            coverRect.setFill(Color.LIGHTGRAY);
            coverRect.setStroke(Color.GRAY);
            coverRect.setStrokeWidth(1);
            coverRect.setArcWidth(6);
            coverRect.setArcHeight(6);
            cover = coverRect;
            } 
            else
                {
                    // quando la copertina è stata caricata

                    String uri = new java.io.File(playlist.getPercorsoCopertina()).toURI().toString();
                    ImageView coverImg = new ImageView(uri);
                    coverImg.setFitWidth(90);
                    coverImg.setFitHeight(90);
                    cover = coverImg;
                }

        // nome della playlist
        Label nome = new Label(playlist.getNome());
        nome.setFont(Font.font("System", 12));
        nome.setTextAlignment(TextAlignment.CENTER);
        nome.setWrapText(true);
        nome.setMaxWidth(90);

        card.getChildren().addAll(cover, nome);

        // Click: apri la playlist
        card.setOnMouseClicked(e -> apriPlaylist(playlist));
        card.setStyle("-fx-cursor: hand;");

        return card;
    }

    // card con il "+" per aggiungere una playlist
    private VBox createAddCard() {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(6));

        StackPane addBox = new StackPane();
        Rectangle background = new Rectangle(90, 90);
        background.setFill(Color.WHITE);
        background.setStroke(Color.GRAY);
        background.setStrokeWidth(1.5);
        background.setArcWidth(6);
        background.setArcHeight(6);

        Label plus = new Label("+");
        plus.setFont(Font.font("System", 36));
        plus.setTextFill(Color.DARKGRAY);

        addBox.getChildren().addAll(background, plus);

        Label etichetta = new Label("Aggiungi\nPlaylist");
        etichetta.setFont(Font.font("System", 12));
        etichetta.setTextAlignment(TextAlignment.CENTER);

        card.getChildren().addAll(addBox, etichetta);
        card.setOnMouseClicked(e -> onPlus());
        card.setStyle("-fx-cursor: hand;");

        return card;
    }

    // funzione che, al press di una playlist, ne apre la vista coi suoi brani
    private void apriPlaylist(Playlist playlist) {
        try {

            // passo al nuovo controller la playlist selezionata dall'utente
            FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/org/example/musicplayergruppo9/fxml/BraniPlaylistView.fxml")
            );
            Parent root = loader.load();
            BraniPlaylistController controllerDestinazione = loader.getController();
            controllerDestinazione.setPlaylist(playlist);

            Stage stage = new Stage();
            stage.setTitle("Playlist: " + playlist.getNome());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // tasto "+", apre il popup per creare una nuova playlist 
    @FXML
    public void onPlus() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/musicplayergruppo9/fxml/CreaPlaylistView.fxml")
            );
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.setTitle("Crea Nuova Playlist");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();

            // Ricarica le playlist dopo la creazione
            ArrayList<Playlist> aggiornate = playlistDAO.getAllPlaylists();
            playlistsObservableList = FXCollections.observableArrayList(aggiornate);
            playlistsObservableList.add(segnaposto_aggiungi);
            mostraPlaylists();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}