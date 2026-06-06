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
import org.example.musicplayergruppo9.model.Playlist;
import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.utilities.FXutilities;

import java.io.File;
import java.util.List;

public class AggiungiBranoPlaylistController {

    private Playlist playlist;
    private ObservableList<Brano> braniObservableList;

    @FXML
    private ListView<Brano> listaView;

    private List<Brano> listaBraniDB;

    private BraniPlaylistController braniPlaylistController;

    public void setPlaylist(Playlist playlist){
        this.playlist = playlist;
    }

    public void setPreviousController(BraniPlaylistController braniPlaylistController){
        this.braniPlaylistController = braniPlaylistController;
    }

    @FXML
    public void initialize(){
        BranoDAO branoDAO = BranoDAO.getInstance();
        listaBraniDB = branoDAO.getTuttiIBrani();
        braniObservableList = FXCollections.observableArrayList(listaBraniDB);

        listaView.setItems(braniObservableList);
        listaView.setCellFactory(param -> new BranoListCell());
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

        // pulsante aggiungi brano a playlist
        private Button btnAggiungi = new Button("+");

        // aggiunge il brano alla playlist utilizzando il controller precedente passato dal metodo previousController
        private void aggiungiAPlaylist(Brano brano) {
            if (brano != null && braniPlaylistController != null) {
                braniPlaylistController.aggiungiBranoAPlaylist(brano);
                System.out.println("Aggiunto " + brano.getTitolo() + " a playlist " +  playlist.getNome());
                FXutilities.mostraAlertSuccesso("Aggiunta riuscita", "Brano aggiunto con successo");
                FXutilities.chiudiFinestra(listaView);
            }
        }

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
            HBox hboxBottoni = new HBox(5, lblDurata, btnAggiungi);
            hboxBottoni.setAlignment(Pos.CENTER_RIGHT);
            lblDurata.setStyle("-fx-padding: 0 10 0 0;"); // Margine destro per la durata

            // Assembla la riga standard
            hboxContainer.getChildren().addAll(imgCopertina, vboxTesti, imgNew, imgExplicit, spacer, hboxBottoni);


            // Gestione bottone Coda
            btnAggiungi.setOnAction(e -> {
                Brano brano = getItem();
                aggiungiAPlaylist(brano);
            });
        }

        @Override
        protected void updateItem(Brano brano, boolean empty) {
            super.updateItem(brano, empty);

            if (empty || brano == null) {
                // La cella è vuota, non mostrare nulla
                setText(null);
                setGraphic(null);
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

                setText(null);
                setGraphic(hboxContainer);
            }
        }
    }
}

