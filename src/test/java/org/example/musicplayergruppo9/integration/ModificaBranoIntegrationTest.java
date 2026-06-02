package org.example.musicplayergruppo9.integration;

import org.example.musicplayergruppo9.database.DAO.BranoDAO;
import org.example.musicplayergruppo9.model.Brano;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ModificaBranoIntegrationTest {

    private BranoDAO branoDAO;
    private Brano branoInserito;

    @BeforeEach
    public void setup() {
        branoDAO = BranoDAO.getInstance();

        // create a unique test brano
        String unique = "TEST_" + System.currentTimeMillis();
        Brano b = new Brano(
                unique,
                "TestArtist",
                "TestGenre",
                2020,
                120,
                "AltriFile/Audio/test_integration.mp3",
                "mp3",
                "AltriFile/Copertine/test_integration.png",
                false,
                false
        );

        boolean saved = branoDAO.salvaBrano(b);
        Assertions.assertTrue(saved, "Impossibile salvare il brano di test nel DB");
        branoInserito = b;
    }

    @AfterEach
    public void teardown() {
        if (branoInserito != null && branoInserito.getId() > 0) {
            branoDAO.eliminaBrano(branoInserito.getId());
        }
    }

    @Test
    public void testUpdateBranoPersistsChanges() {
        // modify some metadata
        String nuovoTitolo = "MOD_" + System.currentTimeMillis();
        branoInserito.setTitolo(nuovoTitolo);
        branoInserito.setGenere("ModifiedGenre");

        boolean aggiornato = branoDAO.aggiornaBrano(branoInserito);
        Assertions.assertTrue(aggiornato, "aggiornaBrano ha restituito false");

        // fetch from DB and verify
        List<Brano> tutti = branoDAO.getTuttiIBrani();
        Brano trovato = tutti.stream().filter(x -> x.getId() == branoInserito.getId()).findFirst().orElse(null);
        Assertions.assertNotNull(trovato, "Brano aggiornato non trovato nel DB");
        Assertions.assertEquals(nuovoTitolo, trovato.getTitolo(), "Titolo non aggiornato nel DB");
        Assertions.assertEquals("ModifiedGenre", trovato.getGenere(), "Genere non aggiornato nel DB");
    }
}
