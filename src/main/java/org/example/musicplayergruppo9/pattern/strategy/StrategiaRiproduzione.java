package org.example.musicplayergruppo9.pattern.strategy;

import org.example.musicplayergruppo9.controller.PlayerController;

//interfaccia del pattern Strategy per gestire cosa fare quando un brano termina

public interface StrategiaRiproduzione {
    //il PlayerController delega quando non sa che comportamento verrà eseguito
    void onFineBrano(PlayerController controller);
}