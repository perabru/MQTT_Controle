package com.example.mqtt_controle;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

//Tutorial -- https://www.youtube.com/watch?v=u55iHZHgaGQ

//Firechain https://www.youtube.com/watch?v=U2X09uKihoM

public class MainActivity extends AppCompatActivity {

    static String MQTTHOST = "tcp://tailor.cloudmqtt.com:13520";
    static String USERNAME = "uffdqqwl";
    static String PASSWORD = "7RJqyFBtPtQN";

    //Arredondar do numeros
    DecimalFormat df;

    //Arrays
    ArrayList<String> arrTemp;
    ArrayList<String> arrUmidade;

    private LocationManager locationManager;

    MqttAndroidClient client;
    TextView txtTemperatura;
    TextView txtUmidade;

    public TextView getTxtId() {
        return txtId;
    }

    public void setTxtId(TextView txtId) {
        this.txtId = txtId;
    }

    final ArrayList<String> arrValores = new ArrayList<>();

    final StringBuilder data = new StringBuilder();

    public TextView txtId;
    TextView txtBPM;
    TextView txtRisco;
    TextView txtTIR;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    String key;
    //Firebase
    Button btnGerarRelatorio;
    Button btnGerarGrafico;
    SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
    //String currentDateandTime = sdf.format(new Date());


    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("prontuario");

    Switch swtPandemia;

    int contadorDeRisco = 0;
    //tópicos que preciso
    String topicoTemperatura = "casa/temperatura";
    String topicoUmidade = "casa/umidade";
    String topicoBPM = "casa/bpm";
    String topicoID = "casa/id";
    String topicoTIR = "casa/tir";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST, clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());

        txtTemperatura = findViewById(R.id.txtTemperatura);
        txtUmidade = findViewById(R.id.txtUmidade);
        txtId = findViewById(R.id.txtID);
        txtBPM = findViewById(R.id.txtBPM);
        txtRisco = findViewById(R.id.txtRisco);
        txtTIR = findViewById(R.id.txtTIR);

        //Firebase
        btnGerarRelatorio = findViewById(R.id.btnRelatorio);
        btnGerarGrafico = findViewById(R.id.btnGrafico);

        arrTemp = new ArrayList<>();
        arrUmidade = new ArrayList<>();

        //Pandemia
        swtPandemia = findViewById(R.id.swtPandemia);



        //---------------------------------------------------------

        swtPandemia.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "Checked", Toast.LENGTH_SHORT).show();

                   //Código para comparar a localização atual com lugares de pandemia

                } else {

                    Toast.makeText(MainActivity.this, "Unchecked", Toast.LENGTH_SHORT).show();
                    
                    //parar de pegar posições
                }
            }
        });

        //-------------------------------------------------------------


        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this, "Conectado ao Servidor IoT", Toast.LENGTH_SHORT).show();
                    setSubscrition();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "Desconectado", Toast.LENGTH_SHORT).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            //-----------------------Função que recebe as mensagens do broker
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {


                if (topic.toString().equals("casa/temperatura")) {

                    txtTemperatura.setText(new String(message.getPayload()));


                    //Antes de escrever no firebase, aguarda 10 segundos para que a conexão com o
                    //ESP8266 e o CloudMQTT seja realizada
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            //Chama a função que escreve a data e os riscos
                            firebaseWrite();
                            //firebaseRead();

                        }
                    }, 10000);


                }
                if (topic.toString().equals("casa/umidade")) {
                    txtUmidade.setText(new String(message.getPayload()));
                }
                if (topic.toString().equals("casa/id")) {
                    txtId.setText(new String(message.getPayload()));


                }

                if (topic.toString().equals("casa/tir")) {
                    txtTIR.setText(new String(message.getPayload()));


                }

                if (topic.toString().equals("casa/bpm")) {
                    txtBPM.setText(new String(message.getPayload()));
                }

            }

            //-----------------------
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        //---------------------------Firebase


        btnGerarRelatorio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    exportCSV();

            }
        });

        btnGerarGrafico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), Grafico.class);
                startActivity(intent);

            }
        });


    }

    //Definir a inscrições
    private void setSubscrition() {
        try {
            client.subscribe(topicoTemperatura, 0);
            client.subscribe(topicoUmidade, 0);
            client.subscribe(topicoID, 0);
            client.subscribe(topicoBPM, 0);
            client.subscribe(topicoTIR, 0);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //Essa função só escreve no firebase caso haja alguma situação de risco
    private void firebaseWrite() {

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
        String currentDateandTimeRisco = sdf.format(new Date());




        if (( Double.parseDouble(txtTemperatura.getText().toString())  > 20 ) || ( Double.parseDouble(txtBPM.getText().toString())  > 300 ))  {

            contadorDeRisco++;
            //Log.d("RISCO", String.valueOf(contadorDeRisco));
            txtRisco.setText(String.valueOf(contadorDeRisco));

           this.key = myRef.push().getKey();


           Users users = new Users(txtId.getText().toString(), key,currentDateandTimeRisco.toString(),
                   txtTemperatura.getText().toString(),
                   txtBPM.getText().toString(),
                   txtRisco.getText().toString());

           myRef.child(txtId.getText().toString()).child(key).setValue(users);



        }

    }


    public void exportCSV() {
        {

             data.append("BPM, DataRisco, IDEsp82, Temperatura, Vezes,");
            FirebaseDatabase.getInstance().getReference().child("prontuario").child("uffdqqwl")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                 Users user = snapshot.getValue(Users.class);
                               // System.out.println("AQUI 2-------->"+user.getVezes());

                                arrValores.add(user.getBpm());
                                arrValores.add(user.getDatarisco());
                                arrValores.add(user.getIdESP82());
                                arrValores.add(user.getTemperatura());
                                arrValores.add(user.getVezes());



                            }


                            for(String i : arrValores){
                               // Log.d("AQUI VE ---->", i) ;
                                data.append(i+",");

                                if(arrValores.indexOf(i)%5 == 0){
                                    data.append("\n");
                                }



                                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                                whatsappIntent.setType("text/csv");
                                whatsappIntent.setPackage("com.whatsapp");
                                whatsappIntent.putExtra(Intent.EXTRA_TEXT, data.toString());
                                try {
                                    startActivity(whatsappIntent);
                                } catch (android.content.ActivityNotFoundException ex) {

                                }

                            }

                            Log.d("AQUI VE 2---->", data.toString()) ;

                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
        }

    }



    }




