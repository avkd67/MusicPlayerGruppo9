package org.example.musicplayergruppo9.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Array;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import org.example.musicplayergruppo9.database.DAO.BranoDAO;
import org.example.musicplayergruppo9.database.DAO.PlaylistBraniDAO;
import org.example.musicplayergruppo9.database.DAO.PlaylistDAO;
import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.Playlist;
import org.example.musicplayergruppo9.pattern.observer.Observer;
import org.example.musicplayergruppo9.pattern.observer.Subject;

public class PlaylistService implements Subject {

    private static PlaylistService instance;
    private PlaylistDAO playlistDAO;
    private PlaylistBraniDAO playlistBraniDAO;
    private BranoDAO branoDAO;
    private ArrayList<Observer> observers;

    private PlaylistService() {
        playlistDAO = PlaylistDAO.getInstance();
        playlistBraniDAO = PlaylistBraniDAO.getInstance();
        branoDAO = BranoDAO.getInstance();
        observers = new ArrayList<>();
    }

    public static PlaylistService getInstance() {
        if (instance == null) {
            instance = new PlaylistService();
        }
        return instance;
    }

    public List<Brano> getBraniByPlaylist(Playlist playlist) {
        return playlistBraniDAO.getInstance().getBraniByPlaylist(playlist);
    }

    public ArrayList<Playlist> getAllPlaylists() {
        return playlistDAO.getInstance().getAllPlaylists();
    }

    // restituisce le playlist generate in base al genere dei brani
    // prendo tutti i brani e li divido per genere,
    // creo una playlist per ogni genere trovato e le restituisco
    public ArrayList<Playlist> getPlaylistsByGenere(){

        List<Brano> brani = branoDAO.getTuttiIBrani();
        HashMap<String, Playlist> mappaPlaylists = new HashMap<>();

        // controllo tutti i brani
        for(Brano brano : brani){
            // solo se il brano ha un genere
            if(brano.getGenere() != null){
                String genere = brano.getGenere();

                // controllo sia già stato considerato come genere nella hashmap, altrimenti ce lo inserisco
                if(!mappaPlaylists.containsKey(genere)){
                    mappaPlaylists.put(genere, new Playlist("Playlist " + genere, null));
                }

                // aggiungo il brano alla playlist dedicata a quel genere
                mappaPlaylists.get(genere).aggiungiBrano(brano);
            }
        }
        ArrayList<Playlist> playlists = new ArrayList<>(mappaPlaylists.values());

        return playlists;
    }

    // restituisce le playlist generate in base all'anno di pubblicazione dei brani
    // prendo tutti i brani e li divido in tanti gruppi quanti sono le date di pubblicazione,
    // assegnandoli poi a playlist diverse e restituendone l'array
    public ArrayList<Playlist> getPlaylistsByAnno(){

        // come ho fatto per i generi, solo che ora stiamo trattando interi e l'assenza di dato è 0 e non più NULL
        HashMap<Integer, Playlist> mappaAnnoPlaylist = new HashMap<>();
        List<Brano> brani = branoDAO.getTuttiIBrani();

        for(Brano brano: brani){
            int dataRilascio = brano.getDataRilascio();

            if(dataRilascio != 0){

                if(!mappaAnnoPlaylist.containsKey(dataRilascio)){
                    mappaAnnoPlaylist.put(dataRilascio, new Playlist("Playlist " + dataRilascio, null));
                }

                mappaAnnoPlaylist.get(dataRilascio).aggiungiBrano(brano);
            }
        }

        return new ArrayList<>(mappaAnnoPlaylist.values());
    }

    // restituisce una playlist con tutti i brani preferiti
    // faccio prima un retrieve di tutti i brani e poi li aggiungo ad una playlist chiamata Preferiti
    public Playlist getPlaylistPreferiti(){

        List<Brano> brani = branoDAO.getTuttiIBrani();
        ArrayList<Playlist> playlists = getAllPlaylists();

        Playlist preferiti = new Playlist("Playlist preferiti", null);

        for(Brano brano: brani){
            if(brano.isPreferito()){
                preferiti.aggiungiBrano(brano);
            }
        }

        return preferiti;
    }


    public boolean checkNomePlaylist(Playlist playlist) {
        return playlistDAO.getInstance().checkNomePlaylist(playlist);
    }

    public boolean salvaPlaylist(Playlist playlist) throws  IOException {
        // prima del salvataggio, rendo il percorso relativo e non più assoluto!
        boolean successo;
        String percorsoAssoluto = playlist.getPercorsoCopertina();
        String percorsoRelativo;

        if (percorsoAssoluto != null && !percorsoAssoluto.startsWith("AltriFile") && !percorsoAssoluto.isBlank()){
            try {
                Path cartellaCopertine = java.nio.file.Paths.get("AltriFile", "Copertine");

                if (!java.nio.file.Files.exists(cartellaCopertine))
                    Files.createDirectories(cartellaCopertine);

                File copertinaOriginale = new File(percorsoAssoluto);
                Path targetCopertina = cartellaCopertine.resolve(copertinaOriginale.getName());
                Files.copy(copertinaOriginale.toPath(), targetCopertina, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                percorsoRelativo = "AltriFile/Copertine/" + copertinaOriginale.getName();
                playlist.setPercorsoCopertina(percorsoRelativo);

            }catch (java.io.IOException e) {
                e.printStackTrace();
                throw e;
            }
        }

        successo = playlistDAO.getInstance().salvaPlaylist(playlist);
        if(successo)
            notifyObservers();
        return successo;
    }

    // salva la playlist nel db e le associa tutti i brani presenti nella lista
    public boolean salvaPlaylistConBrani(Playlist playlist, ArrayList<Brano> brani) {
        try {

            if(!salvaPlaylist(playlist)){
                return false;
            }

            for(Brano brano : brani){
                if(!this.aggiungiBranoAPlaylist(playlist, brano))
                    return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        notifyObservers();
        return true;
    }

    // aggiorna SOLO nome e copertina della playlist
    public boolean aggiornaPlaylist(Playlist playlist, Playlist playlistAggiornata) throws IOException {
        // se il percorso della copertina è stato cambiato, devo provvedere a renderlo relativo!
        boolean successo;
        if(!Objects.equals(playlist.getPercorsoCopertina(), playlistAggiornata.getPercorsoCopertina())){
            if (playlistAggiornata.getPercorsoCopertina() != null){
                try {
                    Path cartellaCopertine = java.nio.file.Paths.get("AltriFile", "Copertine");

                    if(!java.nio.file.Files.exists(cartellaCopertine))
                        Files.createDirectories(cartellaCopertine); // teoricamente c'è per forza essendoci una modifica
                                                                    // ma non si sa mai!
                    File copertinaOriginale = new File(playlistAggiornata.getPercorsoCopertina());
                    Path targetCopertina = cartellaCopertine.resolve(copertinaOriginale.getName());
                    Files.copy(copertinaOriginale.toPath(), targetCopertina, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    String percorsoRelativo = "AltriFile/Copertine/" + copertinaOriginale.getName();
                    playlistAggiornata.setPercorsoCopertina(percorsoRelativo);

                } catch(java.io.IOException e){
                    e.printStackTrace();
                    throw e;
                }
            }
        }
        successo = PlaylistDAO.getInstance().aggiornaPlaylist(playlist, playlistAggiornata);
        if(successo)
            notifyObservers();
        return successo;
    }

    public boolean eliminaPlaylist(Playlist playlist) {
        boolean successo;
        successo = playlistDAO.getInstance().eliminaPlaylist(playlist);
        if(successo)
            notifyObservers();
        return successo;
    }

    public boolean aggiungiBranoAPlaylist(Playlist playlist, Brano brano) {
        boolean successo = playlistBraniDAO.getInstance().aggiungiBranoAPlaylist(playlist, brano);
        if(successo)
            notifyObservers();
        return successo;
    }

    public boolean eliminaBranoDaPlaylist(Playlist playlist, Brano brano) {
        boolean successo;
        successo = playlistBraniDAO.getInstance().rimuoviBranoDaPlaylist(playlist, brano);
        if(successo)
            notifyObservers();
        return successo;
    }

    @Override
    public void attach(Observer o) {
        observers.add(o);
    }

    @Override
    public void detach(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for(Observer o : observers){
            o.update();
        }
    }
}
