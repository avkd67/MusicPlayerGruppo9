package org.example.musicplayergruppo9.utilities;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

public final class FXutilities {

    // per evitare costruttori !
    private FXutilities(){

    }

    // apre il selettore della copertina,
    // imageView è il contenitore in cui msotrare l'immagine in FX
    // il ritorno è il percorso ASSOLUTO dell'immagine scelta
    public static String cercaCopertina(ImageView imgCopertina){
        String percorsoCopertinaSelezionata = "";
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona l'immagine di copertina");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Immagini (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg") //grazie .ExtensionFilter
        );

        Stage stage = (Stage) imgCopertina.getScene().getWindow();
        File fileSelezionato = fileChooser.showOpenDialog(stage);

        if (fileSelezionato != null) {
            percorsoCopertinaSelezionata = fileSelezionato.getAbsolutePath();
            imgCopertina.setImage(new Image(fileSelezionato.toURI().toString())); // Carica l'immagine nell'anteprima grafica ImageView
        }
        return percorsoCopertinaSelezionata;
    }

    // Recupera lo stage corrente partendo da uno qualsiasi dei nodi grafici e lo chiude
    public static void chiudiFinestra(Node nodoQualsiasi){
        Stage stage = (Stage) nodoQualsiasi.getScene().getWindow();
        stage.close();
    }

    // crea un pop up di alert
    public static void mostraAlertErrore(String titolo, String header, String contenuto){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(header);
        alert.setContentText(contenuto);
        alert.showAndWait();
    }

    // crea un pop up per indicare il successo dell'operazione
    public static void mostraAlertSuccesso(String titolo, String header){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(header);
        alert.setContentText(null);
        alert.showAndWait();
    }

    // crea un pop up per confermare la decisione dell'utente
    public static boolean mostraAlertConferma(String titolo, String domanda){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(domanda);
        alert.setContentText(null);
        alert.showAndWait();

        return alert.getResult() == ButtonType.OK;
    }
}
