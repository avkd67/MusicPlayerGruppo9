//Codice aggiunto per la Task 6.2

package org.example.musicplayergruppo9.model;

import java.util.Iterator;


//Interfaccia comune per tutto ciò che può stare in coda di riproduzione.
//Viene implementata da Brano e Playlist.

public interface ElementoCoda {

    String getTitolo();

    Iterator<Brano> iterator();

}