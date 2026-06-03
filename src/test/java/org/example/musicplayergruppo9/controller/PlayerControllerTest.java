package org.example.musicplayergruppo9.controller;

import org.example.musicplayergruppo9.model.Brano;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerControllerTest {

    private PlayerController player;
    private Brano b1, b2;

    //creazione di player e brani
    @BeforeEach
    void setUp() {
        player = new PlayerController();
        b1 = new Brano(1, "Brano1", "Artista1", "Pop", 0, 180, "/path/1.mp3", "mp3", null, false, false, false);
        b2 = new Brano(2, "Brano2", "Artista2", "Pop", 0, 200, "/path/2.mp3", "mp3", null, false, false, false);
    }

    //per verificare che l'elemento aggiunto in coda verrà prodotto dal player
    @Test
    void aggiungiInCodaAvviaIlPrimoBrano() {
        player.aggiungiInCoda(b1);
        assertEquals(b1, player.getBranoCorrente());
    }

    //Test per task 6.2: skip del brano

    //coda: b1 b2; skippa il brano, deve riprodurre b2
    @Test
    void skipBranoSingoloProcedeCoda() {
        player.aggiungiInCoda(b1);
        player.aggiungiInCoda(b2);

        player.skipSong(); //salta b1

        assertEquals(b2, player.getBranoCorrente());
    }

    //skip dell'ultimo brano in coda
    //il player deve fermarsi
    @Test
    void skipSuUltimoBranoSvuotaPlayer() {
        player.aggiungiInCoda(b1);

        player.skipSong();

        assertNull(player.getBranoCorrente());
    }

    //test per task 7.2: loop del brano

    //loop spento quando si avvia il player
    @Test
    void loopDisattivoPerDefault() {
        assertFalse(player.isLoopAttivo());
    }

    @Test
    void loopBranoAttivaIlLoop() {
        player.loopSong();
        assertTrue(player.isLoopAttivo());
    }

    //per verificare che il bottone loop si spenga e accenda
    @Test
    void loopBranoToggle() {
        player.loopSong(); //acceso
        player.loopSong(); //spento
        assertFalse(player.isLoopAttivo());
    }

    //con il loop attivo, lo skip non deve cambiare brano
    @Test
    void skipConLoopAttivoNonAvanza() {
        player.aggiungiInCoda(b1);
        player.aggiungiInCoda(b2);

        player.loopSong();
        player.skipSong();

        assertEquals(b1, player.getBranoCorrente());
    }

    //vale anche dopo due skip
    @Test
    void skipConLoopAttivoIgnoraCoda() {
        player.aggiungiInCoda(b1);
        player.aggiungiInCoda(b2);

        player.loopSong();
        player.skipSong();
        player.skipSong();

        assertEquals(b1, player.getBranoCorrente());
        assertEquals(1, player.getDimensioneCoda());
    }

    //dopo aver disattivato il loop, si deve ripristinare il funzionamento dello skip
    @Test
    void skipConLoopDisattivatoAvanzaCorrettamente() {
        player.aggiungiInCoda(b1);
        player.aggiungiInCoda(b2);

        player.loopSong(); //acceso
        player.loopSong(); //spento
        player.skipSong();

        assertEquals(b2, player.getBranoCorrente());
    }

    //la strategia iniziale deve essere StrategiaSequenziale
    @Test
    void strategiaDefaultESequenziale() {
        assertFalse(player.isLoopAttivo());
    }

    //quando il loop è attivo, deve operare la StrategiaLoop
    @Test
    void loopSongCambiaStrategiaALoop() {
        player.loopSong();
        assertTrue(player.isLoopAttivo());
    }

    //se disattivo il loop, deve ritornare la StrategiaSequenziale
    @Test
    void loopSongToggleTornaASequenziale() {
        player.loopSong();
        player.loopSong();
        assertFalse(player.isLoopAttivo());
    }
}