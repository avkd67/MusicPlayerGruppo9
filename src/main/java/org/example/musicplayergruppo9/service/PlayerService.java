package org.example.musicplayergruppo9.service;

import javafx.application.Platform;
import javafx.util.Duration;
import javazoom.jl.player.Player;
import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.pattern.state.PausedState;
import org.example.musicplayergruppo9.pattern.state.PlayerState;

import java.io.File;
import java.io.FileInputStream;
import java.util.function.Consumer;

public class PlayerService {

    private Player jPlayer;
    private PlayerState currentState;

    // Volatile per garantire la corretta sincronizzazione tra i thread
    private volatile Thread playerThread;

    // Callback
    private Consumer<String> updateButtonCallback;
    private Consumer<Duration> updateTimeCallback;
    private Runnable onReadyCallback;

    // Variabili per JLayer, purtroppo MediaPlayer e Media mi davano problemi
    private File currentFile;

    // Volatile così le modifiche di stato sono subito visibili ai thread secondari
    private volatile boolean isPlaying = false;
    private volatile boolean isStopped = true;

    private double totalSeconds = 0;
    private double currentSeconds = 0;

    public PlayerService() {
        this.currentState = new PausedState(); // Stato iniziale
    }

    public void setCallbacks(Consumer<String> updateButtonCallback, Consumer<Duration> updateTimeCallback, Runnable onReadyCallback) {
        this.updateButtonCallback = updateButtonCallback;
        this.updateTimeCallback = updateTimeCallback;
        this.onReadyCallback = onReadyCallback;
    }

    public void loadTrack(Brano brano) {
        stopAudio();

        File fileAudio = brano.getFileAudio();
        if (fileAudio != null && fileAudio.exists()) {
            this.currentFile = fileAudio;

            // Stima la durata: 24000 bytes al secondo
            this.totalSeconds = currentFile.length() / 24000;
            this.currentSeconds = 0;

            // Notifica la UI per aggiornare la fine dello slider
            if (onReadyCallback != null) {
                Platform.runLater(onReadyCallback);
            }

            // Inizializza lo stato e avvia la riproduzione
            this.currentState = new PausedState();
            togglePlayPause(); // Clicca "Play" in automatico
        }
    }

    public void togglePlayPause() {
        if (currentFile != null) {
            currentState.toggle(this);
        }
    }

    // Metodo chiamato da PlayingState
    public void playAudio() {
        isPlaying = true;
        if (isStopped) {
            isStopped = false;
            startPlayerThread();
        }
    }

    // Metodo chiamato da PausedState
    public void pauseAudio() {
        isPlaying = false;
    }

    private void startPlayerThread() {
        playerThread = new Thread(() -> {
            // Salva il riferimento allo specifico thread
            Thread thisThread = Thread.currentThread();

            try {
                FileInputStream fis = new FileInputStream(currentFile);

                jPlayer = new Player(fis);
                long lastTimeMillis = System.currentTimeMillis();

                // Gira solo se non è stoppato E se questo thread è ancora quello "ufficiale"
                while (!isStopped && playerThread == thisThread) {
                    if (isPlaying) {
                        // play(1) riproduce 1 singolo frame (circa 26ms). Se ritorna false, il brano è finito.
                        if (!jPlayer.play(1)) {
                            // Se il brano finisce e il thread è ancora quello corretto, ferma tutto
                            if (playerThread == thisThread) {
                                isStopped = true;
                                Platform.runLater(this::onEndOfMedia);
                            }
                            break;
                        }

                        // Calcola il tempo trascorso
                        long now = System.currentTimeMillis();
                        double deltaSeconds = (now - lastTimeMillis) / 1000.0;
                        lastTimeMillis = now;
                        currentSeconds += deltaSeconds;

                        // Aggiorna lo slider nella UI tramite Platform.runLater (cruciale per JavaFX)
                        if (updateTimeCallback != null) {
                            Platform.runLater(() -> updateTimeCallback.accept(Duration.seconds(currentSeconds)));
                        }
                    } else {
                        // Se in pausa, evita di usare tutta la CPU
                        Thread.sleep(50);
                        lastTimeMillis = System.currentTimeMillis();
                    }
                }
            } catch (InterruptedException e) {
                // Il thread è stato interrotto da stopAudio(), esce pacificamente
                System.out.println("Vecchio thread interrotto in modo sicuro.");
            } catch (Exception e) {
                // Cattura eventuali stream closed quando facciamo seek
                System.out.println("Stream MP3 interrotto o chiuso.");
            }
        });
        playerThread.setDaemon(true); // Termina se l'app viene chiusa
        playerThread.start();
    }

    public void stopAudio() {
        isStopped = true;
        isPlaying = false;

        if (jPlayer != null) {
            try {
                jPlayer.close(); // Questo farà terminare pacificamente il ciclo if(!jPlayer.play(1))
            } catch (Exception e) {
                // Ignoriamo le eccezioni durante la chiusura forzata del player
            }
            jPlayer = null;
        }

        // Se c'è un thread attivo e in pausa, lo interrompiamo forzatamente
        if (playerThread != null) {
            playerThread.interrupt();
        }
    }

    private void onEndOfMedia() {
        stopAudio();
        currentSeconds = 0;
        this.currentState = new PausedState();
        updatePlayButtonUI("▶");
        if (updateTimeCallback != null) {
            updateTimeCallback.accept(Duration.ZERO);
        }
    }

    // --- Metodi usati dal Pattern State ---

    public void setState(PlayerState state) {
        this.currentState = state;
    }

    public void updatePlayButtonUI(String text) {
        if (updateButtonCallback != null) {
            updateButtonCallback.accept(text);
        }
    }

    public Duration getTotalDuration() {
        return Duration.seconds(totalSeconds);
    }
}