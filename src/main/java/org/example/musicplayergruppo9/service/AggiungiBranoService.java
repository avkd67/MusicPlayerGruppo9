package org.example.musicplayergruppo9.service;

import java.io.File;

import org.example.musicplayergruppo9.database.DAO.BranoDAO;
import org.example.musicplayergruppo9.model.Brano;

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
        String percorsoAudioDefinitivo = percorsoFileAudioSelezionato;
        String percorsoCopertinaDefinitiva = percorsoCopertinaSelezionata;

        try {
            // Creazione cartella Audio
            java.nio.file.Path cartellaAudio = java.nio.file.Paths.get("AltriFile", "Audio");
            if (!java.nio.file.Files.exists(cartellaAudio)) {
                java.nio.file.Files.createDirectories(cartellaAudio); // questo l'ho messo perché non dovremmo tutti pushare le canzoni che ascoltiamo, speriamo funzioni
            }

            // Copia del file Audio
            File fileAudioOriginale = new File(percorsoFileAudioSelezionato);
            java.nio.file.Path targetAudio = cartellaAudio.resolve(fileAudioOriginale.getName());

            // StandardCopyOption.REPLACE_EXISTING sovrascrive il file se ne esiste già uno con lo stesso nome, non sapevo come gestirlo sincero
            java.nio.file.Files.copy(fileAudioOriginale.toPath(), targetAudio, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            percorsoAudioDefinitivo = targetAudio.toString(); // Aggiorno il percorso salvato nel dB

            // Stessa cosa ma per la Copertina praticamente
            if (percorsoCopertinaSelezionata != null) {
                java.nio.file.Path cartellaCopertine = java.nio.file.Paths.get("AltriFile", "Copertine");
                if (!java.nio.file.Files.exists(cartellaCopertine)) {
                    java.nio.file.Files.createDirectories(cartellaCopertine);
                }

                File fileCopertinaOriginale = new File(percorsoCopertinaSelezionata);
                System.out.println("Percorso copertina originale: " + percorsoCopertinaSelezionata);
                java.nio.file.Path targetCopertina = cartellaCopertine.resolve(fileCopertinaOriginale.getName());
                java.nio.file.Files.copy(fileCopertinaOriginale.toPath(), targetCopertina, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                percorsoCopertinaDefinitiva = targetCopertina.toString();
            }

        } catch (java.io.IOException e) {
            e.printStackTrace();
            throw e; // Blocchiamo il salvataggio se la copia fallisce
        }

        // Calcolo automatico di newRelease (è nuovo se l'anno inserito è uguale a quello di oggi)
        int annoCorrente = java.time.LocalDate.now().getYear();
        boolean newRelease = (annoRilascio > 0) && (annoRilascio == annoCorrente);

        // Calcolo/Estrazione della durata
        int durataSecondi = 0;
        File fileAudioSalvato = new File(percorsoAudioDefinitivo);
        durataSecondi = (int) (fileAudioSalvato.length() / 24000);

        // Creazione dell'oggetto Model (uso il costruttore senza ID)
        Brano nuovoBrano = new Brano(
                titolo,
                artista,
                genere,
                annoRilascio,
                durataSecondi,
                percorsoAudioDefinitivo,
                estensioneFileAudio,
                percorsoCopertinaDefinitiva,
                newRelease,
                explicit
        );

        // Salvataggio definitivo nel Database tramite DAO
        return branoDAO.salvaBrano(nuovoBrano);
    }
}