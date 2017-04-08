package edu.msu.ahmedibr.connect4_team17;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Dialog box for instructions
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.instructions)
                .setMessage(R.string.dialog_message)
                .setPositiveButton("OK", null)
                .show();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_instructions, menu);
        return true;
    }

    /**
     * Handles the Start Game button click
     * @param view: the main activity view
     */
    public void onStartGame(View view) {
        String playerOneName = ((TextView)findViewById(R.id.playerOneInput)).getText().toString();
        String playerTwoName = ((TextView)findViewById(R.id.playerTwoInput)).getText().toString();

        if (playerOneName.isEmpty() || playerTwoName.isEmpty()) {
            Snackbar.make(findViewById(R.id.main_coordinator_layout),
                    R.string.names_empty, Snackbar.LENGTH_LONG).show();
        } else {
            // Start the game
            Intent intent = new Intent(this, GameActivity.class);

            intent.putExtra(GameActivity.PLAYER_ONE_NAME_BUNDLE_KEY, playerOneName);
            intent.putExtra(GameActivity.PLAYER_TWO_NAME_BUNDLE_KEY, playerTwoName);

            startActivity(intent);

            // done with the opening activity
            finish();
        }
    }
}
