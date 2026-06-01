package org.example.musicplayergruppo9.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ElementoCodaTest {

    //test per veificare che Brano implementi correttamente ElementoCoda
    //importante per sviluppare la task 6.2 che chiede di skippare un brano
    //test strutturale
    @Test
    void branoImplementaElementoCoda() {
        Brano brano = new Brano(1, "Titolo", "Artista", "Pop", 0, 180, "/path/audio.mp3", "mp3", null, false, false, false);
        assertInstanceOf(ElementoCoda.class, brano);
    }

}