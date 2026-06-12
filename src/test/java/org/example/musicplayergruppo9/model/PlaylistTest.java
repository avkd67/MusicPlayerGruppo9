package org.example.musicplayergruppo9.model;

import org.junit.jupiter.api.*;
import org.example.musicplayergruppo9.pattern.strategy.StrategiaOrdineSequenziale;
import org.example.musicplayergruppo9.pattern.strategy.StrategiaOrdineShuffle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlaylistTest {

    private Playlist playlist;
    private Brano brano1;
    private Brano brano2;
    private Brano brano3;


    // setup prima di ogni esecuzione
    @BeforeEach
    public void setUp(){

        playlist = new Playlist();
        brano1 = new Brano();
        brano2 = new Brano();
        brano3 = new Brano();

        // ovviamente
        brano1.setTitolo("Hung up");
        brano2.setTitolo("Applause");
        brano3.setTitolo("Combi Versace");

    }

    // test del costruttore completo
    @Test
    public void testCostruttore(){
        playlist = new Playlist("Nome playlist", "Percorso Fake!");

        Assertions.assertEquals("Nome playlist", playlist.getNome());
        Assertions.assertEquals("Percorso Fake!", playlist.getPercorsoCopertina());
        Assertions.assertEquals(-1, playlist.getId());
        Assertions.assertNotNull(playlist.getBrani());

    }

    // test del costruttore senza info
    @Test
    public void testCostruttoreNoInfo(){
        Assertions.assertNotNull(playlist.getBrani());
    }

    // test dell'aggiunta di un brano
    @Test
    public void testAggiungiBrano(){
        playlist.aggiungiBrano(brano1);

        // verifico che il brano sia stato aggiunto alla playlist
        Assertions.assertTrue(playlist.getBrani().contains(brano1));
    }

    //test della rimozione di un brano in playlsit
    @Test
    public void testRimuoviBrano() {
        playlist.aggiungiBrano(brano1);
        playlist.aggiungiBrano(brano2);
 
        playlist.rimuoviBrano(brano1);
 
        assertFalse(playlist.getBrani().contains(brano1));
        assertTrue(playlist.getBrani().contains(brano2));
    }

    //test aggiunta brani in ordine in una playlist
    @Test
    public void testAggiungiBraniOrdine() {
        playlist.aggiungiBrano(brano1);
        playlist.aggiungiBrano(brano2);
        playlist.aggiungiBrano(brano3);
 
        List<Brano> brani = playlist.getBrani();
        assertEquals(3, brani.size());
        assertEquals(brano1, brani.get(0));
        assertEquals(brano2, brani.get(1));
        assertEquals(brano3, brani.get(2));
    }

    // test sull'iterator della playlist, sequenziale
    @Test
    public void testIterator() {
        playlist.aggiungiBrano(brano1);
        playlist.aggiungiBrano(brano2);

        Iterator<Brano> iterator = playlist.iterator();

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(brano1, iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals(brano2, iterator.next());

        Assertions.assertFalse(iterator.hasNext(), "Non dovrebbero esserci altri elementi");
    }

    //test playlist vuota
    @Test
    public void testIteratorPlaylistVuota() {
        Iterator<Brano> it = playlist.iterator();
        assertFalse(it.hasNext());
    }

    //test iterator indipendenti che non si influenzano
    @Test
    public void testIteratoriIndipendenti() {
        playlist.aggiungiBrano(brano1);
        playlist.aggiungiBrano(brano2);
 
        Iterator<Brano> it1 = playlist.iterator();
        Iterator<Brano> it2 = playlist.iterator();
 
        assertEquals(brano1, it1.next()); // it1 avanza
        assertEquals(brano1, it2.next()); // it2 parte ancora dall'inizio
    }

    //test riproduzione sequenziale tramite ElementoCodaConOpzioni
    @Test
    public void testRiproduzioneSequenzialeConOpzioni() {
        playlist.aggiungiBrano(brano1);
        playlist.aggiungiBrano(brano2);
        playlist.aggiungiBrano(brano3);
 
        ElementoCodaConOpzioni elemento =
                new ElementoCodaConOpzioni(playlist, new StrategiaOrdineSequenziale(), false);
 
        Iterator<Brano> it = elemento.iterator();
        List<Brano> riprodotti = new ArrayList<>();
        it.forEachRemaining(riprodotti::add);
 
        // L'ordine deve essere esattamente quello di inserimento
        assertEquals(List.of(brano1, brano2, brano3), riprodotti);
    }

    //test riproduzione casuale tramite ElementoCodaConOpzioni
    @Test
    public void testShuffleConOpzioni() {
        playlist.aggiungiBrano(brano1);
        playlist.aggiungiBrano(brano2);
        playlist.aggiungiBrano(brano3);
 
        ElementoCodaConOpzioni elemento =
                new ElementoCodaConOpzioni(playlist, new StrategiaOrdineShuffle(), false);
 
        Iterator<Brano> it = elemento.iterator();
        List<Brano> riprodotti = new ArrayList<>();
        it.forEachRemaining(riprodotti::add);
 
        // Tutti i brani presenti, nessuna perdita
        assertEquals(3, riprodotti.size());
        assertTrue(riprodotti.contains(brano1));
        assertTrue(riprodotti.contains(brano2));
        assertTrue(riprodotti.contains(brano3));
    }

    //test shuffle, restituisce tutti i brani
    @Test
    public void testShuffleCopertura() {
        playlist.aggiungiBrano(brano1);
        playlist.aggiungiBrano(brano2);
        playlist.aggiungiBrano(brano3);
 
        Iterator<Brano> it = new StrategiaOrdineShuffle().creaIterator(playlist.getBrani());
        List<Brano> riprodotti = new ArrayList<>();
        it.forEachRemaining(riprodotti::add);
 
        assertEquals(3, riprodotti.size(), "Lo shuffle deve restituire tutti e 3 i brani");
        assertTrue(riprodotti.contains(brano1));
        assertTrue(riprodotti.contains(brano2));
        assertTrue(riprodotti.contains(brano3));
    }

    //due shuffle, ordine diverso
    @Test
    public void testShuffleOrdineVariabile() {
        for (int i = 1; i <= 10; i++) {
            Brano b = new Brano();
            b.setId(i);
            b.setTitolo("Brano " + i);
            b.setArtista("Artista " + i);
            playlist.aggiungiBrano(b);
        }
 
        List<Brano> esecuzione1 = new ArrayList<>();
        List<Brano> esecuzione2 = new ArrayList<>();
 
        new StrategiaOrdineShuffle().creaIterator(playlist.getBrani()).forEachRemaining(esecuzione1::add);
        new StrategiaOrdineShuffle().creaIterator(playlist.getBrani()).forEachRemaining(esecuzione2::add);
 
        assertEquals(10, esecuzione1.size());
        assertEquals(10, esecuzione2.size());
 
        assertNotEquals(esecuzione1, esecuzione2,
                "Due shuffle su 10 brani non dovrebbero quasi mai produrre lo stesso ordine");
    }

    //test shuffle non modifica l'ordine originale in playlist
    @Test
    public void testShuffleNonModificaOriginale() {
        playlist.aggiungiBrano(brano1);
        playlist.aggiungiBrano(brano2);
        playlist.aggiungiBrano(brano3);
 
        List<Brano> originale = new ArrayList<>(playlist.getBrani());
 
        new StrategiaOrdineShuffle().creaIterator(playlist.getBrani());
 
        assertEquals(originale, playlist.getBrani(),
                "StrategiaOrdineShuffle non deve modificare la lista interna della playlist");
    }

    //loop
    
    //test loop sequenziale
    @Test
    public void testLoopSequenzialeRicomincio() {
        playlist.aggiungiBrano(brano1);
        playlist.aggiungiBrano(brano2);
        playlist.aggiungiBrano(brano3);
 
        Iterator<Brano> it = playlist.iterator();
        it.forEachRemaining(b -> {});
 
        assertFalse(it.hasNext(), "L'iteratore deve essere esaurito alla fine");
 
        Iterator<Brano> itLoop = playlist.iterator();
        assertTrue(itLoop.hasNext(), "Il nuovo iteratore deve ripartire dall'inizio");
        assertEquals(brano1, itLoop.next(), "Il primo brano dopo il loop deve essere brano1");
    }
 
    //test loop shuffle
    @Test
    public void testLoopShuffleRicomincio() {
        playlist.aggiungiBrano(brano1);
        playlist.aggiungiBrano(brano2);
        playlist.aggiungiBrano(brano3);
 
        //priima lo shuffle
        Iterator<Brano> it1 = new StrategiaOrdineShuffle().creaIterator(playlist.getBrani());
        it1.forEachRemaining(b -> {}); 
        assertFalse(it1.hasNext());
 
       //shuffle e loop
        Iterator<Brano> it2 = new StrategiaOrdineShuffle().creaIterator(playlist.getBrani());
        List<Brano> secondaPassata = new ArrayList<>();
        it2.forEachRemaining(secondaPassata::add);
 
        assertEquals(3, secondaPassata.size());
        assertTrue(secondaPassata.contains(brano1));
        assertTrue(secondaPassata.contains(brano2));
        assertTrue(secondaPassata.contains(brano3));
    }
 
    //test più cicli di loop
    @Test
    public void testLoopPiuCicli() {
        playlist.aggiungiBrano(brano1);
        playlist.aggiungiBrano(brano2);
        playlist.aggiungiBrano(brano3);
 
        //Simula 3 cicli di loop sequenziale
        for (int ciclo = 1; ciclo <= 3; ciclo++) {
            List<Brano> riprodotti = new ArrayList<>();
            playlist.iterator().forEachRemaining(riprodotti::add);
 
            assertEquals(3, riprodotti.size(),
                    "Ciclo " + ciclo + ": devono essere riprodotti 3 brani");
            assertEquals(List.of(brano1, brano2, brano3), riprodotti,
                    "Ciclo " + ciclo + ": l'ordine deve essere sempre lo stesso");
        }
    }

    //test sull'observer

    //notifica l'aggiunta di un brano in playlist
    @Test
    public void testObserverAggiunta() {
        List<Brano> notificati = new ArrayList<>();
 
        playlist.addPlaylistObserver(new org.example.musicplayergruppo9.pattern.observer.PlaylistObserver() {
            @Override public void onBranoAggiunto(Brano b) { notificati.add(b); }
            @Override public void onBranoRimosso(Brano b) {}
        });
 
        playlist.aggiungiBrano(brano1);
        playlist.aggiungiBrano(brano2);
 
        assertEquals(2, notificati.size());
        assertTrue(notificati.contains(brano1));
        assertTrue(notificati.contains(brano2));
    }
 
    //notifica la rimozione di un brano in playlist
    @Test
    public void testObserverRimozione() {
        List<Brano> rimossi = new ArrayList<>();
 
        playlist.addPlaylistObserver(new org.example.musicplayergruppo9.pattern.observer.PlaylistObserver() {
            @Override public void onBranoAggiunto(Brano b) {}
            @Override public void onBranoRimosso(Brano b) { rimossi.add(b); }
        });
 
        playlist.aggiungiBrano(brano1);
        playlist.rimuoviBrano(brano1);
 
        assertEquals(1, rimossi.size());
        assertEquals(brano1, rimossi.get(0));
    }
 
}

