package org.example.musicplayergruppo9.integration;
import org.example.musicplayergruppo9.database.DAO.PlaylistDAO;
import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.Playlist;
import org.example.musicplayergruppo9.service.StatisticheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StatisticheServiceTest {
    
    //test per verificare che i brani più riprodotti siano ordinati correttamente
    @Test
    void getPlaylistBraniPiuRiprodottiOrdinaPerAscolti() {
        StatisticheService statisticheService = StatisticheService.getInstance();

        Playlist playlist = statisticheService.getPlaylistBraniPiuRiprodotti();

        assertNotNull(playlist);
        assertEquals("Brani piu riprodotti", playlist.getNome());

        List<Brano> brani = playlist.getBrani();

        for (int i = 0; i < brani.size() - 1; i++) {
            assertTrue(
                    brani.get(i).getContatoreAscolti() >= brani.get(i + 1).getContatoreAscolti(),
                    "I brani devono essere ordinati dal più ascoltato al meno ascoltato"
            );
        }
    }

    //test per verificare che la playlist Brani Più riprodotti non contiene brani con 0 ascolti
    @Test
    void getPlaylistBraniPiuRiprodottiEscludeBraniConZeroAscolti() {
        StatisticheService statisticheService = StatisticheService.getInstance();

        Playlist playlist = statisticheService.getPlaylistBraniPiuRiprodotti();

        for (Brano brano : playlist.getBrani()) {
            assertTrue(
                    brano.getContatoreAscolti() > 0,
                    "La playlist dei più riprodotti non deve contenere brani con zero ascolti"
            );
        }
    }

    //test per verificare che l'asoclto di una playlist ne aumenta il contatore
    @Test
    void registraRiproduzionePlaylistIncrementaContatore() {
        PlaylistDAO playlistDAO = PlaylistDAO.getInstance();
        StatisticheService statisticheService = StatisticheService.getInstance();

        Playlist playlist = playlistDAO.getAllPlaylists().get(0);

        int ascoltiPrima = playlist.getContatoreAscolti();

        statisticheService.registraRiproduzionePlaylist(playlist);

        Playlist playlistAggiornata = playlistDAO.getAllPlaylists()
                .stream()
                .filter(p -> p.getId() == playlist.getId())
                .findFirst()
                .orElseThrow();

        assertEquals(ascoltiPrima + 1, playlistAggiornata.getContatoreAscolti());
    }

    //le playlist più riprodotte sono ordinate correttamente
    @Test
    void getPlaylistPiuRiprodotteOrdinaPerAscolti() {
        StatisticheService statisticheService = StatisticheService.getInstance();

        ArrayList<Playlist> playlists = statisticheService.getPlaylistPiuRiprodotte();

        for (int i = 0; i < playlists.size() - 1; i++) {
            assertTrue(
                    playlists.get(i).getContatoreAscolti() >= playlists.get(i + 1).getContatoreAscolti(),
                    "Le playlist devono essere ordinate dalla più riprodotta alla meno riprodotta"
            );
        }
    }

    //le plyalist automatiche non aumentano gli ascolti, non vengono registrati
    @Test
    void registraRiproduzionePlaylistIgnoraPlaylistAutomatiche() {
        StatisticheService statisticheService = StatisticheService.getInstance();

        Playlist playlistAutomatica = new Playlist("Brani piu riprodotti", null);
        playlistAutomatica.setId(-200);

        assertDoesNotThrow(() -> statisticheService.registraRiproduzionePlaylist(playlistAutomatica));
    }


}
