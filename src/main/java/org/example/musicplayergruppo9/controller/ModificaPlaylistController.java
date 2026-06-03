package org.example.musicplayergruppo9.controller;

import java.io.File;

import org.example.musicplayergruppo9.model.Playlist;
import org.example.musicplayergruppo9.service.PlaylistService;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ModificaPlaylistController {

    @FXML
    private TextField TxtFieldNomePlaylist;

    @FXML
    private ImageView imgCopertina;

    private Playlist playlist;
    private Playlist playlistModificata;

    private String percorsoCopertinaSelezionata = null;

    private PlaylistService playlistService;

    @FXML
    public void initialize(){
        playlistService = new PlaylistService();

    }

    public void setPlaylist(Playlist playlist){
        this.playlist = playlist;
        playlistModificata = new Playlist();
        playlistModificata.setNome(playlist.getNome());
        playlistModificata.setPercorsoCopertina(playlist.getPercorsoCopertina());
        TxtFieldNomePlaylist.setText(this.playlist.getNome());



         // Mette la copertina della playlist, se esiste
        if(this.playlist.getPercorsoCopertina() != null)
            imgCopertina.setImage(new Image(new File(this.playlist.getPercorsoCopertina()).toURI().toString()));
    }

    @FXML
    private void onAnnulla(){
        chiudiFinestra();
    }

    // pulsante OK che salva le modifiche alla playlist, controllando che siano tutte modifiche valide che non vadano a risultare in stati incoerenti del db
    @FXML
    private void onOk(){
        // vieta l'aggiornamento del nome della playlist come vuoto
        if(TxtFieldNomePlaylist.getText().isBlank()){
            mostraAlertErrore("Errore", "Nome playlist non valido", "Il nome della playlist non può essere vuoto. Inserire un nome valido.");
            return;
        }

        playlistModificata.setNome(TxtFieldNomePlaylist.getText());

        // se il nome è stato effettivamente modificato -> controllo la sua validità nel db (altrimenti, era impossibile uscire dalla schermata senza cambiare il nome della playlist)
        if(!playlist.getNome().equals(playlistModificata.getNome()))
                if(!playlistService.checkNomePlaylist(playlistModificata)){
                    mostraAlertErrore("Errore", "Nome playlist non valido", "Una playlist con questo nome esiste già. Inserire un nome diverso.");
                    return;
        }

        // se è stato selezionato un percorso, lo aggiungiamo alla playlistModificata
        if(percorsoCopertinaSelezionata != null){
            playlistModificata.setPercorsoCopertina(percorsoCopertinaSelezionata);
        }

        playlistService.aggiornaPlaylist(playlist, playlistModificata);
        // playlist = playlistModificata;
        chiudiFinestra();
    }

    @FXML
    private void onSfogliaCopertina(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona l'immagine di copertina");
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

    private void mostraAlertErrore(String titolo, String header, String contenuto) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(header);
        alert.setContentText(contenuto);
        alert.showAndWait();
    }

    private void chiudiFinestra() {
        // Recupera lo stage corrente partendo da uno qualsiasi dei nodi grafici e lo chiude
        Stage stage = (Stage) TxtFieldNomePlaylist.getScene().getWindow();
        stage.close();
    }


}
