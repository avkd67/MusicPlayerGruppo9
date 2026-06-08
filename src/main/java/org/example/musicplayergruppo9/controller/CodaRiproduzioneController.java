package org.example.musicplayergruppo9.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.example.musicplayergruppo9.model.ElementoCoda;
import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.Playlist;

import java.io.File;
import java.util.List;

public class CodaRiproduzioneController {

    @FXML private ListView<ElementoCoda> listaCoda;
    @FXML private Button btnRimuovi;
    @FXML private Button btnChiudi;

    private PlayerController playerController;

    public void setPlayerController(PlayerController pc) {
        this.playerController = pc;
        refresh();
    }

    public void refresh() {
        if (playerController == null || listaCoda == null) return;
        List<ElementoCoda> snapshot = playerController.getCodaSnapshot();
        ObservableList<ElementoCoda> items = FXCollections.observableArrayList(snapshot);
        listaCoda.setItems(items);
        listaCoda.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ElementoCoda item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item instanceof Brano) {
                    Brano b = (Brano) item;
                    setText(b.getTitolo() + " — " + b.getArtista());
                } else if (item instanceof Playlist) {
                    Playlist p = (Playlist) item;
                    setText("[Playlist] " + p.getNome());
                } else {
                    setText(item.getTitolo());
                }
            }
        });
    }

    @FXML
    public void rimuoviSelezionato() {
        ElementoCoda sel = listaCoda.getSelectionModel().getSelectedItem();
        if (sel != null && playerController != null) {
            boolean ok = playerController.rimuoviDaCoda(sel);
            if (ok) refresh();
        }
    }

    @FXML
    public void chiudi() {
        Stage s = (Stage) btnChiudi.getScene().getWindow();
        s.close();
    }
}
