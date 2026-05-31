package org.example.musicplayergruppo9.pattern.observer;

public interface Subject {
    void attach(Observer o);
    void detach(Observer o);
    void notifyObservers();
}
