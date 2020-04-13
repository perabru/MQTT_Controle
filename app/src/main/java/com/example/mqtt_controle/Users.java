package com.example.mqtt_controle;

public class Users {

    private String datarisco;
    private String bpm;

    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getIdESP82() {
        return idESP82;
    }

    public void setIdESP82(String idESP82) {
        this.idESP82 = idESP82;
    }

    private String idESP82;



    private String temperatura;
    private String vezes;


    public String getDatarisco() {
        return datarisco;
    }

    public void setDatarisco(String datarisco) {
        this.datarisco = datarisco;
    }

    public String getBpm() {
        return bpm;
    }

    public void setBpm(String bpm) {
        this.bpm = bpm;
    }


    public String getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(String temperatura) {
        this.temperatura = temperatura;
    }

    public String getVezes() {
        return vezes;
    }

    public void setVezes(String vezes) {
        this.vezes = vezes;
    }

public  Users(String Id, String key, String DataRisco, String Temperatura, String BPM, String Vezes){

       idESP82 = Id;
       datarisco = DataRisco;
       temperatura = Temperatura;
       bpm = BPM;
       vezes=Vezes;


}


    public Users() {}
}
