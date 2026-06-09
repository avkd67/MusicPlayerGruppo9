// pattern/observer/PlaylistObserver.java
package org.example.musicplayergruppo9.pattern.observer;

import org.example.musicplayergruppo9.model.Brano;

//pattern observer per gestire la rimozione e l'aggiunta dei brani in playlist durante la riproduzione della playlist
public interface PlaylistObserver {
    void onBranoAggiunto(Brano brano);
    void onBranoRimosso(Brano brano);
}