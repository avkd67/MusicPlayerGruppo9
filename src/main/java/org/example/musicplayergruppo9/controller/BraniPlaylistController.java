package org.example.musicplayergruppo9.controller;

import java.io.File;
import java.util.List;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.musicplayergruppo9.database.DAO.PlaylistBraniDAO;
import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.ElementoCodaConOpzioni;
import org.example.musicplayergruppo9.model.Playlist;
import org.example.musicplayergruppo9.pattern.command.AggiungiBranoPlaylistCommand;
import org.example.musicplayergruppo9.pattern.command.CommandHistory;
import org.example.musicplayergruppo9.pattern.command.RimuoviBranoPlaylistCommand;
import org.example.musicplayergruppo9.pattern.command.RimuoviPlaylistHomeCommand;
import org.example.musicplayergruppo9.pattern.observer.Observer;
import org.example.musicplayergruppo9.service.PlaylistService;
import org.example.musicplayergruppo9.pattern.strategy.StrategiaOrdineBrani;
import org.example.musicplayergruppo9.pattern.strategy.StrategiaOrdineSequenziale;
import org.example.musicplayergruppo9.pattern.strategy.StrategiaOrdineShuffle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.musicplayergruppo9.utilities.FXutilities;
import org.example.musicplayergruppo9.database.DAO.PlaylistDAO;

public class BraniPlaylistController implements Observer {

    // lista dei brani della playlist
    @FXML
    private ListView<Brano> listaBrani;


    @FXML
    private Label LblNomePlaylist;

    @FXML 
    private StackPane copertinaContainer;

    private ImageView imgCopertina = new ImageView();

    // playlist selezionata dall'utente
    @FXML
    private Playlist playlist;

    @FXML private Button btnRandom;
    @FXML private Button btnLoop;
    @FXML private Button btnPlay;

    private boolean randomAttivo = false; 
    private boolean loopAttivo = false;

    private boolean inRiproduzione = false;

    private ObservableList<Brano> braniObservableList;
    private PlaylistService playlistService;
    private CommandHistory commandHistory;
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        commandHistory = CommandHistory.getInstance();

        playlistService = PlaylistService.getInstance();
        playlistService.attach(this);

        braniObservableList = FXCollections.observableArrayList();
        listaBrani.setItems(braniObservableList);
        listaBrani.setCellFactory(param -> new BranoListCell());

        // Drag & drop handling is implemented per-cell in BranoListCell
    }

    private void persistOrder() {
        if (playlist == null || playlist.getId() <= 0) return;
        PlaylistDAO.getInstance().aggiornaOrdinePlaylist(playlist);
    }

    // segnaposto per l'aggiunta del nuovo brano
    private final Brano segnaposto_aggiungi = new Brano();

    // nel setplaylist associo tutta la logica del db
    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;

        LblNomePlaylist.setText(this.playlist.getNome());

        if(this.playlist.getPercorsoCopertina() != null){
            String uri = new File(this.playlist.getPercorsoCopertina()).toURI().toString();

            imgCopertina.setImage(new Image(uri));
            imgCopertina.setFitWidth(90);
            imgCopertina.setFitHeight(90);
            imgCopertina.setPreserveRatio(true);

            copertinaContainer.getChildren().add(imgCopertina);
        }
        else{
            Rectangle background = new Rectangle(90, 90);
            background.setFill(Color.LIGHTGRAY);
            background.setStroke(Color.GRAY);
            background.setStrokeWidth(1);
            background.setArcWidth(6);
            background.setArcHeight(6);

            // iconcina della nota musicale
            Image defaultIcon = new Image(getClass().getResourceAsStream("/org/example/musicplayergruppo9/img/music-note-icon-1.png"));
            ImageView iconView = new ImageView(defaultIcon);

            // grandezza dell'icona, 60x60 in un riquadro 90x90
            iconView.setFitWidth(60);
            iconView.setFitHeight(60);
            iconView.setPreserveRatio(true);
            // mettiamo tutti insieme nello StackPane
            copertinaContainer.getChildren().addAll(background, iconView);
        }

            List<Brano> braniRecuperati;

            if (this.playlist.getId() > 0) {
                braniRecuperati = playlistService.getBraniByPlaylist(this.playlist);
            } else {
                braniRecuperati = new java.util.ArrayList<>(this.playlist.getBrani());
            }

            braniObservableList.clear();
            braniObservableList.addAll(braniRecuperati);

            this.playlist.getBrani().clear();
            this.playlist.getBrani().addAll(braniRecuperati);
         // Aggiungo il placeholder finale 
        segnaposto_aggiungi.setId(-1);
        braniObservableList.add(segnaposto_aggiungi);

        if (mainController != null) {
            loopAttivo = mainController.isLoopPlaylistAttivo(this.playlist);
        }

        sincronizzaStatoPulsanti();
    }

    //legge lo stato reale da PlayerController
    private void sincronizzaStatoPulsanti() {
        
        setBottoneAttivo(btnRandom, randomAttivo);
        setBottoneAttivo(btnLoop, loopAttivo);

    }

    private StrategiaOrdineBrani getStrategiaOrdineCorrente() {
        if (randomAttivo) {
            return new StrategiaOrdineShuffle();
        }
        return new StrategiaOrdineSequenziale();
    }

    public void aggiungiBrano(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/musicplayergruppo9/fxml/AggiungiBranoPlaylistView.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stageInfo = new javafx.stage.Stage();
            stageInfo.setTitle("Aggiungi un brano");
            stageInfo.setScene(new javafx.scene.Scene(root));
            stageInfo.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            AggiungiBranoPlaylistController controller = loader.getController();
            controller.setPlaylist(playlist);
            controller.setPreviousController(this);

            stageInfo.showAndWait();
        } catch (java.io.IOException e) {
            System.err.println("Errore nel caricamento della vista AggiungiBranoPlaylistView.fxml");
            e.printStackTrace();
        }
    }

    public void eliminaBranoDaPlaylist(Brano brano) {
        if (!FXutilities.mostraAlertConferma("Rimozione brano",
                "Sei sicuro di voler rimuovere " + brano.getTitolo() + " dalla playlist?")) {
            return;
        }
    
        RimuoviBranoPlaylistCommand cmd =
                new RimuoviBranoPlaylistCommand(brano, playlist, new PlaylistBraniDAO());
    
        boolean successo = commandHistory.execute(cmd);
        if (successo) {
            playlist.rimuoviBrano(brano);//notifica PlaylistObserver (PlayerController)
            update();//aggiorna UI (BraniPlaylistController)
        }
    }
    

    // eliminazione della playlist selezionata
    @FXML
    public void onEliminaPlaylist(){
        if(FXutilities.mostraAlertConferma("Elimina playlist", "Sicuro di voler eliminare la playlist?")) {
            commandHistory.execute(new RimuoviPlaylistHomeCommand(playlist));
            FXutilities.chiudiFinestra(copertinaContainer);
        }
    }

    // quando premo "modifica playlist"
    public void onModifica(){
        System.out.println("Apertura della vista Modifica Playlist...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/musicplayergruppo9/fxml/ModificaPlaylistView.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stageModifica = new javafx.stage.Stage();
            stageModifica.setTitle("Modifica Playlist");
            stageModifica.setScene(new javafx.scene.Scene(root));

            ModificaPlaylistController controllerModifica = loader.getController();
            controllerModifica.setPlaylist(playlist);

            stageModifica.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            stageModifica.showAndWait();

            LblNomePlaylist.setText(this.playlist.getNome());

            aggiornaVisualizzazioneCopertina();

        } catch (java.io.IOException e) {
            System.err.println("Errore nel caricamento della vista ModificaPlaylist.fxml");
            e.printStackTrace();
        }
    }

    public void aggiungiBranoAPlaylist(Brano brano) {
        AggiungiBranoPlaylistCommand cmd =
                new AggiungiBranoPlaylistCommand(brano, playlist, new PlaylistBraniDAO());
    
        boolean successo = commandHistory.execute(cmd);
        if (successo) {
            playlist.aggiungiBrano(brano);
            update();
        }
    }

    

    //metodo per gestire il play nella finestra della playlist, con annesso shuffle
    @FXML
    public void playPlaylist() {
        if (playlist == null || mainController == null) return;

        if (!inRiproduzione) {
            mainController.svuotaCoda();
            mainController.inizializzaPlayerSeNecessario();

            ElementoCodaConOpzioni playlistConOpzioni =
                    new ElementoCodaConOpzioni(playlist, getStrategiaOrdineCorrente(),
                    loopAttivo);
            mainController.aggiungiInCoda(playlistConOpzioni);

            inRiproduzione = true;
            btnPlay.setText(" ⏸ ");
        } else {
            PlayerController pc = mainController.getPlayerController();
            if (pc != null) pc.togglePlayPause();
            inRiproduzione = false;
            btnPlay.setText(" ▶ ");
        }
    }

    @FXML
    public void aggiungiPlaylistAllaCoda() {

        if (playlist == null || mainController == null) return;

        ElementoCodaConOpzioni playlistConOpzioni =
            new ElementoCodaConOpzioni(playlist, getStrategiaOrdineCorrente(),
            loopAttivo);
        mainController.aggiungiInCoda(playlistConOpzioni);

    }

    @Override
    public void update() {
        // aggiorna i brani
        System.out.println("Si si... sto aggiornando i brani -_-/ ");
        
        braniObservableList.clear();

        List<Brano> braniAggiornati;

        if (playlist.getId() > 0) {
            braniAggiornati = playlistService.getBraniByPlaylist(playlist);
        } else {
            braniAggiornati = new java.util.ArrayList<>(playlist.getBrani());
        }

        braniObservableList.addAll(braniAggiornati);
        braniObservableList.add(segnaposto_aggiungi);

        // aggiorna nome e immagine di copertina
        LblNomePlaylist.setText(this.playlist.getNome());
        aggiornaVisualizzazioneCopertina();

    }

    public void aggiornaVisualizzazioneCopertina() {
        copertinaContainer.getChildren().clear();

        if (this.playlist.getPercorsoCopertina() != null) {
            String uri = new File(this.playlist.getPercorsoCopertina()).toURI().toString();

            imgCopertina.setImage(new Image(uri));
            imgCopertina.setFitWidth(90);
            imgCopertina.setFitHeight(90);
            imgCopertina.setPreserveRatio(true);

            copertinaContainer.getChildren().add(imgCopertina);
        } else {
            Rectangle background = new Rectangle(90, 90);
            background.setFill(Color.LIGHTGRAY);
            background.setStroke(Color.GRAY);
            background.setStrokeWidth(1);
            background.setArcWidth(6);
            background.setArcHeight(6);

            Image defaultIcon = new Image(getClass().getResourceAsStream("/org/example/musicplayergruppo9/img/music-note-icon-1.png"));
            ImageView iconView = new ImageView(defaultIcon);
            iconView.setFitWidth(60);
            iconView.setFitHeight(60);
            iconView.setPreserveRatio(true);

            copertinaContainer.getChildren().addAll(background, iconView);
        }
    }


    // per la visualizzazione dei brani !
    private class BranoListCell extends ListCell<Brano> {
        private HBox hboxContainer = new HBox(15);
        private ImageView imgCopertina = new ImageView();
        private Label lblTitolo = new Label();
        private Label lblAutore = new Label();
        private VBox vboxTesti = new VBox(5);
        private Label lblDurata = new Label();
        private Region spacer = new Region(); // Serve a spingere i pulsanti a destra

        // pulsanti
        private Button btnCoda = new Button("≡+");
        private Button btnPreferito = new Button("⭐");
        private Button btnInfo = new Button("ⓘ");
        private Button btnElimina = new Button("🗑");

        // questi dovranno essere cambiati per vere immagini, l'immagine di stella deve essere anche
        // vuota o piena a seconda di se è preferito

        // Definisce il layout grafico specifico per la riga speciale "Aggiungi Brano" (id -1)
        private HBox hboxAggiungi = new HBox(15);
        private ImageView imgAggiungi = new ImageView();
        private Label lblAggiungi = new Label("Aggiungi brano");

        public BranoListCell() {
            super();

            // Configurazione "Elemento" Brano
            hboxContainer.setAlignment(Pos.CENTER_LEFT);
            imgCopertina.setFitHeight(50);
            imgCopertina.setFitWidth(50);

            lblTitolo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            lblAutore.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");

            vboxTesti.getChildren().addAll(lblTitolo, lblAutore);

            // Spinge tutto ciò che viene dopo lo spacer all'estrema destra
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Vengono applicati stili e azioni ai bottoni
            HBox hboxBottoni = new HBox(5, lblDurata, btnCoda, btnPreferito, btnInfo, btnElimina);
            hboxBottoni.setAlignment(Pos.CENTER_RIGHT);
            lblDurata.setStyle("-fx-padding: 0 10 0 0;"); // Margine destro per la durata

            // Assembla la riga standard
            hboxContainer.getChildren().addAll(imgCopertina, vboxTesti, spacer, hboxBottoni);

            // Definisce l'azione di eliminazione (per la Task 1.4.3)
            btnElimina.setOnAction(e -> {
                Brano brano = getItem();
                System.out.println("Cliccato Elimina sul brano: " + brano.getTitolo());
                eliminaBranoDaPlaylist(brano);
            });

            //bottone per mettere in coda presente vicino ai brani di una playlist
            btnCoda.setOnAction(e -> {
                Brano brano = getItem();
                if (brano != null && brano.getId() != -1 && mainController != null) {
                    mainController.aggiungiInCoda(brano);
                    System.out.println("[BraniPlaylistController] Brano aggiunto in coda: " + brano.getTitolo());
                }
            });

            // Bottone Info apre l'infografica del brano
            btnInfo.setOnAction(e -> {
                Brano b = getItem();
                if (b != null && b.getId() != -1) {
                    apriVistaInfoBrano(b);
                }
            });


            // Configurazione "Elemento" Aggiungi
            hboxAggiungi.setAlignment(Pos.CENTER_LEFT);

            Image iconaPiu = new Image(getClass().getResourceAsStream("/org/example/musicplayergruppo9/img/plus_symbol.svg.png"));
            imgAggiungi.setImage(iconaPiu);
            imgAggiungi.setFitHeight(40);
            imgAggiungi.setFitWidth(40);
            hboxAggiungi.getChildren().addAll(imgAggiungi, lblAggiungi);

            // Rende l'intera riga cliccabile come se fosse un bottone
            hboxAggiungi.setOnMouseClicked(e -> aggiungiBrano());

            // Cambia il cursore con il ditp per far capire che è cliccabile
            hboxAggiungi.setStyle("-fx-cursor: hand;");

            // Drag-and-drop handlers for reordering rows
            setOnDragDetected(event -> {
                Brano item = getItem();
                if (item == null || item.getId() == -1) return;
                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent cc = new ClipboardContent();
                cc.putString(String.valueOf(getIndex()));
                db.setContent(cc);
                event.consume();
            });

            setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString() && event.getGestureSource() != this) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    try {
                        int srcIndex = Integer.parseInt(db.getString());
                        int targetIndex = getIndex();
                        if (srcIndex >= 0 && targetIndex >= 0 && srcIndex != targetIndex) {
                            Brano item = getListView().getItems().remove(srcIndex);
                            // if removing earlier shifts target left
                            if (srcIndex < targetIndex) targetIndex--;
                            getListView().getItems().add(targetIndex, item);

                            // update model playlist and persist
                            playlist.getBrani().clear();
                            for (Brano b : listaBrani.getItems()) if (b.getId() != -1) playlist.getBrani().add(b);
                            persistOrder();
                        }
                        success = true;
                    } catch (NumberFormatException ignored) {}
                }
                event.setDropCompleted(success);
                event.consume();
            });
        }

        // Funzione per aprire la schermata dedicata alle info del brano selezionato dall'utente
        private void apriVistaInfoBrano(Brano brano) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/musicplayergruppo9/fxml/InfoBrano.fxml"));
                javafx.scene.Parent root = loader.load();

                javafx.stage.Stage stageInfo = new javafx.stage.Stage();
                stageInfo.setTitle("Info brano");
                stageInfo.setScene(new javafx.scene.Scene(root));
                stageInfo.initModality(javafx.stage.Modality.APPLICATION_MODAL);

                InfoBranoController controller = loader.getController();
                controller.setBrano(brano);

                stageInfo.showAndWait();
            } catch (java.io.IOException e) {
                System.err.println("Errore nel caricamento della vista InfoBrano.fxml");
                e.printStackTrace();
            }
        }


            @Override
            protected void updateItem(Brano brano, boolean empty) {
                super.updateItem(brano, empty);

                if (empty || brano == null) {
                    // se la cella è vuota non mostro niente
                    setText(null);
                    setGraphic(null);
                } else if (brano.getId() == -1) {
                    // item "aggiungi brano"
                    setText(null);
                    setGraphic(hboxAggiungi);
                } else {
                    // mostro le info del brano normale
                    lblTitolo.setText(brano.getTitolo());
                    lblAutore.setText(brano.getArtista());
                    lblDurata.setText(brano.getDurataFormattata());

                    // copertina
                    File fileCopertina = brano.getCopertina();
                    if (fileCopertina != null && fileCopertina.exists()) {
                        imgCopertina.setImage(new Image(fileCopertina.toURI().toString()));
                    } else {
                        imgCopertina.setImage(null);
                    }

                    setText(null);
                    setGraphic(hboxContainer);
                }
            }

        }


    @FXML
    public void onUndo() {
        if (commandHistory.canUndo()) {
            commandHistory.undo();
            System.out.println("Ultima azione annullata dalla vista Playlist.");
            update();
        } else {
            System.out.println("Nessuna azione da annullare.");
        }
    }


    //playlist in ordine casuale shuffle
    @FXML
    public void onRandom() {
        
        randomAttivo = !randomAttivo;

        setBottoneAttivo(btnRandom, randomAttivo);

        //player è già aperto, aggiorna subito il PlayerController
        if (inRiproduzione && mainController != null) {
            PlayerController pc = mainController.getPlayerController();
            if (pc != null) {
                pc.aggiornaOpzioniPlaylistCorrente(
                    getStrategiaOrdineCorrente(),
                    loopAttivo
            );
            }
        }

    }

    //playlist in loop
    @FXML
    public void onLoop() {
        loopAttivo = !loopAttivo;
        setBottoneAttivo(btnLoop, loopAttivo);

        if (mainController != null) {
            mainController.setLoopPlaylistAttivo(playlist, loopAttivo);

            PlayerController pc = mainController.getPlayerController();
            if (pc != null) {
                pc.aggiornaOpzioniPlaylistCorrente(
                        getStrategiaOrdineCorrente(),
                        loopAttivo
                );
            }
        }
    }

    //chiamato da MainController quando la playlist finisce
    public void onFinePlaylist() {
        inRiproduzione = false;
        btnPlay.setText(" ▶ ");
    }

    private void setBottoneAttivo(Button btn, boolean attivo) {
        btn.getStyleClass().remove("button-attivo");
        if (attivo) {
            btn.getStyleClass().add("button-attivo");
        }
    }

}
