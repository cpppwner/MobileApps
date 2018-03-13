package mobileapps.aau.at.ab02;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import mobileapps.aau.at.ab02.listview.ListViewAdapter;
import mobileapps.aau.at.ab02.listview.RowOnClickListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // toolbar should act as the ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // this is required in order to show are own title
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(false);
        }

        // custom adapter for list view
        final ListViewAdapter adapter = new ListViewAdapter(this);
        adapter.setRowOnClickListener(new RowOnClickListener() {
            @Override
            public void onClick(String rowText) {
                Intent myIntent = new Intent(MainActivity.this, RenderingActivity.class);
                myIntent.putExtra("text", rowText); //Optional parameters
                MainActivity.this.startActivity(myIntent);
            }
        });
        final ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);

        toolbar.setNavigationIcon(android.R.drawable.btn_plus);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.addNewItem();
                    }
                }
        );
    }
}
