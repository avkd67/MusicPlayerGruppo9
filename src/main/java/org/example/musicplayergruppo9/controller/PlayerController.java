package org.example.musicplayergruppo9.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.ElementoCoda;
import org.example.musicplayergruppo9.PlayerService;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class PlayerController {

    @FXML private ImageView imgCopertina;
    @FXML private Label lblTitolo;
    @FXML private Label lblArtista;
    @FXML private Button btnPlayPause;
    @FXML private Label lblTempo;
    @FXML private Slider sliderProgresso;

   
    private static PlayerService playerService;

    //per svolgere task 6.2
    private final Queue<ElementoCoda> coda = new LinkedList<>();

    //iterare sull'elemento in riproduzione
    private Iterator<Brano> iteratorCorrente;

    private Brano branoCorrente;

    //flag per svolgere task 7.2
    private boolean loopAttivo = false;

    
    @FXML
    public void initialize() {
        if (playerService == null) {
            playerService = new PlayerService();
        }

        playerService.setCallbacks(
                testoBottone -> btnPlayPause.setText(testoBottone),
                tempoAttuale -> aggiornaUIProgresso(tempoAttuale),
                () -> sliderProgresso.setMax(playerService.getTotalDuration().toSeconds())
        );
        
        // Task 7.2 — se loop attivo riparte, altrimenti Task 6.2 — skippa al successivo
        playerService.setOnEndOfMediaCallback(() -> {
            if (loopAttivo && branoCorrente != null) {
                playerService.loadTrack(branoCorrente); // riparte da capo
            } else {
                skipSong(); // passa al prossimo elemento in coda
            }
        });
    }

    //gestione coda
    public void aggiungiInCoda(ElementoCoda elemento) {
        coda.add(elemento);
        if (branoCorrente == null) {
            avviaProssimoElemento();
        }
    }

    //gestione loop 
    @FXML
    public void loopSong() {
        loopAttivo = !loopAttivo;
        System.out.println("[PlayerController] Loop: " + (loopAttivo ? "ON" : "OFF"));
    }

    //task 6.2 gestione skip nelle varie situazioni
    @FXML
    public void skipSong() {
        //caso a. loop attivo riparte la stessa canzone
        if (loopAttivo && branoCorrente != null) {
            riproduciBranoCorrente();
            return;
        }
        //caso b. siamo dentro una playlist, skippa al brano presente nella playlist
        if (iteratorCorrente != null && iteratorCorrente.hasNext()) {
            branoCorrente = iteratorCorrente.next();
            riproduciBranoCorrente();
            return;
        }
        //caso c. skippa all'elemento in coda (brano o playlist)
        avviaProssimoElemento();
    }

    //alterna il bottone play pausa
    @FXML
    public void togglePlayPause() {
        if (playerService != null) playerService.togglePlayPause();
    }

    @FXML
    public void play() {
        togglePlayPause();
    }

    @FXML
    public void skipPlaylist() {
        // TODO: implementare skip dell'intera playlist
        System.out.println("[PlayerController] skipPlaylist cliccato!");
    }

    //logica estrazione elemento in coda
    private void avviaProssimoElemento() {
        ElementoCoda prossimo = coda.poll();

        if (prossimo == null) {
            // Coda vuota: nessun elemento da riprodurre
            branoCorrente = null;
            iteratorCorrente = null;
            System.out.println("[PlayerController] Coda vuota.");
            return;
        }

        // Inizializza l'iteratore sul nuovo elemento (Brano o Playlist)
        iteratorCorrente = prossimo.iterator();

        if (iteratorCorrente.hasNext()) {
            branoCorrente = iteratorCorrente.next();
            riproduciBranoCorrente();
        } else {
            // Elemento vuoto: salta al successivo
            avviaProssimoElemento();
        }
    }

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

        if (playerService != null) {
            playerService.loadTrack(branoCorrente);
        }

        System.out.println("[PlayerController] ▶ " + branoCorrente.getTitolo());
    }

    private void aggiornaUIProgresso(Duration currentTime) {
        int minuti = (int) currentTime.toMinutes();
        int secondi = (int) currentTime.toSeconds() % 60;
        if (lblTempo != null) lblTempo.setText(String.format("%02d:%02d", minuti, secondi));
        if (sliderProgresso != null) sliderProgresso.setValue(currentTime.toSeconds());
    }

    public void setBrano(Brano brano) {
        branoCorrente = brano;
        riproduciBranoCorrente();
    }

    public Brano getBranoCorrente() { return branoCorrente; }
    public boolean isLoopAttivo() { return loopAttivo; }
    public int getDimensioneCoda() { return coda.size(); }
}