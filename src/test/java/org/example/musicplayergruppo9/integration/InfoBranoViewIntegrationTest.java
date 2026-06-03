package org.example.musicplayergruppo9.integration;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import org.example.musicplayergruppo9.controller.InfoBranoController;
import org.example.musicplayergruppo9.model.Brano;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class InfoBranoViewIntegrationTest {

    private static final CountDownLatch toolkitLatch = new CountDownLatch(1);

    @BeforeAll
    public static void initJFX() throws InterruptedException {
        // Start JavaFX toolkit once
        Platform.startup(() -> {
            // no-op
        });
        // give some time for toolkit to initialize
        Thread.sleep(200);
    }

    @AfterAll
    public static void teardown() {
        // nothing to do; Platform.exit() can be called but may affect other tests
    }

    @Test
    public void testInfoViewDisplaysBranoData() throws Exception {
        // create a sample Brano
        Brano b = new Brano(
                "TITOLO_TEST",
                "ARTISTA_TEST",
                "GENERE_TEST",
                1999,
                123,
                "AltriFile/Audio/placeholder.mp3",
                "mp3",
                "AltriFile/Copertine/placeholder.png",
                false,
                false
        );

        CountDownLatch latch = new CountDownLatch(1);
        final InfoBranoController[] controllerHolder = new InfoBranoController[1];

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/musicplayergruppo9/fxml/InfoBrano.fxml"));
                Parent root = loader.load();
                InfoBranoController controller = loader.getController();
                controllerHolder[0] = controller;

                // call setBrano and then verify
                controller.setBrano(b);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        boolean ok = latch.await(3, TimeUnit.SECONDS);
        Assertions.assertTrue(ok, "JavaFX operation timeout");

        InfoBranoController controller = controllerHolder[0];
        Assertions.assertNotNull(controller, "Controller non caricato");

        // Verify fields
        Assertions.assertEquals("TITOLO_TEST", controller.getTitoloText());
        Assertions.assertEquals("ARTISTA_TEST", controller.getArtistaText());
        Assertions.assertEquals("Genere: GENERE_TEST", controller.getGenereText());
        Assertions.assertEquals("Anno: 1999", controller.getAnnoText());
        Assertions.assertTrue(controller.getDurataText().contains("Durata"));

        // Copertina potrebbe non esistere fisicamente; testa solo che il getter non lanci
        Image img = controller.getCopertinaImage();
        // image can be null if file missing; ensure no exception and type ok
        if (img != null) {
            Assertions.assertTrue(img instanceof Image);
        }
    }
}
