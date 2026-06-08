package org.example.musicplayergruppo9.controller;

import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.Playlist;
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
}
