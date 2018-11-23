package com.mirhoseini.itag;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class SecondActivity extends AppCompatActivity {
    String prefsName = "Player 1";

    //Calendar calendario = Calendar.getInstance();
    BroadcastReceiver br = new SecondActivity.MyBroadcastReceiver();
    public static final long PERIODO = 15000; // 60 segundos (60 * 1000 millisegundos)
    private Handler handler;
    private Runnable runnable;
    public int currentButtonActionId = 0;
    public int currentButtonPressCount = 0;
    public long currentButtonPressTime = 0;
    public int currentButtonTimerActivationTime = 0;
    public int buttonPressMaxDelay =1500;
    public int buttonPressMaxDelayreset =3000;
    public int buttonActionDelay =0;
    public int requiredButtonPressCount =2;
    public int cantidadDePulseras=2;
    public int cantidadDePulsaciones=0;//0 para 1-click 1-para 2 clicks
    public String[] lastpoint = new String[8];
    public int colorpulsador =633333;
    TextView fechaCompleta;
    TextView hora;
    TextView dia;
    TextView mes;
    TextView año;
    private static String GROVE_SERVICE = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
    private static String CHARACTERISTIC_TX = "beb5483e-36e1-4688-b7f5-ea07361b26a8";
    private static String CHARACTERISTIC_RX = "beb5483f-36e1-4688-b7f5-ea07361b26a8";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000; //5 seconds
    private static final String DEVICE_NAME = "MyESP32"; //display name for Grove BLE

    private BluetoothAdapter mBluetoothAdapter;//our local adapter
    private BluetoothGatt mBluetoothGatt; //provides the GATT functionality for communication
    private BluetoothGattService mBluetoothGattService; //service on mBlueoothGatt
    private static List<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>();//discovered devices in range
    private BluetoothDevice mDevice; //external BLE device (Grove BLE module)

    private TextView mainText;

    private Timer mTimer;





    String tag = "Tennis Score Keeper";
    String player1, player2;
    Button btnStart, btnPause, btnStop, btnReset, resetAll,player1btn,player2btn; //button variable declaration
    TextView player1wins, player2wins, player1Total, player2Total, player1Total1, player2Total1, player1Total2, player2Total2, outPut, textViewTimer; //text variable declaration
    public int counter1, counter2, counter3, counter4, counter5, counter6, hrs , min , sec, clickTime, clickchange,
            player1_won_set, player2_won_set, player1_won_game, player2_won_game, player1_number_point, player2_number_point = 0; //integer variable declaration
    DBHandler cAdapter;
    Intent viewScores;

    final CounterClass timer = new CounterClass(87840000, 1000);
    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //captura la mac guardada
        SharedPreferences prefs = getSharedPreferences(prefsName, MODE_PRIVATE);
        String MACmarcador = prefs.getString("MAC_marcador", "VACIO");
        //fin


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        mainText = (TextView) findViewById(R.id.mainText);
        mainText.setText("Hello Bluetooth LE!");

        mTimer = new Timer();

        //check to see if Bluetooth Low Energy is supported on this device
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        }

        statusUpdate("BLE supported on this device");

        //get a reference to the Bluetooth Manager
        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "BLE not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //Open settings if Bluetooth isn't enabled
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth disabled", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //try to find the Grove BLE V1 module
        //connectDevice("30:AE:A4:84:A0:FE");
        connectDevice(MACmarcador);
        mHandler.postDelayed(new Runnable() {
            public void run() {
                datosMarcador();
            }
        }, 5000);

       // searchForDevices();


        player1wins = (TextView) findViewById(R.id.player1Txt);
        player2wins = (TextView) findViewById(R.id.player2Txt);
        player1Total = (TextView) findViewById(R.id.player1Total);
        player2Total = (TextView) findViewById(R.id.player2Total);
        player1Total1 = (TextView) findViewById(R.id.player1Total1);
        player2Total1 = (TextView) findViewById(R.id.player2Total1);
        player1Total2 = (TextView) findViewById(R.id.player1Total2);
        player2Total2 = (TextView) findViewById(R.id.player2Total2);
        outPut = (TextView) findViewById(R.id.outPut);
        player1btn =(Button) findViewById(R.id.player1Btn);
        player2btn =(Button) findViewById(R.id.player2Btn);
        resetAll = (Button) findViewById(R.id.resetAll); // create button view

        /*=====================================================================================*/

        /******************************Player Name(Start)***************************************/

        TextView player1name = (TextView) findViewById(R.id.player1name);
        TextView player2name = (TextView) findViewById(R.id.player2name);

        player1 = getIntent().getExtras().getString("player1name");
        player2 = getIntent().getExtras().getString("player2name");

        player1name.setText(getIntent().getExtras().getString("player1name")); //Set player 1 name
        player2name.setText(getIntent().getExtras().getString("player2name")); //Set player 2 name
        player1wins.setText("0");
        player2wins.setText("0");

        /******************************Player Name(End)*****************************************/

        /*=====================================================================================*/

        /******************************Timer(Start)********************************************/

        //Button resetAll = (Button) findViewById(R.id.resetAll); // create button view
        if(getIntent().getExtras().getString("reset") == "reset" ) { //if reset event pass
            resetAll.performClick();
        }

        btnStart = (Button) findViewById(R.id.btnStart);
        btnReset = (Button) findViewById(R.id.btnReset);
        textViewTimer = (TextView) findViewById(R.id.textViewTimer);

        assert textViewTimer != null;
        textViewTimer.setText("00:00:00");

        //Timer Start/Pause button
        btnStart.setOnClickListener(new View.OnClickListener() {
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
        //Timer reset button
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewTimer.setText("00:00:00");
                timer.cancel();
                btnStart.setText("Start");
                hrs = 0;  min = 0; sec = 0; clickTime = 0;
            }
        });

        cAdapter = new DBHandler(this);
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

            textViewTimer.setText(String.format("%02d:%02d:%02d", hrs, min, sec));
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
                        SecondActivity.this.finish();
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
    public void PlayerOneWonClicked(View v){
        MarcadorActual();
        // to toast winnig declaration
        player1_number_point++;
        String win_text = getIntent().getExtras().getString("player1name")+" won";
        Context context = getApplicationContext(); // gets the application context to pass to Toast
        int duration = Toast.LENGTH_SHORT; // define length of toast
        Toast toast = Toast.makeText(context, win_text, duration); // crete an object of toast class
        // to toast winnig declaration, end

        final MediaPlayer mp = MediaPlayer.create(this, R.raw.win); //create media player class object

        // get the value of particular place which find by place id
        String score1 = player1wins.getText().toString();
        String score2 = player2wins.getText().toString();
        int first_final_score = Integer.parseInt(player1Total.getText().toString());
        int second_final_score = Integer.parseInt(player2Total.getText().toString());
        int first_final_score1 = Integer.parseInt(player1Total1.getText().toString());
        int second_final_score1 = Integer.parseInt(player2Total1.getText().toString());
        int first_final_score2 = Integer.parseInt(player1Total2.getText().toString());
        int second_final_score2 = Integer.parseInt(player2Total2.getText().toString());
        int total1 = first_final_score + 1; int total2 = first_final_score1 + 1; int total3 = first_final_score2 + 1;
        final Button resetAll = (Button) findViewById(R.id.resetAll); // create button view
        TextView player1name = (TextView) findViewById(R.id.player1name); //getPlayer one name text field

        // check for tie break of set first
        if(first_final_score == 6 && second_final_score == 6)
        {
            counter1++;
            player1_number_point++;
            int x = Integer.parseInt(score2);
            if(counter1 == 7 && (x <= 5)){
                toast.show(); //toast when player one wins
                player1Total.setText("" + total1); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player1name")+" won the first set");
                player1_won_set++; //set1 =(7,6)
                ///outPut.setText(player1_won_set+", 1A");
                player1wins.setText("0");
                player2wins.setText("0");
            }
            else if(counter1 > x+1 && (x >= 6)){
                toast.show(); //toast when player one wins
                player1_won_set++;
                player1Total.setText("" + total1); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player1name")+" won the first set");
                ///outPut.setText(player1_won_set+", 1B");
                player1wins.setText("0");
                player2wins.setText("0");
            }
            else
            {
                player1wins.setText(String.valueOf(counter1));
            }
        }
        else if(first_final_score1 == 6 && second_final_score1 == 6)
        {
            // check for tie break of set second
            counter2++;
            player1_number_point++;
            int x = Integer.parseInt(score2);
            if(counter2 == 7 && (x <= 5)){
                toast.show(); //toast when player one wins
                player1Total1.setText("" + total2); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player1name")+" won the second set");
                player1_won_set++;
                ///outPut.setText(player1_won_set+", 1C");
                player1wins.setText("0");
                player2wins.setText("0");
                // check conditions to find final set won by player one
                if((first_final_score == 6 && total2 == 6 && second_final_score1 < 6 && second_final_score != 7 && second_final_score1 != 5) || (first_final_score == 6 && total2 == 7 && second_final_score1 == 5 && second_final_score != 7) || (first_final_score == 7 && total2 == 6 && second_final_score1 < 5) || (first_final_score == 6 && total2 == 7 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 6 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 5 && second_final_score1 == 5) || (first_final_score == 7 && total2 == 7 && second_final_score == 5 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 6 && second_final_score1 == 5))
                {

                    toast.show(); //toast when player one wins
                    final int player1games = wonGames(first_final_score,total2,first_final_score2);
                    final int player2games = wonGames(second_final_score,second_final_score1,second_final_score2);
                    outPut.setText(getIntent().getExtras().getString("player1name")+" won the match!");
                    ////player1_won_set++; //set2 =(7,6)
                    ///outPut.setText(player1_won_set+", 1D");

                    // for dialogue box start
                    AlertDialog dialog = null; // This will be the returning AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                    builder.setTitle("You won the match!");
                    builder.setIcon(R.drawable.win_match);
                    builder.setCancelable(false);
                    builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel(); //changes require
                            saveData(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                            viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                        }
                    });
                    builder.setMessage(
                            getIntent().getExtras().getString("player1name")+" won the match!"
                    ).setCancelable(false) // disallow user to hit the 'back' button
                            .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    dialog = builder.create(); // now the builder is ready to create the dialog
                    timer.cancel();
                    dialog.show();
                    // for dialogue box end
                    mp.start(); // winning music play

                }
            }
            else if(counter2 > x+1 && (x >= 6)){
                toast.show(); //toast when player one wins
                player1Total1.setText("" + total2); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player1name")+" won the first set");
                player1_won_set++;
                ///outPut.setText(player1_won_set+", 1E");
                player1wins.setText("0");
                player2wins.setText("0");
                // check conditions to find final set won by player one
                if((first_final_score == 6 && total2 == 6 && second_final_score1 < 6 && second_final_score != 7 && second_final_score1 != 5) || (first_final_score == 6 && total2 == 7 && second_final_score1 == 5 && second_final_score != 7) || (first_final_score == 7 && total2 == 6 && second_final_score1 < 5) || (first_final_score == 6 && total2 == 7 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 6 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 5 && second_final_score1 == 5) || (first_final_score == 7 && total2 == 7 && second_final_score == 5 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 6 && second_final_score1 == 5))
                {

                    toast.show(); //toast when player one wins
                    final int player1games = wonGames(first_final_score,total2,first_final_score2);
                    final int player2games = wonGames(second_final_score,second_final_score1,second_final_score2);
                    outPut.setText(getIntent().getExtras().getString("player1name")+" won the match!");
                    ///player1_won_set++;
                    ///outPut.setText(player1_won_set+", 1F");

                    // for dialogue box start
                    AlertDialog dialog = null; // This will be the returning AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                    builder.setTitle("You won the match!");
                    builder.setIcon(R.drawable.win_match);
                    builder.setCancelable(false);
                    builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel(); //changes require
                            saveData(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                            viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                        }
                    });
                    builder.setMessage(
                            getIntent().getExtras().getString("player1name")+" won the match!"
                    )       .setCancelable(false) // disallow user to hit the 'back' button
                            .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    dialog = builder.create(); // now the builder is ready to create the dialog
                    timer.cancel();
                    dialog.show();
                    // for dialogue box end
                    mp.start(); // winning music play

                }
            }
            else
            {
                player1wins.setText(String.valueOf(counter2));
            }
        }
        else if(first_final_score2 == 6 && second_final_score2 == 6)
        {
            // check for tie break of set final
            counter3++;
            player1_number_point++;
            int x = Integer.parseInt(score2);
            if(counter3 == 7 && (x <= 5)){
                toast.show(); //toast when player one wins
                player1Total2.setText("" + total3); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player1name")+" won the second set");
                player1_won_set++;
                ///outPut.setText(player1_won_set+", 1G");
                player1wins.setText("0");
                player2wins.setText("0");
                if((total3 == 6 && second_final_score2 < 5) || (total3 == 7 && second_final_score2 == 5) || (total3 == 7 && second_final_score2 == 6)) {

                    toast.show(); //toast when player one wins
                    final int player1games = wonGames(first_final_score,first_final_score1,total3);
                    final int player2games = wonGames(second_final_score,second_final_score1,second_final_score2);
                    outPut.setText(getIntent().getExtras().getString("player1name")+" won the match!");
                    ///player1_won_set++; //set3 =(7,6)
                    ///outPut.setText(player1_won_set+", 1H");
                    //for dialogue box start
                    AlertDialog dialog = null; // This will be the returning AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                    builder.setTitle("You won the match!");
                    builder.setIcon(R.drawable.win_match);
                    builder.setCancelable(false);
                    builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel(); //changes require
                            saveData(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                            viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                        }
                    });
                    builder.setMessage(
                            getIntent().getExtras().getString("player1name")+" won the match!"
                    )       .setCancelable(false) // disallow user to hit the 'back' button
                            .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    dialog = builder.create(); // builder create the dialog
                    timer.cancel();
                    dialog.show();
                    //for dialogue box end
                    mp.start(); //winning music play
                }
            }
            else if(counter3 > x+1 && (x >= 6)){
                toast.show(); //toast when player one wins
                player1Total2.setText("" + total3); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player1name")+" won the first set");
                player1_won_set++;
                ///outPut.setText(player1_won_set+", 1I");
                player1wins.setText("0");
                player2wins.setText("0");
                if((total3 == 6 && second_final_score2 < 5) || (total3 == 7 && second_final_score2 == 5) || (total3 == 7 && second_final_score2 == 6)) {

                    toast.show(); //toast when player one wins
                    final int player1games = wonGames(first_final_score,first_final_score1,total3);
                    final int player2games = wonGames(second_final_score,second_final_score1,second_final_score2);
                    outPut.setText(getIntent().getExtras().getString("player1name")+" won the match!");
                    ///player1_won_set++;
                    ///outPut.setText(player1_won_set+", 1J");
                    //for dialogue box start
                    AlertDialog dialog = null; // This will be the returning AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                    builder.setTitle("You won the match!");
                    builder.setIcon(R.drawable.win_match);
                    builder.setCancelable(false);
                    builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel(); //changes require
                            saveData(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                            viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                        }
                    });
                    builder.setMessage(
                            getIntent().getExtras().getString("player1name")+" won the match!"
                    )       .setCancelable(false) // disallow user to hit the 'back' button
                            .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    dialog = builder.create(); // builder create the dialog
                    timer.cancel();
                    dialog.show();
                    //for dialogue box end
                    mp.start(); //winning music play
                }
            }
            else
            {
                player1wins.setText(String.valueOf(counter3));
            }
        }
        else if (score1=="0")
        {
            player1wins.setText("15");
        }
        else if (score1=="15")
        {
            player1wins.setText("30");
        }
        else if (score1=="30")
        {
            player1wins.setText("40");
        }
        else if (score1=="40" && score2!="AD" && score2!="40")
        {
            // check conditions to find first set won by player one
            if ((((first_final_score <= 5 && second_final_score != 7) && second_final_score != 6 && (first_final_score1 == 0 && second_final_score1 == 0)) || ((first_final_score == 6) && (second_final_score == 6))) || (second_final_score == 5 && first_final_score == 6) || (second_final_score == 6 && first_final_score == 5))
            {
                if(first_final_score1 == 0 && second_final_score1 == 0) {
                    player1wins.setText("0");
                    player2wins.setText("0");
                    player1Total.setText("" + total1); //increase value when player one won games of first set
                    if((total1 == 6 && second_final_score < 5) || (total1 == 7 && second_final_score == 6) || (total1 == 7 && second_final_score == 5)) {
                        toast.show(); //toast when player one wins
                        outPut.setText(getIntent().getExtras().getString("player1name")+" won the first set");
                        player1_won_set++; //set1 =(6,<=4)(7,5)(7,6)() //if set1 is eq 6 //if set1 is eq 7
                        ///outPut.setText(player1_won_set+", 1K");
                    }
                }
            }
            else if ((((first_final_score1 == 5 && second_final_score1 != 7) && (first_final_score2 == 0 && second_final_score2 == 0)) || ((first_final_score1 < 5) && (second_final_score1 != 6)) || ((first_final_score1 == 6) && (second_final_score1 == 6))) || (second_final_score1 == 5 && first_final_score1 == 6))
            {
                if (first_final_score2 == 0 && second_final_score2 == 0) {

                    player1wins.setText("0");
                    player2wins.setText("0");
                    player1Total1.setText("" + total2);  //increase value when player one won games of second set

                    // check conditions to find second set won by player one
                    if((total2 == 6 && second_final_score1 < 5) || (total2 == 7 && second_final_score1 == 6) || (total2 == 7 && second_final_score1 == 5) || (total2 == 6 && second_final_score1 == 7) || (total2 == 5 && second_final_score1 == 7)) {

                        toast.show(); //toast when player one wins
                        outPut.setText(getIntent().getExtras().getString("player1name")+" won the second set");
                        player1_won_set++;
                        ///outPut.setText(player1_won_set+", 1L");

                    }
                    // check conditions to find final set won by player one
                    if((first_final_score == 6 && total2 == 6 && second_final_score1 < 6 && second_final_score == 7 && second_final_score1 != 5)
                            || (first_final_score == 6 && total2 == 6 && second_final_score1 < 5 && second_final_score <= 5)
                            || (first_final_score == 6 && total2 == 7 && second_final_score1 == 5 && second_final_score != 7) || (first_final_score == 7 && total2 == 6 && second_final_score1 < 5) || (first_final_score == 6 && total2 == 7 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 6 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 5 && second_final_score1 == 5) || (first_final_score == 7 && total2 == 7 && second_final_score == 5 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 6 && second_final_score1 == 5))
                    {

                        toast.show(); //toast when player one wins
                        final int player1games = wonGames(first_final_score,total2,first_final_score2);
                        final int player2games = wonGames(second_final_score,second_final_score1,second_final_score2);
                        outPut.setText(getIntent().getExtras().getString("player1name")+" won the match!");
                        ////player1_won_set++; //set2 =(6,<=4)(7,5) //(not require to increase cause already increased)
                        ///outPut.setText(player1_won_set+", 1M");

                        // for dialogue box start
                        AlertDialog dialog = null; // This will be the returning AlertDialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                        builder.setTitle("You won the match!");
                        builder.setIcon(R.drawable.win_match);
                        builder.setCancelable(false);
                        builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //changes require
                                saveData(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                                viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                            }
                        });
                        builder.setMessage(
                                getIntent().getExtras().getString("player1name")+" won the match!"
                        )       .setCancelable(false) // disallow user to hit the 'back' button
                                .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        dialog = builder.create(); // now the builder is ready to create the dialog
                        timer.cancel();
                        dialog.show();
                        // for dialogue box end
                        mp.start(); // winning music play

                    }
                }
            }
            // check conditions to find final set won by player one
            else if ((first_final_score2 == 5 && second_final_score2 != 7) || (first_final_score2 == 5 && second_final_score2 == 6) || (first_final_score2 < 5 && second_final_score2 != 6) || (first_final_score2 == 6 && second_final_score2 != 7) || (first_final_score2 == 6 && second_final_score2 == 5) || ((first_final_score2 == 6) && (second_final_score2 == 6)))
            {
                if (second_final_score2 <= 7) {

                    player1wins.setText("0");
                    player2wins.setText("0");
                    player1Total2.setText("" + total3);  //increase value when player one won games of third set
                    if((total3 == 6 && second_final_score2 < 5) || (total3 == 7 && second_final_score2 == 5) || (total3 == 7 && second_final_score2 == 6)) {

                        toast.show(); //toast when player one wins
                        final int player1games = wonGames(first_final_score,first_final_score1,total3);
                        final int player2games = wonGames(second_final_score,second_final_score1,second_final_score2);
                        outPut.setText(getIntent().getExtras().getString("player1name")+" won the match!");
                        player1_won_set++; //set3 =(6,<=4)(7,5)
                        ///outPut.setText(player1_won_set+", 1N");
                        //for dialogue box start
                        AlertDialog dialog = null; // This will be the returning AlertDialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                        builder.setTitle("You won the match!");
                        builder.setIcon(R.drawable.win_match);
                        builder.setCancelable(false);
                        builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //changes require
                                saveData(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                                viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                            }
                        });
                        builder.setMessage(
                                getIntent().getExtras().getString("player1name")+" won the match!"
                        )       .setCancelable(false) // disallow user to hit the 'back' button
                                .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        dialog = builder.create(); // builder create the dialog
                        timer.cancel();
                        dialog.show();
                        //for dialogue box end
                        mp.start(); //winning music play
                    }

                }
            }
        }
        else if(score2 == "40" && score1 == "40")
        {
            player1wins.setText("AD");
        }
        else if(score2=="AD")
        {
            player2wins.setText("40"); player1wins.setText("40");
        }
        else if(score1=="AD"){
            if ((((first_final_score <= 5 && second_final_score != 7) && second_final_score != 6 && (first_final_score1 == 0 && second_final_score1 == 0)) || ((first_final_score == 6) && (second_final_score == 6))) || (second_final_score == 5 && first_final_score == 6) || (second_final_score == 6 && first_final_score == 5))
            {
                if(first_final_score1 == 0 && second_final_score1 == 0) {
                    player1wins.setText("0");
                    player2wins.setText("0");
                    player1Total.setText("" + total1); //increase value when player one wins first set
                    if((total1 == 6 && second_final_score < 5) || (total1 == 7 && second_final_score == 6) || (total1 == 7 && second_final_score == 5)) {
                        toast.show(); //toast when player one wins
                        outPut.setText(getIntent().getExtras().getString("player1name")+" won the first set");
                        ///player1_won_set++;
                        ///outPut.setText(player1_won_set+", 1O");
                    }
                }
            }
            else if ((((first_final_score1 == 5 && second_final_score1 != 7) && (first_final_score2 == 0 && second_final_score2 == 0)) || ((first_final_score1 < 5) && (second_final_score1 != 6)) || ((first_final_score1 == 6) && (second_final_score1 == 6))) || (second_final_score1 == 5 && first_final_score1 == 6))
            {
                if (first_final_score2 == 0 && second_final_score2 == 0) {
                    player1wins.setText("0");
                    player2wins.setText("0");
                    player1Total1.setText("" + total2); //increase value when player one wins second set
                    if((total2 == 6 && second_final_score1 < 5) || (total2 == 7 && second_final_score1 == 6) || (total2 == 7 && second_final_score1 == 5) || (total2 == 6 && second_final_score1 == 7) || (total2 == 5 && second_final_score1 == 7)) {

                        toast.show(); //toast when player one wins
                        outPut.setText(getIntent().getExtras().getString("player1name")+" won the second set");
                        ///player1_won_set++;
                        ///outPut.setText(player1_won_set+", 1P");

                    }
                    if((first_final_score == 6 && total2 == 6 && second_final_score1 < 6 && second_final_score == 7 && second_final_score1 != 5)
                            || (first_final_score == 6 && total2 == 6 && second_final_score1 < 5 && second_final_score <= 5)
                            || (first_final_score == 6 && total2 == 7 && second_final_score1 == 5 && second_final_score != 7) || (first_final_score == 7 && total2 == 6 && second_final_score1 < 5) || (first_final_score == 6 && total2 == 7 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 6 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 5 && second_final_score1 == 5) || (first_final_score == 7 && total2 == 7 && second_final_score == 5 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 6 && second_final_score1 == 5))
                    {

                        toast.show(); //toast when player one wins
                        final int player1games = wonGames(first_final_score,total2,first_final_score2);
                        final int player2games = wonGames(second_final_score,second_final_score1,second_final_score2);
                        outPut.setText(getIntent().getExtras().getString("player1name")+" won the match!");
                        ///player1_won_set++;
                        ///outPut.setText(player1_won_set+", 1Q");
                        //for dialogue box start
                        AlertDialog dialog = null; // This will be the returning AlertDialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                        builder.setTitle("You won the match!");
                        builder.setIcon(R.drawable.win_match);
                        builder.setCancelable(false);
                        builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //changes require
                                saveData(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                                viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                            }
                        });
                        builder.setMessage(
                                getIntent().getExtras().getString("player1name")+" won the match!"
                        )       .setCancelable(false) // disallow user to hit the 'back' button
                                .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        dialog = builder.create(); // now the builder is ready to create the dialog
                        timer.cancel();
                        dialog.show();
                        //for dialogue box end
                        mp.start(); //winning music play

                    }
                }
            }
            else if ((first_final_score2 == 5 && second_final_score2 != 7) || (first_final_score2 == 5 && second_final_score2 == 6) || (first_final_score2 < 5 && second_final_score2 != 6) || (first_final_score2 == 6 && second_final_score2 != 7) || (first_final_score2 == 6 && second_final_score2 == 5) || ((first_final_score2 == 6) && (second_final_score2 == 6)))
            {
                if (second_final_score2 <= 7) {
                    player1wins.setText("0");
                    player2wins.setText("0");
                    player1Total2.setText("" + total3); //increase value when player one wins first set
                    if((total3 == 6 && second_final_score2 < 5) || (total3 == 7 && second_final_score2 == 5) || (total3 == 7 && second_final_score2 == 6)) {

                        toast.show(); //toast when player one wins
                        final int player1games = wonGames(first_final_score,first_final_score1,total3);
                        final int player2games = wonGames(second_final_score,second_final_score1,second_final_score2);
                        outPut.setText(getIntent().getExtras().getString("player1name")+" won the match!");
                        ///player1_won_set++;
                        ///outPut.setText(player1_won_set+", 1R");
                        //for dialogue box start
                        AlertDialog dialog = null; // This will be the returning AlertDialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                        builder.setTitle("You won the match!");
                        builder.setIcon(R.drawable.win_match);
                        builder.setCancelable(false);
                        builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //changes require
                                saveData(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                                viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                            }
                        });
                        builder.setMessage(
                                getIntent().getExtras().getString("player1name")+" won the match!"
                        )       .setCancelable(false) // disallow user to hit the 'back' button
                                .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        dialog = builder.create(); // builder create the dialog
                        timer.cancel();
                        dialog.show();
                        //for dialogue box end
                        mp.start(); //winning music play
                    }

                }
            }
        }
        else{ player1wins.setText("0"); player2wins.setText("0"); }
        datosMarcador();
    }
    public void PlayerOneWonPressed(){
        MarcadorActual();
        // to toast winnig declaration
        player1_number_point++;
        String win_text = getIntent().getExtras().getString("player1name")+" won";
        Context context = getApplicationContext(); // gets the application context to pass to Toast
        int duration = Toast.LENGTH_SHORT; // define length of toast
        Toast toast = Toast.makeText(context, win_text, duration); // crete an object of toast class
        // to toast winnig declaration, end

        final MediaPlayer mp = MediaPlayer.create(this, R.raw.win); //create media player class object

        // get the value of particular place which find by place id
        String score1 = player1wins.getText().toString();
        String score2 = player2wins.getText().toString();
        int first_final_score = Integer.parseInt(player1Total.getText().toString());
        int second_final_score = Integer.parseInt(player2Total.getText().toString());
        int first_final_score1 = Integer.parseInt(player1Total1.getText().toString());
        int second_final_score1 = Integer.parseInt(player2Total1.getText().toString());
        int first_final_score2 = Integer.parseInt(player1Total2.getText().toString());
        int second_final_score2 = Integer.parseInt(player2Total2.getText().toString());
        int total1 = first_final_score + 1; int total2 = first_final_score1 + 1; int total3 = first_final_score2 + 1;
        final Button resetAll = (Button) findViewById(R.id.resetAll); // create button view
        TextView player1name = (TextView) findViewById(R.id.player1name); //getPlayer one name text field

        // check for tie break of set first
        if(first_final_score == 6 && second_final_score == 6)
        {
            counter1++;
            player1_number_point++;
            int x = Integer.parseInt(score2);
            if(counter1 == 7 && (x <= 5)){
                toast.show(); //toast when player one wins
                player1Total.setText("" + total1); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player1name")+" won the first set");
                player1_won_set++; //set1 =(7,6)
                ///outPut.setText(player1_won_set+", 1A");
                player1wins.setText("0");
                player2wins.setText("0");
            }
            else if(counter1 > x+1 && (x >= 6)){
                toast.show(); //toast when player one wins
                player1_won_set++;
                player1Total.setText("" + total1); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player1name")+" won the first set");
                ///outPut.setText(player1_won_set+", 1B");
                player1wins.setText("0");
                player2wins.setText("0");
            }
            else
            {
                player1wins.setText(String.valueOf(counter1));
            }
        }
        else if(first_final_score1 == 6 && second_final_score1 == 6)
        {
            // check for tie break of set second
            counter2++;
            player1_number_point++;
            int x = Integer.parseInt(score2);
            if(counter2 == 7 && (x <= 5)){
                toast.show(); //toast when player one wins
                player1Total1.setText("" + total2); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player1name")+" won the second set");
                player1_won_set++;
                ///outPut.setText(player1_won_set+", 1C");
                player1wins.setText("0");
                player2wins.setText("0");
                // check conditions to find final set won by player one
                if((first_final_score == 6 && total2 == 6 && second_final_score1 < 6 && second_final_score != 7 && second_final_score1 != 5) || (first_final_score == 6 && total2 == 7 && second_final_score1 == 5 && second_final_score != 7) || (first_final_score == 7 && total2 == 6 && second_final_score1 < 5) || (first_final_score == 6 && total2 == 7 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 6 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 5 && second_final_score1 == 5) || (first_final_score == 7 && total2 == 7 && second_final_score == 5 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 6 && second_final_score1 == 5))
                {

                    toast.show(); //toast when player one wins
                    final int player1games = wonGames(first_final_score,total2,first_final_score2);
                    final int player2games = wonGames(second_final_score,second_final_score1,second_final_score2);
                    outPut.setText(getIntent().getExtras().getString("player1name")+" won the match!");
                    ////player1_won_set++; //set2 =(7,6)
                    ///outPut.setText(player1_won_set+", 1D");

                    // for dialogue box start
                    AlertDialog dialog = null; // This will be the returning AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                    builder.setTitle("You won the match!");
                    builder.setIcon(R.drawable.win_match);
                    builder.setCancelable(false);
                    builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel(); //changes require
                            saveData(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                            viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                        }
                    });
                    builder.setMessage(
                            getIntent().getExtras().getString("player1name")+" won the match!"
                    ).setCancelable(false) // disallow user to hit the 'back' button
                            .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    dialog = builder.create(); // now the builder is ready to create the dialog
                    timer.cancel();
                    dialog.show();
                    // for dialogue box end
                    mp.start(); // winning music play

                }
            }
            else if(counter2 > x+1 && (x >= 6)){
                toast.show(); //toast when player one wins
                player1Total1.setText("" + total2); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player1name")+" won the first set");
                player1_won_set++;
                ///outPut.setText(player1_won_set+", 1E");
                player1wins.setText("0");
                player2wins.setText("0");
                // check conditions to find final set won by player one
                if((first_final_score == 6 && total2 == 6 && second_final_score1 < 6 && second_final_score != 7 && second_final_score1 != 5) || (first_final_score == 6 && total2 == 7 && second_final_score1 == 5 && second_final_score != 7) || (first_final_score == 7 && total2 == 6 && second_final_score1 < 5) || (first_final_score == 6 && total2 == 7 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 6 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 5 && second_final_score1 == 5) || (first_final_score == 7 && total2 == 7 && second_final_score == 5 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 6 && second_final_score1 == 5))
                {

                    toast.show(); //toast when player one wins
                    final int player1games = wonGames(first_final_score,total2,first_final_score2);
                    final int player2games = wonGames(second_final_score,second_final_score1,second_final_score2);
                    outPut.setText(getIntent().getExtras().getString("player1name")+" won the match!");
                    ///player1_won_set++;
                    ///outPut.setText(player1_won_set+", 1F");

                    // for dialogue box start
                    AlertDialog dialog = null; // This will be the returning AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                    builder.setTitle("You won the match!");
                    builder.setIcon(R.drawable.win_match);
                    builder.setCancelable(false);
                    builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel(); //changes require
                            saveData(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                            viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                        }
                    });
                    builder.setMessage(
                            getIntent().getExtras().getString("player1name")+" won the match!"
                    )       .setCancelable(false) // disallow user to hit the 'back' button
                            .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    dialog = builder.create(); // now the builder is ready to create the dialog
                    timer.cancel();
                    dialog.show();
                    // for dialogue box end
                    mp.start(); // winning music play

                }
            }
            else
            {
                player1wins.setText(String.valueOf(counter2));
            }
        }
        else if(first_final_score2 == 6 && second_final_score2 == 6)
        {
            // check for tie break of set final
            counter3++;
            player1_number_point++;
            int x = Integer.parseInt(score2);
            if(counter3 == 7 && (x <= 5)){
                toast.show(); //toast when player one wins
                player1Total2.setText("" + total3); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player1name")+" won the second set");
                player1_won_set++;
                ///outPut.setText(player1_won_set+", 1G");
                player1wins.setText("0");
                player2wins.setText("0");
                if((total3 == 6 && second_final_score2 < 5) || (total3 == 7 && second_final_score2 == 5) || (total3 == 7 && second_final_score2 == 6)) {

                    toast.show(); //toast when player one wins
                    final int player1games = wonGames(first_final_score,first_final_score1,total3);
                    final int player2games = wonGames(second_final_score,second_final_score1,second_final_score2);
                    outPut.setText(getIntent().getExtras().getString("player1name")+" won the match!");
                    ///player1_won_set++; //set3 =(7,6)
                    ///outPut.setText(player1_won_set+", 1H");
                    //for dialogue box start
                    AlertDialog dialog = null; // This will be the returning AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                    builder.setTitle("You won the match!");
                    builder.setIcon(R.drawable.win_match);
                    builder.setCancelable(false);
                    builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel(); //changes require
                            saveData(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                            viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                        }
                    });
                    builder.setMessage(
                            getIntent().getExtras().getString("player1name")+" won the match!"
                    )       .setCancelable(false) // disallow user to hit the 'back' button
                            .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    dialog = builder.create(); // builder create the dialog
                    timer.cancel();
                    dialog.show();
                    //for dialogue box end
                    mp.start(); //winning music play
                }
            }
            else if(counter3 > x+1 && (x >= 6)){
                toast.show(); //toast when player one wins
                player1Total2.setText("" + total3); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player1name")+" won the first set");
                player1_won_set++;
                ///outPut.setText(player1_won_set+", 1I");
                player1wins.setText("0");
                player2wins.setText("0");
                if((total3 == 6 && second_final_score2 < 5) || (total3 == 7 && second_final_score2 == 5) || (total3 == 7 && second_final_score2 == 6)) {

                    toast.show(); //toast when player one wins
                    final int player1games = wonGames(first_final_score,first_final_score1,total3);
                    final int player2games = wonGames(second_final_score,second_final_score1,second_final_score2);
                    outPut.setText(getIntent().getExtras().getString("player1name")+" won the match!");
                    ///player1_won_set++;
                    ///outPut.setText(player1_won_set+", 1J");
                    //for dialogue box start
                    AlertDialog dialog = null; // This will be the returning AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                    builder.setTitle("You won the match!");
                    builder.setIcon(R.drawable.win_match);
                    builder.setCancelable(false);
                    builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel(); //changes require
                            saveData(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                            viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                        }
                    });
                    builder.setMessage(
                            getIntent().getExtras().getString("player1name")+" won the match!"
                    )       .setCancelable(false) // disallow user to hit the 'back' button
                            .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    dialog = builder.create(); // builder create the dialog
                    timer.cancel();
                    dialog.show();
                    //for dialogue box end
                    mp.start(); //winning music play
                }
            }
            else
            {
                player1wins.setText(String.valueOf(counter3));
            }
        }
        else if (score1=="0")
        {
            player1wins.setText("15");
        }
        else if (score1=="15")
        {
            player1wins.setText("30");
        }
        else if (score1=="30")
        {
            player1wins.setText("40");
        }
        else if (score1=="40" && score2!="AD" && score2!="40")
        {
            // check conditions to find first set won by player one
            if ((((first_final_score <= 5 && second_final_score != 7) && second_final_score != 6 && (first_final_score1 == 0 && second_final_score1 == 0)) || ((first_final_score == 6) && (second_final_score == 6))) || (second_final_score == 5 && first_final_score == 6) || (second_final_score == 6 && first_final_score == 5))
            {
                if(first_final_score1 == 0 && second_final_score1 == 0) {
                    player1wins.setText("0");
                    player2wins.setText("0");
                    player1Total.setText("" + total1); //increase value when player one won games of first set
                    if((total1 == 6 && second_final_score < 5) || (total1 == 7 && second_final_score == 6) || (total1 == 7 && second_final_score == 5)) {
                        toast.show(); //toast when player one wins
                        outPut.setText(getIntent().getExtras().getString("player1name")+" won the first set");
                        player1_won_set++; //set1 =(6,<=4)(7,5)(7,6)() //if set1 is eq 6 //if set1 is eq 7
                        ///outPut.setText(player1_won_set+", 1K");
                    }
                }
            }
            else if ((((first_final_score1 == 5 && second_final_score1 != 7) && (first_final_score2 == 0 && second_final_score2 == 0)) || ((first_final_score1 < 5) && (second_final_score1 != 6)) || ((first_final_score1 == 6) && (second_final_score1 == 6))) || (second_final_score1 == 5 && first_final_score1 == 6))
            {
                if (first_final_score2 == 0 && second_final_score2 == 0) {

                    player1wins.setText("0");
                    player2wins.setText("0");
                    player1Total1.setText("" + total2);  //increase value when player one won games of second set

                    // check conditions to find second set won by player one
                    if((total2 == 6 && second_final_score1 < 5) || (total2 == 7 && second_final_score1 == 6) || (total2 == 7 && second_final_score1 == 5) || (total2 == 6 && second_final_score1 == 7) || (total2 == 5 && second_final_score1 == 7)) {

                        toast.show(); //toast when player one wins
                        outPut.setText(getIntent().getExtras().getString("player1name")+" won the second set");
                        player1_won_set++;
                        ///outPut.setText(player1_won_set+", 1L");

                    }
                    // check conditions to find final set won by player one
                    if((first_final_score == 6 && total2 == 6 && second_final_score1 < 6 && second_final_score == 7 && second_final_score1 != 5)
                            || (first_final_score == 6 && total2 == 6 && second_final_score1 < 5 && second_final_score <= 5)
                            || (first_final_score == 6 && total2 == 7 && second_final_score1 == 5 && second_final_score != 7) || (first_final_score == 7 && total2 == 6 && second_final_score1 < 5) || (first_final_score == 6 && total2 == 7 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 6 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 5 && second_final_score1 == 5) || (first_final_score == 7 && total2 == 7 && second_final_score == 5 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 6 && second_final_score1 == 5))
                    {

                        toast.show(); //toast when player one wins
                        final int player1games = wonGames(first_final_score,total2,first_final_score2);
                        final int player2games = wonGames(second_final_score,second_final_score1,second_final_score2);
                        outPut.setText(getIntent().getExtras().getString("player1name")+" won the match!");
                        ////player1_won_set++; //set2 =(6,<=4)(7,5) //(not require to increase cause already increased)
                        ///outPut.setText(player1_won_set+", 1M");

                        // for dialogue box start
                        AlertDialog dialog = null; // This will be the returning AlertDialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                        builder.setTitle("You won the match!");
                        builder.setIcon(R.drawable.win_match);
                        builder.setCancelable(false);
                        builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //changes require
                                saveData(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                                viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                            }
                        });
                        builder.setMessage(
                                getIntent().getExtras().getString("player1name")+" won the match!"
                        )       .setCancelable(false) // disallow user to hit the 'back' button
                                .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        dialog = builder.create(); // now the builder is ready to create the dialog
                        timer.cancel();
                        dialog.show();
                        // for dialogue box end
                        mp.start(); // winning music play

                    }
                }
            }
            // check conditions to find final set won by player one
            else if ((first_final_score2 == 5 && second_final_score2 != 7) || (first_final_score2 == 5 && second_final_score2 == 6) || (first_final_score2 < 5 && second_final_score2 != 6) || (first_final_score2 == 6 && second_final_score2 != 7) || (first_final_score2 == 6 && second_final_score2 == 5) || ((first_final_score2 == 6) && (second_final_score2 == 6)))
            {
                if (second_final_score2 <= 7) {

                    player1wins.setText("0");
                    player2wins.setText("0");
                    player1Total2.setText("" + total3);  //increase value when player one won games of third set
                    if((total3 == 6 && second_final_score2 < 5) || (total3 == 7 && second_final_score2 == 5) || (total3 == 7 && second_final_score2 == 6)) {

                        toast.show(); //toast when player one wins
                        final int player1games = wonGames(first_final_score,first_final_score1,total3);
                        final int player2games = wonGames(second_final_score,second_final_score1,second_final_score2);
                        outPut.setText(getIntent().getExtras().getString("player1name")+" won the match!");
                        player1_won_set++; //set3 =(6,<=4)(7,5)
                        ///outPut.setText(player1_won_set+", 1N");
                        //for dialogue box start
                        AlertDialog dialog = null; // This will be the returning AlertDialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                        builder.setTitle("You won the match!");
                        builder.setIcon(R.drawable.win_match);
                        builder.setCancelable(false);
                        builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //changes require
                                saveData(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                                viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                            }
                        });
                        builder.setMessage(
                                getIntent().getExtras().getString("player1name")+" won the match!"
                        )       .setCancelable(false) // disallow user to hit the 'back' button
                                .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        dialog = builder.create(); // builder create the dialog
                        timer.cancel();
                        dialog.show();
                        //for dialogue box end
                        mp.start(); //winning music play
                    }

                }
            }
        }
        else if(score2 == "40" && score1 == "40")
        {
            player1wins.setText("AD");
        }
        else if(score2=="AD")
        {
            player2wins.setText("40"); player1wins.setText("40");
        }
        else if(score1=="AD"){
            if ((((first_final_score <= 5 && second_final_score != 7) && second_final_score != 6 && (first_final_score1 == 0 && second_final_score1 == 0)) || ((first_final_score == 6) && (second_final_score == 6))) || (second_final_score == 5 && first_final_score == 6) || (second_final_score == 6 && first_final_score == 5))
            {
                if(first_final_score1 == 0 && second_final_score1 == 0) {
                    player1wins.setText("0");
                    player2wins.setText("0");
                    player1Total.setText("" + total1); //increase value when player one wins first set
                    if((total1 == 6 && second_final_score < 5) || (total1 == 7 && second_final_score == 6) || (total1 == 7 && second_final_score == 5)) {
                        toast.show(); //toast when player one wins
                        outPut.setText(getIntent().getExtras().getString("player1name")+" won the first set");
                        ///player1_won_set++;
                        ///outPut.setText(player1_won_set+", 1O");
                    }
                }
            }
            else if ((((first_final_score1 == 5 && second_final_score1 != 7) && (first_final_score2 == 0 && second_final_score2 == 0)) || ((first_final_score1 < 5) && (second_final_score1 != 6)) || ((first_final_score1 == 6) && (second_final_score1 == 6))) || (second_final_score1 == 5 && first_final_score1 == 6))
            {
                if (first_final_score2 == 0 && second_final_score2 == 0) {
                    player1wins.setText("0");
                    player2wins.setText("0");
                    player1Total1.setText("" + total2); //increase value when player one wins second set
                    if((total2 == 6 && second_final_score1 < 5) || (total2 == 7 && second_final_score1 == 6) || (total2 == 7 && second_final_score1 == 5) || (total2 == 6 && second_final_score1 == 7) || (total2 == 5 && second_final_score1 == 7)) {

                        toast.show(); //toast when player one wins
                        outPut.setText(getIntent().getExtras().getString("player1name")+" won the second set");
                        ///player1_won_set++;
                        ///outPut.setText(player1_won_set+", 1P");

                    }
                    if((first_final_score == 6 && total2 == 6 && second_final_score1 < 6 && second_final_score == 7 && second_final_score1 != 5)
                            || (first_final_score == 6 && total2 == 6 && second_final_score1 < 5 && second_final_score <= 5)
                            || (first_final_score == 6 && total2 == 7 && second_final_score1 == 5 && second_final_score != 7) || (first_final_score == 7 && total2 == 6 && second_final_score1 < 5) || (first_final_score == 6 && total2 == 7 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 6 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 5 && second_final_score1 == 5) || (first_final_score == 7 && total2 == 7 && second_final_score == 5 && second_final_score1 == 6) || (first_final_score == 7 && total2 == 7 && second_final_score == 6 && second_final_score1 == 5))
                    {

                        toast.show(); //toast when player one wins
                        final int player1games = wonGames(first_final_score,total2,first_final_score2);
                        final int player2games = wonGames(second_final_score,second_final_score1,second_final_score2);
                        outPut.setText(getIntent().getExtras().getString("player1name")+" won the match!");
                        ///player1_won_set++;
                        ///outPut.setText(player1_won_set+", 1Q");
                        //for dialogue box start
                        AlertDialog dialog = null; // This will be the returning AlertDialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                        builder.setTitle("You won the match!");
                        builder.setIcon(R.drawable.win_match);
                        builder.setCancelable(false);
                        builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //changes require
                                saveData(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                                viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                            }
                        });
                        builder.setMessage(
                                getIntent().getExtras().getString("player1name")+" won the match!"
                        )       .setCancelable(false) // disallow user to hit the 'back' button
                                .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        dialog = builder.create(); // now the builder is ready to create the dialog
                        timer.cancel();
                        dialog.show();
                        //for dialogue box end
                        mp.start(); //winning music play

                    }
                }
            }
            else if ((first_final_score2 == 5 && second_final_score2 != 7) || (first_final_score2 == 5 && second_final_score2 == 6) || (first_final_score2 < 5 && second_final_score2 != 6) || (first_final_score2 == 6 && second_final_score2 != 7) || (first_final_score2 == 6 && second_final_score2 == 5) || ((first_final_score2 == 6) && (second_final_score2 == 6)))
            {
                if (second_final_score2 <= 7) {
                    player1wins.setText("0");
                    player2wins.setText("0");
                    player1Total2.setText("" + total3); //increase value when player one wins first set
                    if((total3 == 6 && second_final_score2 < 5) || (total3 == 7 && second_final_score2 == 5) || (total3 == 7 && second_final_score2 == 6)) {

                        toast.show(); //toast when player one wins
                        final int player1games = wonGames(first_final_score,first_final_score1,total3);
                        final int player2games = wonGames(second_final_score,second_final_score1,second_final_score2);
                        outPut.setText(getIntent().getExtras().getString("player1name")+" won the match!");
                        ///player1_won_set++;
                        ///outPut.setText(player1_won_set+", 1R");
                        //for dialogue box start
                        AlertDialog dialog = null; // This will be the returning AlertDialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                        builder.setTitle("You won the match!");
                        builder.setIcon(R.drawable.win_match);
                        builder.setCancelable(false);
                        builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //changes require
                                saveData(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                                viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                            }
                        });
                        builder.setMessage(
                                getIntent().getExtras().getString("player1name")+" won the match!"
                        )       .setCancelable(false) // disallow user to hit the 'back' button
                                .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        viewScore(getIntent().getExtras().getString("player1name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        dialog = builder.create(); // builder create the dialog
                        timer.cancel();
                        dialog.show();
                        //for dialogue box end
                        mp.start(); //winning music play
                    }

                }
            }
        }
        else{ player1wins.setText("0"); player2wins.setText("0"); }


    }

    /******************************Player one won button(End)*********************************/

    /*=======================================================================================*/

    /******************************Player two won button(Start)*******************************/

    //Create method: by clicking this button won player 2 and check all conditions to make player 2 won

    public void PlayerTwoWonClicked(View v){
        MarcadorActual();
        // to toast winnig declaration
        player2_number_point++;
        String win_text = getIntent().getExtras().getString("player2name")+" won";
        Context context = getApplicationContext(); // gets the application context to pass to Toast
        int duration = Toast.LENGTH_SHORT; // define length of toast
        Toast toast = Toast.makeText(context, win_text, duration); // crete an object of toast class
        // to toast winnig declaration

        final MediaPlayer mp = MediaPlayer.create(this, R.raw.win); //create media player class object

        // get the value of particular place which find by place id
        String score1 = player1wins.getText().toString();
        String score2 = player2wins.getText().toString();
        int first_final_score = Integer.parseInt(player1Total.getText().toString());
        int second_final_score = Integer.parseInt(player2Total.getText().toString());
        int first_final_score1 = Integer.parseInt(player1Total1.getText().toString());
        int second_final_score1 = Integer.parseInt(player2Total1.getText().toString());
        int first_final_score2 = Integer.parseInt(player1Total2.getText().toString());
        int second_final_score2 = Integer.parseInt(player2Total2.getText().toString());
        int total1 = second_final_score + 1; int total2 = second_final_score1 + 1; int total3 = second_final_score2 + 1;
        final Button resetAll = (Button) findViewById(R.id.resetAll); // create button view
        TextView player2name = (TextView) findViewById(R.id.player2name); //getPlayer two name text field

        // check for tie break of set first
        if(first_final_score == 6 && second_final_score == 6)
        {
            counter4++;
            player1_number_point++;
            int x = Integer.parseInt(score1);
            if(counter4 == 7 && (x <= 5)){
                toast.show(); //toast when player one wins
                player2Total.setText("" + total1); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player2name")+" won the first set");
                player2_won_set++; //set1 =(6,7)
                ///outPut.setText(player2_won_set+", 2A");
                player1wins.setText("0");
                player2wins.setText("0");
            }
            else if(counter4 > x+1 && (x >= 6)){
                toast.show(); //toast when player one wins
                player2Total.setText("" + total1); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player2name")+" won the first set");
                player2_won_set++;
                ///outPut.setText(player2_won_set+", 2B");
                player1wins.setText("0");
                player2wins.setText("0");
            }
            else
            {
                player2wins.setText(String.valueOf(counter4));
            }
        }
        else if(first_final_score1 == 6 && second_final_score1 == 6)
        {
            // check for tie break of set second
            counter5++;
            player1_number_point++;
            int x = Integer.parseInt(score1);
            if(counter5 == 7 && (x <= 5)){
                toast.show(); //toast when player one wins
                player2Total1.setText("" + total2); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player2name")+" won the second set");
                player2_won_set++; //set2 =(6,7)
                ///outPut.setText(player2_won_set+", 2C");
                player1wins.setText("0");
                player2wins.setText("0");
                // check conditions to find final set won by player two
                if((second_final_score == 6 && total2 == 6 && first_final_score1 < 5 && first_final_score != 7) || (second_final_score == 7 && total2 == 6 && first_final_score1 < 5 && first_final_score == 6) || (second_final_score == 7 && total2 == 6 && first_final_score1 < 5 && first_final_score == 5) || (second_final_score == 6 && total2 == 7 && first_final_score1 == 6) || (second_final_score == 6 && total2 == 7 && first_final_score1 == 5) || (second_final_score == 7 && total2 == 7 && first_final_score == 6 && first_final_score1 == 5) || (second_final_score == 7 && total2 == 7 && first_final_score == 5 && first_final_score1 == 6) || (second_final_score == 7 && total2 == 7 && first_final_score == 6 && first_final_score1 == 6) || (second_final_score == 7 && total2 == 7 && first_final_score == 5 && first_final_score1 == 5))
                {

                    toast.show(); //toast when player two wins
                    final int player1games = wonGames(first_final_score,first_final_score1,first_final_score2);
                    final int player2games = wonGames(second_final_score,total2,second_final_score2);
                    outPut.setText(getIntent().getExtras().getString("player2name")+" won the match!");
                    ///player2_won_set++;
                    ///outPut.setText(player2_won_set+", 2C");
                    //for dialogue box start
                    AlertDialog dialog = null; // This will be the returning AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                    builder.setTitle("You won the match!");
                    builder.setIcon(R.drawable.win_match);
                    builder.setCancelable(false);
                    builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel(); //changes require
                            saveData(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                            viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                        }
                    });
                    builder.setMessage(
                            getIntent().getExtras().getString("player2name")+" won the match!"
                    )       .setCancelable(false) // disallow user to hit the 'back' button
                            .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    dialog = builder.create(); // now the builder is ready to create the dialog
                    timer.cancel();
                    dialog.show();
                    //for dialogue box end
                    mp.start(); //winning music play

                }
            }
            else if(counter5 > x+1 && (x >= 6)){
                toast.show(); //toast when player one wins
                player2Total1.setText("" + total2); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player2name")+" won the second set");
                ///player2_won_set++;
                ///outPut.setText(player2_won_set+", 2D");
                player1wins.setText("0");
                player2wins.setText("0");
                // check conditions to find final set won by player two
                if((second_final_score == 6 && total2 == 6 && first_final_score1 < 5 && first_final_score != 7) || (second_final_score == 7 && total2 == 6 && first_final_score1 < 5 && first_final_score == 6) || (second_final_score == 7 && total2 == 6 && first_final_score1 < 5 && first_final_score == 5) || (second_final_score == 6 && total2 == 7 && first_final_score1 == 6) || (second_final_score == 6 && total2 == 7 && first_final_score1 == 5) || (second_final_score == 7 && total2 == 7 && first_final_score == 6 && first_final_score1 == 5) || (second_final_score == 7 && total2 == 7 && first_final_score == 5 && first_final_score1 == 6) || (second_final_score == 7 && total2 == 7 && first_final_score == 6 && first_final_score1 == 6) || (second_final_score == 7 && total2 == 7 && first_final_score == 5 && first_final_score1 == 5))
                {

                    toast.show(); //toast when player two wins
                    final int player1games = wonGames(first_final_score,first_final_score1,first_final_score2);
                    final int player2games = wonGames(second_final_score,total2,second_final_score2);
                    outPut.setText(getIntent().getExtras().getString("player2name")+" won the match!");
                    player2_won_set++;
                    ///outPut.setText(player2_won_set+", 2E");
                    //for dialogue box start
                    AlertDialog dialog = null; // This will be the returning AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                    builder.setTitle("You won the match!");
                    builder.setIcon(R.drawable.win_match);
                    builder.setCancelable(false);
                    builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel(); //changes require
                            saveData(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                            viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                        }
                    });
                    builder.setMessage(
                            getIntent().getExtras().getString("player2name")+" won the match!"
                    )       .setCancelable(false) // disallow user to hit the 'back' button
                            .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    dialog = builder.create(); // now the builder is ready to create the dialog
                    timer.cancel();
                    dialog.show();
                    //for dialogue box end
                    mp.start(); //winning music play

                }
            }
            else
            {
                player2wins.setText(String.valueOf(counter5));
            }
        }
        else if(first_final_score2 == 6 && second_final_score2 == 6)
        {
            // check for tie break of set final
            counter6++;
            player1_number_point++;
            int x = Integer.parseInt(score1);
            if(counter6 == 7 && (x <= 5)){
                toast.show(); //toast when player one wins
                player2Total2.setText("" + total3); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player2name")+" won the second set");
                ///player2_won_set++;
                ///outPut.setText(player2_won_set+", 2E");
                player1wins.setText("0");
                player2wins.setText("0");
                if((total3 == 6 && first_final_score2 < 5) || (total3 == 7 && first_final_score2 == 5) || (total3 == 7 && first_final_score2 == 6))
                {
                    toast.show(); //toast when player two wins
                    final int player1games = wonGames(first_final_score,first_final_score1,first_final_score2);
                    final int player2games = wonGames(second_final_score,second_final_score1,total3);
                    outPut.setText(getIntent().getExtras().getString("player2name")+" won the match!");
                    player2_won_set++; //set3 =(6,7)
                    ///outPut.setText(player2_won_set+", 2F");
                    //for dialogue box start
                    AlertDialog dialog = null; // This will be the returning AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                    builder.setTitle("You won the match!");
                    builder.setIcon(R.drawable.win_match);
                    builder.setCancelable(false);
                    builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel(); //changes require
                            saveData(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                            viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                        }
                    });
                    builder.setMessage(
                            getIntent().getExtras().getString("player2name")+" won the match!"
                    )       .setCancelable(false) // disallow user to hit the 'back' button
                            .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    dialog = builder.create(); // builder create the dialog
                    timer.cancel();
                    dialog.show();
                    //for dialogue box end
                    mp.start(); //winning music play

                }
            }
            else if(counter6 > x+1 && (x >= 6)){
                toast.show(); //toast when player one wins
                player2Total2.setText("" + total3); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player2name")+" won the second set");
                ///player2_won_set++;
                ///outPut.setText(player2_won_set+", 2G");
                player1wins.setText("0");
                player2wins.setText("0");
                if((total3 == 6 && first_final_score2 < 5) || (total3 == 7 && first_final_score2 == 5) || (total3 == 7 && first_final_score2 == 6)) {

                    toast.show(); //toast when player two wins
                    final int player1games = wonGames(first_final_score,first_final_score1,first_final_score2);
                    final int player2games = wonGames(second_final_score,second_final_score1,total3);
                    outPut.setText(getIntent().getExtras().getString("player2name")+" won the match!");
                    player2_won_set++;
                    ///outPut.setText(player2_won_set+", 2H");
                    //for dialogue box start
                    AlertDialog dialog = null; // This will be the returning AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                    builder.setTitle("You won the match!");
                    builder.setIcon(R.drawable.win_match);
                    builder.setCancelable(false);
                    builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel(); //changes require
                            saveData(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                            viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                        }
                    });
                    builder.setMessage(
                            getIntent().getExtras().getString("player2name")+" won the match!"
                    )       .setCancelable(false) // disallow user to hit the 'back' button
                            .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    dialog = builder.create(); // builder create the dialog
                    timer.cancel();
                    dialog.show();
                    //for dialogue box end
                    mp.start(); //winning music play

                }
            }
            else
            {
                player2wins.setText(String.valueOf(counter6));
            }
        }
        else if (score2 == "0")
        {
            player2wins.setText("15");
        }
        else if (score2 == "15")
        {
            player2wins.setText("30");
        }
        else if (score2 == "30")
        {
            player2wins.setText("40");
        }
        else if (score2 == "40" && score1 != "AD" && score1!="40")
        {
            // check conditions to find first set won by player two
            if ((((second_final_score <= 5 && first_final_score != 7) && first_final_score != 6 && (first_final_score1 == 0 && second_final_score1 == 0)) || ((first_final_score == 6) && (second_final_score == 6))) || (first_final_score == 5 && second_final_score == 6) || (first_final_score == 6 && second_final_score == 5))
            {
                if(first_final_score1 == 0 && second_final_score1 == 0)
                {
                    player2wins.setText("0");
                    player1wins.setText("0");
                    player2Total.setText("" + total1); //increase value when player two won first set
                    if((total1 == 6 && first_final_score < 5) || (total1 == 7 && first_final_score == 6) || (total1 == 7 && first_final_score == 5)) {
                        toast.show(); //toast when player two wins
                        outPut.setText(getIntent().getExtras().getString("player2name")+" won the first set");
                        player2_won_set++;
                        ///outPut.setText(player2_won_set+", 2I");
                    }
                }
            }
            // check conditions to find second set won by player two
            else if ((((second_final_score1 == 5 && first_final_score1 != 7) && (first_final_score2 == 0 && second_final_score2 == 0)) || ((second_final_score1 < 5) && (first_final_score1 != 6)) || ((second_final_score1 == 6) && (first_final_score1 == 6))) || (first_final_score1 == 5 && second_final_score1 == 6))
            {
                if (first_final_score2 == 0 && second_final_score2 == 0)
                {
                    player2wins.setText("0");
                    player1wins.setText("0");
                    player2Total1.setText("" + total2); //increase value when player two wins second set
                    if((total2 == 6 && first_final_score1 < 5) || (total2 == 7 && first_final_score1 == 6) || (total2 == 7 && first_final_score1 == 5) || (total2 == 6 && first_final_score1 == 7) || (total2 == 5 && first_final_score1 == 7)) {

                        toast.show(); //toast when player two wins
                        outPut.setText(getIntent().getExtras().getString("player2name")+" won the second set");
                        player2_won_set++; //if set2 is eq 6 //if set2 is eq 7
                        ///outPut.setText(player2_won_set+", 2J");

                    }
                    // check conditions to find final set won by player two
                    if((second_final_score == 6 && total2 == 6 && first_final_score1 < 5 && first_final_score == 7)
                            || (second_final_score == 6 && total2 == 6 && first_final_score1 < 5 && first_final_score < 5)
                            || (second_final_score == 7 && total2 == 6 && first_final_score1 < 5 && first_final_score == 6) || (second_final_score == 7 && total2 == 6 && first_final_score1 < 5 && first_final_score == 5) || (second_final_score == 6 && total2 == 7 && first_final_score1 == 6) || (second_final_score == 6 && total2 == 7 && first_final_score1 == 5) || (second_final_score == 7 && total2 == 7 && first_final_score == 6 && first_final_score1 == 5) || (second_final_score == 7 && total2 == 7 && first_final_score == 5 && first_final_score1 == 6) || (second_final_score == 7 && total2 == 7 && first_final_score == 6 && first_final_score1 == 6) || (second_final_score == 7 && total2 == 7 && first_final_score == 5 && first_final_score1 == 5))
                    {

                        toast.show(); //toast when player two wins
                        final int player1games = wonGames(first_final_score,first_final_score1,first_final_score2);
                        final int player2games = wonGames(second_final_score,total2,second_final_score2);
                        outPut.setText(getIntent().getExtras().getString("player2name")+" won the match!");
                        //player2_won_set++; //set2 =(4,6)(5,7)
                        ///outPut.setText(player2_won_set+", 2K");
                        //for dialogue box start
                        AlertDialog dialog = null; // This will be the returning AlertDialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                        builder.setTitle("You won the match!");
                        builder.setIcon(R.drawable.win_match);
                        builder.setCancelable(false);
                        builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //changes require
                                saveData(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                                viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                            }
                        });
                        builder.setMessage(
                                getIntent().getExtras().getString("player2name")+" won the match!"
                        )       .setCancelable(false) // disallow user to hit the 'back' button
                                .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        dialog = builder.create(); // now the builder is ready to create the dialog
                        timer.cancel();
                        dialog.show();
                        //for dialogue box end
                        mp.start(); //winning music play

                    }
                }
            }
            // check conditions to find final set won by player two
            else if ((second_final_score2 == 5 && first_final_score2 != 7) || (second_final_score2 == 5 && first_final_score2 == 6) || ((second_final_score2 < 5) && (first_final_score2 != 6)) || (second_final_score2 == 6 && first_final_score2 != 7) || (second_final_score2 == 6 && first_final_score2 == 5) || ((second_final_score2 == 6) && (first_final_score2 == 6)))
            {
                if (second_final_score2 <= 7)
                {
                    player1wins.setText("0");
                    player2wins.setText("0");
                    player2Total2.setText("" + total3); //increase value when player one wins third set
                    if((total3 == 6 && first_final_score2 < 5) || (total3 == 7 && first_final_score2 == 5) || (total3 == 7 && first_final_score2 == 6)) {

                        toast.show(); //toast when player two wins
                        final int player1games = wonGames(first_final_score,first_final_score1,first_final_score2);
                        final int player2games = wonGames(second_final_score,second_final_score1,total3);
                        outPut.setText(getIntent().getExtras().getString("player2name")+" won the match!");
                        player2_won_set++; //set1 =(<=4,6)(5,7) //set3 =(4,6)(5,7)
                        ///outPut.setText(player2_won_set+", 2L");
                        //for dialogue box start
                        AlertDialog dialog = null; // This will be the returning AlertDialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                        builder.setTitle("You won the match!");
                        builder.setIcon(R.drawable.win_match);
                        builder.setCancelable(false);
                        builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //changes require
                                saveData(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                                viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                            }
                        });
                        builder.setMessage(
                                getIntent().getExtras().getString("player2name")+" won the match!"
                        )       .setCancelable(false) // disallow user to hit the 'back' button
                                .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        dialog = builder.create(); // builder create the dialog
                        timer.cancel();
                        dialog.show();
                        //for dialogue box end
                        mp.start(); //winning music play

                    }
                }
            }
        }
        else if(score1=="40" && score2 == "40")
        {
            player2wins.setText("AD");
        }
        else if(score1=="AD")
        {
            player1wins.setText("40"); player2wins.setText("40");
        }
        else if(score2=="AD") {
            if ((((second_final_score <= 5 && first_final_score != 7) && first_final_score != 6 && (first_final_score1 == 0 && second_final_score1 == 0)) || ((first_final_score == 6) && (second_final_score == 6))) || (first_final_score == 5 && second_final_score == 6) || (first_final_score == 6 && second_final_score == 5))
            {
                if(first_final_score1 == 0 && second_final_score1 == 0)
                {
                    player2wins.setText("0");
                    player1wins.setText("0");
                    player2Total.setText("" + total1); //increase value when player one wins first set
                    if((total1 == 6 && first_final_score < 5) || (total1 == 7 && first_final_score == 6) || (total1 == 7 && first_final_score == 5)) {

                        toast.show(); //toast when player two wins
                        outPut.setText(getIntent().getExtras().getString("player2name")+" won the first set");
                        ///player2_won_set++;
                        ///outPut.setText(player2_won_set+", 2M");

                    }
                }
            }
            else if ((((second_final_score1 == 5 && first_final_score1 != 7) && (first_final_score2 == 0 && second_final_score2 == 0)) || ((second_final_score1 < 5) && (first_final_score1 != 6)) || ((second_final_score1 == 6) && (first_final_score1 == 6))) || (first_final_score1 == 5 && second_final_score1 == 6))
            {
                if (first_final_score2 == 0 && second_final_score2 == 0)
                {
                    player2wins.setText("0");
                    player1wins.setText("0");
                    player2Total1.setText("" + total2); //increase value when player one wins second set
                    if((total2 == 6 && first_final_score1 < 5) || (total2 == 7 && first_final_score1 == 6) || (total2 == 7 && first_final_score1 == 5) || (total2 == 6 && first_final_score1 == 7) || (total2 == 5 && first_final_score1 == 7)) {

                        toast.show(); //toast when player two wins
                        outPut.setText(getIntent().getExtras().getString("player2name")+" won the second set");
                        ///player2_won_set++;
                        ///outPut.setText(player2_won_set+", 2N");

                    }
                    if((second_final_score == 6 && total2 == 6 && first_final_score1 < 5 && first_final_score == 7)
                            || (second_final_score == 6 && total2 == 6 && first_final_score1 < 5 && first_final_score < 5)
                            || (second_final_score == 7 && total2 == 6 && first_final_score1 < 5 && first_final_score == 6) || (second_final_score == 7 && total2 == 6 && first_final_score1 < 5 && first_final_score == 5) || (second_final_score == 6 && total2 == 7 && first_final_score1 == 6) || (second_final_score == 6 && total2 == 7 && first_final_score1 == 5) || (second_final_score == 7 && total2 == 7 && first_final_score == 6 && first_final_score1 == 5) || (second_final_score == 7 && total2 == 7 && first_final_score == 5 && first_final_score1 == 6) || (second_final_score == 7 && total2 == 7 && first_final_score == 6 && first_final_score1 == 6) || (second_final_score == 7 && total2 == 7 && first_final_score == 5 && first_final_score1 == 5))
                    {

                        toast.show(); //toast when player two wins
                        final int player1games = wonGames(first_final_score,first_final_score1,first_final_score2);
                        final int player2games = wonGames(second_final_score,total2,second_final_score2);
                        outPut.setText(getIntent().getExtras().getString("player2name")+" won the match!");
                        ///player2_won_set++;
                        ///outPut.setText(player2_won_set+", 2O");
                        //for dialogue box start
                        AlertDialog dialog = null; // This will be the returning AlertDialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                        builder.setTitle("You won the match!");
                        builder.setIcon(R.drawable.win_match);
                        builder.setCancelable(false);
                        builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //changes require
                                saveData(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                                viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                            }
                        });
                        builder.setMessage(
                                getIntent().getExtras().getString("player2name")+" won the match!"
                        )       .setCancelable(false) // disallow user to hit the 'back' button
                                .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        dialog = builder.create(); // now the builder is ready to create the dialog
                        timer.cancel();
                        dialog.show();
                        //for dialogue box end
                        mp.start(); //winning music play

                    }
                }
            }
            else if ((second_final_score2 == 5 && first_final_score2 != 7) || (second_final_score2 == 5 && first_final_score2 == 6) || ((second_final_score2 < 5) && (first_final_score2 != 6)) || (second_final_score2 == 6 && first_final_score2 != 7) || (second_final_score2 == 6 && first_final_score2 == 5) || ((second_final_score2 == 6) && (first_final_score2 == 6)))
            {
                if (second_final_score2 <= 7)
                {
                    player1wins.setText("0");
                    player2wins.setText("0");
                    player2Total2.setText("" + total3); //increase value when player one wins third set
                    if((total3 == 6 && first_final_score2 < 5) || (total3 == 7 && first_final_score2 == 5) || (total3 == 7 && first_final_score2 == 6)) {

                        toast.show(); //toast when player two wins
                        final int player1games = wonGames(first_final_score,first_final_score1,first_final_score2);
                        final int player2games = wonGames(second_final_score,second_final_score1,total3);
                        outPut.setText(getIntent().getExtras().getString("player2name")+" won the match!");
                        ///player2_won_set++;
                        ///outPut.setText(player2_won_set+", 2P");
                        //for dialogue box start
                        AlertDialog dialog = null; // This will be the returning AlertDialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                        builder.setTitle("You won the match!");
                        builder.setIcon(R.drawable.win_match);
                        builder.setCancelable(false);
                        builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //changes require
                                saveData(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                                viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                            }
                        });
                        builder.setMessage(
                                getIntent().getExtras().getString("player2name")+" won the match!"
                        )       .setCancelable(false) // disallow user to hit the 'back' button
                                .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        dialog = builder.create(); // builder create the dialog
                        timer.cancel();
                        dialog.show();
                        //for dialogue box end
                        mp.start(); //winning music play

                    }
                }
            }
        }
        else{ player2wins.setText("0"); player1wins.setText("0"); }
        datosMarcador();

    }
    public void PlayerTwoWonPressed(){
        MarcadorActual();
        // to toast winnig declaration
        player2_number_point++;
        String win_text = getIntent().getExtras().getString("player2name")+" won";
        Context context = getApplicationContext(); // gets the application context to pass to Toast
        int duration = Toast.LENGTH_SHORT; // define length of toast
        Toast toast = Toast.makeText(context, win_text, duration); // crete an object of toast class
        // to toast winnig declaration

        final MediaPlayer mp = MediaPlayer.create(this, R.raw.win); //create media player class object

        // get the value of particular place which find by place id
        String score1 = player1wins.getText().toString();
        String score2 = player2wins.getText().toString();
        int first_final_score = Integer.parseInt(player1Total.getText().toString());
        int second_final_score = Integer.parseInt(player2Total.getText().toString());
        int first_final_score1 = Integer.parseInt(player1Total1.getText().toString());
        int second_final_score1 = Integer.parseInt(player2Total1.getText().toString());
        int first_final_score2 = Integer.parseInt(player1Total2.getText().toString());
        int second_final_score2 = Integer.parseInt(player2Total2.getText().toString());
        int total1 = second_final_score + 1; int total2 = second_final_score1 + 1; int total3 = second_final_score2 + 1;
        final Button resetAll = (Button) findViewById(R.id.resetAll); // create button view
        TextView player2name = (TextView) findViewById(R.id.player2name); //getPlayer two name text field

        // check for tie break of set first
        if(first_final_score == 6 && second_final_score == 6)
        {
            counter4++;
            player1_number_point++;
            int x = Integer.parseInt(score1);
            if(counter4 == 7 && (x <= 5)){
                toast.show(); //toast when player one wins
                player2Total.setText("" + total1); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player2name")+" won the first set");
                player2_won_set++; //set1 =(6,7)
                ///outPut.setText(player2_won_set+", 2A");
                player1wins.setText("0");
                player2wins.setText("0");
            }
            else if(counter4 > x+1 && (x >= 6)){
                toast.show(); //toast when player one wins
                player2Total.setText("" + total1); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player2name")+" won the first set");
                player2_won_set++;
                ///outPut.setText(player2_won_set+", 2B");
                player1wins.setText("0");
                player2wins.setText("0");
            }
            else
            {
                player2wins.setText(String.valueOf(counter4));
            }
        }
        else if(first_final_score1 == 6 && second_final_score1 == 6)
        {
            // check for tie break of set second
            counter5++;
            player1_number_point++;
            int x = Integer.parseInt(score1);
            if(counter5 == 7 && (x <= 5)){
                toast.show(); //toast when player one wins
                player2Total1.setText("" + total2); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player2name")+" won the second set");
                player2_won_set++; //set2 =(6,7)
                ///outPut.setText(player2_won_set+", 2C");
                player1wins.setText("0");
                player2wins.setText("0");
                // check conditions to find final set won by player two
                if((second_final_score == 6 && total2 == 6 && first_final_score1 < 5 && first_final_score != 7) || (second_final_score == 7 && total2 == 6 && first_final_score1 < 5 && first_final_score == 6) || (second_final_score == 7 && total2 == 6 && first_final_score1 < 5 && first_final_score == 5) || (second_final_score == 6 && total2 == 7 && first_final_score1 == 6) || (second_final_score == 6 && total2 == 7 && first_final_score1 == 5) || (second_final_score == 7 && total2 == 7 && first_final_score == 6 && first_final_score1 == 5) || (second_final_score == 7 && total2 == 7 && first_final_score == 5 && first_final_score1 == 6) || (second_final_score == 7 && total2 == 7 && first_final_score == 6 && first_final_score1 == 6) || (second_final_score == 7 && total2 == 7 && first_final_score == 5 && first_final_score1 == 5))
                {

                    toast.show(); //toast when player two wins
                    final int player1games = wonGames(first_final_score,first_final_score1,first_final_score2);
                    final int player2games = wonGames(second_final_score,total2,second_final_score2);
                    outPut.setText(getIntent().getExtras().getString("player2name")+" won the match!");
                    ///player2_won_set++;
                    ///outPut.setText(player2_won_set+", 2C");
                    //for dialogue box start
                    AlertDialog dialog = null; // This will be the returning AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                    builder.setTitle("You won the match!");
                    builder.setIcon(R.drawable.win_match);
                    builder.setCancelable(false);
                    builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel(); //changes require
                            saveData(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                            viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                        }
                    });
                    builder.setMessage(
                            getIntent().getExtras().getString("player2name")+" won the match!"
                    )       .setCancelable(false) // disallow user to hit the 'back' button
                            .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    dialog = builder.create(); // now the builder is ready to create the dialog
                    timer.cancel();
                    dialog.show();
                    //for dialogue box end
                    mp.start(); //winning music play

                }
            }
            else if(counter5 > x+1 && (x >= 6)){
                toast.show(); //toast when player one wins
                player2Total1.setText("" + total2); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player2name")+" won the second set");
                ///player2_won_set++;
                ///outPut.setText(player2_won_set+", 2D");
                player1wins.setText("0");
                player2wins.setText("0");
                // check conditions to find final set won by player two
                if((second_final_score == 6 && total2 == 6 && first_final_score1 < 5 && first_final_score != 7) || (second_final_score == 7 && total2 == 6 && first_final_score1 < 5 && first_final_score == 6) || (second_final_score == 7 && total2 == 6 && first_final_score1 < 5 && first_final_score == 5) || (second_final_score == 6 && total2 == 7 && first_final_score1 == 6) || (second_final_score == 6 && total2 == 7 && first_final_score1 == 5) || (second_final_score == 7 && total2 == 7 && first_final_score == 6 && first_final_score1 == 5) || (second_final_score == 7 && total2 == 7 && first_final_score == 5 && first_final_score1 == 6) || (second_final_score == 7 && total2 == 7 && first_final_score == 6 && first_final_score1 == 6) || (second_final_score == 7 && total2 == 7 && first_final_score == 5 && first_final_score1 == 5))
                {

                    toast.show(); //toast when player two wins
                    final int player1games = wonGames(first_final_score,first_final_score1,first_final_score2);
                    final int player2games = wonGames(second_final_score,total2,second_final_score2);
                    outPut.setText(getIntent().getExtras().getString("player2name")+" won the match!");
                    player2_won_set++;
                    ///outPut.setText(player2_won_set+", 2E");
                    //for dialogue box start
                    AlertDialog dialog = null; // This will be the returning AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                    builder.setTitle("You won the match!");
                    builder.setIcon(R.drawable.win_match);
                    builder.setCancelable(false);
                    builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel(); //changes require
                            saveData(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                            viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                        }
                    });
                    builder.setMessage(
                            getIntent().getExtras().getString("player2name")+" won the match!"
                    )       .setCancelable(false) // disallow user to hit the 'back' button
                            .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    dialog = builder.create(); // now the builder is ready to create the dialog
                    timer.cancel();
                    dialog.show();
                    //for dialogue box end
                    mp.start(); //winning music play

                }
            }
            else
            {
                player2wins.setText(String.valueOf(counter5));
            }
        }
        else if(first_final_score2 == 6 && second_final_score2 == 6)
        {
            // check for tie break of set final
            counter6++;
            player1_number_point++;
            int x = Integer.parseInt(score1);
            if(counter6 == 7 && (x <= 5)){
                toast.show(); //toast when player one wins
                player2Total2.setText("" + total3); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player2name")+" won the second set");
                ///player2_won_set++;
                ///outPut.setText(player2_won_set+", 2E");
                player1wins.setText("0");
                player2wins.setText("0");
                if((total3 == 6 && first_final_score2 < 5) || (total3 == 7 && first_final_score2 == 5) || (total3 == 7 && first_final_score2 == 6))
                {
                    toast.show(); //toast when player two wins
                    final int player1games = wonGames(first_final_score,first_final_score1,first_final_score2);
                    final int player2games = wonGames(second_final_score,second_final_score1,total3);
                    outPut.setText(getIntent().getExtras().getString("player2name")+" won the match!");
                    player2_won_set++; //set3 =(6,7)
                    ///outPut.setText(player2_won_set+", 2F");
                    //for dialogue box start
                    AlertDialog dialog = null; // This will be the returning AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                    builder.setTitle("You won the match!");
                    builder.setIcon(R.drawable.win_match);
                    builder.setCancelable(false);
                    builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel(); //changes require
                            saveData(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                            viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                        }
                    });
                    builder.setMessage(
                            getIntent().getExtras().getString("player2name")+" won the match!"
                    )       .setCancelable(false) // disallow user to hit the 'back' button
                            .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    dialog = builder.create(); // builder create the dialog
                    timer.cancel();
                    dialog.show();
                    //for dialogue box end
                    mp.start(); //winning music play

                }
            }
            else if(counter6 > x+1 && (x >= 6)){
                toast.show(); //toast when player one wins
                player2Total2.setText("" + total3); //increase value when player one won games of first set
                outPut.setText(getIntent().getExtras().getString("player2name")+" won the second set");
                ///player2_won_set++;
                ///outPut.setText(player2_won_set+", 2G");
                player1wins.setText("0");
                player2wins.setText("0");
                if((total3 == 6 && first_final_score2 < 5) || (total3 == 7 && first_final_score2 == 5) || (total3 == 7 && first_final_score2 == 6)) {

                    toast.show(); //toast when player two wins
                    final int player1games = wonGames(first_final_score,first_final_score1,first_final_score2);
                    final int player2games = wonGames(second_final_score,second_final_score1,total3);
                    outPut.setText(getIntent().getExtras().getString("player2name")+" won the match!");
                    player2_won_set++;
                    ///outPut.setText(player2_won_set+", 2H");
                    //for dialogue box start
                    AlertDialog dialog = null; // This will be the returning AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                    builder.setTitle("You won the match!");
                    builder.setIcon(R.drawable.win_match);
                    builder.setCancelable(false);
                    builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel(); //changes require
                            saveData(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                            viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                        }
                    });
                    builder.setMessage(
                            getIntent().getExtras().getString("player2name")+" won the match!"
                    )       .setCancelable(false) // disallow user to hit the 'back' button
                            .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    dialog = builder.create(); // builder create the dialog
                    timer.cancel();
                    dialog.show();
                    //for dialogue box end
                    mp.start(); //winning music play

                }
            }
            else
            {
                player2wins.setText(String.valueOf(counter6));
            }
        }
        else if (score2 == "0")
        {
            player2wins.setText("15");
        }
        else if (score2 == "15")
        {
            player2wins.setText("30");
        }
        else if (score2 == "30")
        {
            player2wins.setText("40");
        }
        else if (score2 == "40" && score1 != "AD" && score1!="40")
        {
            // check conditions to find first set won by player two
            if ((((second_final_score <= 5 && first_final_score != 7) && first_final_score != 6 && (first_final_score1 == 0 && second_final_score1 == 0)) || ((first_final_score == 6) && (second_final_score == 6))) || (first_final_score == 5 && second_final_score == 6) || (first_final_score == 6 && second_final_score == 5))
            {
                if(first_final_score1 == 0 && second_final_score1 == 0)
                {
                    player2wins.setText("0");
                    player1wins.setText("0");
                    player2Total.setText("" + total1); //increase value when player two won first set
                    if((total1 == 6 && first_final_score < 5) || (total1 == 7 && first_final_score == 6) || (total1 == 7 && first_final_score == 5)) {
                        toast.show(); //toast when player two wins
                        outPut.setText(getIntent().getExtras().getString("player2name")+" won the first set");
                        player2_won_set++;
                        ///outPut.setText(player2_won_set+", 2I");
                    }
                }
            }
            // check conditions to find second set won by player two
            else if ((((second_final_score1 == 5 && first_final_score1 != 7) && (first_final_score2 == 0 && second_final_score2 == 0)) || ((second_final_score1 < 5) && (first_final_score1 != 6)) || ((second_final_score1 == 6) && (first_final_score1 == 6))) || (first_final_score1 == 5 && second_final_score1 == 6))
            {
                if (first_final_score2 == 0 && second_final_score2 == 0)
                {
                    player2wins.setText("0");
                    player1wins.setText("0");
                    player2Total1.setText("" + total2); //increase value when player two wins second set
                    if((total2 == 6 && first_final_score1 < 5) || (total2 == 7 && first_final_score1 == 6) || (total2 == 7 && first_final_score1 == 5) || (total2 == 6 && first_final_score1 == 7) || (total2 == 5 && first_final_score1 == 7)) {

                        toast.show(); //toast when player two wins
                        outPut.setText(getIntent().getExtras().getString("player2name")+" won the second set");
                        player2_won_set++; //if set2 is eq 6 //if set2 is eq 7
                        ///outPut.setText(player2_won_set+", 2J");

                    }
                    // check conditions to find final set won by player two
                    if((second_final_score == 6 && total2 == 6 && first_final_score1 < 5 && first_final_score == 7)
                            || (second_final_score == 6 && total2 == 6 && first_final_score1 < 5 && first_final_score < 5)
                            || (second_final_score == 7 && total2 == 6 && first_final_score1 < 5 && first_final_score == 6) || (second_final_score == 7 && total2 == 6 && first_final_score1 < 5 && first_final_score == 5) || (second_final_score == 6 && total2 == 7 && first_final_score1 == 6) || (second_final_score == 6 && total2 == 7 && first_final_score1 == 5) || (second_final_score == 7 && total2 == 7 && first_final_score == 6 && first_final_score1 == 5) || (second_final_score == 7 && total2 == 7 && first_final_score == 5 && first_final_score1 == 6) || (second_final_score == 7 && total2 == 7 && first_final_score == 6 && first_final_score1 == 6) || (second_final_score == 7 && total2 == 7 && first_final_score == 5 && first_final_score1 == 5))
                    {

                        toast.show(); //toast when player two wins
                        final int player1games = wonGames(first_final_score,first_final_score1,first_final_score2);
                        final int player2games = wonGames(second_final_score,total2,second_final_score2);
                        outPut.setText(getIntent().getExtras().getString("player2name")+" won the match!");
                        //player2_won_set++; //set2 =(4,6)(5,7)
                        ///outPut.setText(player2_won_set+", 2K");
                        //for dialogue box start
                        AlertDialog dialog = null; // This will be the returning AlertDialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                        builder.setTitle("You won the match!");
                        builder.setIcon(R.drawable.win_match);
                        builder.setCancelable(false);
                        builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //changes require
                                saveData(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                                viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                            }
                        });
                        builder.setMessage(
                                getIntent().getExtras().getString("player2name")+" won the match!"
                        )       .setCancelable(false) // disallow user to hit the 'back' button
                                .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        dialog = builder.create(); // now the builder is ready to create the dialog
                        timer.cancel();
                        dialog.show();
                        //for dialogue box end
                        mp.start(); //winning music play

                    }
                }
            }
            // check conditions to find final set won by player two
            else if ((second_final_score2 == 5 && first_final_score2 != 7) || (second_final_score2 == 5 && first_final_score2 == 6) || ((second_final_score2 < 5) && (first_final_score2 != 6)) || (second_final_score2 == 6 && first_final_score2 != 7) || (second_final_score2 == 6 && first_final_score2 == 5) || ((second_final_score2 == 6) && (first_final_score2 == 6)))
            {
                if (second_final_score2 <= 7)
                {
                    player1wins.setText("0");
                    player2wins.setText("0");
                    player2Total2.setText("" + total3); //increase value when player one wins third set
                    if((total3 == 6 && first_final_score2 < 5) || (total3 == 7 && first_final_score2 == 5) || (total3 == 7 && first_final_score2 == 6)) {

                        toast.show(); //toast when player two wins
                        final int player1games = wonGames(first_final_score,first_final_score1,first_final_score2);
                        final int player2games = wonGames(second_final_score,second_final_score1,total3);
                        outPut.setText(getIntent().getExtras().getString("player2name")+" won the match!");
                        player2_won_set++; //set1 =(<=4,6)(5,7) //set3 =(4,6)(5,7)
                        ///outPut.setText(player2_won_set+", 2L");
                        //for dialogue box start
                        AlertDialog dialog = null; // This will be the returning AlertDialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                        builder.setTitle("You won the match!");
                        builder.setIcon(R.drawable.win_match);
                        builder.setCancelable(false);
                        builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //changes require
                                saveData(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                                viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                            }
                        });
                        builder.setMessage(
                                getIntent().getExtras().getString("player2name")+" won the match!"
                        )       .setCancelable(false) // disallow user to hit the 'back' button
                                .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        dialog = builder.create(); // builder create the dialog
                        timer.cancel();
                        dialog.show();
                        //for dialogue box end
                        mp.start(); //winning music play

                    }
                }
            }
        }
        else if(score1=="40" && score2 == "40")
        {
            player2wins.setText("AD");
        }
        else if(score1=="AD")
        {
            player1wins.setText("40"); player2wins.setText("40");
        }
        else if(score2=="AD") {
            if ((((second_final_score <= 5 && first_final_score != 7) && first_final_score != 6 && (first_final_score1 == 0 && second_final_score1 == 0)) || ((first_final_score == 6) && (second_final_score == 6))) || (first_final_score == 5 && second_final_score == 6) || (first_final_score == 6 && second_final_score == 5))
            {
                if(first_final_score1 == 0 && second_final_score1 == 0)
                {
                    player2wins.setText("0");
                    player1wins.setText("0");
                    player2Total.setText("" + total1); //increase value when player one wins first set
                    if((total1 == 6 && first_final_score < 5) || (total1 == 7 && first_final_score == 6) || (total1 == 7 && first_final_score == 5)) {

                        toast.show(); //toast when player two wins
                        outPut.setText(getIntent().getExtras().getString("player2name")+" won the first set");
                        ///player2_won_set++;
                        ///outPut.setText(player2_won_set+", 2M");

                    }
                }
            }
            else if ((((second_final_score1 == 5 && first_final_score1 != 7) && (first_final_score2 == 0 && second_final_score2 == 0)) || ((second_final_score1 < 5) && (first_final_score1 != 6)) || ((second_final_score1 == 6) && (first_final_score1 == 6))) || (first_final_score1 == 5 && second_final_score1 == 6))
            {
                if (first_final_score2 == 0 && second_final_score2 == 0)
                {
                    player2wins.setText("0");
                    player1wins.setText("0");
                    player2Total1.setText("" + total2); //increase value when player one wins second set
                    if((total2 == 6 && first_final_score1 < 5) || (total2 == 7 && first_final_score1 == 6) || (total2 == 7 && first_final_score1 == 5) || (total2 == 6 && first_final_score1 == 7) || (total2 == 5 && first_final_score1 == 7)) {

                        toast.show(); //toast when player two wins
                        outPut.setText(getIntent().getExtras().getString("player2name")+" won the second set");
                        ///player2_won_set++;
                        ///outPut.setText(player2_won_set+", 2N");

                    }
                    if((second_final_score == 6 && total2 == 6 && first_final_score1 < 5 && first_final_score == 7)
                            || (second_final_score == 6 && total2 == 6 && first_final_score1 < 5 && first_final_score < 5)
                            || (second_final_score == 7 && total2 == 6 && first_final_score1 < 5 && first_final_score == 6) || (second_final_score == 7 && total2 == 6 && first_final_score1 < 5 && first_final_score == 5) || (second_final_score == 6 && total2 == 7 && first_final_score1 == 6) || (second_final_score == 6 && total2 == 7 && first_final_score1 == 5) || (second_final_score == 7 && total2 == 7 && first_final_score == 6 && first_final_score1 == 5) || (second_final_score == 7 && total2 == 7 && first_final_score == 5 && first_final_score1 == 6) || (second_final_score == 7 && total2 == 7 && first_final_score == 6 && first_final_score1 == 6) || (second_final_score == 7 && total2 == 7 && first_final_score == 5 && first_final_score1 == 5))
                    {

                        toast.show(); //toast when player two wins
                        final int player1games = wonGames(first_final_score,first_final_score1,first_final_score2);
                        final int player2games = wonGames(second_final_score,total2,second_final_score2);
                        outPut.setText(getIntent().getExtras().getString("player2name")+" won the match!");
                        ///player2_won_set++;
                        ///outPut.setText(player2_won_set+", 2O");
                        //for dialogue box start
                        AlertDialog dialog = null; // This will be the returning AlertDialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                        builder.setTitle("You won the match!");
                        builder.setIcon(R.drawable.win_match);
                        builder.setCancelable(false);
                        builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //changes require
                                saveData(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                                viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                            }
                        });
                        builder.setMessage(
                                getIntent().getExtras().getString("player2name")+" won the match!"
                        )       .setCancelable(false) // disallow user to hit the 'back' button
                                .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        dialog = builder.create(); // now the builder is ready to create the dialog
                        timer.cancel();
                        dialog.show();
                        //for dialogue box end
                        mp.start(); //winning music play

                    }
                }
            }
            else if ((second_final_score2 == 5 && first_final_score2 != 7) || (second_final_score2 == 5 && first_final_score2 == 6) || ((second_final_score2 < 5) && (first_final_score2 != 6)) || (second_final_score2 == 6 && first_final_score2 != 7) || (second_final_score2 == 6 && first_final_score2 == 5) || ((second_final_score2 == 6) && (first_final_score2 == 6)))
            {
                if (second_final_score2 <= 7)
                {
                    player1wins.setText("0");
                    player2wins.setText("0");
                    player2Total2.setText("" + total3); //increase value when player one wins third set
                    if((total3 == 6 && first_final_score2 < 5) || (total3 == 7 && first_final_score2 == 5) || (total3 == 7 && first_final_score2 == 6)) {

                        toast.show(); //toast when player two wins
                        final int player1games = wonGames(first_final_score,first_final_score1,first_final_score2);
                        final int player2games = wonGames(second_final_score,second_final_score1,total3);
                        outPut.setText(getIntent().getExtras().getString("player2name")+" won the match!");
                        ///player2_won_set++;
                        ///outPut.setText(player2_won_set+", 2P");
                        //for dialogue box start
                        AlertDialog dialog = null; // This will be the returning AlertDialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                        builder.setTitle("You won the match!");
                        builder.setIcon(R.drawable.win_match);
                        builder.setCancelable(false);
                        builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //changes require
                                saveData(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point);
                                viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "no");
                            }
                        });
                        builder.setMessage(
                                getIntent().getExtras().getString("player2name")+" won the match!"
                        )       .setCancelable(false) // disallow user to hit the 'back' button
                                .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        viewScore(getIntent().getExtras().getString("player2name"), player1_won_set, player2_won_set, player1games, player2games, player1_number_point, player2_number_point, "yes");
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        dialog = builder.create(); // builder create the dialog
                        timer.cancel();
                        dialog.show();
                        //for dialogue box end
                        mp.start(); //winning music play

                    }
                }
            }
        }
        else{ player2wins.setText("0"); player1wins.setText("0"); }
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
        Intent viewScores = new Intent(SecondActivity.this, viewScores.class);
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
        Intent viewScores = new Intent(SecondActivity.this, viewScores.class);
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
        searchForDevices();
        btnReset.post(new Runnable(){
            @Override
            public void run() {
                btnReset.performClick();
            }
        });
        datosMarcador();


    }
    public void resetAllPressed() {

        clickTime = 0;
        player1wins.setText("0"); player2wins.setText("0"); player1Total.setText("0"); player2Total.setText("0"); player1Total1.setText("0"); player2Total1.setText("0"); player1Total2.setText("0"); player2Total2.setText("0"); outPut.setText("");
        counter1 = 0; counter2 = 0; counter3 = 0; counter4 = 0; counter5 = 0; counter6 = 0; player1_won_set = 0; player2_won_set = 0; player1_won_game = 0; player2_won_game = 0; player1_number_point = 0; player2_number_point = 0;
        btnReset.post(new Runnable(){
            @Override
            public void run() {
                btnReset.performClick();
            }
        });
        datosMarcador();

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
        player1wins.setText(lastpoint[0]);
        player2wins.setText(lastpoint[1]);
        player1Total.setText(lastpoint[2]);
        player2Total.setText(lastpoint[3]);
        player1Total1.setText(lastpoint[4]);
        player2Total1.setText(lastpoint[5]);
        player1Total2.setText(lastpoint[6]);
        player2Total2.setText(lastpoint[7]);
    }
    public void functionUndoPressed(View v){
        player1wins.setText(lastpoint[0]);
        player2wins.setText(lastpoint[1]);
        player1Total.setText(lastpoint[2]);
        player2Total.setText(lastpoint[3]);
        player1Total1.setText(lastpoint[4]);
        player2Total1.setText(lastpoint[5]);
        player1Total2.setText(lastpoint[6]);
        player2Total2.setText(lastpoint[7]);
        datosMarcador();

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
        handler = new Handler();
        runnable = new Runnable(){
            @Override
            public void run(){
                enviarhora();
            handler.postDelayed(this, PERIODO);

           }
        };
        handler.postDelayed(runnable, PERIODO);

        Log.d(tag,"In the onResume() event");
        IntentFilter filter = new IntentFilter("com.mirhoseini.itag.button_pressed");
        //IntentFilter miband2filtere1 = new IntentFilter("pulsera1");
        //IntentFilter miband2filtere2 = new IntentFilter("pulsera2");
        this.registerReceiver(br, filter);
        //this.registerReceiver(br, miband2filtere1);
        //this.registerReceiver(br, miband2filtere2);


    }

    public void onPause(){
        super.onPause();
        Log.d(tag,"In the onPause() event");
        handler.removeCallbacks(runnable);
    }

    public void onStop(){
        super.onStop();
        Log.d(tag,"In the onStop() event");
        unregisterReceiver(br);
   // mBluetoothGatt.disconnect();

     }

    public void onDestroy(){
        super.onDestroy();
        Log.d(tag,"In the onDestroy() event");
    //mBluetoothGatt.disconnect();

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

        player1wins.setText(savedInstanceState.getString("PLAYER1WIN"));
        player2wins.setText(savedInstanceState.getString("PLAYER2WIN"));
        player1Total.setText(savedInstanceState.getString("PLAYER1SET1"));
        player2Total.setText(savedInstanceState.getString("PLAYER2SET1"));
        player1Total1.setText(savedInstanceState.getString("PLAYER1SET2"));
        player2Total1.setText(savedInstanceState.getString("PLAYER2SET2"));
        player1Total2.setText(savedInstanceState.getString("PLAYER1SET3"));
        player2Total2.setText(savedInstanceState.getString("PLAYER2SET3"));

        hrs = savedInstanceState.getInt("HRS"); min = savedInstanceState.getInt("MIN"); sec = savedInstanceState.getInt("SEC");
        textViewTimer.setText(String.format("%02d:%02d:%02d", hrs, min, sec-1));
        btnStart.setText(savedInstanceState.getString("BTNSTART"));
        if (btnStart.getText().toString() == "Pause") { timer.start(); btnStart.setText("Pause"); clickTime = 1; }
        else if (btnStart.getText().toString() == "Start") { timer.cancel(); btnStart.setText("Start"); clickTime = 0; }
        counter1 = savedInstanceState.getInt("COUNTER1");
        counter2 = savedInstanceState.getInt("COUNTER2");
        counter3 = savedInstanceState.getInt("COUNTER3");
        counter4 = savedInstanceState.getInt("COUNTER4");
        counter5 = savedInstanceState.getInt("COUNTER5");
        counter6 = savedInstanceState.getInt("COUNTER6");
        player1_won_set = savedInstanceState.getInt("PLAYER1_WON_SET");
        player2_won_set = savedInstanceState.getInt("PLAYER2_WON_SET");
        player1_won_game = savedInstanceState.getInt("PLAYER1_WON_GAME");
        player2_won_game = savedInstanceState.getInt("PLAYER2_WON_GAME");
        player1_number_point = savedInstanceState.getInt("PLAYER1_NUMBER_POINT");
        player2_number_point = savedInstanceState.getInt("PLAYER2_NUMBER_POINT");
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
            Intent helpGame = new Intent(SecondActivity.this, helpGame.class);
            startActivity(helpGame);
            return true;
        }
        if (id == R.id.action_history) {
            Intent viewRecord = new Intent(SecondActivity.this, viewRecords.class);
            startActivity(viewRecord);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /******************************Dropdown Menu (End)*********************************/
    public class MyBroadcastReceiver extends BroadcastReceiver {
        int pulsaciones;
        private static final String TAG = "MyBroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            int player =0;
            //String cadena=intent.getAction();
            StringBuilder sb = new StringBuilder();
            sb.append("Action: " + intent.getAction() + "\n");
            sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");

            Bundle extras =intent.getExtras();
            if (extras !=null) {
             pulsaciones = extras.getInt("CLICKS");
            if (pulsaciones<=2){ pulsaciones=pulsaciones-cantidadDePulsaciones;}

             pulsaciones++;
            }
            String log = sb.toString();

            if (log.contains("equipo1")){

                player=1;
            }
            else if (log.contains("equipo2")){
                player=2;
            }
            else {player=0;}

           /* Log.d(TAG, log);
            Toast.makeText(context, log, Toast.LENGTH_LONG).show();*/
            onKeyPressed(context,intent,player, pulsaciones);
        }
    }
  /*  public class myband2MyBroadcastReceiver extends BroadcastReceiver {
        int pulsaciones=2;
        private static final String TAG = "miband2MyBroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            int player =0;*/
            //String cadena=intent.getAction();
        /*    StringBuilder sb = new StringBuilder();
            sb.append("Action: " + intent.getAction() + "\n");
            sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");*/
            /*Bundle extras =intent.getExtras();
            if (extras !=null) {
                pulsaciones = extras.getInt("CLICKS");
                pulsaciones++;
            }*/
         /*   String log = sb.toString();

            if (log.contains("equipo1")){

                player=1;
            }
            else if (log.contains("equipo2")){
                player=2;
            }
            else {player=0;}*/

           /* Log.d(TAG, log);
            Toast.makeText(context, log, Toast.LENGTH_LONG).show();*/
           /* onKeyPressed(context,intent,player, pulsaciones);
        }
    }*/



    public void onKeyPressed(Context context,Intent intent,int player,int currentButtonPressCount) {

        if (cantidadDePulseras==1 && (player==1 || player==2)){
           /*long timeSinceLastPress = System.currentTimeMillis() - currentButtonPressTime;

            if ((currentButtonPressTime == 0L) || (timeSinceLastPress < buttonPressMaxDelay)) {
                currentButtonPressCount++;
                //String log = "apretado";
                //Toast.makeText(context, log, Toast.LENGTH_LONG).show();
                currentButtonPressTime = System.currentTimeMillis();
                player1btn.setBackgroundColor(Color.parseColor("#"+colorpulsador));
                player2btn.setBackgroundColor(Color.parseColor("#"+colorpulsador));
                colorpulsador=633333+1000*currentButtonPressCount;
            }


            else {

                if (System.currentTimeMillis() > currentButtonPressTime+buttonPressMaxDelayreset){
                    currentButtonPressCount=1;
                    currentButtonPressTime = System.currentTimeMillis();
                    colorpulsador=633333;
                    player1btn.setBackgroundColor(Color.parseColor("#"+colorpulsador));
                    player2btn.setBackgroundColor(Color.parseColor("#"+colorpulsador));
                }
                else {
                    player1btn.setBackgroundColor(Color.parseColor("#D32F2F"));
                    player2btn.setBackgroundColor(Color.parseColor("#D32F2F"));
                    //String log="boton validado y pulsado " + currentButtonPressCount + " veces";
                    //Toast.makeText(context, log, Toast.LENGTH_LONG).show();
                    */
                    if(currentButtonPressCount==1){
                        PlayerOneWonPressed();}

                    else if(currentButtonPressCount==2){
                        PlayerTwoWonPressed();}

                    //else if(currentButtonPressCount==3){
                    //    timer.start();
                    //    btnStart.setText("Pause");
                    //    clickTime = 1;}

                    else if(currentButtonPressCount==4){
                        if (clickTime == 0) {
                            timer.start();
                            btnStart.setText("Pause");
                            clickTime = 1;
                        } else if (clickTime == 1) {
                            timer.cancel();
                            btnStart.setText("Start");
                            clickTime = 0;
                        }
                        enviarhora();
                        //timer.cancel();
                        //btnStart.setText("Start");
                        //clickTime = 0;
                    }

                    else if(currentButtonPressCount==5){textViewTimer.setText("00:00:00");
                        timer.cancel();
                        btnStart.setText("Start");
                        hrs = 0;  min = 0; sec = 0; clickTime = 0;
                    }

                    else if(currentButtonPressCount==6){
                        functionUndoPressed();

                    }
                    else if(currentButtonPressCount==7){
                        resetAllPressed();
                        mTimer = new Timer();
                        searchForDevices();
                    }
                    else if(currentButtonPressCount==8){
                        //if (clickchange == 0) {
                            cambiapulserasclicks();
                        //}
                            /*
                            cantidadDePulseras = 1;
                            player1btn.setBackgroundColor(Color.parseColor("#D32F2F"));
                            player2btn.setBackgroundColor(Color.parseColor("#D32F2F"));
                            clickchange=1;
                        }
                        else if (clickchange == 1) {
                            cantidadDePulsaciones = 1;
                            player1btn.setBackgroundColor(Color.parseColor("#197600"));
                            player2btn.setBackgroundColor(Color.parseColor("#197600"));
                            clickchange=2;
                        }
                        else if (clickchange == 2) {
                            cantidadDePulseras=2;
                            player1btn.setBackgroundColor(Color.parseColor("#D32F2F"));
                            player2btn.setBackgroundColor(Color.parseColor("#197600"));
                            clickchange=3;
                        }
                        else if (clickchange == 3) {
                            cantidadDePulsaciones=0;
                            player1btn.setBackgroundColor(Color.parseColor("#D32F2F"));
                            player2btn.setBackgroundColor(Color.parseColor("#1976D2"));
                            clickchange=0;
                        }*/
                   //     cantidadDePulseras=1;
                   //     player1btn.setBackgroundColor(Color.parseColor("#D32F2F"));
                   //     player2btn.setBackgroundColor(Color.parseColor("#D32F2F"));
                    }

                    /*
                    currentButtonPressCount = 0;
                    currentButtonPressTime = 0;
                    currentButtonActionId=0;
*/
                    datosMarcador();


                }

           // }


       // }


        else if (cantidadDePulseras==2 && player==1 )//&& (currentButtonActionId==0 || currentButtonActionId==player)){
          //  long timeSinceLastPress = System.currentTimeMillis() - currentButtonPressTime;

          /*  if ((currentButtonPressTime == 0L) || (timeSinceLastPress < buttonPressMaxDelay)) {
                currentButtonPressCount++;
                currentButtonActionId=player;
                currentButtonPressTime = System.currentTimeMillis();

                player1btn.setBackgroundColor(Color.parseColor("#"+colorpulsador));
                colorpulsador=633333+1000*currentButtonPressCount;
            }


            else {

                if (System.currentTimeMillis() > currentButtonPressTime+buttonPressMaxDelayreset){
                    currentButtonPressCount=1;
                    currentButtonPressTime = System.currentTimeMillis();
                    colorpulsador=633333;
                    player1btn.setBackgroundColor(Color.parseColor("#"+colorpulsador));

                }
                else {
                    player1btn.setBackgroundColor(Color.parseColor("#D32F2F"));
                    colorpulsador=333333;
                    */{
                    if(currentButtonPressCount==1){
                        PlayerOneWonPressed();}

                    else if(currentButtonPressCount==2){
                        /*       PlayerTwoWonPressed();*/}

                   // else if(currentButtonPressCount==3){
                   //     timer.start();
                   //     btnStart.setText("Pause");
                   //     clickTime = 1;}

                    else if(currentButtonPressCount==4){
                        if (clickTime == 0) {
                            timer.start();
                            btnStart.setText("Pause");
                            clickTime = 1;
                        } else if (clickTime == 1) {
                            timer.cancel();
                            btnStart.setText("Start");
                            clickTime = 0;
                        }
                        enviarhora();
                        //timer.cancel();
                        //btnStart.setText("Start");
                        //clickTime = 0;
                    }

                    else if(currentButtonPressCount==5){textViewTimer.setText("00:00:00");
                        timer.cancel();
                        btnStart.setText("Start");
                        hrs = 0;  min = 0; sec = 0; clickTime = 0;
                    }

                    else if(currentButtonPressCount==6){
                        functionUndoPressed();

                    }
                    else if(currentButtonPressCount==7){
                        resetAllPressed();
                        mTimer = new Timer();
                        searchForDevices();
                    }
                    else if(currentButtonPressCount==8){
                        cambiapulserasclicks();/*
                        if (clickchange == 0) {
                            cantidadDePulseras = 1;
                            player1btn.setBackgroundColor(Color.parseColor("#D32F2F"));
                            player2btn.setBackgroundColor(Color.parseColor("#D32F2F"));
                            clickchange=1;

                        }
                        else if (clickchange == 1) {
                            cantidadDePulsaciones = 1;
                            player1btn.setBackgroundColor(Color.parseColor("#197600"));
                            player2btn.setBackgroundColor(Color.parseColor("#197600"));
                            clickchange=2;

                        }
                        else if (clickchange == 2) {
                            cantidadDePulseras=2;
                            player1btn.setBackgroundColor(Color.parseColor("#D32F2F"));
                            player2btn.setBackgroundColor(Color.parseColor("#197600"));
                            clickchange=3;

                        }
                        else if (clickchange == 3) {
                            cantidadDePulsaciones=0;
                            player1btn.setBackgroundColor(Color.parseColor("#D32F2F"));
                            player2btn.setBackgroundColor(Color.parseColor("#1976D2"));
                            clickchange=0;

                        }*/
                    }


                  /*  currentButtonPressCount = 0;
                    currentButtonPressTime = 0;
                    currentButtonActionId=0;*/
                    datosMarcador();


                }

          //  }


        //}
        else if (cantidadDePulseras==2 && player==2 )/* && (currentButtonActionId==0 || currentButtonActionId==player)){
            long timeSinceLastPress = System.currentTimeMillis() - currentButtonPressTime;

            if ((currentButtonPressTime == 0L) || (timeSinceLastPress < buttonPressMaxDelay)) {
                currentButtonPressCount++;
                currentButtonActionId=player;
                //String log = "apretado";
                //Toast.makeText(context, log, Toast.LENGTH_LONG).show();
                currentButtonPressTime = System.currentTimeMillis();

                player2btn.setBackgroundColor(Color.parseColor("#"+colorpulsador));
                colorpulsador=633333+1000*currentButtonPressCount;
            }


            else {
                if (System.currentTimeMillis() > currentButtonPressTime+buttonPressMaxDelayreset){
                    currentButtonPressCount=1;
                    currentButtonPressTime = System.currentTimeMillis();
                    colorpulsador=633333;
                    player2btn.setBackgroundColor(Color.parseColor("#"+colorpulsador));
                }
                else {
                    player2btn.setBackgroundColor(Color.parseColor("#1976D2"));
                    colorpulsador=633333;
                    */{
                    if(currentButtonPressCount==1){
                        PlayerTwoWonPressed();}

                    else if(currentButtonPressCount==2){
                        /*  PlayerTwoWonPressed();*/}

                   // else if(currentButtonPressCount==3){
                   //     timer.start();
                   //     btnStart.setText("Pause");
                   //     clickTime = 1;}

                    else if(currentButtonPressCount==4){
                        if (clickTime == 0) {
                            timer.start();
                            btnStart.setText("Pause");
                            clickTime = 1;
                        } else if (clickTime == 1) {
                            timer.cancel();
                            btnStart.setText("Start");
                            clickTime = 0;
                        }
                        enviarhora();

                     //   timer.cancel();
                     //   btnStart.setText("Start");
                     //   clickTime = 0;
                    }

                    else if(currentButtonPressCount==5){textViewTimer.setText("00:00:00");
                        timer.cancel();
                        btnStart.setText("Start");
                        hrs = 0;  min = 0; sec = 0; clickTime = 0;
                    }

                    else if(currentButtonPressCount==6){
                        functionUndoPressed();

                    }
                    else if(currentButtonPressCount==7){
                        resetAllPressed();
                        mTimer = new Timer();
                        searchForDevices();
                    }

                    else if(currentButtonPressCount==8) {
                        cambiapulserasclicks();
                     /*   if (clickchange == 0) {
                            cantidadDePulseras = 1;
                            player1btn.setBackgroundColor(Color.parseColor("#D32F2F"));
                            player2btn.setBackgroundColor(Color.parseColor("#D32F2F"));
                            clickchange=1;

                        }
                        else if (clickchange == 1) {
                            cantidadDePulsaciones = 1;
                            player1btn.setBackgroundColor(Color.parseColor("#D197600"));
                            player2btn.setBackgroundColor(Color.parseColor("#197600"));
                            clickchange=2;
                        }
                        else if (clickchange == 2) {
                            cantidadDePulseras=2;
                            player1btn.setBackgroundColor(Color.parseColor("#D32F2F"));
                            player2btn.setBackgroundColor(Color.parseColor("#197600"));
                            clickchange=3;
                        }
                        else if (clickchange == 3) {
                            cantidadDePulsaciones=0;
                            player1btn.setBackgroundColor(Color.parseColor("#D32F2F"));
                            player2btn.setBackgroundColor(Color.parseColor("#1976D2"));
                            clickchange=0;
                        }
                        //cantidadDePulseras = 1;
                        //if (cantidadDePulsaciones == 0) {
                        //    player1btn.setBackgroundColor(Color.parseColor("#D32F2F"));
                        //    player2btn.setBackgroundColor(Color.parseColor("#D32F2F"));
                        //} else if (cantidadDePulsaciones == 1) {
                        //    player1btn.setBackgroundColor(Color.parseColor("#197600"));
                        //    player2btn.setBackgroundColor(Color.parseColor("#197600"));
                        //}
                    */}

                    currentButtonPressCount = 0;
                    currentButtonPressTime = 0;
                    currentButtonActionId=0;
                    datosMarcador();

                }





     /*   else if (cantidadDePulseras==2 && (currentButtonActionId>0 && currentButtonActionId!=player )){
            currentButtonPressTime = System.currentTimeMillis();
            currentButtonPressCount = 1;
            currentButtonActionId=player;

        }*/
    }
    public void cambiapulserasclicks(){
        if (clickchange == 0) {
            cantidadDePulseras = 1;
            player1btn.setBackgroundColor(Color.parseColor("#D32F2F"));
            player2btn.setBackgroundColor(Color.parseColor("#D32F2F"));
            clickchange=1;
            sendMessage("P U L  1 C L I 12");
            try { Thread.sleep(4000); }
            catch (InterruptedException ex) { android.util.Log.d("YourApplicationName", ex.toString()); }
            datosMarcador();
        }
        else if (clickchange == 1) {
            cantidadDePulsaciones = 1;
            player1btn.setBackgroundColor(Color.parseColor("#197600"));
            player2btn.setBackgroundColor(Color.parseColor("#197600"));
            clickchange=2;
            sendMessage("P U L  1 C L I 23");
            try { Thread.sleep(4000); }
            catch (InterruptedException ex) { android.util.Log.d("YourApplicationName", ex.toString()); }
            datosMarcador();
        }
        else if (clickchange == 2) {
            cantidadDePulseras=2;
            player1btn.setBackgroundColor(Color.parseColor("#D32F2F"));
            player2btn.setBackgroundColor(Color.parseColor("#197600"));
            clickchange=3;
            sendMessage("P U L  2 C L I 22");
            try { Thread.sleep(4000); }
            catch (InterruptedException ex) { android.util.Log.d("YourApplicationName", ex.toString()); }
            datosMarcador();
        }
        else if (clickchange == 3) {
            cantidadDePulsaciones=0;
            player1btn.setBackgroundColor(Color.parseColor("#D32F2F"));
            player2btn.setBackgroundColor(Color.parseColor("#1976D2"));
            clickchange=0;
            sendMessage("P U L  2 C L I 11");
            try { Thread.sleep(4000); }
            catch (InterruptedException ex) { android.util.Log.d("YourApplicationName", ex.toString()); }
            datosMarcador();
        }
        return;
    }

    public void datosMarcador(){//para enviar a la pantalla de leds

        String juegoj1 = player1wins.getText().toString();
        int longitud=juegoj1.length();
        if (longitud<2){juegoj1="0"+juegoj1;}
        String juegoj2 = player2wins.getText().toString();
        longitud=juegoj2.length();
        if (longitud<2){juegoj2="0"+juegoj2;}
        String set1j1 = player1Total.getText().toString();
        String set1j2 = player2Total.getText().toString();
        String set2j1 = player1Total1.getText().toString();
        String set2j2 = player2Total1.getText().toString();
        String set3j1 = player1Total2.getText().toString();
        String set3j2 = player2Total2.getText().toString();
        String marcador=set1j1+" "+set2j1+" "+set3j1+" "+juegoj1+" "+set1j2+" "+set2j2+" "+set3j2+" "+juegoj2;
        marcador=marcador.replace("0","O");
        sendMessage(marcador);
        return;
        }


    public void MarcadorActual(){//para recuperar en caso de error
        String juegoj1 = player1wins.getText().toString();
        String juegoj2 = player2wins.getText().toString();
        String set1j1 = player1Total.getText().toString();
        String set1j2 = player2Total.getText().toString();
        String set2j1 = player1Total1.getText().toString();
        String set2j2 = player2Total1.getText().toString();
        String set3j1 = player1Total2.getText().toString();
        String set3j2 = player2Total2.getText().toString();
        lastpoint[0]=juegoj1;lastpoint[1]=juegoj2;lastpoint[2]=set1j1;lastpoint[3]=set1j2;lastpoint[4]=set2j1;
        lastpoint[5]=set2j2;lastpoint[6]=set3j1;lastpoint[7]=set3j1;
        //String marcador=juegoj1+"-"+juegoj2+" "+set1j1+" "+set1j2+" "+set2j1+" "+set2j2+" "+set3j1+" "+set3j2;

    }
   /* final Runnable HoraenMarcador =new Runnable() {
        @Override
        public void run() { Date d=new Date();
        hora= (TextView) findViewById(R.id.txtHora);
        SimpleDateFormat ho=new SimpleDateFormat("h:mm a");
        String horaString = ho.format(d);
        hora.setText(horaString);

    }};*/
   private void searchForDevices() {
       statusUpdate("Searching for devices ...");

       if (mTimer != null) {
           mTimer.cancel();
       }

       scanLeDevice();
       mTimer = new Timer();
       mTimer.schedule(new TimerTask() {
           @Override
           public void run() {
               statusUpdate("Search complete");
               findGroveBLE();
           }
       }, SCAN_PERIOD);
   }

    private void findGroveBLE() {
        if (mDevices == null || mDevices.size() == 0) {
            statusUpdate("No BLE devices found");
            return;
        } else if (mDevice == null) {
            statusUpdate("Unable to find Grove BLE");
            return;
        } else {
            statusUpdate("Found Grove BLE V1");
            statusUpdate("Address: " + mDevice.getAddress());
            connectDevice();
        }
    }
    private boolean connectDevice() {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDevice.getAddress());
        if (device == null) {
            statusUpdate("Unable to connect");
            return false;
        }
        // directly connect to the device
        statusUpdate("Connecting ...");
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        return true;
    }
    private boolean connectDevice(String address) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            statusUpdate("Unable to connect");
            return false;
        }
        // directly connect to the device
        statusUpdate("Connecting ...");
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        return true;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                statusUpdate("Connected");
                statusUpdate("Searching for services");
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                statusUpdate("Device disconnected");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> gattServices = mBluetoothGatt.getServices();

                for (BluetoothGattService gattService : gattServices) {
                    statusUpdate("Service discovered: " + gattService.getUuid());
                    if (GROVE_SERVICE.equals(gattService.getUuid().toString())) {
                        mBluetoothGattService = gattService;
                        statusUpdate("Found communication Service");
                        sendMessage("C O N OO E C T OO");
                        try { Thread.sleep(2000); }
                        catch (InterruptedException ex) { android.util.Log.d("YourApplicationName", ex.toString()); }
                        datosMarcador();



                    }
                }
            } else {
                statusUpdate("onServicesDiscovered received: " + status);
            }
                }
    };

    private void sendMessage(String marcador) {
        if (mBluetoothGattService == null)
            return;

        statusUpdate("Finding Characteristic...");
        BluetoothGattCharacteristic gattCharacteristic =
                mBluetoothGattService.getCharacteristic(UUID.fromString(CHARACTERISTIC_TX));

        if (gattCharacteristic == null) {
            statusUpdate("Couldn't find TX characteristic: " + CHARACTERISTIC_TX);
            return;
        }

        statusUpdate("Found TX characteristic: " + CHARACTERISTIC_TX);

        statusUpdate("Enviado resultados" + marcador);

        String msg = marcador;

        byte b = 0x00;
        byte[] temp = msg.getBytes();
        byte[] tx = new byte[temp.length + 1];
        tx[0] = b;

        for (int i = 0; i < temp.length; i++)
            tx[i + 1] = temp[i];

        gattCharacteristic.setValue(tx);
        mBluetoothGatt.writeCharacteristic(gattCharacteristic);
        return;
    }

    private void scanLeDevice() {
        new Thread() {

            @Override
            public void run() {
                mBluetoothAdapter.startLeScan(mLeScanCallback);

                try {
                    Thread.sleep(SCAN_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }.start();
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                             byte[] scanRecord) {
            if (device != null) {
                if (mDevices.indexOf(device) == -1)//to avoid duplicate entries
                {
                    if (DEVICE_NAME.equals(device.getName())) {
                        mDevice = device;//we found our device!
                    }
                    mDevices.add(device);
                    statusUpdate("Found device " + device.getName());
                }
            }
        }
    };

    //output helper method
    private void statusUpdate(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.w("BLE", msg);
                mainText.setText(mainText.getText() + "\r\n" + msg);
            }
        });
    }
    private void enviarhora(){
        Calendar calendario = Calendar.getInstance();
        int horaactual=calendario.get(Calendar.HOUR_OF_DAY);
        int minutoactual=calendario.get(Calendar.MINUTE);
        int dia=calendario.get(Calendar.DATE);
        String stringhora  = Integer.toString(horaactual);
        String stringminuto  = Integer.toString(minutoactual);
        String stringdia  = Integer.toString(dia);

        if (stringhora.length()==1){stringhora=" "+stringhora;}
        if (stringminuto.length()==1){stringminuto=" "+stringminuto;}
        String message=(stringhora.substring(0,1)+" "+stringhora.substring(1,2)+" : "+stringminuto.substring(0,1)+stringminuto.substring(1,2)+" "+diaenletras()+" "+stringdia);
        //message=message.replace("0","O");



        sendMessage(message);
        try { Thread.sleep(5000); }
        catch (InterruptedException ex) { android.util.Log.d("YourApplicationName", ex.toString()); }
        datosMarcador();
        }

private String diaenletras(){
    Calendar calendario = Calendar.getInstance();
    String texto = null;
    int dia=calendario.get(Calendar.DAY_OF_WEEK);
    if(dia==Calendar.SUNDAY){
        texto="D O M";
    }if(dia==Calendar.MONDAY){
        texto="L U N";//Lunes
    }
    if(dia==Calendar.TUESDAY){
        texto="M A R";
    }
    if(dia==Calendar.WEDNESDAY){
        texto="M I E";
    }if(dia==Calendar.THURSDAY){
        texto="J U E";//Lunes
    }
    if(dia==Calendar.FRIDAY){
        texto="V I E";
    }
    if (dia==Calendar.SATURDAY){
        texto="S A B";
    }
    return texto;
}

}