package org.example.musicplayergruppo9.service;

import org.example.musicplayergruppo9.database.DAO.BranoDAO;
import org.example.musicplayergruppo9.model.Brano;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class AggiungiBranoService {

    private BranoDAO branoDAO;

    public AggiungiBranoService() {
        branoDAO = BranoDAO.getInstance();
    }

    public boolean gestisciSalvataggio(String titolo, String artista, String genere,
                                       int annoRilascio,
                                       String percorsoFileAudioSelezionato, String estensioneFileAudio,
                                       String percorsoCopertinaSelezionata, boolean explicit) throws IllegalArgumentException, java.io.IOException {

        // Validazione: Gestione degli omonimi nel DataBase
        if (branoDAO.esisteOmonimo(titolo, artista)) {
            throw new IllegalArgumentException("Brano duplicato");
        }

        // copia i file nella cartella del progetto
        String percorsoAudioRelativo = "";
        String percorsoCopertinaRelativa = "";

        try {
            // Creazione cartella Audio
            Path cartellaAudio = Paths.get("AltriFile", "Audio");
            if (!Files.exists(cartellaAudio)) {
                Files.createDirectories(cartellaAudio);
            }

            // Copia del file Audio
            File fileAudioOriginale = new File(percorsoFileAudioSelezionato);
            Path targetAudio = cartellaAudio.resolve(fileAudioOriginale.getName());

            // StandardCopyOption.REPLACE_EXISTING sovrascrive il file se ne esiste già uno con lo stesso nome, non sapevo come gestirlo sincero
            Files.copy(fileAudioOriginale.toPath(), targetAudio, StandardCopyOption.REPLACE_EXISTING);
            percorsoAudioRelativo = "AltriFile/Audio/" + fileAudioOriginale.getName();

            // Stessa cosa ma per la Copertina praticamente
            if (percorsoCopertinaSelezionata != null) {
                Path cartellaCopertine = java.nio.file.Paths.get("AltriFile", "Copertine");
                if (!java.nio.file.Files.exists(cartellaCopertine)) {
                    java.nio.file.Files.createDirectories(cartellaCopertine);
                }

                File fileCopertinaOriginale = new File(percorsoCopertinaSelezionata);
                Path targetCopertina = cartellaCopertine.resolve(fileCopertinaOriginale.getName());
                Files.copy(fileCopertinaOriginale.toPath(), targetCopertina, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                percorsoCopertinaRelativa = "AltriFile/Copertine/" + fileCopertinaOriginale.getName();            }

        } catch (java.io.IOException e) {
            e.printStackTrace();
            throw e; // Blocchiamo il salvataggio se la copia fallisce
        }

        // Calcolo automatico di newRelease (è nuovo se l'anno inserito è uguale a quello di oggi)
        int annoCorrente = java.time.LocalDate.now().getYear();
        boolean newRelease = (annoRilascio > 0) && (annoRilascio == annoCorrente);

        // Calcolo/Estrazione della durata
        int durataSecondi = 0;
        File fileAudioSalvato = new File(percorsoAudioRelativo);
        durataSecondi = (int) (fileAudioSalvato.length() / 24000);

        // Creazione dell'oggetto Model (uso il costruttore senza ID)
        Brano nuovoBrano = new Brano(
                titolo,
                artista,
                genere,
                annoRilascio,
                durataSecondi,
                percorsoAudioRelativo,
                estensioneFileAudio,
                percorsoCopertinaRelativa,
                newRelease,
                explicit
        );

        // Salvataggio definitivo nel Database tramite DAO
        return branoDAO.salvaBrano(nuovoBrano);
    }
}