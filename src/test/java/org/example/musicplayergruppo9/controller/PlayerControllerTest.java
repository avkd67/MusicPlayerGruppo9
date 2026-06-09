package org.example.musicplayergruppo9.controller;

import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.Playlist;
import org.example.musicplayergruppo9.service.PlayerService;
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
        b1 = new Brano(1, "Brano1", "Artista1", "Pop", 0, 180, "/path/1.mp3", "mp3", null, false, false, false, 0);
        b2 = new Brano(2, "Brano2", "Artista2", "Pop", 0, 200, "/path/2.mp3", "mp3", null, false, false, false, 0);
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

    //playlist con un solo brano: dopo lo skip il player si ferma
    @Test
    void skipUnicoBranoDiPlaylistSvuotaPlayer() {
        Brano solo = new Brano(3, "Solo", "Artista3", "Pop", 0, 120, "/path/3.mp3", "mp3", null, false, false, false, 0);
        Playlist pl = creaPlaylist("Singola", solo);
 
        player.aggiungiInCoda(pl);
        player.skipSong();
 
        assertNull(player.getBranoCorrente());
    }
 
    //playlist con b1, b2 -> skipSong deve passare a b2 restando nella playlist
    @Test
    void skipBranoDentroPlaylistPassaAlSuccessivoDellaPlaylist() {
        Playlist pl = creaPlaylist("Playlist Due Brani", b1, b2);
 
        player.aggiungiInCoda(pl);
        player.skipSong();
 
        assertEquals(b2, player.getBranoCorrente());
    }
 
    // dopo uno skip che esaurisce la playlist, il player si ferma se non c'è altro in coda
    @Test
    void skipFinePlaylistSenzaAltroInCodaSvuotaPlayer() {
        Playlist pl = creaPlaylist("Playlist Due Brani", b1, b2);
 
        player.aggiungiInCoda(pl);
        player.skipSong();
        player.skipSong();
 
        assertNull(player.getBranoCorrente());
    }
 
    //dopo lo skip che esaurisce la playlist, deve partire il brano successivo in coda
    @Test
    void skipFinePlaylistPassaAllElementoSuccessivoInCoda() {
        Brano bEsterno = new Brano(3, "BranoEsterno", "Artista3", "Pop", 0, 150, "/path/3.mp3", "mp3", null, false, false, false, 0);
        Playlist pl = creaPlaylist("Playlist Corta", b1);
 
        player.aggiungiInCoda(pl);
        player.aggiungiInCoda(bEsterno);
 
        player.skipSong();
 
        assertEquals(bEsterno, player.getBranoCorrente());
    }
 
    //con il loop attivo, skipSong dentro la playlist riparte dallo stesso brano
    @Test
    void skipBranoDentroPlaylistConLoopAttivoRiparteDaStessoBrano() {
        Playlist pl = creaPlaylist("Playlist Loop", b1, b2);
 
        player.aggiungiInCoda(pl);
        player.loopSong();
        player.skipSong();
 
        assertEquals(b1, player.getBranoCorrente());
    }

    //skipPlaylist deve saltare tutti i brani rimanenti della playlist e passare al successivo in coda
    @Test
    void skipPlaylistSaltaTuttiIBraniEPassaAlSuccessivo() {
        Brano bEsterno = new Brano(3, "BranoEsterno", "Artista3", "Pop", 0, 150, "/path/3.mp3", "mp3", null, false, false, false, 0);
        Playlist pl = creaPlaylist("Playlist Lunga", b1, b2);
 
        player.aggiungiInCoda(pl);
        player.aggiungiInCoda(bEsterno);
 
        player.skipPlaylist();
 
        assertEquals(bEsterno, player.getBranoCorrente());
    }
 
    //skipPlaylist quando non c'è nient'altro in coda deve svuotare il player
    @Test
    void skipPlaylistSenzaAltroInCodaSvuotaPlayer() {
        Playlist pl = creaPlaylist("Playlist Sola", b1, b2);
 
        player.aggiungiInCoda(pl);
        player.skipPlaylist();
 
        assertNull(player.getBranoCorrente());
    }
 
    // skipPlaylist con loop attivo non deve fare nulla (comportamento definito nel controller)
    @Test
    void skipPlaylistConLoopAttivoNonCambiaBrano() {
        Playlist pl = creaPlaylist("Playlist Loop", b1, b2);
 
        player.aggiungiInCoda(pl);
        player.loopSong();
        player.skipPlaylist();
 
        assertEquals(b1, player.getBranoCorrente());
    }
    
    private Playlist creaPlaylist(String nome, Brano... brani) {
        Playlist pl = new Playlist();
        pl.setId(99);
        pl.setNome(nome);
        for (Brano b : brani) {
            pl.aggiungiBrano(b);
        }
        return pl;
    }
    // --- TEST PER LO SLIDER (DRAG) ---

    @Test
    void sliderDragAggiornaTempoDiRiproduzione() throws Exception {
        player.aggiungiInCoda(b1);
        PlayerService service = getPlayerServiceTramiteReflection(player);

        // Inseriamo un file fittizio per superare il blocco "if (currentFile == null)"
        java.lang.reflect.Field fileField = service.getClass().getDeclaredField("currentFile");
        fileField.setAccessible(true);
        fileField.set(service, new java.io.File("dummy.mp3"));

        // Simuliamo l'utente che trascina lo slider al secondo 60
        service.seek(60.0);

        double tempoAttuale = ottieniTempoTramiteReflection(service);
        assertEquals(60.0, tempoAttuale, "Il tempo di riproduzione interno deve corrispondere alla posizione dello slider");
    }

    // Se lo slider viene trascinato a un valore negativo (errore UI)
    @Test
    void sliderDragValoreNegativoRipristinaAInizio() throws Exception {
        player.aggiungiInCoda(b1);
        PlayerService service = getPlayerServiceTramiteReflection(player);

        // Inseriamo un file fittizio
        java.lang.reflect.Field fileField = service.getClass().getDeclaredField("currentFile");
        fileField.setAccessible(true);
        fileField.set(service, new java.io.File("dummy.mp3"));

        // Simuliamo un drag accidentale a valori negativi
        service.seek(-10.0);

        double tempoAttuale = ottieniTempoTramiteReflection(service);
        assertEquals(0.0, tempoAttuale, "I valori negativi dovrebbero essere sanificati all'inizio del brano (0.0)");
    }

    // Se lo slider viene trascinato a un valore superiore alla durata del brano
    @Test
    void sliderDragOltreDurataPassaAlBranoSuccessivo() {
        player.aggiungiInCoda(b1); // dura 180 secondi
        player.aggiungiInCoda(b2);

        PlayerService service = getPlayerServiceTramiteReflection(player);

        // Simuliamo un drag oltre il limite (es. 185 secondi)
        service.seek(185.0);
        assertEquals(b2, player.getBranoCorrente(), "Trascinare lo slider oltre la fine dovrebbe far scattare il brano successivo");
    }



    // --- METODI DI UTILITY PER TEST DI SLIDER ---

    // Recupera il PlayerService nascosto dentro il PlayerController
    private PlayerService getPlayerServiceTramiteReflection(PlayerController controller) {
        try {
            java.lang.reflect.Field serviceField = null;
            for (java.lang.reflect.Field f : controller.getClass().getDeclaredFields()) {
                if (f.getType().equals(org.example.musicplayergruppo9.service.PlayerService.class)) {
                    serviceField = f;
                    break;
                }
            }
            if (serviceField == null) {
                fail("Istanza di PlayerService non trovata dentro PlayerController");
            }
            serviceField.setAccessible(true);

            // Leggiamo il valore attuale
            PlayerService service = (PlayerService) serviceField.get(controller);

            // Se il service è null (perché il costruttore o l'initialize di JavaFX non l'hanno creato),
            // lo creiamo noi e lo iniettiamo direttamente nel controller!
            if (service == null) {
                service = new PlayerService();
                serviceField.set(controller, service);
            }

            return service;
        } catch (Exception e) {
            fail("Impossibile accedere al PlayerService: " + e.getMessage());
            return null;
        }
    }

    // Legge i secondi correnti dal PlayerService
    private double ottieniTempoTramiteReflection(PlayerService service) {
        try {
            java.lang.reflect.Field secondsField = service.getClass().getDeclaredField("currentSeconds");
            secondsField.setAccessible(true);
            return (double) secondsField.get(service);
        } catch (Exception e) {
            fail("Impossibile accedere a currentSeconds: " + e.getMessage());
            return -1;
        }
    }
}
