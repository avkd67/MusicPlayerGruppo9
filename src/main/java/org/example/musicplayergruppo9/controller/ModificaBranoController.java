package org.example.musicplayergruppo9.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.musicplayergruppo9.database.DAO.BranoDAO;
import org.example.musicplayergruppo9.model.Brano;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ModificaBranoController {

    @FXML
    private TextField txtTitolo;

    @FXML
    private TextField txtArtista;

    @FXML
    private TextField txtGenere;

    @FXML
    private TextField txtAnnoRilascio;

    @FXML
    private ImageView imgCopertina;

    @FXML
    private CheckBox chkExplicit;

    private Brano branoCorrente;
    private BranoDAO branoDAO;
    private String percorsoCopertinaSelezionata = null;

    @FXML
    public void initialize() {
        branoDAO = BranoDAO.getInstance();
    }

    public void setBrano(Brano brano) {
        this.branoCorrente = brano;

        // Alcuni casi di timing possono far sì che i campi FXML siano ancora null.
        // Usiamo Platform.runLater per assicurarci che l'aggiornamento UI avvenga sul JavaFX Application Thread
        javafx.application.Platform.runLater(() -> {
            try {
                if (txtTitolo != null) txtTitolo.setText(brano.getTitolo());
                if (txtArtista != null) txtArtista.setText(brano.getArtista());
                if (txtGenere != null) txtGenere.setText(brano.getGenere());
                if (txtAnnoRilascio != null) txtAnnoRilascio.setText(brano.getDataRilascio() > 0 ? String.valueOf(brano.getDataRilascio()) : "");
                if (chkExplicit != null) chkExplicit.setSelected(brano.isExplicit());

                if (brano.getPercorsoCopertina() != null && !brano.getPercorsoCopertina().isBlank()) {
                    File fileCopertina = new File(brano.getPercorsoCopertina());
                    if (fileCopertina.exists() && imgCopertina != null) {
                        imgCopertina.setImage(new Image(fileCopertina.toURI().toString()));
                    }
                }
            } catch (Exception ex) {
                System.err.println("Errore durante setBrano: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    @FXML
    private void onSfogliaCopertina() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona l'immagine di copertina");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Immagini (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) imgCopertina.getScene().getWindow();
        File fileSelezionato = fileChooser.showOpenDialog(stage);

        if (fileSelezionato != null) {
            percorsoCopertinaSelezionata = fileSelezionato.getAbsolutePath();
            imgCopertina.setImage(new Image(fileSelezionato.toURI().toString()));
        }
    }

    @FXML
    private void onOk() {
        String titolo = txtTitolo.getText() != null ? txtTitolo.getText().trim() : "";
        String artista = txtArtista.getText() != null ? txtArtista.getText().trim() : "";
        String genere = txtGenere.getText() != null ? txtGenere.getText().trim() : "";

        if (titolo.isEmpty() || artista.isEmpty()) {
            mostraAlertErrore("Campi incompleti", "Titolo e artista sono obbligatori.",
                    "Inserisci almeno il titolo e l'artista del brano.");
            return;
        }

        int annoRilascio = 0;
        String annoStr = txtAnnoRilascio.getText() != null ? txtAnnoRilascio.getText().trim() : "";
        if (!annoStr.isEmpty()) {
            try {
                annoRilascio = Integer.parseInt(annoStr);
                if (annoRilascio < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                mostraAlertErrore("Anno non valido", "Formato anno non corretto.",
                        "Inserisci un anno di pubblicazione valido, ad esempio 2024.");
                return;
            }
        }

        if (branoDAO.esisteOmonimoDiversoDaId(titolo, artista, branoCorrente.getId())) {
            mostraAlertErrore("Brano duplicato", "Esiste già un brano con lo stesso titolo e artista.",
                    "Modifica il titolo o l'artista oppure conserva i dati esistenti.");
            return;
        }

        branoCorrente.setTitolo(titolo);
        branoCorrente.setArtista(artista);
        branoCorrente.setGenere(genere);
        branoCorrente.setDataRilascio(annoRilascio);
        branoCorrente.setExplicit(chkExplicit.isSelected());

        int annoCorrente = java.time.LocalDate.now().getYear();
        branoCorrente.setNewRelease(annoRilascio > 0 && annoRilascio == annoCorrente);

        if (percorsoCopertinaSelezionata != null) {
            String percorsoRelativo = copiaCopertinaNelProgetto(percorsoCopertinaSelezionata);
            if (percorsoRelativo != null) {
                branoCorrente.setPercorsoCopertina(percorsoRelativo);
            }
        }

        boolean aggiornato = branoDAO.aggiornaBrano(branoCorrente);
        if (aggiornato) {
            mostraAlertSuccesso("Modifica salvata", "Le informazioni del brano sono state aggiornate con successo.");
            chiudiFinestra();
        } else {
            mostraAlertErrore("Errore salvataggio", "Impossibile aggiornare il brano.",
                    "Verifica di avere i permessi necessari o riprova più tardi.");
        }
    }

    @FXML
    private void onAnnulla() {
        chiudiFinestra();
    }

    private String copiaCopertinaNelProgetto(String percorsoOriginale) {
        try {
            Path cartellaCopertine = Paths.get("AltriFile", "Copertine");
            if (!Files.exists(cartellaCopertine)) {
                Files.createDirectories(cartellaCopertine);
            }

            File fileOriginale = new File(percorsoOriginale);
            Path targetPath = cartellaCopertine.resolve(fileOriginale.getName());
            Files.copy(fileOriginale.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return "AltriFile/Copertine/" + fileOriginale.getName();
        } catch (IOException e) {
            e.printStackTrace();
            mostraAlertErrore("Errore file", "Impossibile copiare il file immagine.",
                    "Controlla che il file non sia bloccato o che tu abbia i permessi di scrittura.");
            return null;
        }
    }

    private void chiudiFinestra() {
        Stage stage = (Stage) txtTitolo.getScene().getWindow();
        stage.close();
    }

    private void mostraAlertErrore(String titolo, String header, String contenuto) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(header);
        alert.setContentText(contenuto);
        alert.showAndWait();
    }

    private void mostraAlertSuccesso(String titolo, String header) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(header);
        alert.setContentText(null);
        alert.showAndWait();
    }
}
