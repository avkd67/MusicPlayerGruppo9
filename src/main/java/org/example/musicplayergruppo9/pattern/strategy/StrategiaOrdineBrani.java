package org.example.musicplayergruppo9.pattern.strategy;

import org.example.musicplayergruppo9.model.Brano;

import java.util.Iterator;
import java.util.List;

public interface StrategiaOrdineBrani {
    Iterator<Brano> creaIterator(List<Brano> brani);
}
