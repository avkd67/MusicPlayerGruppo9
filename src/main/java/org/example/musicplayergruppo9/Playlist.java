package org.example.musicplayergruppo9;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Playlist implements Iterable<Song> {

    // name of the playlist and data structure for the songs
    private String name; 
    private ArrayList<Song> songs;

    // Constructor
    public Playlist(String name){
        this.name = name; 
        this.songs = new ArrayList<Song>();
    }

    // override of the iterator method
    @Override 
    public Iterator<Song> iterator(){
        return List.copyOf(this.songs).iterator();
    }

    // method to add a song to the playlist
    public void addSong(Song song){
        songs.add(song);
    }

    // method to remove a song from the playlist
    public void removeSong(Song song){
        songs.remove(song);
    }

    // method to get the name of the playlist
    public String getName() {
        return name;
    }
    
    // method to get the list of songs in the playlist
    public ArrayList<Song> getSongs() {
        return songs;
    }

    // method to set a new name for the playlist
    public void setName(String name){
        this.name = name;
    }

}
