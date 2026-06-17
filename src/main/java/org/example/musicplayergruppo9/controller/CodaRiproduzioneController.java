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
import org.example.musicplayergruppo9.pattern.strategy.StrategiaOrdineShuffle;
import org.example.musicplayergruppo9.model.ElementoCodaConOpzioni;
import org.example.musicplayergruppo9.pattern.strategy.StrategiaOrdineShuffle;

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
                    return;
                }
            
                ElementoCoda reale = estraiElementoReale(item);
            
                boolean shuffle = item instanceof ElementoCodaConOpzioni && ((ElementoCodaConOpzioni) item).getStrategiaOrdine() instanceof StrategiaOrdineShuffle;

                if (reale instanceof Brano) {
                    Brano b = (Brano) reale;
                    setText(b.getTitolo() + " — " + b.getArtista());
                } else if (reale instanceof Playlist) {
                    Playlist p = (Playlist) reale;
            
                    //in coda sarà visualizzabile la playlist, il suo ordine di riproduzione e i suoi brani 
                    
                    StringBuilder testo = new StringBuilder();
                    testo.append("[Playlist] ").append(p.getNome());
                    
                    if (shuffle) {
                        testo.append("  shuffle");
                    }

                    for (Brano b : p.getBrani()) {
                        testo.append("\n   - ")
                             .append(b.getTitolo())
                             .append(" — ")
                             .append(b.getArtista());
                    }
            
                    setText(testo.toString());
                } else {
                    setText(reale.getTitolo());
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

    //utilizzato per sviluppare la visualizzazione dei brani delle playlist in coda
    private ElementoCoda estraiElementoReale(ElementoCoda elemento) {
        if (elemento instanceof ElementoCodaConOpzioni) {
            return ((ElementoCodaConOpzioni) elemento).getElemento();
        }
        return elemento;
    }

}
