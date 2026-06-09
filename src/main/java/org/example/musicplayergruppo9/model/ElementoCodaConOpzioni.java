package org.example.musicplayergruppo9.model;

import java.util.Iterator;

//Così ogni elemento in coda ha il proprio stato shuffle/loop indipendente.

public class ElementoCodaConOpzioni implements ElementoCoda {

    private final ElementoCoda elemento;
    private final boolean random;

    public ElementoCodaConOpzioni(ElementoCoda elemento, boolean random) {
        this.elemento = elemento;
        this.random = random;
    }

    public ElementoCoda getElemento() { return elemento; }
    public boolean isRandom() { return random; }

    @Override
    public String getTitolo() { return elemento.getTitolo(); }

    @Override
    public Iterator<Brano> iterator() {
        // Se random e l'elemento è una Playlist, usa l'iteratore casuale
        if (random && elemento instanceof Playlist) {
            return ((Playlist) elemento).getRandomIterator();
        }
        return elemento.iterator();
    }
}