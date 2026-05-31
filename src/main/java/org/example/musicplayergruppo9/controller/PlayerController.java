package org.example.musicplayergruppo9.controller;

import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.ElementoCoda;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class PlayerController {

    // Coda
    private final Queue<ElementoCoda> coda = new LinkedList<>();

    // Iterator sull'elemento in coda
    private Iterator<Brano> iteratorCorrente;

    //riferimento all'elemento in coda corrente
    private ElementoCoda elementoCorrenteInCoda;

    // Brano attualmente in riproduzione
    private Brano branoCorrente;

    //Task 7.2 Loop del singolo brano
    private boolean loopAttivo = false;

    public void aggiungiInCoda(ElementoCoda elemento) {
        coda.add(elemento);
        if (branoCorrente == null) {
            avviaProssimoElemento();
        }
    }

    // Task 7.2 Loop Brano
    public void loopBrano() {
        loopAttivo = !loopAttivo;
        System.out.println("[PlayerController] Loop: " + (loopAttivo ? "ON" : "OFF"));
    }

    /* Task 6.2 Salta Brano:
     * - se siamo dentro una playlist, skippa al prossimo brano presente in playlist
     * - altrimenti, passa all'elemento successivo in coda
     */
    public void skipBrano() {
        //caso a. loop attivo: riproduce il brano corrente
        if (loopAttivo && branoCorrente != null) {
            riproduciBranoCorrente();
            return;
        }

        //caso b. siamo nella playlist, passa al brano successivo nella stessa playlist
        if (iteratorCorrente != null && iteratorCorrente.hasNext()) {
            branoCorrente = iteratorCorrente.next();
            riproduciBranoCorrente();
            return;
        } 

        //caso c. brano o playlist terminati, passa al prossimo elemento in coda
        avviaProssimoElemento();

    }

    //Estrae il prossimo elemento in coda e lo avvia.
    private void avviaProssimoElemento() {
        ElementoCoda prossimo = coda.poll();

        if (prossimo == null) {
            //coda vuota
            branoCorrente = null;
            iteratorCorrente = null;
            elementoCorrenteInCoda = null;
            System.out.println("[PlayerController] Coda vuota.");
            return;
        }

        elementoCorrenteInCoda = prossimo;
        iteratorCorrente = prossimo.iterator();

        if (iteratorCorrente.hasNext()) {
            branoCorrente = iteratorCorrente.next();
            riproduciBranoCorrente();
        } else {
            // Elemento vuoto (es. playlist senza brani): salta al successivo
            System.out.println("[PlayerController] Elemento vuoto, salto: " + prossimo.getTitolo());
            avviaProssimoElemento();
        }
    }

    //avvia e riavvia la riproduzione branoCorrente
    private void riproduciBranoCorrente() {
        if (branoCorrente == null) return;
        // integrare con MediaPlayer
        System.out.println("[PlayerController] ▶ Riproduco: " + branoCorrente.getTitolo());
    }

    public Brano getBranoCorrente() {
        return branoCorrente;
    }

    public boolean isLoopAttivo() {
        return loopAttivo;
    }

    public int getDimensioneCoda() {
        return coda.size();
    }
}