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
import org.example.musicplayergruppo9.model.ElementoCodaConOpzioni;
import org.example.musicplayergruppo9.model.Playlist;
import org.example.musicplayergruppo9.pattern.strategy.StrategiaLoop;
import org.example.musicplayergruppo9.pattern.strategy.StrategiaRiproduzione;
import org.example.musicplayergruppo9.pattern.strategy.StrategiaSequenziale;
import org.example.musicplayergruppo9.pattern.strategy.StrategiaRandom;
import org.example.musicplayergruppo9.service.PlayerService;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.musicplayergruppo9.controller.MainController;
import org.example.musicplayergruppo9.pattern.observer.PlaylistObserver;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PlayerController implements PlaylistObserver {

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
    
    private boolean randomAttivo = false;

    private Playlist playlistOsservata = null;

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
            if (btnLoop != null) btnLoop.getStyleClass().remove("button-attivo");
            System.out.println("[PlayerController] Loop: OFF");
        } else {
            strategia = new StrategiaLoop();
            if (btnLoop != null) btnLoop.getStyleClass().add("button-attivo");
            System.out.println("[PlayerController] Loop: ON");
        }
    }

    //task 6.2 gestione skip nelle varie situazioni
    @FXML
    public void skipSong() {
        ElementoCoda elementoReale = estraiElementoReale(elementoCorrente);
        boolean thisRandom = estraiRandom(elementoCorrente);

        //caso a. loop attivo riparte la stessa canzone
        if (strategia instanceof StrategiaLoop && !(elementoCorrente instanceof Playlist)) {
            riproduciBranoCorrente();
            return;
        }

         // caso b. siamo dentro una playlist e ha ancora brani
        if (iteratorCorrente != null && iteratorCorrente.hasNext()) {
            branoCorrente = iteratorCorrente.next();
            riproduciBranoCorrente();
            return;
        }

        // caso c. playlist finita — se loop playlist, ricomincia dall'inizio
        if (elementoCorrente instanceof Playlist && strategia instanceof StrategiaLoop) {
            Playlist p = (Playlist) elementoReale;
            iteratorCorrente = thisRandom ? p.getRandomIterator() : p.iterator();
            if (iteratorCorrente.hasNext()) {
                branoCorrente = iteratorCorrente.next();
                riproduciBranoCorrente();
                return;
            }
        }
        
        //caso d. skippa all'elemento in coda (brano o playlist)
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

        // Salta l'intera playlist corrente
        iteratorCorrente = null;

        avviaProssimoElemento();
    }


    //logica estrazione elemento in coda
    private void avviaProssimoElemento() {
        smetteDiOsservare();

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
                mainController.notificaFinePlaylist();
            }
            
            return;
        }

        elementoCorrente = prossimo;
        aggiornaVisibilitaSkipPlaylist();

        //Legge il flag random dall'elemento stesso (se wrappato), non dal globale
        boolean thisRandom = estraiRandom(prossimo);
        ElementoCoda elementoReale = estraiElementoReale(prossimo);

         //Registra observer se è una playlist
         if (elementoReale instanceof Playlist) {
            osservaPlaylist((Playlist) elementoReale);
        }

        //Crea l'iteratore con il random corretto per questo specifico elemento
        if (thisRandom && elementoReale instanceof Playlist) {
            iteratorCorrente = ((Playlist) elementoReale).getRandomIterator();
        } else {
            iteratorCorrente = elementoReale.iterator();
        }

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

    //per gestire l'eliminazione di un file in riproduzione
    public void fermaErilasciaFileSeCorrente(Brano branoDaEliminare) {
        if (branoCorrente != null && branoCorrente.getId() == branoDaEliminare.getId()) {
            if (playerService != null) {
                playerService.stopAudio();
            }

            branoCorrente = null;
            if (lblTitolo != null) lblTitolo.setText("Nessun brano");
            if (lblArtista != null) lblArtista.setText("");
            if (imgCopertina != null) imgCopertina.setImage(null);
            if (lblTempo != null) lblTempo.setText("00:00");
            if (sliderProgresso != null) sliderProgresso.setValue(0);

        }
    }

    public void svuotaCoda() {
        smetteDiOsservare();

        //Svuota la coda degli elementi futuri
        coda.clear();

        // Resetta l'iteratore della playlist corrente
        iteratorCorrente = null;

        branoCorrente = null;

        elementoCorrente = null;

        if (playerService != null) {
            playerService.stopAudio();
        }

    }

    private void aggiornaVisibilitaSkipPlaylist() {
        ElementoCoda reale = estraiElementoReale(elementoCorrente);
        boolean isPlaylist = elementoCorrente instanceof Playlist;
        if (btnSkipPlaylist != null) {
            btnSkipPlaylist.setVisible(isPlaylist);
            btnSkipPlaylist.setManaged(isPlaylist); 
        }
    }

    @FXML
    public void randomPlaylist() {
        randomAttivo = !randomAttivo;

        // Aggiorna solo l'iteratore per i brani futuri, senza avviare nulla
        if (elementoCorrente instanceof Playlist) {
            Playlist p = (Playlist) elementoCorrente;
            iteratorCorrente = randomAttivo ? p.getRandomIterator() : p.iterator();
        }

    }

    public boolean isRandomAttivo() { return randomAttivo; }

    public void setRandomAttivo(boolean valore) {
        randomAttivo = valore;

        // Se la playlist corrente NON è wrappata (aggiunta senza opzioni),
        // aggiorna l'iteratore in tempo reale
        if (!(elementoCorrente instanceof ElementoCodaConOpzioni)) {
            ElementoCoda reale = estraiElementoReale(elementoCorrente);
            if (reale instanceof Playlist) {
                Playlist p = (Playlist) reale;
                iteratorCorrente = randomAttivo ? p.getRandomIterator() : p.iterator();
            }
        }
    }

    // Inizia ad osservare una nuova playlist
    private void osservaPlaylist(Playlist p) {
        if (playlistOsservata != null) {
            playlistOsservata.removePlaylistObserver(this);
        }
        playlistOsservata = p;
        if (p != null) {
            p.addPlaylistObserver(this);
        }
    }

    //Smette di osservare la playlist corrente
    private void smetteDiOsservare() {
        if (playlistOsservata != null) {
            playlistOsservata.removePlaylistObserver(this);
            playlistOsservata = null;
        }
    }

    //PlaylistObserver — notifiche aggiunta/rimozione brani
 
    @Override
    public void onBranoAggiunto(Brano brano) {
        if (playlistOsservata == null) return;
 
        // Ricrea l'iteratore dal brano successivo a quello corrente,
        // così il nuovo brano viene raggiunto naturalmente in sequenza
        List<Brano> brani = playlistOsservata.getBrani();
        int posCorrente = brani.indexOf(branoCorrente);
        int riparti = posCorrente >= 0 ? posCorrente + 1 : brani.size();
        iteratorCorrente = brani.listIterator(riparti);
     }

    @Override
    public void onBranoRimosso(Brano brano) {
        if (playlistOsservata == null) return;
 
        //Caso critico: il brano rimosso è quello in riproduzione ora
        if (branoCorrente != null && branoCorrente.getId() == brano.getId()) {
            skipSong();
            return;
        }
 
        //Brano futuro: ricrea l'iteratore dal punto corretto
        List<Brano> brani = playlistOsservata.getBrani();
        int posCorrente = brani.indexOf(branoCorrente);
        int riparti = posCorrente >= 0 ? posCorrente + 1 : brani.size();
        iteratorCorrente = brani.listIterator(riparti);
     }

    //Estrae il flag random dall'elemento: se è un wrapper legge il suo valore,
    //altrimenti usa il flag globale (per brani singoli aggiunti senza wrapper)
    private boolean estraiRandom(ElementoCoda elemento) {
        if (elemento instanceof ElementoCodaConOpzioni) {
            return ((ElementoCodaConOpzioni) elemento).isRandom();
        }
        return randomAttivo;
    }
 
    
    //Scarta il wrapper per ottenere l'elemento reale (Brano o Playlist)
    private ElementoCoda estraiElementoReale(ElementoCoda elemento) {
        if (elemento instanceof ElementoCodaConOpzioni) {
            return ((ElementoCodaConOpzioni) elemento).getElemento();
        }
        return elemento;
    }
    
}