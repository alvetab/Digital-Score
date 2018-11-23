package com.mirhoseini.itag;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

/**
 * Created by DELL on 01-Jun-16.
 */

public class viewRecords extends ListActivity {

    String tag = "Tennis Score Keeper";
    DBHandler cAdapter = new DBHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.view_history);
        ListRecord();
    }

    private void ListRecord() {
        cAdapter.open();

        // obtains a cursor with the data {query results)
        Cursor curRes = cAdapter.getAllContacts();

        // cursor columns to be presented in the ListView
        String[] columns = new String[] { "_id", "date", "player1name", "player1sets", "player2sets", "player2name" };

        // TextViews that will present the data
        int[] views = new int[] { R.id.id, R.id.date, R.id.player1name, R.id.one, R.id.two, R.id.player2name };

        SimpleCursorAdapter adapter;
        // A new Adapter (SimpleCursorAdapter) constructor called
        if (android.os.Build.VERSION.SDK_INT < 11) {
            adapter = new SimpleCursorAdapter(this, R.layout.view_records, curRes, columns, views);
        } else {
            adapter = new SimpleCursorAdapter(this, R.layout.view_records, curRes, columns, views, 0);
        }

        // associates the adapter to the ListView
        this.setListAdapter(adapter);

        if(curRes.getCount()==0){
            showToast("The table is empty");
        }

        cAdapter.close();
    }

    public void showResult(View v) {

        Button b = (Button)v;
        final String gid = b.getText().toString();
        final int del = Integer.parseInt(gid);

        AlertDialog dialog = null; // This will be the returning AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(viewRecords.this);
        builder.setTitle("Recode List View!");
        builder.setIcon(R.drawable.win_match);
        builder.setCancelable(false);
        builder.setMessage(
                "Please choose an option!"
        ).setCancelable(false) // disallow user to hit the 'back' button
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Details", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        Intent viewRecord = new Intent(viewRecords.this, view_list_score.class);
                        viewRecord.putExtra("id", String.valueOf(gid));
                        startActivity(viewRecord);
                    }
                });
                builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        cAdapter.open();
                        int record_id = cAdapter.deleteContact(del);
                        showToast("Record Deleted Successfully.");
                        ListRecord();
                        cAdapter.close();

                    }
                });
        dialog = builder.create();
        dialog.show();
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
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}