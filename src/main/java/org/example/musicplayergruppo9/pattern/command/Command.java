package org.example.musicplayergruppo9.pattern.command;

public interface Command {
    boolean execute(); //esegue l'operazione
    void undo(); //inverte l'operazione
}
