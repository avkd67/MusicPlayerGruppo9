package org.example.musicplayergruppo9.controller;

import java.io.File;
import java.io.IOException;

import org.example.musicplayergruppo9.utilities.FXutilities;
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
        playlistService = PlaylistService.getInstance();

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
    private void onSfogliaCopertina(){
        percorsoCopertinaSelezionata = FXutilities.cercaCopertina(imgCopertina);
    }

    @FXML
    private void onAnnulla(){
        FXutilities.chiudiFinestra(TxtFieldNomePlaylist);
    }

    // pulsante OK che salva le modifiche alla playlist, controllando che siano tutte modifiche valide che non vadano a risultare in stati incoerenti del db
    @FXML
    private void onOk() throws IOException {
        // vieta l'aggiornamento del nome della playlist come vuoto
        if(TxtFieldNomePlaylist.getText().isBlank()){
            FXutilities.mostraAlertErrore("Errore", "Nome playlist non valido", "Il nome della playlist non può essere vuoto. Inserire un nome valido.");
            return;
        }

        playlistModificata.setNome(TxtFieldNomePlaylist.getText());

        // se il nome è stato effettivamente modificato -> controllo la sua validità nel db (altrimenti, era impossibile uscire dalla schermata senza cambiare il nome della playlist)
        if(!playlist.getNome().equals(playlistModificata.getNome()))
                if(!playlistService.checkNomePlaylist(playlistModificata)){
                    FXutilities.mostraAlertErrore("Errore", "Nome playlist non valido", "Una playlist con questo nome esiste già. Inserire un nome diverso.");
                    return;
        }

        // se è stato selezionato un percorso, lo aggiungiamo alla playlistModificata
        if(percorsoCopertinaSelezionata != null){
            playlistModificata.setPercorsoCopertina(percorsoCopertinaSelezionata);
        }

        boolean successo = playlistService.aggiornaPlaylist(playlist, playlistModificata);
        if(successo){
            playlist.setNome(playlistModificata.getNome());
            playlist.setPercorsoCopertina(playlistModificata.getPercorsoCopertina());
        }
        else
            FXutilities.mostraAlertErrore("Errore","Problema nell'aggiornamento della playlist","Impossibile aggiornare la playlist");

        FXutilities.chiudiFinestra(TxtFieldNomePlaylist);
    }


}
