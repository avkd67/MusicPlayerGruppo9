package org.example.musicplayergruppo9.integration;

import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.Playlist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrdinamentoPlaylistTest {

    private List<Brano> brani;
    private Brano b1, b2, b3, b4;

    @BeforeEach
    public void setUp() {
        // Inizializziamo una lista di brani fittizia prima di ogni test
        brani = new ArrayList<>();

        b1 = new Brano(); b1.setTitolo("Brano 1"); b1.setId(1);
        b2 = new Brano(); b2.setTitolo("Brano 2"); b2.setId(2);
        b3 = new Brano(); b3.setTitolo("Brano 3"); b3.setId(3);
        b4 = new Brano(); b4.setTitolo("Brano 4"); b4.setId(4);

        brani.add(b1);
        brani.add(b2);
        brani.add(b3);
        brani.add(b4);
    }

    /**
     * Metodo di supporto che replica ESATTAMENTE la logica del Drop
     * presente in BranoListCell nel tuo controller.
     */
    private void simulaDragAndDrop(int srcIndex, int targetIndex) {
        if (srcIndex >= 0 && targetIndex >= 0 && srcIndex != targetIndex) {
            Brano item = brani.remove(srcIndex);

            // Se rimuovo un elemento prima della destinazione,
            // tutta la lista "scala" a sinistra di 1, quindi adatto l'indice.
            if (srcIndex < targetIndex) {
                targetIndex--;
            }
            brani.add(targetIndex, item);
        }
    }

    @Test
    public void testSpostamentoBranoInAvanti() {
        // ACT: Sposto "Brano 1" (indice 0) sotto "Brano 3" (indice 2)
        simulaDragAndDrop(0, 2);

        // ASSERT: Verifico che il nuovo ordine sia corretto
        // Ordine atteso: Brano 2, Brano 3, Brano 1, Brano 4
        assertEquals(b2, brani.get(0), "Il Brano 2 dovrebbe essere scalato al primo posto");
        assertEquals(b1, brani.get(1), "Il Brano 1 dovrebbe essere inserito al secondo posto");
        assertEquals(b3, brani.get(2), "Il Brano 3 dovrebbe essere scalato al terzo posto");
        assertEquals(b4, brani.get(3), "Il Brano 4 dovrebbe rimanere all'ultimo posto");
    }


    @Test
    public void testSpostamentoBranoAllIndietro() {
        // ACT: Sposto "Brano 4" (indice 3) al posto di "Brano 2" (indice 1)
        simulaDragAndDrop(3, 1);

        // ASSERT: Verifico che il nuovo ordine sia corretto
        // Ordine atteso: Brano 1, Brano 4, Brano 2, Brano 3
        assertEquals(b1, brani.get(0), "Il Brano 1 dovrebbe rimanere al primo posto");
        assertEquals(b4, brani.get(1), "Il Brano 4 dovrebbe essere al secondo posto");
        assertEquals(b2, brani.get(2), "Il Brano 2 dovrebbe essere al terzo posto");
        assertEquals(b3, brani.get(3), "Il Brano 3 dovrebbe essere all'ultimo posto");
    }

    @Test
    public void testSpostamentoStessaPosizione() {
        // ACT: Tento di spostare "Brano 2" (indice 1) sulla sua stessa posizione (indice 1)
        simulaDragAndDrop(1, 1);

        // ASSERT: L'ordine deve rimanere invariato
        assertEquals(b1, brani.get(0));
        assertEquals(b2, brani.get(1));
        assertEquals(b3, brani.get(2));
        assertEquals(b4, brani.get(3));
    }
}