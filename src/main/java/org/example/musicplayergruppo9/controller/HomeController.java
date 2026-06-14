package org.example.musicplayergruppo9.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.image.Image;
import org.example.musicplayergruppo9.pattern.observer.Observer;
import org.example.musicplayergruppo9.service.PlaylistService;
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
import javafx.application.Platform;
import org.example.musicplayergruppo9.service.StatisticheService;

public class HomeController implements Observer {

    // il flowpane permette la visualizzazione a griglia e va a capo automaticamente
    @FXML
    private FlowPane playlistsFlowPane;

    @FXML
    private FlowPane suggeriteFlowPane;

    @FXML
    private FlowPane topPlaylistFlowPane;

    private ObservableList<Playlist> playlistsObservableList;
    private PlaylistService playlistService;
    private ArrayList<Playlist> playlistRecuperate;

    // Riferimento al MainController, verrà iniettato da MainController
    private MainController mainController;

    // segnaposto per il tasto aggiungi playlist
    private final Playlist segnaposto_aggiungi = new Playlist();

    private final Map<Integer, Stage> finestrePlaylistAperte = new HashMap<>();

    private StatisticheService statisticheService;

    @FXML
    public void initialize() {
        playlistService = PlaylistService.getInstance();
        playlistService.attach(this);

        statisticheService = StatisticheService.getInstance();
        statisticheService.attach(this);

        // prendo le playlists presenti nel db e ci lego l'observable list
        playlistRecuperate = playlistService.getAllPlaylists();
        playlistsObservableList = FXCollections.observableArrayList(playlistRecuperate);

        segnaposto_aggiungi.setId(-1);
        playlistsObservableList.add(segnaposto_aggiungi);

        ArrayList<Playlist> playlistAutomatiche = new ArrayList<>();
        playlistAutomatiche.add(playlistService.getPlaylistPreferiti());

        playlistAutomatiche.add(statisticheService.getPlaylistBraniPiuRiprodotti());

        playlistAutomatiche.addAll(playlistService.getPlaylistsByAnno());

        playlistAutomatiche.addAll(playlistService.getPlaylistsByGenere());

        mostraPlaylists();
        mostraPlaylistSuggerite(playlistAutomatiche);
        mostraPlaylistPiuRiprodotte();

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

    private void mostraPlaylistSuggerite(ArrayList<Playlist> playlistAutomatiche){
        suggeriteFlowPane.getChildren().clear();

        for(Playlist p : playlistAutomatiche) {
            suggeriteFlowPane.getChildren().add(createPlaylistCard(p));
        }
    }

    // Card per una playlist esistente
    private VBox createPlaylistCard(Playlist playlist) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(6));

        // Copertina 
        javafx.scene.Node cover; // inizializzazione generale, per far riconoscere al codice "cover" anche fuori dall'if
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
        nome.setAlignment(Pos.TOP_CENTER);
        nome.setWrapText(true);
        nome.setMaxWidth(90);

        card.getChildren().addAll(cover, nome);

        // se cliccata: apri la playlist
        card.setOnMouseClicked(e -> apriPlaylist(playlist));
        card.setStyle("-fx-cursor: hand;");

        return card;
    }

    // card con il "+" per aggiungere una playlist
    private VBox createAddCard() {
        VBox card = new VBox(6);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(6));

        StackPane addBox = new StackPane();
        Rectangle background = new Rectangle(90, 90);
        background.setFill(Color.WHITE);
        background.setStroke(Color.GRAY);
        background.setStrokeWidth(1.5);
        background.setArcWidth(6);
        background.setArcHeight(6);

        Image iconaPiu = new Image(getClass().getResourceAsStream("/org/example/musicplayergruppo9/img/plus_symbol.svg.png"));
        ImageView plusImg = new ImageView(iconaPiu);
        plusImg.setFitWidth(60);
        plusImg.setFitHeight(60);
        plusImg.setPreserveRatio(true);
        addBox.getChildren().addAll(background, plusImg);

        Label etichetta = new Label("Aggiungi\nPlaylist");
        etichetta.setFont(Font.font("System", 12));
        etichetta.setTextAlignment(TextAlignment.CENTER);
        etichetta.setAlignment(Pos.TOP_CENTER);


        // onPlus è la funzione associata al pulsante +
        card.getChildren().addAll(addBox, etichetta);
        card.setOnMouseClicked(e -> onPlus());
        card.setStyle("-fx-cursor: hand;");

        return card;
    }

    private void apriPlaylist(Playlist playlist) {
        try {
            int playlistId = playlist.getId();

            if (finestrePlaylistAperte.containsKey(playlistId)) {
                Stage stageGiaAperto = finestrePlaylistAperte.get(playlistId);

                if (stageGiaAperto != null && stageGiaAperto.isShowing()) {
                    stageGiaAperto.toFront();
                    stageGiaAperto.requestFocus();
                    return;
                } else {
                    finestrePlaylistAperte.remove(playlistId);
                }
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/musicplayergruppo9/fxml/BraniPlaylistView.fxml")
            );

            Parent root = loader.load();
            BraniPlaylistController controllerDestinazione = loader.getController();

            controllerDestinazione.setMainController(this.mainController);
            this.mainController.setBraniPlaylistController(controllerDestinazione);
            controllerDestinazione.setPlaylist(playlist);

            Stage stage = new Stage();
            stage.setTitle("Playlist: " + playlist.getNome());
            stage.setScene(new Scene(root));

            finestrePlaylistAperte.put(playlistId, stage);

            stage.setOnCloseRequest(e -> finestrePlaylistAperte.remove(playlistId));

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
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
            //ArrayList<Playlist> aggiornate = playlistService.getAllPlaylists();
            //playlistsObservableList = FXCollections.observableArrayList(aggiornate);
            //playlistsObservableList.add(segnaposto_aggiungi);
            update();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::update);
            return;
        }

        playlistsObservableList.clear();
        playlistRecuperate = playlistService.getAllPlaylists();
        playlistsObservableList.addAll(playlistRecuperate);
        playlistsObservableList.add(segnaposto_aggiungi);
        mostraPlaylists();

        // ricalcolo per aggiornare le automatiche
        ArrayList<Playlist> playlistAutomatiche = new ArrayList<>();
        playlistAutomatiche.add(playlistService.getPlaylistPreferiti());

        playlistAutomatiche.add(statisticheService.getPlaylistBraniPiuRiprodotti());

        playlistAutomatiche.addAll(playlistService.getPlaylistsByAnno());

        playlistAutomatiche.addAll(playlistService.getPlaylistsByGenere());

        mostraPlaylistSuggerite(playlistAutomatiche);
        mostraPlaylistPiuRiprodotte();
        
    }

    private void mostraPlaylistPiuRiprodotte() {
        topPlaylistFlowPane.getChildren().clear();
    
        for (Playlist p : statisticheService.getPlaylistPiuRiprodotte()) {
            topPlaylistFlowPane.getChildren().add(createPlaylistCard(p));
        }
    }

}