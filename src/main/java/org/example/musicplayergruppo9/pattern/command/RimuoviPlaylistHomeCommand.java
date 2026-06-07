package org.example.musicplayergruppo9.pattern.command;

import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.Playlist;
import org.example.musicplayergruppo9.service.PlaylistService;
import org.example.musicplayergruppo9.utilities.FXutilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;


public class RimuoviPlaylistHomeCommand implements Command {

    private Playlist playlistCancellata; // per mantenere le info della playlist cancellata
    private PlaylistService playlistService; // per le operazioni col db
    private ArrayList<Brano> backupBrani;

    // Cartella temporanea
    private static final String CESTINO_DIR = "CestinoTemp";

    // per il salvataggio della copertina vecchia e nuova
    private Path vecchiaCopertina;
    private Path copertinaTemp;

    public RimuoviPlaylistHomeCommand(Playlist playlistCancellata) {
        this.playlistCancellata = playlistCancellata;
        playlistService = PlaylistService.getInstance();
        this.backupBrani = new ArrayList<>(playlistService.getBraniByPlaylist(playlistCancellata));
    }

    // esegue il command: cancella la playlist e le informazioni a lei legate
    public boolean execute(){

        boolean eliminato = playlistService.eliminaPlaylist(playlistCancellata);

        if(eliminato)
            spostaInCestino();

        return eliminato;
    }

    // ripristina la playlist e i file associati
    public void undo(){

        // ripristino prima l'immagine dal cestino altrimenti la home non la trova quando viene notificata!!
        ripristinaDaCestino();
        playlistService.salvaPlaylistConBrani(playlistCancellata, backupBrani);

        FXutilities.mostraAlertSuccesso("Ripristino playlist","Playlist ripristinata correttamente");
    }

    public void ripristinaDaCestino(){
        try {
            // Ripristino File Copertina
            if (copertinaTemp != null && vecchiaCopertina != null && Files.exists(copertinaTemp)) {
                Files.move(copertinaTemp, vecchiaCopertina, StandardCopyOption.REPLACE_EXISTING);
            }
        }   catch (IOException e) {
            System.err.println("Errore durante il ripristino dei file dal Cestino: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // sposta in CestinoTemp il vecchio percorso dell'img per ripristinarla se l'utente la riesuma
    public void spostaInCestino(){
        try {
            Path cestinoDir = Paths.get(CESTINO_DIR);
            if (!Files.exists(cestinoDir)) {
                Files.createDirectories(cestinoDir);
            }
            if (playlistCancellata.getPercorsoCopertina() != null && !playlistCancellata.getPercorsoCopertina().isBlank()) {
                vecchiaCopertina = Paths.get(playlistCancellata.getPercorsoCopertina());
                if (Files.exists(vecchiaCopertina)) {
                    copertinaTemp = cestinoDir.resolve(vecchiaCopertina.getFileName());
                    Files.move(vecchiaCopertina, copertinaTemp, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            System.err.println("[RimuoviPlaylistHomeCommand] Errore durante lo spostamento dei file nel Cestino: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
