package org.example.musicplayergruppo9.pattern.strategy;

import org.example.musicplayergruppo9.controller.PlayerController;

//implementa StrategiaRiproduzione definendo il comportamento del loop:
//quando il brano termina, riparte

//il loop avrà priorità maggiore, se premo skip, riparte lo stesso brano.
//andrà al successivo solo quando disattivo il loop

public class StrategiaLoop implements StrategiaRiproduzione {

    @Override
    public void onFineBrano(PlayerController controller) {
        controller.riproduciBranoCorrente(); //riparte il brano
    }
}