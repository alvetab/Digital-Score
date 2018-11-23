package com.mirhoseini.itag;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by DELL on 20-Jul-16.
 */
public class view_list_score extends AppCompatActivity {

    String tag = "Tennis Score Keeper";
    DBHandler cAdapter = new DBHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_score_detail);

        String id = getIntent().getExtras().getString("id");
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

        cAdapter.open();
        Cursor curRes = cAdapter.getContact(id);
        if(curRes!=null){
            if(curRes.getCount()>=1){
                curRes.moveToFirst();
                player1.setText(curRes.getString(1));
                player2.setText(curRes.getString(2));
                winner.setText(curRes.getString(3)+" won this match!");
                player1set.setText(curRes.getString(4));
                player2set.setText(curRes.getString(5));
                player1game.setText(curRes.getString(6));
                player2game.setText(curRes.getString(7));
                player1point.setText(curRes.getString(8));
                player2point.setText(curRes.getString(9));
                matchTime.setText(curRes.getString(10)+"("+curRes.getString(11)+")");
            }
            else{
                showToast("Record not found");
            }
        }

        cAdapter.close();
    }

    public void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    //to go in previous page
    public void btnReturn(View v){
        finish();
    }

    /*=====================================================================================*/

    //different actions
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

        switch (id){
            case R.id.action_help:
                Intent helpGame = new Intent(view_list_score.this, helpGame.class);
                helpGame.putExtra("page", "viewRecords");
                startActivity(helpGame);
                break;
            case R.id.action_history:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("You are already in History page!")
                        .setCancelable(false)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
        }

        return super.onOptionsItemSelected(item);
    }

}
