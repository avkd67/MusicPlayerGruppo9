package org.example.musicplayergruppo9.controller;

import java.io.File;
import java.util.List;

import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.Playlist;
import org.example.musicplayergruppo9.pattern.command.CommandHistory;
import org.example.musicplayergruppo9.pattern.command.RimuoviPlaylistHomeCommand;
import org.example.musicplayergruppo9.pattern.observer.Observer;
import org.example.musicplayergruppo9.service.PlaylistService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.musicplayergruppo9.utilities.FXutilities;

public class BraniPlaylistController implements Observer {

    // lista dei brani della playlist
    @FXML
    private ListView<Brano> listaBrani;

    @FXML
    private Label LblNomePlaylist;

    @FXML 
    private ImageView imgCopertina; 

    // playlist selezionata dall'utente
    @FXML
    private Playlist playlist;

    private ObservableList<Brano> braniObservableList;
    private PlaylistService playlistService;
    private CommandHistory commandHistory;
    // Riferimento al MainController per poter aprire/controllare il player
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
    }

    // segnaposto per l'aggiunta del nuovo brano
    private final Brano segnaposto_aggiungi = new Brano();

    // nel setplaylist associo tutta la logica del db
    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;

        LblNomePlaylist.setText(this.playlist.getNome());

         // Mette la copertina della playlist, se esiste
        if(this.playlist.getPercorsoCopertina() != null)
            imgCopertina.setImage(new Image(new File(this.playlist.getPercorsoCopertina()).toURI().toString()));

        // carico i brani associati a quella playlist dal database
        List<Brano> braniRecuperati = playlistService.getBraniByPlaylist(this.playlist);
        braniObservableList.addAll(braniRecuperati);

        // Assicuro che la Playlist contenga internamente i brani recuperati
        // in modo che il suo iterator() non sia vuoto quando viene messa in coda
        this.playlist.getBrani().clear();
        this.playlist.getBrani().addAll(braniRecuperati);

         // Aggiungo il placeholder finale 
        segnaposto_aggiungi.setId(-1);
        braniObservableList.add(segnaposto_aggiungi);
    }

    // TODO: aggiunta brano dalla lista di tutti i brani / creazione sul momento ?
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

    // TODO: eliminazione brano dalla playlist !
    public void eliminaBranoDaPlaylist(Brano brano){
        if(FXutilities.mostraAlertConferma("Rimozione brano", "Sei sicuro di voler rimuovere " + brano.getTitolo() + " dalla playlist?"))
            playlistService.eliminaBranoDaPlaylist(playlist, brano);
        update();
    }

    // eliminazione della playlist selezionata
    @FXML
    public void onEliminaPlaylist(){
        if(FXutilities.mostraAlertConferma("Elimina playlist", "Sicuro di voler eliminare la playlist?")) {
            commandHistory.execute(new RimuoviPlaylistHomeCommand(playlist));
            FXutilities.chiudiFinestra(imgCopertina);
        }
    }

    // quando premo "modifica playlist"
    public void onModifica(){
        System.out.println("Apertura della vista Modifica Playlist...");
        try {
            // Carica il file FXML della vista Modifica Playlist
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/musicplayergruppo9/fxml/ModificaPlaylistView.fxml"));
            javafx.scene.Parent root = loader.load();

            // crea una nuova finestra popup
            javafx.stage.Stage stageModifica = new javafx.stage.Stage();
            stageModifica.setTitle("Modifica Playlist");
            stageModifica.setScene(new javafx.scene.Scene(root));

            // mando le info sulla playlist alla schermata di modifica della stessa
            ModificaPlaylistController controllerModifica = loader.getController();
            controllerModifica.setPlaylist(playlist);

            // per fare in modo che non si possa cliccare dietro
            stageModifica.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            stageModifica.showAndWait();

            LblNomePlaylist.setText(this.playlist.getNome());

            if(this.playlist.getPercorsoCopertina() != null)
                imgCopertina.setImage(new Image(new File(this.playlist.getPercorsoCopertina()).toURI().toString()));

        } catch (java.io.IOException e) {
            System.err.println("Errore nel caricamento della vista ModificaPlaylist.fxml");
            e.printStackTrace();
        }
    }

    public void aggiungiBranoAPlaylist(Brano brano){
        playlist.aggiungiBrano(brano);
        playlistService.aggiungiBranoAPlaylist(playlist, brano);
        update();
    }

    // Metodo chiamato dal bottone Play nella vista Playlist
    @FXML
    public void playPlaylist() {
        if (playlist == null) return;
        if (mainController != null) {
            // Apre il player (se necessario) e aggiunge la playlist in coda
            mainController.apriPlayer(playlist);
            System.out.println("[BraniPlaylistController] Riproduzione playlist richiesta: " + playlist.getNome());
        } else {
            System.out.println("[BraniPlaylistController] MainController non impostato. Impossibile riprodurre playlist.");
        }
    }

    // Metodo chiamato dal pulsante 'Aggiungi alla coda' nella vista Playlist
    @FXML
    public void aggiungiPlaylistAllaCoda() {
        if (playlist == null) return;
        if (mainController != null) {
            mainController.aggiungiInCoda(playlist);
            System.out.println("[BraniPlaylistController] Playlist aggiunta in coda: " + playlist.getNome());
        } else {
            System.out.println("[BraniPlaylistController] MainController non impostato. Impossibile aggiungere in coda.");
        }
    }

    @Override
    public void update() {
        // aggiorna i brani
        System.out.println("Si si... sto aggiornando i brani -_-/ ");
        braniObservableList.clear();
        List<Brano> braniAggiornati = playlistService.getBraniByPlaylist(playlist);
        braniObservableList.addAll(braniAggiornati);
        braniObservableList.add(segnaposto_aggiungi);

        // aggiorna nome e immagine di copertina
        LblNomePlaylist.setText(this.playlist.getNome());
        if(this.playlist.getPercorsoCopertina() != null)
            imgCopertina.setImage(new Image(new File(this.playlist.getPercorsoCopertina()).toURI().toString()));

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
}
