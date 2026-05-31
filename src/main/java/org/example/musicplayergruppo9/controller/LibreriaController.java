package org.example.musicplayergruppo9.controller;

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
import org.example.musicplayergruppo9.database.DAO.BranoDAO;
import org.example.musicplayergruppo9.model.Brano;

import java.io.File;
import java.util.List;

public class LibreriaController {

    @FXML
    private ListView<Brano> listaBrani;

    private ObservableList<Brano> braniObservableList;
    private BranoDAO branoDAO;

    // Un oggetto vuoto, usato come "segnaposto" per l'ultima riga
    private final Brano SEGNAPOSTO_AGGIUNGI = new Brano();

    @FXML
    public void initialize() {
        branoDAO = BranoDAO.getInstance();

        // Carica i brani reali dal database
        List<Brano> braniRecuperati = branoDAO.getTuttiIBrani();
        braniObservableList = FXCollections.observableArrayList(braniRecuperati);

        // Aggiunge l'oggetto "finto" alla fine della lista, gli ho dato id -1 così si capisce a che mi riferisco spero
        SEGNAPOSTO_AGGIUNGI.setId(-1);
        braniObservableList.add(SEGNAPOSTO_AGGIUNGI);

        // Collega la lista di dati alla ListView grafica
        listaBrani.setItems(braniObservableList);

        // Definisce la fabbrica delle celle (COME disegnare ogni riga)
        listaBrani.setCellFactory(param -> new BranoListCell());
    }

    /**
     * Metodo per aprire la vista di inserimento (Task 1.4.1)
     */
    private void apriVistaAggiungiBrano() {
        System.out.println("Apertura della vista Aggiungi Brano...");

        try {
            // Carica il file FXML della vista Aggiungi Brano
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/musicplayergruppo9/fxml/AggiungiBrano.fxml"));
            javafx.scene.Parent root = loader.load();

            // Crea una nuova finestra
            javafx.stage.Stage stageAggiungi = new javafx.stage.Stage();
            stageAggiungi.setTitle("Aggiungi Nuovo Brano");
            stageAggiungi.setScene(new javafx.scene.Scene(root));

            // Imposta la finestra come Modale, ovvero non puoi cliccare sotto
            stageAggiungi.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            // Mostra la finestra e METTE IN PAUSA questo metodo finché non viene chiusa
            stageAggiungi.showAndWait();

            // Quando viene premuto ok, annulla o chiusura (equivalente ad annulla)
            // il codice riparte da qui. Aggiorniamo la lista per mostrare il nuovo brano!!!!!!!
            aggiornaListaBrani();

        } catch (java.io.IOException e) {
            System.err.println("Errore nel caricamento della vista AggiungiBranoView.fxml");
            e.printStackTrace();
        }

    }

     /**
     * Ricarica i brani dal database e aggiorna la ListView
     */
    private void aggiornaListaBrani() {
        System.out.println("Si si... sto aggiornando i brani -_-/ ");

        // Pulisce la lista attuale
        braniObservableList.clear();

        // Ripesca tutti i brani aggiornati dal DB
        List<Brano> braniAggiornati = branoDAO.getTuttiIBrani();
        braniObservableList.addAll(braniAggiornati);

        // Reinserisce il pulsante finto "Aggiungi Brano" in fondo
        braniObservableList.add(SEGNAPOSTO_AGGIUNGI);
    }

    private void riproduciBrano() {
        System.out.println("Apertura della vista riproduci Brano...");

    }

    // Definisce il layout grafico per ogni singola cella della ListView
    private class BranoListCell extends ListCell<Brano> {

        private HBox hboxContainer = new HBox(15);
        private ImageView imgCopertina = new ImageView();
        private ImageView imgNew = new ImageView();
        private ImageView imgExplicit = new ImageView();
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

            Image iconaNew = new Image(getClass().getResourceAsStream("/org/example/musicplayergruppo9/img/new.png"));
            imgNew.setImage(iconaNew);
            imgNew.setFitHeight(40); imgNew.setFitWidth(40);

            Image iconaExplicit = new Image(getClass().getResourceAsStream("/org/example/musicplayergruppo9/img/explicit.png"));
            imgExplicit.setImage(iconaExplicit);
            imgExplicit.setFitHeight(40); imgExplicit.setFitWidth(40);

            // Spinge tutto ciò che viene dopo lo spacer all'estrema destra
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Vengono applicati stili e azioni ai bottoni
            HBox hboxBottoni = new HBox(5, lblDurata, btnCoda, btnPreferito, btnInfo, btnElimina);
            hboxBottoni.setAlignment(Pos.CENTER_RIGHT);
            lblDurata.setStyle("-fx-padding: 0 10 0 0;"); // Margine destro per la durata

            // Assembla la riga standard
            hboxContainer.getChildren().addAll(imgCopertina, vboxTesti, imgNew, imgExplicit, spacer, hboxBottoni);

            
            // Definisce l'azione di eliminazione (per la Task 1.4.3)
            btnElimina.setOnAction(e -> {
                Brano b = getItem();
                // Assicuriamoci di non provare a eliminare il bottone finto "Aggiungi brano" (id -1)
                if (b != null && b.getId() != -1) {
                    
                    // 1. Diciamo al DAO di cancellarlo dal database
                    boolean eliminato = branoDAO.eliminaBrano(b.getId());
                    
                    if (eliminato) {
                        // 2. Lo togliamo dalla lista grafica per farlo sparire subito dallo schermo!
                        braniObservableList.remove(b);
                        System.out.println("Brano eliminato con successo: " + b.getTitolo());
                    } else {
                        System.out.println("Errore: impossibile eliminare il brano dal database.");
                    }
                }
            });

            // Gestione bottone Preferito
            btnPreferito.setOnAction(e -> {
                Brano b = getItem();
                if (b != null && b.getId() != -1) { // Evitiamo crash sulla riga finta "Aggiungi brano"
                    boolean nuovoStato = !b.isPreferito();
                    b.setPreferito(nuovoStato);
                    branoDAO.aggiornaPreferito(b.getId(), nuovoStato);
                    btnPreferito.setText(nuovoStato ? "★" : "☆");
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
            hboxAggiungi.setOnMouseClicked(e -> apriVistaAggiungiBrano());
            hboxContainer.setOnMouseClicked(e -> riproduciBrano());

            // Cambia il cursore con il ditp per far capire che è cliccabile
            hboxAggiungi.setStyle("-fx-cursor: hand;");
        }

        @Override
        protected void updateItem(Brano brano, boolean empty) {
            super.updateItem(brano, empty);

            if (empty || brano == null) {
                // La cella è vuota, non mostrare nulla
                setText(null);
                setGraphic(null);
            } else if (brano.getId() == -1) {
                // Questa è la nostra riga automatica per "Aggiungi Brano", deve esserci a prescindere
                setText(null);
                setGraphic(hboxAggiungi);
            } else {
                // Questa è una riga normale per un brano reale
                lblTitolo.setText(brano.getTitolo());
                lblAutore.setText(brano.getArtista());
                lblDurata.setText(brano.getDurataFormattata());

                // Gestione della copertina
                File fileCopertina = brano.getCopertina();
                if (fileCopertina != null && fileCopertina.exists()) {
                    imgCopertina.setImage(new Image(fileCopertina.toURI().toString()));
                } else {
                    // Imposta un'immagine di default (se non c'è la copertina)
                    // imgCopertina.setImage(new Image(getClass().getResource("/org/example/.../default.png").toString())); //devo caricare ancora un png di default
                    imgCopertina.setImage(null);
                }

                imgNew.setVisible(brano.isNewRelease());
                imgNew.setManaged(brano.isNewRelease());
                imgExplicit.setVisible(brano.isExplicit());
                imgExplicit.setManaged(brano.isExplicit());
                btnPreferito.setText(brano.isPreferito() ? "★" : "☆");

                setText(null);
                setGraphic(hboxContainer);
            }
        }
    }
}