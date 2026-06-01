package org.example.musicplayergruppo9.model;

import org.junit.jupiter.api.*;


import java.util.ArrayList;
import java.util.Iterator;

public class PlaylistTest {

    private Playlist playlist;
    private Brano brano1;
    private Brano brano2;

    // setup prima di ogni esecuzione
    @BeforeEach
    public void setUp(){

        playlist = new Playlist();
        brano1 = new Brano();
        brano2 = new Brano();

        // ovviamente
        brano1.setTitolo("Hung up");
        brano2.setTitolo("Applause");
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

    // test sull'iterator della playlist, controllo ci siano i giusti elementi e il giusto numeor di elementi
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
}
