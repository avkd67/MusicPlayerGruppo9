package org.example.musicplayergruppo9.controller;

import java.io.File;

import org.example.musicplayergruppo9.database.DAO.PlaylistDAO;
import org.example.musicplayergruppo9.model.Playlist;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class ModificaPlaylistController {

    @FXML
    private TextField TxtFieldNomePlaylist;

    @FXML
    private ImageView imgCopertina;

    private Playlist playlist;
    private Playlist playlistModificata;

    private PlaylistDAO playlistDAO;

    @FXML
    public void initialize(){
        playlistDAO = new PlaylistDAO();

    }

    public void setPlaylist(Playlist playlist){
        this.playlist = playlist;
        playlistModificata = playlist;
        TxtFieldNomePlaylist.setText(this.playlist.getNome());



         // Mette la copertina della playlist, se esiste
        if(this.playlist.getPercorsoCopertina() != null)
            imgCopertina.setImage(new Image(new File(this.playlist.getPercorsoCopertina()).toURI().toString()));
    }

    @FXML
    private void onAnnulla(){
        chiudiFinestra();
    }

    @FXML
    private void onOk(){
        if(TxtFieldNomePlaylist.getText().isBlank()){
            mostraAlertErrore("Errore", "Nome playlist non valido", "Il nome della playlist non può essere vuoto. Inserire un nome valido.");
            return;
        }

        playlistModificata.setNome(TxtFieldNomePlaylist.getText());

        if(!playlistDAO.checkNomePlaylist(playlistModificata)){
            mostraAlertErrore("Errore", "Nome playlist non valido", "Una playlist con questo nome esiste già. Inserire un nome diverso.");
            return;
        }

        playlistDAO.aggiornaPlaylist(playlist, playlistModificata);
        playlist = playlistModificata;
        chiudiFinestra();
    }

    @FXML
    private void onSfogliaCopertina(){

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
