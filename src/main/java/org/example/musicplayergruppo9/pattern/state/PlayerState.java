package org.example.musicplayergruppo9.pattern.state;

import org.example.musicplayergruppo9.service.PlayerService;

public interface PlayerState {
    void toggle(PlayerService context); //equivale al DoThis() DoThat() visto che ci sono solo due stati basta una funzione sec me
}