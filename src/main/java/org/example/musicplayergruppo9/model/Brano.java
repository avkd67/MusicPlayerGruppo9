package org.example.musicplayergruppo9.model;

import java.io.File;
import java.util.Objects;
import java.util.Collections;
import java.util.Iterator;

public class Brano implements ElementoCoda{

    //toDo aggiungere campi preferiti, explicit e new release
    private int id;
    private String titolo;
    private String artista;
    private String genere;
    private int dataRilascio;
    private int durata;
    private String percorsoFileAudio;
    private String estensione;
    private String percorsoCopertina;

    private boolean preferito;
    private boolean newRelease;
    private boolean explicit;

    private int contatoreAscolti;


    // costruttore senza id per l'inserimento
    public Brano(String titolo, String artista, String genere, int dataRilascio, int durata, String percorsoFileAudio, String estensione, String percorsoCopertina, boolean newRelease, boolean explicit) {
        this.titolo = titolo;
        this.artista = artista;
        this.genere = genere;
        this.dataRilascio = dataRilascio;
        this.durata = durata;
        this.percorsoFileAudio = percorsoFileAudio;
        this.estensione = estensione;
        this.percorsoCopertina = percorsoCopertina;
        this.newRelease = newRelease;
        this.explicit = explicit;
    }

    //costruttore con id per il recupero
    public Brano(int id, String titolo, String artista, String genere, int dataRilascio, int durata, String percorsoFileAudio, String estensione, String percorsoCopertina, boolean preferito, boolean newRelease, boolean explicit, int contatoreAscolti) {
        this.id = id;
        this.titolo = titolo;
        this.artista = artista;
        this.genere = genere;
        this.dataRilascio = dataRilascio;
        this.durata = durata;
        this.percorsoFileAudio = percorsoFileAudio;
        this.estensione = estensione;
        this.percorsoCopertina = percorsoCopertina;
        this.preferito = preferito;
        this.newRelease = newRelease;
        this.explicit = explicit;
        this.contatoreAscolti = contatoreAscolti;
    }

    //per la creazione di oggetti brano che saranno riempiti successivamente con i dati necessari.
    public Brano() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public String getGenere() {
        return genere;
    }

    public void setGenere(String genere) {
        this.genere = genere;
    }

    public int getDataRilascio() {
        return dataRilascio;
    }

    public void setDataRilascio(int dataRilascio) {
        this.dataRilascio = dataRilascio;
    }

    public int getDurata() {
        return durata;
    }

    public void setDurata(int durata) {
        this.durata = durata;
    }

    public String getPercorsoFileAudio() {
        return percorsoFileAudio;
    }

    public void setPercorsoFileAudio(String percorsoFileAudio) {
        this.percorsoFileAudio = percorsoFileAudio;
    }

    public String getEstensione() {
        return estensione;
    }

    public void setEstensione(String estensione) {
        this.estensione = estensione;
    }

    public String getPercorsoCopertina() {
        return percorsoCopertina;
    }

    public void setPercorsoCopertina(String percorsoCopertina) {
        this.percorsoCopertina = percorsoCopertina;
    }

    public boolean isPreferito() {
        return preferito;
    }

    public void setPreferito(boolean preferito) {
        this.preferito = preferito;
    }

    public boolean isNewRelease() {
        return newRelease;
    }

    public void setNewRelease(boolean newRelease) {
        this.newRelease = newRelease;
    }

    public boolean isExplicit() {
        return explicit;
    }

    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }

    public int getContatoreAscolti() {
        return contatoreAscolti; 
    }

    public void setContatoreAscolti(int contatoreAscolti) { 
        this.contatoreAscolti = contatoreAscolti; 
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Brano brano = (Brano) o;
        return Objects.equals(titolo, brano.titolo) && Objects.equals(artista, brano.artista);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titolo.toLowerCase(), artista.toLowerCase());
    }

    @Override
    public String toString() {
        return "Brano{" +
                "id=" + id +
                ", titolo='" + titolo + '\'' +
                ", artista='" + artista + '\'' +
                ", genere='" + genere + '\'' +
                ", dataRilascio=" + dataRilascio +
                ", durata=" + durata +
                ", percorsoFileAudio='" + percorsoFileAudio + '\'' +
                ", estensione='" + estensione + '\'' +
                ", percorsoCopertina='" + percorsoCopertina + '\'' +
                '}';
    }


    // Metodi Aggiunti

    public File getFileAudio() {
        return new File(this.percorsoFileAudio);
    }

    public File getCopertina() {
        if (this.percorsoCopertina != null) {
            return new File(this.percorsoCopertina);
        }
        return null;
    }

    public String getDurataFormattata() {
        int ore = durata / 3600;               // 3600 secondi in un'ora (messo per sicurezza, sia mai qualcuno volesse caricare un podcast)
        int minuti = (durata / 60) % 60;       // Minuti restanti (da 0 a 59)
        int secondi = durata % 60;             // Secondi restanti (da 0 a 59)

        if (ore == 0) {
            // Se non ci sono ore, mostra solo Minuti:Secondi (MM:SS)
            return String.format("%02d:%02d", minuti, secondi);
        } else {
            // Se ci sono ore, mostra Ore:Minuti:Secondi (HH:MM:SS)
            return String.format("%02d:%02d:%02d", ore, minuti, secondi);
        }
    }

    //Da ElementoCoda.java per soddisfare la task 6.2
    @Override
    public Iterator<Brano> iterator() {
        //per restituire se stesso
        return Collections.singletonList(this).iterator();
    }

}
