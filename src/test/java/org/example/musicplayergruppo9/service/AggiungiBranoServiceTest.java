package org.example.musicplayergruppo9.service;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

public class AggiungiBranoServiceTest {

    private AggiungiBranoService service;
    private File fileAudioFake;
    private File fileCopertinaFake;

    Random random = new Random();
    int numeroCasuale = random.nextInt(1000000);

    // setup prima di ogni esecuzione
    @BeforeEach
    public void setUp() throws IOException {
        service = new AggiungiBranoService();

        // Creo dei file temporanei "finti" sul sistema operativo per simulare l'upload dell'utente
        fileAudioFake = File.createTempFile("test_audio", ".mp3");
        fileCopertinaFake = File.createTempFile("test_copertina", ".jpg");

        // Scrivo 48.000 byte finti nel file audio.
        Files.write(fileAudioFake.toPath(), new byte[48000]);
    }

    // pulizia dopo ogni esecuzione
    @AfterEach
    public void tearDown() {
        // Elimino i file temporanei finti originali creati nel sistema operativo
        if (fileAudioFake != null && fileAudioFake.exists()) {
            fileAudioFake.delete();
        }
        if (fileCopertinaFake != null && fileCopertinaFake.exists()) {
            fileCopertinaFake.delete();
        }

        // Elimino le copie create dal Service nella cartella AltriFile durante i test
        File audioCopiato = new File("AltriFile/Audio/" + fileAudioFake.getName());
        if (audioCopiato.exists()) {
            audioCopiato.delete();
        }

        File copertinaCopiata = new File("AltriFile/Copertine/" + fileCopertinaFake.getName());
        if (copertinaCopiata.exists()) {
            copertinaCopiata.delete();
        }
    }

    // test per verificare che venga lanciata un'eccezione se si passa un percorso file inesistente
    @Test
    public void testGestisciSalvataggioFileInesistente() {
        Assertions.assertThrows(IOException.class, () -> {
            service.gestisciSalvataggio(
                    "Titolo", "Artista", "Pop", 2023,
                    "percorso/completamente/inventato.mp3", ".mp3",
                    null, false
            );
        });
    }

    // test del flusso normale di salvataggio
    @Test
    public void testGestisciSalvataggioSuccesso() {
        try {
            // Uso i millisecondi attuali per creare un titolo unico e non far arrabbiare il DAO con un duplicato
            String titoloUnico = "Bad Romance" + numeroCasuale;
            boolean risultato = service.gestisciSalvataggio(
                    titoloUnico,
                    "Lady Gaga",
                    "Pop",
                    2009,
                    fileAudioFake.getAbsolutePath(),
                    ".mp3",
                    fileCopertinaFake.getAbsolutePath(),
                    false
            );

            // Verifico che il service restituisca true
            Assertions.assertTrue(risultato, "Il salvataggio dovrebbe andare a buon fine e restituire true");

            // Verifico che i file siano stati effettivamente copiati nelle cartelle "AltriFile"
            File audioCopiato = new File("AltriFile/Audio/" + fileAudioFake.getName());
            File copertinaCopiata = new File("AltriFile/Copertine/" + fileCopertinaFake.getName());

            Assertions.assertTrue(audioCopiato.exists(), "Il file audio deve essere stato copiato nella cartella di destinazione");
            Assertions.assertTrue(copertinaCopiata.exists(), "La copertina deve essere stata copiata nella cartella di destinazione");

            // Elimino i file copiati per non riempire il progetto di file inutili
            audioCopiato.delete();
            copertinaCopiata.delete();

        } catch (Exception e) {
            Assertions.fail("Il test è fallito lanciando un'eccezione inaspettata: " + e.getMessage());
        }
    }

    // test della validazione "Brano duplicato"
    @Test
    public void testGestisciSalvataggioBranoDuplicato() {
        String titolo = "Duplicato " + System.currentTimeMillis();
        String artista = "Artista Clone";

        try {
            // Salvo il brano la prima volta (andrà a buon fine)
            service.gestisciSalvataggio(titolo, artista, "Trap", 2021,
                    fileAudioFake.getAbsolutePath(), ".mp3", null, true);

            // Tento di salvare di nuovo lo stesso esatto brano
            // Asserisco che questa seconda chiamata lanci la IllegalArgumentException
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                service.gestisciSalvataggio(titolo, artista, "Trap", 2021,
                        fileAudioFake.getAbsolutePath(), ".mp3", null, true);
            }, "Dovrebbe lanciare IllegalArgumentException perché il brano esiste già");

        } catch (IOException e) {
            Assertions.fail("Errore di I/O inaspettato durante il test dei duplicati");
        }
    }
}