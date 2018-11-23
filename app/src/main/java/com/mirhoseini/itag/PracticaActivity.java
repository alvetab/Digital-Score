package com.mirhoseini.itag;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PracticaActivity extends AppCompatActivity {
    BroadcastReceiver br = new PracticaActivity.MyBroadcastReceiver();
    public int currentButtonActionId = 0;
    public int currentButtonPressCount = 0;
    public long currentButtonPressTime = 0;
    public int currentButtonTimerActivationTime = 0;
    public int buttonPressMaxDelay =1000;
    public int buttonActionDelay =0;
    public int requiredButtonPressCount =2;
    public int cantidadDePulseras=1;
    public String[] lastpoint = new String[8];
    TextView fechaCompleta;
    TextView hora;
    TextView dia;
    TextView mes;
    TextView año;





    String tag = "Tennis Score Keeper";
    String player1, player2;
    Button btnStart, btnPause, btnStop, btnReset, resetAll; //button variable declaration
    TextView txtpracticaplayer1,txtpracticaplayer2,txtpracticacrono,player1wins, player2wins, player1Total, player2Total, player1Total1, player2Total1, player1Total2, player2Total2, outPut, textViewTimer; //text variable declaration
    public int counter1, counter2, counter3, counter4, counter5, counter6, hrs , min , sec, clickTime,
            player1_won_set, player2_won_set, player1_won_game, player2_won_game, player1_number_point, player2_number_point = 0; //integer variable declaration
    DBHandler cAdapter;
    Intent viewScores;

    final CounterClass timer = new CounterClass(87840000, 1000);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practica);


        txtpracticaplayer1 = (TextView) findViewById(R.id.practicaplayer1);
        txtpracticaplayer2 = (TextView) findViewById(R.id.practicaplayer2);
        txtpracticacrono = (TextView) findViewById(R.id.practicacrono);
        /*=====================================================================================*/

        /******************************Player Name(Start)***************************************/

        /*TextView player1name = (TextView) findViewById(R.id.player1name);
        TextView player2name = (TextView) findViewById(R.id.player2name);

        player1 = getIntent().getExtras().getString("player1name");
        player2 = getIntent().getExtras().getString("player2name");

        player1name.setText(getIntent().getExtras().getString("player1name")); //Set player 1 name
        player2name.setText(getIntent().getExtras().getString("player2name")); //Set player 2 name
        player1wins.setText("0");
        player2wins.setText("0");
        */
        /******************************Player Name(End)*****************************************/

        /*=====================================================================================*/

        /******************************Timer(Start)********************************************/

        //Button resetAll = (Button) findViewById(R.id.resetAll); // create button view
    /*      if(getIntent().getExtras().getString("reset") == "reset" ) { //if reset event pass
            resetAll.performClick();
        }
    */
     /*   btnStart = (Button) findViewById(R.id.btnStart);
        btnReset = (Button) findViewById(R.id.btnReset);
        textViewTimer = (TextView) findViewById(R.id.textViewTimer);
    */
        assert txtpracticacrono != null;
        txtpracticacrono.setText("00:00:00");

        //Timer Start/Pause button
      /*  btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickTime == 0) {
                    timer.start();
                    btnStart.setText("Pause");
                    clickTime = 1;
                } else if (clickTime == 1) {
                    timer.cancel();
                    btnStart.setText("Start");
                    clickTime = 0;
                }
            }
        });
        *///Timer reset button
        /*btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewTimer.setText("00:00:00");
                timer.cancel();
                btnStart.setText("Start");
                hrs = 0;  min = 0; sec = 0; clickTime = 0;
            }
        });

        cAdapter = new DBHandler(this);*/
    }

    public void deleteDatabase(){
        //String inputText = setInput.getText.toString();
        //dbHandler.deleteRecord(inputText);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public class CounterClass extends CountDownTimer {
        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override
        public void onTick(long millisUntilFinished) {

            txtpracticacrono.setText(String.format("%02d:%02d:%02d", hrs, min, sec));
            if(sec == 59){ sec = 00; min++; }
            else if(min == 59){ min = 00; hrs++; if(hrs == 24){ hrs = 00; } }
            else{ sec++; }

        }

        @Override
        public void onFinish() {

        }
    }

        /******************************Timer(End)***********************************************/

        /*=====================================================================================*/

        /******************************Game Back button(Start)**********************************/

        //Create method: by clicking this button can go back at player name activity
        public void gameBack(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to go back?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            PracticaActivity.this.finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

        /******************************Game Back button(End)************************************/

        /*=====================================================================================*/

        /******************************Player one won button(Start)*****************************/

        //Create method: by clicking this button won player 1 and check all conditions to make player 1 won
      public void PlayerOneWonpPressed(){
          MarcadorActual();
          txtpracticaplayer1 = (TextView) findViewById(R.id.practicaplayer1);
          int marcadorpplayer1prueba = Integer.parseInt(txtpracticaplayer1.getText().toString());

          marcadorpplayer1prueba++;

          txtpracticaplayer1.setText(marcadorpplayer1prueba + "");

        }

    /******************************Player one won button(End)*********************************/

    /*=======================================================================================*/

    /******************************Player two won button(Start)*******************************/

    //Create method: by clicking this button won player 2 and check all conditions to make player 2 won

     public void PlayerTwoWonPressed(){
        MarcadorActual();
         int marcadorpplayer2prueba = Integer.parseInt(txtpracticaplayer2.getText().toString());

         marcadorpplayer2prueba++;

         txtpracticaplayer2.setText(marcadorpplayer2prueba + "");
     }




        /******************************Player two won button(End)*******************************/

        /*=====================================================================================*/

        /******************************Store data into database (Start)************************************/
        public void saveData(String winner, int player1set, int player2set, int player1game, int player2game, int player1point, int player2point) {
            String player1name = player1;
            String player2name = player2;
            String time = String.valueOf(String.format("%02d:%02d:%02d", hrs, min, sec));

            cAdapter.open();
            long result = cAdapter.insertContact(player1name, player2name, winner, player1set, player2set, player1game, player2game, player1point, player2point, time);
            if(result < 0){
                showToast("Error Saving Data");
            }
            else{
                showToast("Data Saved Successfully");
            }
            cAdapter.close();

        }
        public void showToast(String message){
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
        /******************************Store data into database (End)************************************/

        /******************************View Score(Start)************************************/

        //Create method: To view record ( sets, games, points )
        public void viewScore(String winner, int player1set, int player2set, int player1game, int player2game, int player1point, int player2point, String save){
            Intent viewScores = new Intent(PracticaActivity.this, viewScores.class);
            viewScores.putExtra("winner", String.valueOf(winner));
            viewScores.putExtra("player1name", player1);
            viewScores.putExtra("player2name", player2);
            viewScores.putExtra("player1set", String.valueOf(player1set));
            viewScores.putExtra("player2set", String.valueOf(player2set));
            viewScores.putExtra("player1game", String.valueOf(player1game));
            viewScores.putExtra("player2game", String.valueOf(player2game));
            viewScores.putExtra("player1point", String.valueOf(player1point));
            viewScores.putExtra("player2point", String.valueOf(player2point));
            viewScores.putExtra("matchTime", String.valueOf(String.format("%02d:%02d:%02d", hrs, min, sec)));
            viewScores.putExtra("save", String.valueOf(save));
            //resetAll.performClick();
            startActivity(viewScores);
        }

        /***********************************View Scrore (End)***********************************************/

        /******************************View Score Mid time(Start)************************************/

        //Create method: To view record ( sets, games, points )
        public void viewScoreMidtime(){
            int first_final_score = Integer.parseInt(player1Total.getText().toString());
            int second_final_score = Integer.parseInt(player2Total.getText().toString());
            int first_final_score1 = Integer.parseInt(player1Total1.getText().toString());
            int second_final_score1 = Integer.parseInt(player2Total1.getText().toString());
            int first_final_score2 = Integer.parseInt(player1Total2.getText().toString());
            int second_final_score2 = Integer.parseInt(player2Total2.getText().toString());
            Intent viewScores = new Intent(PracticaActivity.this, viewScores.class);
            final int player1games = wonGames(first_final_score,first_final_score1,first_final_score2);
            final int player2games = wonGames(second_final_score,second_final_score1,second_final_score2);
            viewScores.putExtra("player1name", player1);
            viewScores.putExtra("player2name", player2);
            viewScores.putExtra("player1set", String.valueOf(player1_won_set));
            viewScores.putExtra("player2set", String.valueOf(player2_won_set));
            viewScores.putExtra("player1game", String.valueOf(player1games));
            viewScores.putExtra("player2game", String.valueOf(player2games));
            viewScores.putExtra("player1point", String.valueOf(player1_number_point));
            viewScores.putExtra("player2point", String.valueOf(player2_number_point));
            viewScores.putExtra("matchTime", String.valueOf(String.format("%02d:%02d:%02d", hrs, min, sec)));
            viewScores.putExtra("save", "no");
            startActivity(viewScores);
        }

        /***********************************View Scrore Mid time(End)***********************************************/

        ////////**********************************************************************************/////////////


        /*=====================================================================================*/

        /******************************Reset All button(Start)************************************/

        //Create method: by clicking this button set all value as default value
        public void resetAll(View v) {

            clickTime = 0;
            player1wins.setText("0"); player2wins.setText("0"); player1Total.setText("0"); player2Total.setText("0"); player1Total1.setText("0"); player2Total1.setText("0"); player1Total2.setText("0"); player2Total2.setText("0"); outPut.setText("");
            counter1 = 0; counter2 = 0; counter3 = 0; counter4 = 0; counter5 = 0; counter6 = 0; player1_won_set = 0; player2_won_set = 0; player1_won_game = 0; player2_won_game = 0; player1_number_point = 0; player2_number_point = 0;
            btnReset.post(new Runnable(){
                @Override
                public void run() {
                    btnReset.performClick();
                }
            });

        }
    public void resetAllPressed() {
        txtpracticaplayer1.setText("0");
        txtpracticaplayer2.setText("0");
        txtpracticacrono.setText("00:00:00");

    }

        /******************************Reset All button(End)************************************/

        /******************************Winning set count(Start)*********************************/
        private int wonGames(int set1, int set2, int set3) {
            int wonGames = set1 + set2 + set3;
            return wonGames;
        }
        /*******************************Winning set count(End)**********************************/

        /******************************Undo (Start)*********************************/
        public void functionUndoPressed(){
            txtpracticaplayer1.setText(lastpoint[0]);
            txtpracticaplayer2.setText(lastpoint[1]);
            }
        /******************************Undo (End)*********************************/

        //different auto call actions
        public void onStart(){
            super.onStart();
            Log.d(tag,"In the onStart() event");
        }

        public void onRestart(){
            super.onRestart();
            Log.d(tag,"In the onRestart() event");
        }

        public void onResume(){
            super.onResume();
            Log.d(tag,"In the onResume() event");
            IntentFilter filter = new IntentFilter("com.mirhoseini.itag.button_pressed");
            this.registerReceiver(br, filter);
        }

        public void onPause(){
            super.onPause();
            Log.d(tag,"In the onPause() event");
        }

        public void onStop(){
            super.onStop();
            Log.d(tag,"In the onStop() event");
        }

        public void onDestroy(){
            super.onDestroy();
            Log.d(tag,"In the onDestroy() event");

        }

        /******************************Save InstanceState while screen rotate (Start)*********************************/
        @Override
        protected void onSaveInstanceState(Bundle outState) {
            // Make sure to call the super method so that the states of our views are saved
            super.onSaveInstanceState(outState);
            // Save our own state now
            outState.putString("PLAYER1WIN", player1wins.getText().toString());
            outState.putString("PLAYER2WIN", player2wins.getText().toString());
            outState.putString("PLAYER1SET1", player1Total.getText().toString());
            outState.putString("PLAYER2SET1", player2Total.getText().toString());
            outState.putString("PLAYER1SET2", player1Total1.getText().toString());
            outState.putString("PLAYER2SET2", player2Total1.getText().toString());
            outState.putString("PLAYER1SET3", player1Total2.getText().toString());
            outState.putString("PLAYER2SET3", player2Total2.getText().toString());

            outState.putInt("HRS", hrs); outState.putInt("MIN", min); outState.putInt("SEC", sec);
            outState.putString("BTNSTART", btnStart.getText().toString());
            outState.putInt("COUNTER1", counter1);
            outState.putInt("COUNTER2", counter2);
            outState.putInt("COUNTER3", counter3);
            outState.putInt("COUNTER4", counter4);
            outState.putInt("COUNTER5", counter5);
            outState.putInt("COUNTER6", counter6);
            outState.putInt("PLAYER1_WON_SET", player1_won_set);
            outState.putInt("PLAYER2_WON_SET", player2_won_set);
            outState.putInt("PLAYER1_WON_GAME", player1_won_game);
            outState.putInt("PLAYER2_WON_GAME", player2_won_game);
            outState.putInt("PLAYER1_NUMBER_POINT", player1_number_point);
            outState.putInt("PLAYER2_NUMBER_POINT", player2_number_point);
        }
        /******************************Save InstanceState while screen rotate (End)*********************************/

        /******************************Restore InstanceState while screen rotate (Start)*********************************/
        @Override
        protected void onRestoreInstanceState(Bundle savedInstanceState) {
            // restore data from Savedinstance
            super.onRestoreInstanceState(savedInstanceState);
        }
        /******************************Restore InstanceState while screen rotate (Start)*********************************/

    /******************************Dropdown Menu (Start)*********************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_score) {
            viewScoreMidtime();
            return true;
        }
        if (id == R.id.action_help) {
            Intent helpGame = new Intent(PracticaActivity.this, helpGame.class);
            startActivity(helpGame);
            return true;
        }
        if (id == R.id.action_history) {
            Intent viewRecord = new Intent(PracticaActivity.this, viewRecords.class);
            startActivity(viewRecord);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /******************************Dropdown Menu (End)*********************************/
    public class MyBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "MyBroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            /*StringBuilder sb = new StringBuilder();
            sb.append("Action: " + intent.getAction() + "\n");
            sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
            String log = sb.toString();
            Log.d(TAG, log);
            Toast.makeText(context, log, Toast.LENGTH_LONG).show();*/
            onKeyPressed(context);
        }
    }
    public void onKeyPressed(Context context) {

        if (cantidadDePulseras==1){
            long timeSinceLastPress = System.currentTimeMillis() - currentButtonPressTime;

            if ((currentButtonPressTime == 0L) || (timeSinceLastPress < buttonPressMaxDelay)) {
                currentButtonPressCount++;
                //String log = "apretado";
                //Toast.makeText(context, log, Toast.LENGTH_LONG).show();
                currentButtonPressTime = System.currentTimeMillis();
            }


            else {/*
                if (currentButtonPressCount == 1) {
                printLog("Buton 1 vez presionado!!")
                currentButtonActionId=1;
                currentButtonPressCount = 0
                currentButtonPressTime =0

                }
                else if (currentButtonPressCount == 2) {
                printLog("Buton 2 veces presionado!!")
                currentButtonActionId=2;
                currentButtonPressCount = 0
                currentButtonPressTime =0

                }
                else if (currentButtonPressCount == 3) {
                printLog("Buton 3 veces presionado!!")
                currentButtonActionId=3;
                currentButtonPressCount = 0
                currentButtonPressTime =0

                }
                else if (currentButtonPressCount == 4) {
                printLog("Buton 4 veces presionado!!")
                currentButtonActionId=4;
                currentButtonPressCount = 0
                currentButtonPressTime =0
                }

                else {
                printLog("Buton presionado mas de 4 veces!!")
                currentButtonActionId = 5
                currentButtonPressCount =0;
                currentButtonPressTime =0
                //Log.i(TAG, "Botton presionado!!!")
                //val intent = Intent("com.mirhoseini.itag.button_pressed")
                //sendBroadcast(intent)

                }*/

                if (System.currentTimeMillis() > currentButtonPressTime+5000){
                    currentButtonPressCount=1;
                    currentButtonPressTime = System.currentTimeMillis();
                }
                else {
                    //String log="boton validado y pulsado " + currentButtonPressCount + " veces";
                    //Toast.makeText(context, log, Toast.LENGTH_LONG).show();
                    if(currentButtonPressCount==1){
                    PlayerOneWonpPressed();}

                   else if(currentButtonPressCount==2){
                        PlayerTwoWonPressed();}

                   else if(currentButtonPressCount==3){
                        timer.start();
                        }

                    else if(currentButtonPressCount==4){
                        timer.cancel();

                    }

                        else if(currentButtonPressCount==5){txtpracticacrono.setText("00:00:00");
                        timer.cancel();
                        hrs = 0;  min = 0; sec = 0; clickTime = 0;
                        }

                        else if(currentButtonPressCount==6){
                            resetAllPressed();
                        }
                    else if(currentButtonPressCount==7){
                        functionUndoPressed();
                    }

                    currentButtonPressCount = 0;
                    currentButtonPressTime = 0;


                }

            }


        }







    }
    public void MarcadorActual(){
        String juegoj1 = txtpracticaplayer1.getText().toString();
        String juegoj2 = txtpracticaplayer2.getText().toString();

        lastpoint[0]=juegoj1;lastpoint[1]=juegoj2;
    }
   /* final Runnable HoraenMarcador =new Runnable() {
        @Override
        public void run() { Date d=new Date();
        hora= (TextView) findViewById(R.id.txtHora);
        SimpleDateFormat ho=new SimpleDateFormat("h:mm a");
        String horaString = ho.format(d);
        hora.setText(horaString);

    }};*/

}