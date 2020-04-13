package com.example.mqtt_controle;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class Grafico extends AppCompatActivity {


    BarChart mBarChart;
    String data;
    String risco;

    int indexVezes = 6;
    int indexDataRisco = 2;
    String[] resultadoDataRisco;
    String[] resultadoVezes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafico);

         mBarChart = (BarChart) findViewById(R.id.barchart);

        mBarChart.startAnimation();



                //Chama a função que escreve a data e os riscos
                firebaseRead();


    }

    private void firebaseRead() {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("prontuario").child("uffdqqwl");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
       final String currentDateandTimeRisco = sdf.format(new Date());

        mBarChart.addBar(new BarModel("2019", 5, 0xFF123456));
        mBarChart.addBar(new BarModel("2018", 3, 0xFF123456));
        mBarChart.addBar(new BarModel("2017", 9, 0xFF123456));
            ValueEventListener changeListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                        risco = String.valueOf(dataSnapshot.getChildrenCount());

                        resultadoDataRisco = dataSnapshot.getValue().toString().split("[=,]");

                       for (int i = 0; i < resultadoDataRisco.length - 1; i++) {

                            indexDataRisco = indexDataRisco + 11;
                            Log.d("AQUI------>", resultadoDataRisco[indexDataRisco]);
                                   mBarChart.addBar(new BarModel(currentDateandTimeRisco, Float.parseFloat(risco), 0xFF123456));
                                   break;
                     }


                   // Log.d("AQUI------>",dataSnapshot.getChildrenCount() + "");


                   // Log.d("aqui------------>", "Resultado: " +resultadoDataRisco[13]);


                //resultadoVezes = dataSnapshot.getValue().toString().split("[=,]");

                       /*try {
                        //pega o vetor inteiro


                        for (int i = 0; i <= resultadoDataRisco.length-2; i++) {
                            indexDataRisco = indexDataRisco + 13;
                            for (int j = 0; j <= resultadoVezes.length - 1; j++) {
                                mBarChart.addBar(new BarModel(resultadoDataRisco[indexDataRisco],Integer.parseInt(resultadoVezes[indexVezes]), 0xFF123456));

                                Log.d("aqui------------>", "Resultado J: " + resultadoVezes[indexVezes]);
                                indexVezes = indexVezes + 13;
                            }


                             Log.d("aqui------------>", "Resultado: " +resultadoDataRisco[indexDataRisco]);



                       }




                    }catch (Exception ex){
                        Log.d("Erro------------>", ex.toString());
                    }*/

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            myRef.addValueEventListener(changeListener);

    }




}
