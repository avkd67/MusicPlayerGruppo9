package org.example.musicplayergruppo9.model;

import org.junit.jupiter.api.Test;
import java.util.Iterator;
import static org.junit.jupiter.api.Assertions.*;

public class BranoIteratorTest {

    //test per verificare il corretto funzionamento dell'iterator nel caso del brano
    //utile per verificare l'implementazione di task 6.2 che gestisce lo skip del brano

    @Test
    void iteratorBranoRestituisceSeStesso() {
        Brano brano = new Brano(1, "Blessed", "Night Sinny", "pop", 0, 354, "/path/audio.mp3", "mp3", null, false, false, false);

        Iterator<Brano> it = brano.iterator();

        assertTrue(it.hasNext()); // positivo: c'è il brano
        assertEquals(brano, it.next()); //l'elemento è proprio quello che abbiamo creato
        assertFalse(it.hasNext()); //negativo: non ci sono altri brani
    }

    @Test
    void iteratorBranoNonRestituisceAltriElementi() {
        Brano brano = new Brano(2, "Malamente", "Rosalia", "pop", 0, 482, "/path/audio.mp3", "mp3", null, false, false, false);

        Iterator<Brano> it = brano.iterator();
        it.next(); // consuma l'elemento

        assertFalse(it.hasNext()); //verifica che non ce ne siano altri
        //verifica che dopo next, l'iterator sia vuoto
    }
}