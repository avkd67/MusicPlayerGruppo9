package org.example.musicplayergruppo9.pattern.state;

import org.example.musicplayergruppo9.PlayerService;

public class PausedState implements PlayerState {
    @Override
    public void toggle(PlayerService context) {
        context.playAudio();
        context.setState(new PlayingState());
        context.updatePlayButtonUI("⏸"); // Cambia in simbolo Pausa
    }
}
