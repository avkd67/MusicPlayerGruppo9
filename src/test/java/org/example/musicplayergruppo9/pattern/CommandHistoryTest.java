package org.example.musicplayergruppo9.pattern;
import org.example.musicplayergruppo9.pattern.command.Command;
import org.example.musicplayergruppo9.pattern.command.CommandHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandHistoryTest {

    private CommandHistory commandHistory;
    private List<String> logOperazioni;

    private class ComandoFittizio implements Command {
        private final String nomeAzione;

        public ComandoFittizio(String nomeAzione) {
            this.nomeAzione = nomeAzione;
        }

        @Override
        public boolean execute() {
            logOperazioni.add("Eseguito: " + nomeAzione);
            return true;
        }

        @Override
        public void undo() {
            logOperazioni.add("Annullato: " + nomeAzione);
        }
    }

    @BeforeEach
    void setUp() {
        commandHistory = CommandHistory.getInstance();
        logOperazioni = new ArrayList<>();

        while (commandHistory.canUndo()) {
            commandHistory.undo();
        }

        logOperazioni.clear();
    }

    @Test
    void testEsecuzioneEUndoMultiploLIFO() {
        Command cmdCreaPlaylist = new ComandoFittizio("Creazione Playlist");
        Command cmdAggiungiBrano = new ComandoFittizio("Aggiunta Brano A");
        Command cmdRimuoviBrano = new ComandoFittizio("Rimozione Brano B");

        commandHistory.execute(cmdCreaPlaylist);
        commandHistory.execute(cmdAggiungiBrano);
        commandHistory.execute(cmdRimuoviBrano);

        assertEquals(3, logOperazioni.size());
        assertEquals("Eseguito: Creazione Playlist", logOperazioni.get(0));
        assertEquals("Eseguito: Aggiunta Brano A", logOperazioni.get(1));
        assertEquals("Eseguito: Rimozione Brano B", logOperazioni.get(2));

        assertTrue(commandHistory.canUndo());

        commandHistory.undo();
        assertEquals("Annullato: Rimozione Brano B", logOperazioni.get(3));

        commandHistory.undo();
        assertEquals("Annullato: Aggiunta Brano A", logOperazioni.get(4));

        commandHistory.undo();
        assertEquals("Annullato: Creazione Playlist", logOperazioni.get(5));

        assertFalse(commandHistory.canUndo(), "La history dovrebbe essere vuota dopo aver annullato tutto");
    }
}