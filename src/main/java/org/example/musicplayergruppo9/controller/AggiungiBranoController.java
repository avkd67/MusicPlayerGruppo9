package org.example.musicplayergruppo9.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.musicplayergruppo9.AggiungiBranoService;

import java.io.File;

public class AggiungiBranoController {

    @FXML private TextField txtTitolo;
    @FXML private TextField txtArtista;
    @FXML private TextField txtGenere;
    @FXML private TextField txtAnnoRilascio;
    @FXML private ImageView imgCopertina;
    @FXML private Label lblFileAudio;
    @FXML private CheckBox chkExplicit;


    private AggiungiBranoService aggiungiBranoService;

    // Variabili di appoggio per memorizzare i percorsi dei file scelti, ovviamente sono inizialmente null
    private String percorsoFileAudioSelezionato = null;
    private String estensioneFileAudio = null;
    private String percorsoCopertinaSelezionata = null;

    @FXML
    public void initialize() {
        aggiungiBranoService = new AggiungiBranoService();
    }

    /**
     * Gestisce la selezione del file audio tramite FileChooser nativo
     */
    @FXML
    private void onSfogliaFileAudio() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona il file audio della canzone");

        // Filtro estensioni (Task 1.3: Logica di validazione estensione)
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("File Audio (*.mp3)", "*.mp3") //ringraziamo i tutorial su youtbe per ExtensionFilter ^-^
        );

        // Mostra la finestra di dialogo aperta sopra la finestra attuale
        Stage stage = (Stage) lblFileAudio.getScene().getWindow();
        File fileSelezionato = fileChooser.showOpenDialog(stage);

        if (fileSelezionato != null) {
            percorsoFileAudioSelezionato = fileSelezionato.getAbsolutePath();
            lblFileAudio.setText(fileSelezionato.getName());

            // Estrazione dell'estensione del file
            String nomeFile = fileSelezionato.getName();
            int index = nomeFile.lastIndexOf('.');
            if (index > 0) {
                estensioneFileAudio = nomeFile.substring(index).toLowerCase(); // sempre lowercase per sicurezza
            }
        }
    }

    /**
     * Gestisce la selezione opzionale dell'immagine di copertina
     */
    @FXML
    private void onSfogliaCopertina() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona l'immagine di coeprtina");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Immagini (*.jpg, *.jpeg)", "*.jpg", "*.jpeg") //grazie .ExtensionFilter
        );

        Stage stage = (Stage) imgCopertina.getScene().getWindow();
        File fileSelezionato = fileChooser.showOpenDialog(stage);

        if (fileSelezionato != null) {
            percorsoCopertinaSelezionata = fileSelezionato.getAbsolutePath();
            // Carica l'immagine nell'anteprima grafica ImageView
            imgCopertina.setImage(new Image(fileSelezionato.toURI().toString()));
        }
    }

    /**
     * TASK 1.2 e 1.3: Logica di ricezione, validazione e salvataggio (Pulsante OK)
     * */
    @FXML
    private void onOk() {
        // Recupero e pulizia dei dati inseriti dai campi
        String titolo = txtTitolo.getText() != null ? txtTitolo.getText().trim() : "";
        String artista = txtArtista.getText() != null ? txtArtista.getText().trim() : "";
        String genere = txtGenere.getText() != null ? txtGenere.getText().trim() : "";

        // Lettura e validazione dell'anno
        int annoRilascio = 0;
        String annoStr = txtAnnoRilascio.getText() != null ? txtAnnoRilascio.getText().trim() : "";
        if (!annoStr.isEmpty()) {
            try {
                annoRilascio = Integer.parseInt(annoStr);
            } catch (NumberFormatException e) {
                mostraAlertErrore("Anno non valido", "Inserisci un anno numerico.", "Es: 2024");
                return;
            }
        }

        // Validazione: Blocco se i campi minimi obbligatori sono vuoti
        if (titolo.isEmpty() || artista.isEmpty() || percorsoFileAudioSelezionato == null) {
            mostraAlertErrore("Campi Incompleti", "Impossibile salvare il brano.",
                    "Assicurati di inserire almeno il Titolo, l'Artista e di selezionare un File Audio valido.");
            return;
        }

        try {
            // Passiamo annoRilascio al service (il check newRelease lo fa il service)
            boolean successo = aggiungiBranoService.gestisciSalvataggio(
                    titolo, artista, genere, annoRilascio,
                    percorsoFileAudioSelezionato, estensioneFileAudio, percorsoCopertinaSelezionata, chkExplicit.isSelected()
            );

            if (successo) {
                mostraAlertSuccesso("Salvataggio Completato", "Il brano '" + titolo + "' è stato aggiunto alla tua libreria.");
                chiudiFinestra();
            } else {
                mostraAlertErrore("Errore del Sistema", "Salvataggio fallito.", "Si è verificato un errore imprevisto scrivendo sul database.");
            }

        } catch (IllegalArgumentException e) {
            // Validazione: Gestione degli omonimi nel DataBase
            mostraAlertErrore("Brano Duplicato", "Rilevato omonimo nel sistema.",
                    "Esiste già una canzone intitolata '" + titolo + "con artista: '" + artista + "'.");
        } catch (java.io.IOException e) {
            mostraAlertErrore("Errore File System", "Impossibile copiare i file.", "Controlla i permessi della cartella.");
        }
    }

    /**
     * Azione associata al tasto annulla: chiude la schermata senza salvare nulla
     */
    @FXML
    private void onAnnulla() {
        chiudiFinestra();
    }

    private void chiudiFinestra() {
        // Recupera lo stage corrente partendo da uno qualsiasi dei nodi grafici e lo chiude
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