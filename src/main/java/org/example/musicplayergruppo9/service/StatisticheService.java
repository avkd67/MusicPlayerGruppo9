package org.example.musicplayergruppo9.service;

import org.example.musicplayergruppo9.database.DAO.BranoDAO;
import org.example.musicplayergruppo9.database.DAO.PlaylistDAO;
import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.Playlist;
import org.example.musicplayergruppo9.pattern.observer.Observer;
import org.example.musicplayergruppo9.pattern.observer.Subject;

import java.util.ArrayList;
import java.util.List;

public class StatisticheService implements Subject, Observer {

    private static StatisticheService instance;

    private final BranoDAO branoDAO;
    private final PlaylistDAO playlistDAO;
    private final ArrayList<Observer> observers = new ArrayList<>();

    private StatisticheService() {
        branoDAO = BranoDAO.getInstance();
        playlistDAO = PlaylistDAO.getInstance();

        branoDAO.attach(this);
    }

    public static synchronized StatisticheService getInstance() {
        if (instance == null) {
            instance = new StatisticheService();
        }
        return instance;
    }

    public Playlist getPlaylistBraniPiuRiprodotti() {
        List<Brano> brani = branoDAO.getBraniPiuRiprodotti(10);

        Playlist playlist = new Playlist("Brani piu riprodotti", null);
        playlist.setId(-200);
        playlist.getBrani().addAll(brani);

        return playlist;
    }

    public ArrayList<Playlist> getPlaylistPiuRiprodotte() {
        return playlistDAO.getPlaylistsPiuRiprodotte(10);
    }

    public void registraRiproduzionePlaylist(Playlist playlist) {
        if (playlist == null || playlist.getId() <= 0) return;

        boolean aggiornata = playlistDAO.incrementaAscolti(playlist.getId());

        if (aggiornata) {
            notifyObservers();
        }
    }

    @Override
    public void update() {
        notifyObservers();
    }

    @Override
    public void attach(Observer o) {
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void detach(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update();
        }
    }
}