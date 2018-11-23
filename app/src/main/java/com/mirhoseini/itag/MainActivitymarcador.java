package com.mirhoseini.itag;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivitymarcador extends AppCompatActivity {

    String tag = "Tennis Score Keeper";
    String prefsName = "Player 1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_marcador);

        final EditText player1 = (EditText) findViewById(R.id.player1name); //find player one name by id
        final EditText player2 = (EditText) findViewById(R.id.player2name); //find player two name by id
        Button start = (Button) findViewById(R.id.btnStart); //find start button

        // obtains the SharedPreferences object
        SharedPreferences prefs = getSharedPreferences(prefsName, MODE_PRIVATE);

        // reads from sharedPreferences
        // The second argument defines the return value if it does not exist
        String player1name = prefs.getString("player1name", null);
        String player2name = prefs.getString("player2name", null);

        if(player1name != null && player1name != null){
            // writes to the players name
            player1.setText(player1name);
            player2.setText(player2name);
        }

        //showSoftKeyboard(player1);
        assert start != null;
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player1.getText().toString().length() == 0 || player2.getText().toString().length() == 0) {
                    fieldValidation();
                } else {
                   /* Intent intent = new Intent(MainActivitymarcador.this, MainActivity.class);
                    startActivity(intent);*/
                    Intent intent = new Intent(MainActivitymarcador.this, SecondActivity.class);
                    intent.putExtra("player1name", player1.getText().toString());
                    intent.putExtra("player2name", player2.getText().toString());
                    startActivity(intent);
                }
            }
        });

    }

    /*=====================================================================================*/

    /******************************
     * Game Exit button(Start)
     **********************************/

    //Create method: by clicking back button can exit from the game
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivitymarcador.this.finish();
                        finish();
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

    public void fieldValidation() {
        final EditText player1 = (EditText) findViewById(R.id.player1name); //find player one name by id
        final EditText player2 = (EditText) findViewById(R.id.player2name); //find player two name by id
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please fill players name.")
                .setCancelable(false)
                .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (player1.getText().toString() == "") {
                            player1.requestFocus();
                        } else if (player2.getText().toString() == "") {
                            player2.requestFocus();
                        }
                    }
                });
                /*.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });*/
        AlertDialog alert = builder.create();
        alert.show();
    }

    /******************************
     * Game Exit button(End)
     ************************************/
    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    //different actions
    public void onStart() {
        super.onStart();
        Log.d(tag, "In the onStart() event");
    }

    public void onRestart() {
        super.onRestart();
        Log.d(tag, "In the onRestart() event");
    }



    public void onPause() {
        super.onPause();
        Log.d(tag, "In the onPause() event");
        // obtains the SharedPreferences object
        SharedPreferences prefs = getSharedPreferences(prefsName, MODE_PRIVATE);

        // obtains an editor to it - needed only for writing
        SharedPreferences.Editor editor = prefs.edit();

        EditText player1name = (EditText) findViewById(R.id.player1name);
        EditText player2name = (EditText) findViewById(R.id.player2name);

        // includes or changes the value of variables
        editor.putString("player1name", player1name.getText().toString());
        editor.putString("player2name", player2name.getText().toString());

        // we need to commit at the end
        editor.commit();
    }

    public void onStop() {
        super.onStop();
        Log.d(tag, "In the onStop() event");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(tag, "In the onDestroy() event");
    }

    //to open keyboad onload
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_help:
                Intent helpGame = new Intent(MainActivitymarcador.this, helpGame.class);
                helpGame.putExtra("page", "MainActivitymarcador");
                startActivity(helpGame);
                break;
            case R.id.action_history:
                Intent viewRecord = new Intent(MainActivitymarcador.this, viewRecords.class);
                viewRecord.putExtra("page", "MainActivitymarcador");
                startActivity(viewRecord);
        }
        return super.onOptionsItemSelected(item);
    }


}

