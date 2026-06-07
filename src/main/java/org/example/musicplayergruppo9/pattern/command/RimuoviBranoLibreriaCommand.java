package org.example.musicplayergruppo9.pattern.command;

import org.example.musicplayergruppo9.database.DAO.BranoDAO;
import org.example.musicplayergruppo9.database.DAO.PlaylistBraniDAO;
import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.Playlist;
import org.example.musicplayergruppo9.utilities.FXutilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class RimuoviBranoLibreriaCommand implements Command {


    private final Brano brano;
    private final BranoDAO branoDAO;
    private final PlaylistBraniDAO playlistBraniDAO;

    // path per evitare l'eliminazione definitiva dei dati

    private Path originalAudioPath;
    private Path tempAudioPath;
    private Path originalCoverPath;
    private Path tempCoverPath;
    // Cartella temporanea
    private static final String CESTINO_DIR = "CestinoTemp";

    private List<Playlist> playlistConBrano; //la lista delle playlist in cui è presente il brano


    public RimuoviBranoLibreriaCommand(Brano brano, BranoDAO branoDAO, PlaylistBraniDAO playlistBraniDAO) {
        this.brano = brano;
        this.branoDAO = branoDAO;
        this.playlistBraniDAO = playlistBraniDAO;
    }


    @Override
    public boolean execute() {
        this.playlistConBrano = playlistBraniDAO.getPlaylistsByBrano(brano);

        //

        boolean eliminato = branoDAO.eliminaBrano(brano.getId());

        if (eliminato) {
            spostaInCestino();
        }

        return eliminato;
    }

    private void spostaInCestino() {
        try {
            Path cestinoDir = Paths.get(CESTINO_DIR);
            if (!Files.exists(cestinoDir)) {
                Files.createDirectories(cestinoDir);
            }

            // Spostamento File Audio
            if (brano.getPercorsoFileAudio() != null && !brano.getPercorsoFileAudio().isBlank()) {
                originalAudioPath = Paths.get(brano.getPercorsoFileAudio());
                if (Files.exists(originalAudioPath)) {
                    tempAudioPath = cestinoDir.resolve(originalAudioPath.getFileName());
                    Files.move(originalAudioPath, tempAudioPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            // Spostamento File Copertina
            if (brano.getPercorsoCopertina() != null && !brano.getPercorsoCopertina().isBlank()) {
                originalCoverPath = Paths.get(brano.getPercorsoCopertina());
                if (Files.exists(originalCoverPath)) {
                    tempCoverPath = cestinoDir.resolve(originalCoverPath.getFileName());
                    Files.move(originalCoverPath, tempCoverPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            System.err.println("Errore durante lo spostamento dei file nel Cestino: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void undo() {
        boolean ripristinato = branoDAO.salvaBrano(brano);

        if (ripristinato) {
            // Ripristino i collegamenti con le playlist
            if (playlistConBrano != null) {
                for (Playlist p : playlistConBrano) {
                    playlistBraniDAO.aggiungiBranoAPlaylist(p, brano);
                }
            }

            // Riporto i file fisici nelle cartelle originali
            ripristinaDaCestino();

            FXutilities.mostraAlertSuccesso("Ripristino brano","Brano ripristinato correttamente");
        }
    }

    private void ripristinaDaCestino() {
        try {
            // Ripristino File Audio
            if (tempAudioPath != null && originalAudioPath != null && Files.exists(tempAudioPath)) {
                Files.move(tempAudioPath, originalAudioPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Ripristino File Copertina
            if (tempCoverPath != null && originalCoverPath != null && Files.exists(tempCoverPath)) {
                Files.move(tempCoverPath, originalCoverPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.err.println("Errore durante il ripristino dei file dal Cestino: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
