package com.example.k.lab6;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GamesList extends AppCompatActivity {

    private int game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Geting info about choosed game
        Bundle extras = getIntent().getExtras();
        game = extras.getInt("gra");

        //Implement Refresh by default gesture (slide down)
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshGameList();
                    }
                }
        );

        refreshGameList();

        //Implement onClick Action - chose game for play
        ListView list = (ListView)findViewById(R.id.listView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                //Seting loading spinner (while reciving message)
                ProgressBar spinner = (ProgressBar)findViewById(R.id.progressBar1);
                spinner.setVisibility(View.VISIBLE);
                //Get info about chosen game
                String game_id = arg0.getItemAtPosition(arg2).toString().replace("ID: ","");

                //Creating intent for custom Service
                Intent intencja = new Intent(
                        getApplicationContext(),
                        HttpService.class);
                //Creating PendingIntent - for getting list back
                PendingIntent pendingResult = createPendingResult(HttpService.GAME_INFO, new Intent(),0);
                //Set data - URL
                if(game == R.id.inRow){
                    intencja.putExtra(HttpService.URL, HttpService.LINES+game_id);
                }else{
                    //TODO - geting ticTacToe games list
                }
                //Set data - method of request
                intencja.putExtra(HttpService.METHOD, HttpService.GET);
                //Set data - intent for result
                intencja.putExtra(HttpService.RETURN, pendingResult);
                //Start unBound Service in another Thread
                startService(intencja);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intencja = null;
                switch (game) {
                    case R.id.inRow:
                        intencja = new Intent(getApplicationContext(), inRow.class);
                        //Sending info about game status and taken moves
                        intencja.putExtra(inRow.STATUS, inRow.NEW_GAME);
                        //as new game there is no previous moves
                        intencja.putExtra(inRow.MOVES, "");
                        break;
                    default:
                        //TODO - when gamer choose TicTacToe Game
                        break;
                }
                startActivity(intencja);
            }
        });
    }
    //Refreshing ListView with available games, geted from serwer (API)
    public void refreshGameList(){
        //Seting loading spinner (while reciving message)
        ProgressBar spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);
        //Show a status Bar with info
        Snackbar.make(findViewById(R.id.main_list), getString(R.string.refresh), Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();

        //Geting Layout elements for modyfication
        ListView list = (ListView)findViewById(R.id.listView);
        TextView emptyText = (TextView)findViewById(android.R.id.empty);


        //Geting available games
        //Creating intent for custom Service
        Intent intencja = new Intent(
                getApplicationContext(),
                HttpService.class);
        //Creating PendingIntent - for getting list back
        PendingIntent pendingResult = createPendingResult(HttpService.GAMES_LIST, new Intent(),0);
        //Set data - URL for choosen game
        if(game == R.id.inRow){
            intencja.putExtra(HttpService.URL, HttpService.LINES);
        }else{
            //TODO - geting ticTacToe games list
        }
        //Set data - method of request
        intencja.putExtra(HttpService.METHOD, HttpService.GET);
        //Set data - intent for result
        intencja.putExtra(HttpService.RETURN, pendingResult);
        //Start unBound Service in another Thread
        startService(intencja);
    }

    //When Service return games list
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode==HttpService.GAMES_LIST) {
            //Hide loading spinner
            ProgressBar spinner = (ProgressBar) findViewById(R.id.progressBar1);
            spinner.setVisibility(View.GONE);
            SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
            swipeLayout.setRefreshing(false);

            //Get answer as JsonObject
            try {
                //Parse response as JSON
                JSONObject response = new JSONObject(data.getStringExtra(HttpService.RESPONSE));

                if(response.getInt("games_count")>0)
                {
                    //hide message "no game"
                    TextView no_game = (TextView)findViewById(R.id.empty);
                    no_game.setVisibility(View.GONE);

                    //get array of games from JSON
                    JSONArray games = new JSONArray(response.getString("games"));
                    ArrayList<String> items = new ArrayList<String>();

                    //Parse String list of games (for adapter)
                    for(int i=0; i<response.getInt("games_count");i++){
                        JSONObject game = games.getJSONObject(i);
                        items.add("ID: "+game.getString("id"));
                    }

                    //Set adapter to list
                    ArrayAdapter<String> gamesAdapter =
                            new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
                    ListView list = (ListView)findViewById(R.id.listView);
                    list.setAdapter(gamesAdapter);
                }//if "no game" do nothing
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }else if(requestCode==HttpService.GAME_INFO){
            //Hide loading spinner
            ProgressBar spinner = (ProgressBar) findViewById(R.id.progressBar1);
            spinner.setVisibility(View.GONE);

            if(game==R.id.inRow) {
                //Create intent to start game 4inRow
                Intent intencja = new Intent(getApplicationContext(), inRow.class);

                try {
                    //Parse server response
                    JSONObject response = new JSONObject(data.getStringExtra(HttpService.RESPONSE));

                    //Set Game number
                    intencja.putExtra(inRow.GAME_ID, response.getInt("id"));

                    if (response.getInt("status") == 0 && response.getInt("player1") == 2) {
                        //connect to new game
                        intencja.putExtra(inRow.STATUS, inRow.YOUR_TURN);
                    } else if (response.getInt("status") == 1 && response.getInt("player1") == 1) {
                        //time to player1 move
                        intencja.putExtra(inRow.STATUS, inRow.YOUR_TURN);
                    } else if (response.getInt("status") == 2 && response.getInt("player1") == 2) {
                        //time to player2 move
                        intencja.putExtra(inRow.STATUS, inRow.YOUR_TURN);
                    } else
                        intencja.putExtra(inRow.STATUS, inRow.WAIT);

                    //set player number
                    intencja.putExtra(inRow.PLAYER, response.getInt("player1"));
                    //set previous moves
                    intencja.putExtra(inRow.MOVES, response.getString("moves"));
                    //start game
                    startActivity(intencja);

                } catch (Exception ex) {
                    //For JSON Object
                    ex.printStackTrace();
                }
            }else if(game==R.id.ticTac){
                //TODO - start chosen game for TicTacToe
            }
        }
    }
}