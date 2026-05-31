package org.example.musicplayergruppo9.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.ElementoCoda;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;


import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;



public class PlayerController {

    //per gestire la riproduzione dei brani
    private MediaPlayer mediaPlayer;

    @FXML
    private ImageView imgCopertina;

    @FXML
    private Label lblTitolo;

    @FXML
    private Label lblArtista;

    //elemento in coda
    private final Queue<ElementoCoda> coda = new LinkedList<>();
    private Iterator<Brano> iteratorCorrente;

    private Brano branoCorrente;

    //flag per indicare lo stato del loop
    private boolean loopAttivo = false;

    //aggiunge un elemento in coda, se non c'è nulla in riproduzione
    //avvia il nuovo elemento
    public void aggiungiInCoda(ElementoCoda elemento) {
        coda.add(elemento);
        if (branoCorrente == null) {
            avviaProssimoElemento();
        }
    }

    //task 7.2 Loop del brano
    //attiva/disattiva il loop sul brano corrente
    //comportamento in base alla pressione del tasto
    @FXML
    public void loopSong() {
        loopAttivo = !loopAttivo;
    }

    //task 6.2 — Skip elemento in riproduzione
    @FXML
    public void skipSong() {
        // caso a: il loop è attivo, riparte il brano corrente
        if (loopAttivo && branoCorrente != null) {
            riproduciBranoCorrente();
            return;
        }
        //caso b: dentro una playlist, skippa al prossimo brano presente nella playlist
        if (iteratorCorrente != null && iteratorCorrente.hasNext()) {
            branoCorrente = iteratorCorrente.next();
            riproduciBranoCorrente();
            return;
        }
        //caso c: skip al prossimo elemento, brano o playlist
        avviaProssimoElemento();
    }

    @FXML
    public void play() {
        if (mediaPlayer == null) return;

        //se sta riproducendo mette in pausa
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
        } else {
            //se è in pausa, riprende la riproduzione
            mediaPlayer.play();
        }
    }

    @FXML
    public void skipPlaylist() {
    }

    //logica per la gestione dell'ElementoCoda nella coda di riproduzione
    private void avviaProssimoElemento() {
        ElementoCoda prossimo = coda.poll();

        //coda vuota
        if (prossimo == null) {
            branoCorrente = null;
            iteratorCorrente = null;
            return;
        }

        //iterator per il nuovo elemento
        iteratorCorrente = prossimo.iterator();

        if (iteratorCorrente.hasNext()) {
            branoCorrente = iteratorCorrente.next();
            riproduciBranoCorrente();
        } else {
            //se è vuoto salta al successivo
            avviaProssimoElemento();
        }
    }

    //gestione riproduzione brano corrente
    private void riproduciBranoCorrente() {
        if (branoCorrente == null) return;

        if (lblTitolo != null) lblTitolo.setText(branoCorrente.getTitolo());
        if (lblArtista != null) lblArtista.setText(branoCorrente.getArtista());

        if (imgCopertina != null) {
            File fileCopertina = branoCorrente.getCopertina();
            if (fileCopertina != null && fileCopertina.exists()) {
                imgCopertina.setImage(new Image(fileCopertina.toURI().toString()));
            } else {
                imgCopertina.setImage(null);
            }
        }

        //gestione del mediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        File fileAudio = branoCorrente.getFileAudio();
        if (fileAudio != null && fileAudio.exists()) {
            Media media = new Media(fileAudio.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();

            //task 7.2 loop
            // Quando il brano finisce, passa automaticamente al prossimo
            mediaPlayer.setOnEndOfMedia(() -> {
                if (loopAttivo) {
                    mediaPlayer.seek(javafx.util.Duration.ZERO);
                    mediaPlayer.play();
                } else {
                    skipSong();
                }
            });
        } 
    }

   //setter per Brano
   //selezionato e riprodotto subito
    public void setBrano(Brano brano) {
        branoCorrente = brano;
        riproduciBranoCorrente();
    }

    public Brano getBranoCorrente() { return branoCorrente; }
    public boolean isLoopAttivo() { return loopAttivo; }
    public int getDimensioneCoda() { return coda.size(); }
}