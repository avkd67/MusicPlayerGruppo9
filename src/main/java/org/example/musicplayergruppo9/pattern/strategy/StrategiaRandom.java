package org.example.musicplayergruppo9.pattern.strategy;

import org.example.musicplayergruppo9.controller.PlayerController;

//startegy pattern
//quando il brano finisce, passa al successivo (casuale) nella playlist
public class StrategiaRandom implements StrategiaRiproduzione {

    @Override
    public void onFineBrano(PlayerController controller) {
        controller.skipSong();
    }
}