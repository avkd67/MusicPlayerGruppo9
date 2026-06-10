package org.example.musicplayergruppo9.pattern.strategy;

import org.example.musicplayergruppo9.model.Brano;

import java.util.Iterator;
import java.util.List;

public class StrategiaOrdineSequenziale implements StrategiaOrdineBrani {
    @Override
    public Iterator<Brano> creaIterator(List<Brano> brani) {
        return List.copyOf(brani).iterator();
    }
}