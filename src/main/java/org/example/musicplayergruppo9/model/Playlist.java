package org.example.musicplayergruppo9.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.example.musicplayergruppo9.pattern.observer.PlaylistObserver;

import org.example.musicplayergruppo9.pattern.observer.PlaylistObserver;

import java.util.Collections;



public class Playlist implements Iterable<Brano>, ElementoCoda {

    // nome della playlist, lista contenente i brani e ID della playlist nel database
    private String nome; 
    private ArrayList<Brano> brani;
    private int id;
    private String percorsoCopertina;

    //patter Observer per gestire inserimento e cancellazione brano in playlist durante l'esecuzione
    private final ArrayList<PlaylistObserver> playlistObservers = new ArrayList<>();

    public void addPlaylistObserver(PlaylistObserver o) {
        if (!playlistObservers.contains(o)) playlistObservers.add(o);
    }

    public void removePlaylistObserver(PlaylistObserver o) {
        playlistObservers.remove(o);
    }

    // Costruttore, percorsoCopertina può essere null se l'utente non ne seleziona una
    public Playlist(String nome, String percorsoCopertina){
        this.nome = nome;
        this.percorsoCopertina = percorsoCopertina;
        this.brani = new ArrayList<Brano>();
        this.id = -1; // ID non ancora assegnato
    }

    // costruttore vuoto per creare nuove playlist da riempire successivamente
    public Playlist(){
        brani = new ArrayList<Brano>();
    }

    // Override di Iterator
    @Override 
    public Iterator<Brano> iterator(){
        return List.copyOf(this.brani).iterator();
    }

    // metodo per aggiungere un brano alla playlist
    public void aggiungiBrano(Brano brano){
        brani.add(brano);
        //notifica dopo l'aggiunta
        playlistObservers.forEach(o -> o.onBranoAggiunto(brano));
    }

    // metodo per rimuovere un brano dalla playlist
    public void rimuoviBrano(Brano brano){
        brani.remove(brano);
        //notifica dopo la rimozione
        playlistObservers.forEach(o -> o.onBranoRimosso(brano));
    }

    // metodo per ottenere il nome della playlist
    public String getNome() {
        return nome;
    }
    
    // metodo per ottenere la lista dei brani nella playlist
    public ArrayList<Brano> getBrani() {
        return brani;
    }

    // metodo per impostare un nuovo nome per la playlist
    public void setNome(String nome){
        this.nome = nome;
    }

    // metodo per inserire l'id della playlist assegnato dal database
    public void setId(int id) {
        this.id = id;
    }

    // metodo per ottenere l'id della playlist
    public int getId() {
        return id;
    }
    
    // metodo per ottenere il percorso della copertina
    public String getPercorsoCopertina() {
        return percorsoCopertina;
    }

    // metodo per impostare il percorso della copertina
    public void setPercorsoCopertina(String percorsoCopertina) {
        this.percorsoCopertina = percorsoCopertina;
    }

    // override del metodo toString
    @Override
    public String toString(){
        return "nome playlist: " + this.nome + "\n" +
            "numero brani: " + this.brani.size() + "\n" + "brani: " + this.brani.toString() + ")";
    }

    // da ElementoCoda
    @Override
    public String getTitolo() {
        return nome;
    }
       
    /* brani.forEach(brano -> {
            System.out.println(brano.toString());
             });
    */ 

    public Iterator<Brano> getRandomIterator() {
        ArrayList<Brano> braniMescolati = new ArrayList<>(this.brani);
        Collections.shuffle(braniMescolati);
        return braniMescolati.iterator();
    }

}
