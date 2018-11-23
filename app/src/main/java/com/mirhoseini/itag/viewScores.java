package com.mirhoseini.itag;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by DELL on 07-Jun-16.
 */
public class viewScores extends AppCompatActivity {

    String tag = "Tennis Score Keeper";
    DBHandler cAdapter = new DBHandler(this);
    int savedata = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_scores);

        TextView player1 = (TextView) findViewById(R.id.player1);
        TextView player2 = (TextView) findViewById(R.id.player2);
        TextView player1set = (TextView) findViewById(R.id.player1set);
        TextView player2set = (TextView) findViewById(R.id.player2set);
        TextView player1game = (TextView) findViewById(R.id.player1game);
        TextView player2game = (TextView) findViewById(R.id.player2game);
        TextView player1point = (TextView) findViewById(R.id.player1point);
        TextView player2point = (TextView) findViewById(R.id.player2point);
        TextView winner = (TextView) findViewById(R.id.winner);
        TextView matchTime = (TextView) findViewById(R.id.matchTime);


        player1.setText(getIntent().getExtras().getString("player1name"));
        player2.setText(getIntent().getExtras().getString("player2name"));
        player1set.setText(getIntent().getExtras().getString("player1set"));
        player2set.setText(getIntent().getExtras().getString("player2set"));
        player1game.setText(getIntent().getExtras().getString("player1game"));
        player2game.setText(getIntent().getExtras().getString("player2game"));
        player1point.setText(getIntent().getExtras().getString("player1point"));
        player2point.setText(getIntent().getExtras().getString("player2point"));
        matchTime.setText(getIntent().getExtras().getString("matchTime"));
        String save = getIntent().getExtras().getString("save"); String no = "no";
        if (getIntent().getExtras().getString("winner") != null){
            winner.setText(getIntent().getExtras().getString("winner")+" won the Match!");
        }
        if (save.equals(no)){
            Button b = (Button) findViewById(R.id.saveData);
            b.setVisibility(View.GONE);
        }
    }

    /******************************Store data into database (Start)************************************/
    public void saveData(View v){
        TextView player1 = (TextView) findViewById(R.id.player1);
        TextView player2 = (TextView) findViewById(R.id.player2);
        TextView player1set = (TextView) findViewById(R.id.player1set);
        TextView player2set = (TextView) findViewById(R.id.player2set);
        TextView player1game = (TextView) findViewById(R.id.player1game);
        TextView player2game = (TextView) findViewById(R.id.player2game);
        TextView player1point = (TextView) findViewById(R.id.player1point);
        TextView player2point = (TextView) findViewById(R.id.player2point);
        TextView winner = (TextView) findViewById(R.id.winner);
        TextView matchTime = (TextView) findViewById(R.id.matchTime);

        String player1name = player1.getText().toString();
        String player2name = player2.getText().toString();
        String winer = winner.getText().toString();

        int oneset = Integer.parseInt(player1set.getText().toString());
        int twoset = Integer.parseInt(player2set.getText().toString());
        int onegame = Integer.parseInt(player1game.getText().toString());
        int twogame = Integer.parseInt(player2game.getText().toString());
        int onepoint = Integer.parseInt(player1point.getText().toString());
        int twopoint = Integer.parseInt(player2point.getText().toString());
        String time = matchTime.getText().toString();

        if(savedata == 0) {
            cAdapter.open();
            long result = cAdapter.insertContact(player1name, player2name, winer, oneset, twoset, onegame, twogame, onepoint, twopoint, time);
            savedata = 1;
            if (result < 0) {
                showToast("Error Inserting Data");
            } else {
                showToast("Data Inserted Successfully");
            }
            cAdapter.close();
        }else{
            showToast("Data has been already Inserted");
        }

    }
    public void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    /******************************Store data into database (End)************************************/

    //to go in previous page
    public void btnReturn(View v){
        finish();
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

    public void onResume() {
        super.onResume();
        Log.d(tag, "In the onResume() event");
    }

    public void onPause() {
        super.onPause();
        Log.d(tag, "In the onPause() event");
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
                Intent helpGame = new Intent(viewScores.this, helpGame.class);
                helpGame.putExtra("page", "MainActivitymarcador");
                startActivity(helpGame);
                break;
            case R.id.action_history:
                Intent viewRecord = new Intent(viewScores.this, viewRecords.class);
                viewRecord.putExtra("page", "MainActivitymarcador");
                startActivity(viewRecord);
        }
        return super.onOptionsItemSelected(item);
    }

}
