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
import org.example.musicplayergruppo9.model.Playlist;
import org.example.musicplayergruppo9.pattern.strategy.StrategiaLoop;
import org.example.musicplayergruppo9.pattern.strategy.StrategiaRiproduzione;
import org.example.musicplayergruppo9.pattern.strategy.StrategiaSequenziale;
import org.example.musicplayergruppo9.service.PlayerService;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.musicplayergruppo9.controller.MainController;

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
    @FXML private Button btnLoop;
    @FXML private Button btnSkipPlaylist;

   
    private static PlayerService playerService;
    //per svolgere task 6.2
    private final Queue<ElementoCoda> coda = new LinkedList<>();
    //iterare sull'elemento in riproduzione
    private Iterator<Brano> iteratorCorrente;
    private Brano branoCorrente;
    //per permettere lo skip dell'elemento (brano o playlist) in coda
    private ElementoCoda elementoCorrente;
    //Design Pattern: Strategy
    //scelto per soddisfare la task 7.2: loop
    private StrategiaRiproduzione strategia = new StrategiaSequenziale();
    
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

        //gestione slider per mandare avanti e indietro la musica drag and drop
        sliderProgresso.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(sliderProgresso.isValueChanging() && playerService != null) {
                playerService.seek(sliderProgresso.getValue());
            }
        });

        //gestione slier per mandare avanti e indietro la musica onClick
        /*
        sliderProgresso.setOnMouseReleased(event -> {
            if(playerService != null && !sliderProgresso.isValueChanging()) {
                playerService.seek(sliderProgresso.getValue());
            }
        });
        //Non so perché ma a volte non funziona
        */

        // Task 7.2 — se loop attivo riparte, altrimenti Task 6.2 — skippa al successivo
        playerService.setOnEndOfMediaCallback(() -> {
            strategia.onFineBrano(this); 
        });


    }

    //gestione coda
    public void aggiungiInCoda(ElementoCoda elemento) {
        coda.add(elemento);
        if (branoCorrente == null) {
            avviaProssimoElemento();
        }
    }

    // Restituisce una snapshot della coda per visualizzazione (non altera lo stato)
    public java.util.List<ElementoCoda> getCodaSnapshot() {
        return new java.util.ArrayList<>(coda);
    }

    // Rimuove il primo elemento uguale passato dalla coda (ritorna true se rimosso)
    public boolean rimuoviDaCoda(ElementoCoda elemento) {
        return coda.remove(elemento);
    }

    @FXML
    public void loopSong() {
        if (strategia instanceof StrategiaLoop) {
            strategia = new StrategiaSequenziale();
            btnLoop.getStyleClass().remove("button-attivo");
            System.out.println("[PlayerController] Loop: OFF");
        } else {
            strategia = new StrategiaLoop();
            btnLoop.getStyleClass().add("button-attivo");
            System.out.println("[PlayerController] Loop: ON");
        }
    }

    //task 6.2 gestione skip nelle varie situazioni
    @FXML
    public void skipSong() {

        //caso a. loop attivo riparte la stessa canzone
        if (strategia instanceof StrategiaLoop) {
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
        // Se loop attivo, non fare nulla (o decidi tu il comportamento)
        if (strategia instanceof StrategiaLoop) return;

        //playlist in riproduzione, skippa l'intera playlist e passa al  prossimo elemento in coda
        if (elementoCorrente instanceof Playlist) {
            iteratorCorrente = null;   // esaurisci l'iteratore della playlist corrente
            avviaProssimoElemento();
            return;
        }

        avviaProssimoElemento();
    }

    


    //logica estrazione elemento in coda
    private void avviaProssimoElemento() {
        ElementoCoda prossimo = coda.poll();

        if (prossimo == null) {
            // Coda vuota: nessun elemento da riprodurre
            branoCorrente = null;
            iteratorCorrente = null;
            elementoCorrente = null;

            aggiornaVisibilitaSkipPlaylist();

            svuotaCoda();
            if (mainController != null) {
                mainController.chiudiPlayerVisivamente();
            }
            
            return;
        }

        elementoCorrente = prossimo;
        aggiornaVisibilitaSkipPlaylist();

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

    public void riproduciBranoCorrente() {
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

    }

    private void aggiornaUIProgresso(Duration currentTime) {
        int minuti = (int) currentTime.toMinutes();
        int secondi = (int) currentTime.toSeconds() % 60;
        if (lblTempo != null) lblTempo.setText(String.format("%02d:%02d", minuti, secondi));
        if (sliderProgresso != null) sliderProgresso.setValue(currentTime.toSeconds());

        if (sliderProgresso != null && !sliderProgresso.isValueChanging()) {
            sliderProgresso.setValue(currentTime.toSeconds());
        }
    }

    public void setBrano(Brano brano) {
        branoCorrente = brano;
        riproduciBranoCorrente();
    }

    // Riferimento al MainController per aprire finestre figlie
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void apriCoda() {
        if (mainController != null) {
            mainController.apriCodaRiproduzione();
        }
    }

    public Brano getBranoCorrente() { return branoCorrente; }

    public boolean isLoopAttivo() { return strategia instanceof StrategiaLoop; }
    
    public int getDimensioneCoda() { return coda.size(); }

    // per gestire l'eliminazione di un file in riproduzione
    public void fermaErilasciaFileSeCorrente(Brano branoDaEliminare) {
        if (branoCorrente != null && branoCorrente.getId() == branoDaEliminare.getId()) {
            if (playerService != null) {
                playerService.stopAudio(); // Chiude il FileInputStream
            }

            // Ripulisce l'interfaccia utente del player
            branoCorrente = null;
            if (lblTitolo != null) lblTitolo.setText("Nessun brano");
            if (lblArtista != null) lblArtista.setText("");
            if (imgCopertina != null) imgCopertina.setImage(null);
            if (lblTempo != null) lblTempo.setText("00:00");
            if (sliderProgresso != null) sliderProgresso.setValue(0);


            System.out.println("[PlayerController] File rilasciato per eliminazione.");
        }
    }

    public void svuotaCoda() {
        // Svuota la coda degli elementi futuri
        coda.clear();

        // Resetta l'iteratore della playlist corrente
        iteratorCorrente = null;

        branoCorrente = null;

        elementoCorrente = null;

        // Ferma fisicamente jPlayer tramite il PlayerService
        if (playerService != null) {
            playerService.stopAudio();
        }

        System.out.println("[PlayerController] Coda svuotata e jPlayer fermato.");
    }

    private void aggiornaVisibilitaSkipPlaylist() {
        boolean isPlaylist = elementoCorrente instanceof Playlist;
        btnSkipPlaylist.setVisible(isPlaylist);
        btnSkipPlaylist.setManaged(isPlaylist); 
    }
}