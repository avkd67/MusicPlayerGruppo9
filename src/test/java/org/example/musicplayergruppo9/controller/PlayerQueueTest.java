package org.example.musicplayergruppo9.controller;

import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.ElementoCoda;
import org.example.musicplayergruppo9.model.ElementoCodaConOpzioni;
import org.example.musicplayergruppo9.model.Playlist;
import org.example.musicplayergruppo9.pattern.strategy.StrategiaOrdineSequenziale;
import org.example.musicplayergruppo9.pattern.strategy.StrategiaOrdineShuffle;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerQueueTest {

    @Test
    void accodaBrano_senzaInterrompereCorrente() {
        PlayerController pc = new PlayerController();

        Brano corrente = new Brano("C","A","g",2020,100,"/tmp/c.mp3","mp3",null,false,false);
        pc.setBrano(corrente);

        Brano b = new Brano("N","B","g",2021,120,"/tmp/n.mp3","mp3",null,false,false);
        pc.aggiungiInCoda(b);

        assertSame(corrente, pc.getBranoCorrente());
        assertEquals(1, pc.getDimensioneCoda());
    }

    @Test
    void accodaPlaylist_senzaInterrompereCorrente() {
        PlayerController pc = new PlayerController();

        Brano corrente = new Brano("X","Y","g",2020,90,"/tmp/x.mp3","mp3",null,false,false);
        pc.setBrano(corrente);

        Playlist pl = new Playlist("P", null);
        pl.aggiungiBrano(new Brano("a","a","g",2021,80,"/tmp/a.mp3","mp3",null,false,false));
        pc.aggiungiInCoda(pl);

        assertSame(corrente, pc.getBranoCorrente());
        assertEquals(1, pc.getDimensioneCoda());
    }

    @Test
    void rimuoviDaCoda_funzionaERiduceDimensione() {
        PlayerController pc = new PlayerController();

        Brano b1 = new Brano("b1","a1","g",2020,60,"/tmp/1.mp3","mp3",null,false,false);
        Brano b2 = new Brano("b2","a2","g",2020,60,"/tmp/2.mp3","mp3",null,false,false);

        pc.aggiungiInCoda(b1);
        pc.aggiungiInCoda(b2);

        // se il primo avvia la riproduzione, la coda conterrà 1 elemento (b2)
        int sizeBefore = pc.getDimensioneCoda();

        List<ElementoCoda> snapshot = pc.getCodaSnapshot();
        assertFalse(snapshot.isEmpty());

        boolean removed = pc.rimuoviDaCoda(snapshot.get(0));
        assertTrue(removed);
        assertEquals(sizeBefore - 1, pc.getDimensioneCoda());
    }

    @Test
    void getCodaSnapshot_isCopy() {
        PlayerController pc = new PlayerController();
        Brano b = new Brano("b","a","g",2020,60,"/tmp/1.mp3","mp3",null,false,false);
        pc.aggiungiInCoda(b);

        List<ElementoCoda> snap = pc.getCodaSnapshot();
        int oldSize = snap.size();
        // modifica snapshot non deve riflettersi nella coda interna
        snap.clear();
        assertEquals(oldSize, pc.getCodaSnapshot().size());
    }

    //test sulla possibilità di aggiunere stessa playlist in coda con riproduzione diversa
    @Test
    void accodaStessaPlaylistConOpzioniDiverse_mantieneElementiDistinti() {
        PlayerController pc = new PlayerController();
        Brano corrente = new Brano("Corrente","Artista","g",2020,60,"/tmp/c.mp3","mp3",null,false,false);
        pc.setBrano(corrente);

        Playlist playlist = creaPlaylist("Stessa playlist",
                new Brano("a","a","g",2021,80,"/tmp/a.mp3","mp3",null,false,false),
                new Brano("b","b","g",2021,90,"/tmp/b.mp3","mp3",null,false,false)
        );
        ElementoCodaConOpzioni sequenziale =
                new ElementoCodaConOpzioni(playlist, new StrategiaOrdineSequenziale(), false);
        ElementoCodaConOpzioni casuale =
                new ElementoCodaConOpzioni(playlist, new StrategiaOrdineShuffle(), true);

        pc.aggiungiInCoda(sequenziale);
        pc.aggiungiInCoda(casuale);

        List<ElementoCoda> snapshot = pc.getCodaSnapshot();
        assertEquals(2, snapshot.size());
        assertSame(sequenziale, snapshot.get(0));
        assertSame(casuale, snapshot.get(1));
        assertSame(playlist, ((ElementoCodaConOpzioni) snapshot.get(0)).getElemento());
        assertSame(playlist, ((ElementoCodaConOpzioni) snapshot.get(1)).getElemento());
        assertInstanceOf(StrategiaOrdineSequenziale.class,
                ((ElementoCodaConOpzioni) snapshot.get(0)).getStrategiaOrdine());
        assertFalse(((ElementoCodaConOpzioni) snapshot.get(0)).isLoop());
        assertInstanceOf(StrategiaOrdineShuffle.class,
                ((ElementoCodaConOpzioni) snapshot.get(1)).getStrategiaOrdine());
        assertTrue(((ElementoCodaConOpzioni) snapshot.get(1)).isLoop());
    }

    //stesso scenario ma con skip
    @Test
    void riproduceStessaPlaylistAccodataSequenzialeECasuale_senzaPerdereLaSecondaOccorrenza() {
        PlayerController pc = new PlayerController();
        Brano primo = new Brano(1, "Primo", "a", "g", 2021, 80, "/tmp/1.mp3", "mp3", null, false, false, false, 0);
        Brano secondo = new Brano(2, "Secondo", "a", "g", 2021, 90, "/tmp/2.mp3", "mp3", null, false, false, false, 0);
        Playlist playlist = creaPlaylist("Stessa playlist", primo, secondo);

        pc.aggiungiInCoda(new ElementoCodaConOpzioni(
                playlist, new StrategiaOrdineSequenziale(), false));
        pc.aggiungiInCoda(new ElementoCodaConOpzioni(
                playlist, new StrategiaOrdineShuffle(), false));

        assertSame(primo, pc.getBranoCorrente(), "La prima occorrenza deve rispettare l'opzione sequenziale");
        assertEquals(1, pc.getDimensioneCoda(), "La seconda occorrenza della stessa playlist deve restare in coda");

        pc.skipPlaylist();

        assertTrue(List.of(primo, secondo).contains(pc.getBranoCorrente()),
                "Dopo lo skip deve partire la seconda occorrenza della playlist");
        assertEquals(0, pc.getDimensioneCoda());
    }

    private Playlist creaPlaylist(String nome, Brano... brani) {
        Playlist playlist = new Playlist(nome, null);
        for (Brano brano : brani) {
            playlist.aggiungiBrano(brano);
        }
        return playlist;
    }

    //test sull'aggiunta di un brano in coda, verrà subito messo in riproduzione
    @Test
    void aggiungiInCoda_primoElemento_divienesSubitoBranoCorrente() {
        PlayerController pc = new PlayerController();
        assertNull(pc.getBranoCorrente(), "Senza elementi la riproduzione non deve essere attiva");
    
        Brano b = new Brano("Solo","A","g",2021,60,"/tmp/s.mp3","mp3",null,false,false);
        pc.aggiungiInCoda(b);
    
        assertSame(b, pc.getBranoCorrente(), "Il primo brano accodato deve avviarsi come corrente");
        assertEquals(0, pc.getDimensioneCoda(), "La coda deve essere vuota: il brano è già in riproduzione");
    }

    //test sull'aggiunta di una playlist in coda, verrà subito messo in riproduzione
    @Test
    void aggiungiInCoda_primaPlaylist_primoTransoBranoCorrenteCodaVuota() {
        PlayerController pc = new PlayerController();
        Brano primo  = new Brano(1,"P1","A","g",2021,60,"/tmp/p1.mp3","mp3",null,false,false,false,0);
        Brano secondo = new Brano(2,"P2","A","g",2021,60,"/tmp/p2.mp3","mp3",null,false,false,false,0);
        Playlist pl = creaPlaylist("PL", primo, secondo);
    
        pc.aggiungiInCoda(pl);
    
        assertSame(primo, pc.getBranoCorrente(),
                "Il primo brano della playlist deve diventare il corrente");
        assertEquals(0, pc.getDimensioneCoda(),
                "La coda deve essere vuota: la playlist è in riproduzione");
    }

    //brano: loop disattivato, passa all'elemento successivo in coda
    @Test
    void loopDisattivato_skipSong_avanzaAllaCanzoneSuccessivaInCoda() {
        PlayerController pc = new PlayerController();
        Brano corrente = new Brano(1,"Corrente","A","g",2021,60,"/tmp/c.mp3","mp3",null,false,false,false,0);
        Brano successivo = new Brano(2,"Successivo","A","g",2021,60,"/tmp/s.mp3","mp3",null,false,false,false,0);
    
        pc.setBrano(corrente);
        pc.aggiungiInCoda(successivo);
    
        assertFalse(pc.isLoopAttivo(), "Il loop deve essere disattivo di default");
        pc.skipSong();
    
        assertSame(successivo, pc.getBranoCorrente(),
                "Senza loop, skipSong deve avanzare al brano successivo in coda");
        assertEquals(0, pc.getDimensioneCoda());
    }

    //brano: loop attivo, ignora la coda
    @Test
    void loopAttivo_skipSong_riparteDalloStessoBrano() {
        PlayerController pc = new PlayerController();
        Brano b = new Brano(1,"Loop","A","g",2021,60,"/tmp/loop.mp3","mp3",null,false,false,false,0);
        pc.setBrano(b);
    
        pc.loopSong();
        assertTrue(pc.isLoopAttivo(), "Il loop deve risultare attivo dopo loopSong()");
    
        pc.skipSong();
    
        assertSame(b, pc.getBranoCorrente(),
                "Con loop attivo su brano singolo, skipSong deve mantenere lo stesso brano");
    }

    //playlist: loop disattivato, passa all'elemento successivo in coda
    @Test
    void loopDisattivo_playlistFinita_avanzaAlProssimoElementoInCoda() {
        PlayerController pc = new PlayerController();
        Brano b1 = new Brano(1,"B1","A","g",2021,60,"/tmp/b1.mp3","mp3",null,false,false,false,0);
        Brano b2 = new Brano(2,"B2","A","g",2021,60,"/tmp/b2.mp3","mp3",null,false,false,false,0);
        Brano dopo = new Brano(3,"Dopo","A","g",2021,60,"/tmp/d.mp3","mp3",null,false,false,false,0);
    
        Playlist pl = creaPlaylist("PL", b1, b2);
        pc.aggiungiInCoda(pl);
        pc.aggiungiInCoda(dopo);
    
        pc.skipSong(); // da b1 a b2
        pc.skipSong(); // da b2 (fine playlist) a brano: "dopo"
    
        assertSame(dopo, pc.getBranoCorrente(),
                "Senza loop, dopo la fine della playlist deve partire l'elemento successivo in coda");
        assertEquals(0, pc.getDimensioneCoda());
    }

    //playlist: loop attivo, ignora la coda
    @Test
    void loopAttivo_playlistFinita_riparteDallInizio() {
        PlayerController pc = new PlayerController();
        Brano b1 = new Brano(1,"B1","A","g",2021,60,"/tmp/b1.mp3","mp3",null,false,false,false,0);
        Brano b2 = new Brano(2,"B2","A","g",2021,60,"/tmp/b2.mp3","mp3",null,false,false,false,0);
        Playlist pl = creaPlaylist("PL", b1, b2);
    
        pc.loopSong();
        pc.aggiungiInCoda(pl);
    
        assertSame(b1, pc.getBranoCorrente(), "Il primo brano deve essere b1");
    
        pc.skipSong();
        assertSame(b2, pc.getBranoCorrente(), "Dopo un skip deve essere in riproduzione b2");
    
        pc.skipSong();//playlist con loop skippata ,riparte
        assertSame(b1, pc.getBranoCorrente(),
                "Con loop attivo, alla fine della playlist deve ripartire dal primo brano");
        assertEquals(0, pc.getDimensioneCoda(),
                "Non ci devono essere altri elementi in coda");
    }

    //playlist messa in coda con ordine di riproduzione casuale
    @Test
    void strategiaShuffle_aggiornaPlaylistCorrente_riproduceTuttiIRimanenti() {
        PlayerController pc = new PlayerController();
        Brano b1 = new Brano(1,"R1","A","g",2021,60,"/tmp/r1.mp3","mp3",null,false,false,false,0);
        Brano b2 = new Brano(2,"R2","A","g",2021,60,"/tmp/r2.mp3","mp3",null,false,false,false,0);
        Brano b3 = new Brano(3,"R3","A","g",2021,60,"/tmp/r3.mp3","mp3",null,false,false,false,0);
        Playlist pl = creaPlaylist("PL", b1, b2, b3);
    
        pc.aggiungiInCoda(new ElementoCodaConOpzioni(
                pl, new StrategiaOrdineSequenziale(), false)); //parte b1, sequenziale
        pc.aggiornaOpzioniPlaylistCorrente(new StrategiaOrdineShuffle(), false);
    
        java.util.Set<Brano> riprodotti = new java.util.HashSet<>();
        riprodotti.add(pc.getBranoCorrente());
        pc.skipSong(); riprodotti.add(pc.getBranoCorrente());
        pc.skipSong(); riprodotti.add(pc.getBranoCorrente());
    
        assertTrue(riprodotti.containsAll(List.of(b1, b2, b3)),
                "Con strategia shuffle tutti i brani della playlist devono essere riprodotti");
    }
 
    //playlist messa in coda con riproduzione sequenziale dei suoi brani
    @Test
    void elementoCodaConOpzioni_strategiaSequenziale_rispettaOrdineSequenziale() {
        PlayerController pc = new PlayerController();
        Brano corrente = new Brano("C","A","g",2020,60,"/tmp/c.mp3","mp3",null,false,false);
        pc.setBrano(corrente);
    
        Brano b1 = new Brano(1,"S1","A","g",2021,60,"/tmp/s1.mp3","mp3",null,false,false,false,0);
        Brano b2 = new Brano(2,"S2","A","g",2021,60,"/tmp/s2.mp3","mp3",null,false,false,false,0);
        Playlist pl = creaPlaylist("PL", b1, b2);
    
        pc.aggiungiInCoda(new ElementoCodaConOpzioni(
                pl, new StrategiaOrdineSequenziale(), false));
    
        pc.skipSong();
        assertSame(b1, pc.getBranoCorrente(),
                "Con ElementoCodaConOpzioni sequenziale il primo brano deve essere b1");
        pc.skipSong();
        assertSame(b2, pc.getBranoCorrente(),
                "Con ElementoCodaConOpzioni sequenziale il secondo brano deve essere b2");
    }

    //brano aggiunto in coda quando il player è in riproduzione
    @Test
    void osservatore_branoAggiunto_vieneProdottoDalloSkip() {
        PlayerController pc = new PlayerController();
        Brano b1 = new Brano(1,"O1","A","g",2021,60,"/tmp/o1.mp3","mp3",null,false,false,false,0);
        Brano b2 = new Brano(2,"O2","A","g",2021,60,"/tmp/o2.mp3","mp3",null,false,false,false,0);
        Brano nuovo = new Brano(3,"Nuovo","A","g",2021,60,"/tmp/n.mp3","mp3",null,false,false,false,0);
        Playlist pl = creaPlaylist("PL", b1, b2);
    
        pc.aggiungiInCoda(pl);
    
        //Aggiunge un brano alla playlist durante la riproduzione di b1
        pl.aggiungiBrano(nuovo);
    
        pc.skipSong(); //deve avanzare a b2
        assertSame(b2, pc.getBranoCorrente(), "Dopo skip deve venire b2");
    
        pc.skipSong(); //deve avanzare al brano aggiunto
        assertSame(nuovo, pc.getBranoCorrente(),
                "Il brano aggiunto alla playlist durante la riproduzione deve essere raggiunto");
    }
 
    //brano rimosso dalla coda quando il player è in riproduzione
    @Test
    void osservatore_branoRimossoCorrentemente_skippaAlSuccessivo() {
        PlayerController pc = new PlayerController();
        Brano b1 = new Brano(1,"OR1","A","g",2021,60,"/tmp/or1.mp3","mp3",null,false,false,false,0);
        Brano b2 = new Brano(2,"OR2","A","g",2021,60,"/tmp/or2.mp3","mp3",null,false,false,false,0);
        Playlist pl = creaPlaylist("PL", b1, b2);
    
        pc.aggiungiInCoda(pl);
        assertSame(b1, pc.getBranoCorrente());
    
        //Rimuove il brano in riproduzione, deve passare a b2
        pl.rimuoviBrano(b1);
    
        assertSame(b2, pc.getBranoCorrente(),
                "Rimuovendo il brano in riproduzione, il player deve avanzare automaticamente a b2");
    }
 

    @Test
    void osservatore_branoFuturoRimosso_nonInfluenzaBranoCorrente() {
        PlayerController pc = new PlayerController();
        Brano b1 = new Brano(1,"F1","A","g",2021,60,"/tmp/f1.mp3","mp3",null,false,false,false,0);
        Brano b2 = new Brano(2,"F2","A","g",2021,60,"/tmp/f2.mp3","mp3",null,false,false,false,0);
        Brano b3 = new Brano(3,"F3","A","g",2021,60,"/tmp/f3.mp3","mp3",null,false,false,false,0);
        Playlist pl = creaPlaylist("PL", b1, b2, b3);
    
        pc.aggiungiInCoda(pl); //parte b1
    
        //rimuove b2 mentre è in riproduzione b1
        pl.rimuoviBrano(b2);
    
        assertSame(b1, pc.getBranoCorrente(),
                "La rimozione di un brano futuro non deve interrompere il brano corrente");
    
        pc.skipSong(); //salta a b3
        assertSame(b3, pc.getBranoCorrente(),
                "Dopo la rimozione di b2, lo skip deve portare a b3");
    }
}
