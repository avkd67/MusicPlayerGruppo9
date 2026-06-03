package org.example.musicplayergruppo9.integration;

import org.example.musicplayergruppo9.database.DAO.BranoDAO;
import org.example.musicplayergruppo9.database.DAO.PlaylistBraniDAO;
import org.example.musicplayergruppo9.database.DAO.PlaylistDAO;
import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.Playlist;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class RimozioneBranoIntegrationTest {

    private BranoDAO branoDAO;
    private PlaylistDAO playlistDAO;
    private PlaylistBraniDAO playlistBraniDAO;

    private Brano branoInserito;
    private Playlist playlistInserita;

    @BeforeEach
    public void setup() {
        branoDAO = BranoDAO.getInstance();
        playlistDAO = PlaylistDAO.getInstance();
        playlistBraniDAO = PlaylistBraniDAO.getInstance();

        // crea playlist di test
        playlistInserita = new Playlist("PL_TEST_" + System.currentTimeMillis(), "AltriFile/Copertine/pl_test.png");
        boolean plSaved = playlistDAO.salvaPlaylist(playlistInserita);
        Assertions.assertTrue(plSaved, "Impossibile salvare la playlist di test");

        // crea brano di test
        branoInserito = new Brano(
                "BR_TEST_" + System.currentTimeMillis(),
                "TestArtist",
                "TestGenre",
                2021,
                100,
                "AltriFile/Audio/test_del.mp3",
                "mp3",
                "AltriFile/Copertine/test_del.png",
                false,
                false
        );
        boolean bSaved = branoDAO.salvaBrano(branoInserito);
        Assertions.assertTrue(bSaved, "Impossibile salvare il brano di test");

        // associa il brano alla playlist
        boolean assoc = playlistBraniDAO.salvaPlaylistBrano(playlistInserita.getId(), branoInserito.getId());
        Assertions.assertTrue(assoc, "Impossibile associare brano alla playlist");
    }

    @AfterEach
    public void teardown() {
        if (branoInserito != null && branoInserito.getId() > 0) {
            branoDAO.eliminaBrano(branoInserito.getId());
        }
        if (playlistInserita != null && playlistInserita.getId() > 0) {
            // non esiste metodo delete in PlaylistDAO; ignoro o lasciarla
        }
    }

    @Test
    public void testRimozioneBranoCascadeFromPlaylists() {
        // verifica preliminare: il brano è nella playlist
        List<Brano> braniInPl = playlistBraniDAO.getBraniByPlaylist(playlistInserita);
        boolean trovato = braniInPl.stream().anyMatch(b -> b.getId() == branoInserito.getId());
        Assertions.assertTrue(trovato, "Il brano non è stato trovato nella playlist prima dell'eliminazione");

        // elimina il brano dalla libreria
        boolean eliminato = branoDAO.eliminaBrano(branoInserito.getId());
        Assertions.assertTrue(eliminato, "eliminaBrano ha restituito false");

        // dopo l'eliminazione, il brano non deve essere presente nella libreria
        List<Brano> tutti = branoDAO.getTuttiIBrani();
        boolean ancoraPresente = tutti.stream().anyMatch(b -> b.getId() == branoInserito.getId());
        Assertions.assertFalse(ancoraPresente, "Il brano è ancora presente nella libreria dopo l'eliminazione");

        // e non deve comparire nella playlist
        List<Brano> braniDopo = playlistBraniDAO.getBraniByPlaylist(playlistInserita);
        boolean presenteNellaPlaylist = braniDopo.stream().anyMatch(b -> b.getId() == branoInserito.getId());
        Assertions.assertFalse(presenteNellaPlaylist, "Il brano è ancora presente nella playlist dopo l'eliminazione");
    }
}
