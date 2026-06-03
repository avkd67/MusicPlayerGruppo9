package org.example.musicplayergruppo9.service;

import javafx.application.Platform;
import org.example.musicplayergruppo9.model.Brano;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;

public class PlayerServiceTest {

    private PlayerService playerService;
    private File tempAudioFile;
    private String statoBottone; // Variabile per intercettare i cambiamenti di stato

    @BeforeAll
    public static void initJFX() {
        try {
            // Forza l'avvio del Toolkit di JavaFX in background per evitare l'IllegalStateException
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Se il Toolkit è già stato avviato (es. lanciando tutta la suite di test assieme),
            // la startup lancia un'eccezione che ignoro beatamente
        }
    }

    @BeforeEach
    public void setUp() throws IOException {
        playerService = new PlayerService();
        statoBottone = "Non Inizializzato";

        // Invece di aggiornare la vera UI (che non esiste durante i test), ho creato un callback che salva il testo del bottone nella nostra variabile.
        playerService.setCallbacks(
                testo -> statoBottone = testo, // updateButtonCallback
                tempo -> {},                   // updateTimeCallback (lo lasciamo vuoto, non ci serve)
                () -> {}                       // onReadyCallback (vuoto, anche questo non ci serve qui)
        );

        // Creo un file temporaneo vuoto per permettere al togglePlayPause() di funzionare
        tempAudioFile = File.createTempFile("fake_audio_playpause", ".mp3");
    }

    @AfterEach
    public void tearDown() {
        // Chiudo il player per uccidere il Thread e non intasare la memoria
        if (playerService != null) {
            playerService.stopAudio();
        }

        // Pulisco il file temporaneo
        if (tempAudioFile != null && tempAudioFile.exists()) {
            tempAudioFile.delete();
        }
    }

    @Test
    public void testAlternanzaPlayPause() {
        // Creo un brano base puntato al file temporaneo
        Brano branoFinto = new Brano();
        branoFinto.setPercorsoFileAudio(tempAudioFile.getAbsolutePath());

        // Carico il brano tramite load (che chiama poi toggleplay in automatico
        playerService.loadTrack(branoFinto);

        // A questo punto il pattern State dovrebbe essere passato in PlayingState
        // e aver aggiornato la UI con il simbolo di pausa.
        Assertions.assertEquals("⏸", statoBottone, "Dopo aver caricato il brano, deve entrare in Play e mostrare Pausa");

        // Passo allo stato di pausa
        playerService.togglePlayPause();

        // Il pattern State dovrebbe essere passato in PausedState
        Assertions.assertEquals("▶", statoBottone, "Dopo aver cliccato toggle, deve andare in Pausa e mostrare Play");

        // Ripasso allo stato di play
        playerService.togglePlayPause();

        // Il pattern State dovrebbe tornare in PlayingState
        Assertions.assertEquals("⏸", statoBottone, "Dopo un altro toggle, deve tornare in Play e mostrare Pausa di nuovo");
    }
}