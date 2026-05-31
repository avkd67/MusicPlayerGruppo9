package org.example.musicplayergruppo9;

import org.example.musicplayergruppo9.model.Brano;
import org.example.musicplayergruppo9.model.ElementoCoda;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Playlist implements ElementoCoda, Iterable<Brano> {

    // name of the playlist and data structure for the songs
    private String name; 
    private ArrayList<Brano> brani;

    // Constructor
    public Playlist(String name){
        this.name = name; 
        this.brani = new ArrayList<>();
    }

    // override of the iterator method
    @Override 
    public Iterator<Brano> iterator(){
        return List.copyOf(brani).iterator();
    }

    // method to add a song to the playlist
    public void addBrano(Brano brano){
        brani.add(brano);
    }

    // method to remove a song from the playlist
    public void removeBrano(Brano brano){
        brani.remove(brano);
    }

    // method to get the name of the playlist
    //probabilmente da togliere poichè c'è già quello di ElementoCoda
    public String getName() {
        return name;
    }
    
    // method to get the list of songs in the playlist
    public ArrayList<Brano> getBrani() {
        return brani;
    }

    // method to set a new name for the playlist
    public void setName(String name){
        this.name = name;
    }

    //da ElementoCoda
    @Override
    public String getTitolo() {
        return name;
    }

}
