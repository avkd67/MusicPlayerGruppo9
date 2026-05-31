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
import org.example.musicplayergruppo9.service.PlayerService;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class PlayerController {

    // ── Componenti UI collegati all'FXML ─────────────────────
    @FXML private ImageView imgCopertina;
    @FXML private Label lblTitolo;
    @FXML private Label lblArtista;
    @FXML private Button btnPlayPause;
    @FXML private Label lblTempo;
    @FXML private Slider sliderProgresso;

    // ── Servizio audio (sostituisce MediaPlayer) ──────────────
    /**
     * PlayerService gestisce la riproduzione audio tramite JLayer.
     * È statico per sopravvivere ai cambi di vista senza perdere
     * lo stato di riproduzione.
     */
    private static PlayerService playerService;

    // ── Stato della coda (Task 6.2) ───────────────────────────

    /**
     * Coda principale di riproduzione.
     * Ogni elemento può essere un Brano singolo o una Playlist,
     * entrambi implementano l'interfaccia ElementoCoda.
     */
    private final Queue<ElementoCoda> coda = new LinkedList<>();

    /**
     * Iteratore sull'elemento correntemente in riproduzione.
     * - Se l'elemento è un Brano:    itera su un solo elemento (sé stesso).
     * - Se l'elemento è una Playlist: itera su tutti i suoi brani in ordine.
     */
    private Iterator<Brano> iteratorCorrente;

    /** Brano attualmente in riproduzione (null = niente in riproduzione). */
    private Brano branoCorrente;

    // ── Stato del loop (Task 7.2) ─────────────────────────────

    /**
     * Flag che indica se il loop è attivo.
     * - Se true:  il brano corrente viene ripetuto alla fine,
     *             sia allo skip manuale che alla fine naturale.
     * - Se false: alla fine del brano si passa all'elemento successivo in coda.
     */
    private boolean loopAttivo = false;

    // ── Inizializzazione ──────────────────────────────────────

    /**
     * Chiamato da JavaFX dopo il caricamento dell'FXML.
     * Inizializza il PlayerService (una sola volta grazie allo static)
     * e ricollega i callback UI a questo controller.
     */
    @FXML
    public void initialize() {
        if (playerService == null) {
            playerService = new PlayerService();
        }

        // Ricollega sempre i callback UI al controller corrente,
        // perché potrebbero puntare a un vecchio controller dopo un reload
        playerService.setCallbacks(
                testoBottone -> btnPlayPause.setText(testoBottone),
                tempoAttuale -> aggiornaUIProgresso(tempoAttuale),
                () -> sliderProgresso.setMax(playerService.getTotalDuration().toSeconds())
        );
    }

    // ── Gestione coda (Task 6.2) ──────────────────────────────

    /**
     * Aggiunge un elemento (Brano o Playlist) in fondo alla coda.
     * Se non c'è nulla in riproduzione, avvia immediatamente il nuovo elemento.
     */
    public void aggiungiInCoda(ElementoCoda elemento) {
        coda.add(elemento);
        if (branoCorrente == null) {
            avviaProssimoElemento();
        }
    }

    // ── Task 7.2: Loop del brano ──────────────────────────────

    /**
     * Attiva o disattiva il loop sul brano corrente.
     * Chiamato dal bottone 🔁 nel PlayerView.fxml.
     * Ogni pressione inverte lo stato: OFF→ON oppure ON→OFF.
     */
    @FXML
    public void loopSong() {
        loopAttivo = !loopAttivo;
        System.out.println("[PlayerController] Loop: " + (loopAttivo ? "ON" : "OFF"));
    }

    // ── Task 6.2: Skip brano ──────────────────────────────────

    /**
     * Salta il brano corrente secondo questa logica:
     *
     * CASO A — Loop attivo:
     *   Il brano corrente riparte da capo (il loop ha priorità sullo skip).
     *
     * CASO B — Siamo dentro una Playlist con altri brani:
     *   Passa al brano successivo nella stessa Playlist.
     *
     * CASO C — Brano singolo o Playlist esaurita:
     *   Estrae il prossimo ElementoCoda dalla coda principale.
     */
    @FXML
    public void skipSong() {
        // Caso A: loop attivo → risuona il brano corrente da capo
        if (loopAttivo && branoCorrente != null) {
            riproduciBranoCorrente();
            return;
        }

        // Caso B: dentro una playlist con altri brani → avanza al successivo
        if (iteratorCorrente != null && iteratorCorrente.hasNext()) {
            branoCorrente = iteratorCorrente.next();
            riproduciBranoCorrente();
            return;
        }

        // Caso C: elemento corrente esaurito → prossimo elemento in coda
        avviaProssimoElemento();
    }

    // ── Controlli player ──────────────────────────────────────

    /**
     * Alterna tra play e pausa tramite PlayerService.
     * Il testo del bottone viene aggiornato dal callback impostato
     * in initialize() → playerService.setCallbacks(...).
     */
    @FXML
    public void togglePlayPause() {
        if (playerService != null) playerService.togglePlayPause();
    }

    /** Metodo mantenuto per compatibilità con il MainView.fxml se presente. */
    @FXML
    public void play() {
        togglePlayPause();
    }

    @FXML
    public void skipPlaylist() {
        // TODO: implementare skip dell'intera playlist
        System.out.println("[PlayerController] skipPlaylist cliccato!");
    }

    // ── Logica interna ────────────────────────────────────────

    /**
     * Estrae il prossimo ElementoCoda dalla coda principale e lo avvia.
     * Se l'elemento è vuoto (es. playlist senza brani), lo salta ricorsivamente.
     * Se la coda è vuota, resetta lo stato del player.
     */
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
            // Elemento vuoto (es. playlist senza brani): salta al successivo
            System.out.println("[PlayerController] Elemento vuoto, salto: " + prossimo.getTitolo());
            avviaProssimoElemento();
        }
    }

    /**
     * Avvia la riproduzione del branoCorrente:
     * 1. Aggiorna i componenti UI (titolo, artista, copertina).
     * 2. Carica il brano nel PlayerService che gestisce l'audio.
     * 3. PlayerService al termine del brano chiama onEndOfMedia()
     *    internamente — per il loop e lo skip automatico gestiamo
     *    la logica nel callback di fine brano registrato qui.
     */
    private void riproduciBranoCorrente() {
        if (branoCorrente == null) return;

        // Aggiorna UI (null check per sicurezza durante i test)
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

        // Carica e avvia il brano nel PlayerService
        if (playerService != null) {
            playerService.loadTrack(branoCorrente);
        }

        System.out.println("[PlayerController] ▶ " + branoCorrente.getTitolo());
    }

    /**
     * Aggiorna l'etichetta del tempo e lo slider di avanzamento.
     * Chiamato dal callback del PlayerService ad ogni frame riprodotto.
     */
    private void aggiornaUIProgresso(Duration currentTime) {
        int minuti = (int) currentTime.toMinutes();
        int secondi = (int) currentTime.toSeconds() % 60;
        if (lblTempo != null) lblTempo.setText(String.format("%02d:%02d", minuti, secondi));
        if (sliderProgresso != null) sliderProgresso.setValue(currentTime.toSeconds());
    }

    /**
     * Imposta direttamente un brano e lo riproduce subito.
     * Usato da MainController quando l'utente clicca su un brano in libreria.
     */
    public void setBrano(Brano brano) {
        branoCorrente = brano;
        riproduciBranoCorrente();
    }

    // ── Getter ────────────────────────────────────────────────

    public Brano getBranoCorrente() { return branoCorrente; }
    public boolean isLoopAttivo() { return loopAttivo; }
    public int getDimensioneCoda() { return coda.size(); }
}