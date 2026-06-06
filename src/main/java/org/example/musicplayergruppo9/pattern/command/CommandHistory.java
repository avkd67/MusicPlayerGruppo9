package org.example.musicplayergruppo9.pattern.command;

import org.example.musicplayergruppo9.database.DAO.PlaylistBraniDAO;

import java.util.ArrayDeque;
import java.util.Deque;

//questa è la classe che funge da Invoker per il pattern Command
public class CommandHistory {
    //singleton
    private static CommandHistory instance;

    //coda di "command" del tipo LIFO
    private final Deque<Command> undoDeque = new ArrayDeque<Command>();


    public static synchronized CommandHistory getInstance() {
        if (instance == null) {
            instance = new CommandHistory();
        }
        return instance;
    }


    // esegue l'eventuale comando e lo salva
    public boolean execute(Command command) {
        if(command.execute()) {
            undoDeque.push(command);
            return true;
        }
        return false;
    }

    //reverte l'ultimo comando e lo rimuove dalla lista dei comandi salvati
    public void undo() {
        if(!undoDeque.isEmpty()) {
            undoDeque.pop().undo();
        }
    }

    public boolean canUndo() {
        return !undoDeque.isEmpty();
    }


}
