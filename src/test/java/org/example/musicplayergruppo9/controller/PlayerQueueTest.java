package org.example.musicplayergruppo9.controller;

import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.ElementoCoda;
import org.example.musicplayergruppo9.model.Playlist;
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
}
