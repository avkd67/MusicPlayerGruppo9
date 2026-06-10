package org.example.musicplayergruppo9.model;

import java.util.Iterator;

import org.example.musicplayergruppo9.pattern.strategy.StrategiaOrdineBrani;

//Così ogni elemento in coda ha il proprio stato shuffle/loop indipendente.

public class ElementoCodaConOpzioni implements ElementoCoda {

    private final ElementoCoda elemento;
    private final StrategiaOrdineBrani strategiaOrdine;
    private final boolean loop;

    public ElementoCodaConOpzioni(
        ElementoCoda elemento,
        StrategiaOrdineBrani strategiaOrdine,
        boolean loop
    ) {
        this.elemento = elemento;
        this.strategiaOrdine = strategiaOrdine;
        this.loop = loop;
    }

    public ElementoCoda getElemento() { return elemento; }

    public StrategiaOrdineBrani getStrategiaOrdine() {
        return strategiaOrdine;
    }

    public boolean isLoop() {
        return loop;
    }

    @Override
    public String getTitolo() { return elemento.getTitolo(); }

    @Override
    public Iterator<Brano> iterator() {
        
        if (elemento instanceof Playlist playlist) {
            return strategiaOrdine.creaIterator(playlist.getBrani());
        }

        return elemento.iterator();
    }
}