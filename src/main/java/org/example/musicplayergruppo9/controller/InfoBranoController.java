package org.example.musicplayergruppo9.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.musicplayergruppo9.model.Brano;

import java.io.File;

public class InfoBranoController {

    @FXML
    private ImageView imgCopertina;

    @FXML
    private Label lblTitolo;

    @FXML
    private Label lblArtista;

    @FXML
    private Label lblGenere;

    @FXML
    private Label lblAnno;

    @FXML
    private Label lblDurata;

    private Brano branoCorrente;

    public void setBrano(Brano brano) {
        this.branoCorrente = brano;

        if (brano.getPercorsoCopertina() != null && !brano.getPercorsoCopertina().isBlank()) {
            File f = new File(brano.getPercorsoCopertina());
            if (f.exists()) imgCopertina.setImage(new Image(f.toURI().toString()));
        }

        lblTitolo.setText(brano.getTitolo() != null ? brano.getTitolo() : "");
        lblArtista.setText(brano.getArtista() != null ? brano.getArtista() : "");
        lblGenere.setText("Genere: " + (brano.getGenere() != null ? brano.getGenere() : "-"));
        lblAnno.setText("Anno: " + (brano.getDataRilascio() > 0 ? String.valueOf(brano.getDataRilascio()) : "-"));
        lblDurata.setText("Durata: " + (brano.getDurataFormattata() != null ? brano.getDurataFormattata() : "-"));
    }

    @FXML
    private void onAnnulla() {
        Stage stage = (Stage) lblTitolo.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onModifica() {
        // Apri la vista di modifica; mantieni la finestra Info aperta finché la modifica non è chiusa
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/org/example/musicplayergruppo9/fxml/ModificaBrano.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stageModifica = new javafx.stage.Stage();
            stageModifica.setTitle("Modifica Brano");
            stageModifica.setScene(new javafx.scene.Scene(root));
            stageModifica.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            ModificaBranoController controller = loader.getController();
            controller.setBrano(branoCorrente);

            stageModifica.showAndWait();

            // Dopo la chiusura della finestra di modifica, chiudi anche la finestra Info
            Stage stage = (Stage) lblTitolo.getScene().getWindow();
            stage.close();
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }
}
