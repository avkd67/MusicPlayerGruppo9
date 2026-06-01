package org.example.musicplayergruppo9.model;

import org.junit.jupiter.api.*;


import java.util.ArrayList;

public class PlaylistTest {

    private Playlist playlist;

    @BeforeEach
    public void setUp(){
        playlist = new Playlist();
    }

    @Test
    public void testAggiungiBrano(){
        Brano brano = new Brano();
        brano.setTitolo("titolo");
        playlist.aggiungiBrano(brano);

        // verifico che il brano sia stato aggiunto alla playlist
        Assertions.assertTrue(playlist.getBrani().contains(brano));
    }

    // test sull'iterator della playlist
    @Test
    public void testIterator(){
        Brano brano1 = new Brano();
        Brano brano2 = new Brano();
        ArrayList<Brano> brani;

        brano1.setArtista("artista1");
        brano2.setArtista("artista2");

        playlist.aggiungiBrano(brano1);
        playlist.aggiungiBrano(brano2);

        brani = playlist.getBrani();
        // verifoco che i brani aggiunti siano stati correttamente inseriti
        Assertions.assertTrue(brani.contains(brano1));
        Assertions.assertTrue(brani.contains(brano2));
    }
}
