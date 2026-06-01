package org.example.musicplayergruppo9.pattern.state;

import org.example.musicplayergruppo9.PlayerService;

public class PlayingState implements PlayerState {
    @Override
    public void toggle(PlayerService context) {
        context.pauseAudio();
        context.setState(new PausedState());
        context.updatePlayButtonUI("▶"); // Torna al simbolo Play
    }
}
