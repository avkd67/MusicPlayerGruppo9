package org.example.musicplayergruppo9.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.musicplayergruppo9.database.DAO.BranoDAO;
import org.example.musicplayergruppo9.model.Brano;

import java.io.File;
import java.time.ZoneId;
import java.util.Date;

public class AggiungiBranoController {

    @FXML private TextField txtTitolo;
    @FXML private TextField txtArtista;
    @FXML private TextField txtGenere;
    @FXML private DatePicker dpDataRilascio;
    @FXML private ImageView imgCopertina;
    @FXML private Label lblFileAudio;

    private BranoDAO branoDAO;

    // Variabili di appoggio per memorizzare i percorsi dei file scelti, ovviamente sono inizialmente null
    private String percorsoFileAudioSelezionato = null;
    private String estensioneFileAudio = null;
    private String percorsoCopertinaSelezionata = null;

    @FXML
    public void initialize() {
        branoDAO = new BranoDAO();
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
                new FileChooser.ExtensionFilter("File Audio (*.mp3, *.wav)", "*.mp3", "*.wav") //ringraziamo i tutorial su youtbe per ExtensionFilter ^-^
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
                new FileChooser.ExtensionFilter("Immagini (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg") //grazie .ExtensionFilter
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

        // Validazione: Blocco se i campi minimi obbligatori sono vuoti
        if (titolo.isEmpty() || artista.isEmpty() || percorsoFileAudioSelezionato == null) {
            mostraAlertErrore("Campi Incompleti", "Impossibile salvare il brano.",
                    "Assicurati di inserire almeno il Titolo, l'Artista e di selezionare un File Audio valido.");
            return;
        }

        // Validazione: Gestione degli omonimi nel DataBase
        if (branoDAO.esisteOmonimo(titolo, artista)) {
            mostraAlertErrore("Brano Duplicato", "Rilevato omonimo nel sistema.",
                    "Esiste già una canzone intitolata '" + titolo + "con artista: '" + artista + "'.");
            return;
        }

        // copia i file nella cartella del progetto
        String percorsoAudioDefinitivo = percorsoFileAudioSelezionato;
        String percorsoCopertinaDefinitiva = percorsoCopertinaSelezionata;

        try {
            // Creazione cartella Audio
            java.nio.file.Path cartellaAudio = java.nio.file.Paths.get("AltriFile", "Audio");
            if (!java.nio.file.Files.exists(cartellaAudio)) {
                java.nio.file.Files.createDirectories(cartellaAudio); // questo l'ho messo perché non dovremmo tutti pushare le canzoni che ascoltiamo, speriamo funzioni
            }

            // Copia del file Audio
            File fileAudioOriginale = new File(percorsoFileAudioSelezionato);
            java.nio.file.Path targetAudio = cartellaAudio.resolve(fileAudioOriginale.getName());

            // StandardCopyOption.REPLACE_EXISTING sovrascrive il file se ne esiste già uno con lo stesso nome, non sapevo come gestirlo sincero
            java.nio.file.Files.copy(fileAudioOriginale.toPath(), targetAudio, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            percorsoAudioDefinitivo = targetAudio.toString(); // Aggiorno il percorso salvato nel dB

            // Stessa cosa ma per la Copertina praticamente
            if (percorsoCopertinaSelezionata != null) {
                java.nio.file.Path cartellaCopertine = java.nio.file.Paths.get("AltriFile", "Copertine");
                if (!java.nio.file.Files.exists(cartellaCopertine)) {
                    java.nio.file.Files.createDirectories(cartellaCopertine);
                }

                File fileCopertinaOriginale = new File(percorsoCopertinaSelezionata);
                java.nio.file.Path targetCopertina = cartellaCopertine.resolve(fileCopertinaOriginale.getName());
                java.nio.file.Files.copy(fileCopertinaOriginale.toPath(), targetCopertina, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                percorsoCopertinaDefinitiva = targetCopertina.toString();
            }

        } catch (java.io.IOException e) {
            mostraAlertErrore("Errore File System", "Impossibile copiare i file.", "Controlla i permessi della cartella.");
            e.printStackTrace();
            return; // Blocchiamo il salvataggio se la copia fallisce
        }




        // Conversione della data da LocalDate (JavaFX) a java.util.Date (Vostro Model)
        // Valerio mi ha ricordato che effettivamente volevamo salvare solo l'anno quindi magari cambio toDo
        Date dataRilascio = null;
        if (dpDataRilascio.getValue() != null) {
            dataRilascio = Date.from(dpDataRilascio.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        // Calcolo/Estrazione della durata
        // non sapevo come farlo, per ora ho fatto 180s toDo
        int durataSecondi = 180;

        // Creazione dell'oggetto Model (uso il costruttore senza ID)
        Brano nuovoBrano = new Brano(
                titolo,
                artista,
                genere,
                dataRilascio,
                durataSecondi,
                percorsoAudioDefinitivo,
                estensioneFileAudio,
                percorsoCopertinaDefinitiva
        );

        // Salvataggio definitivo nel Database tramite DAO
        boolean successo = branoDAO.salvaBrano(nuovoBrano);

        if (successo) {
            mostraAlertSuccesso("Salvataggio Completato", "Il brano '" + titolo + "' è stato aggiunto alla tua libreria.");
            chiudiFinestra();
        } else {
            mostraAlertErrore("Errore del Sistema", "Salvataggio fallito.", "Si è verificato un errore imprevisto scrivendo sul database.");
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