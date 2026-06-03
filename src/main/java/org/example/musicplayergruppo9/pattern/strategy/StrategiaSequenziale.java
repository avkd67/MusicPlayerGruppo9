package org.example.musicplayergruppo9.pattern.strategy;

import org.example.musicplayergruppo9.controller.PlayerController;

//implementa StrategiaRiproduzione definendo il comportamento di default:
//quando un brano termina, segue il brano successivo

    //utilizzata quando il loop viene disattivo poichè ripristina il funzionamento

public class StrategiaSequenziale implements StrategiaRiproduzione {

    @Override
    public void onFineBrano(PlayerController controller) {
        controller.skipSong(); //prossimo brano
    }
}