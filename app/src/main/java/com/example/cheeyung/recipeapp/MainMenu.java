package com.example.cheeyung.recipeapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.cheeyung.recipeapp.Fragment.MyRecipeFragment;

public class MainMenu extends AppCompatActivity {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new MyRecipeFragment())
                            .commit();
                    return true;
                case R.id.navigation_dashboard:
                    Intent intent = new Intent(getApplicationContext(), NewRecipe.class);
                    Bundle bundle=new Bundle();
                    bundle.putString("Recipe ID","");
                    intent.putExtras(bundle);
                    startActivity(intent);
                    return false;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        getSupportActionBar().hide();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new MyRecipeFragment())
                .commit();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
