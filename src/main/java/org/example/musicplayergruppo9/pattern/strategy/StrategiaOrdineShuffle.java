package org.example.musicplayergruppo9.pattern.strategy;

import org.example.musicplayergruppo9.model.Brano;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class StrategiaOrdineShuffle implements StrategiaOrdineBrani {
    @Override
    public Iterator<Brano> creaIterator(List<Brano> brani) {
        List<Brano> copia = new ArrayList<>(brani);
        Collections.shuffle(copia);
        return copia.iterator();
    }
}