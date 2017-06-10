package com.example.k.lab6;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void gameList(View v) {
        Intent intencja = new Intent(
                getApplicationContext(),
                GamesList.class);
        intencja.putExtra("gra", v.getId());
        startActivity(intencja);
    }

}
