package com.phuang.pizzame;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.phuang.pizzame.model.Store;

public class MainActivity extends AppCompatActivity
        implements StoreListFragment.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        showStoreList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showStoreList() {
        Fragment fragment = getSupportFragmentManager()
                .findFragmentByTag(StoreListFragment.class.getSimpleName());
        if (fragment == null) {
            fragment = StoreListFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_content, fragment, StoreListFragment.class.getSimpleName())
                    .commit();
        }
    }

    @Override
    public void onItemSelected(Store store) {
        StoreDetailsFragment fragment = StoreDetailsFragment.newInstance(store);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_content, fragment, StoreDetailsFragment.class.getSimpleName())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        // route permission result to appropriate fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.main_content);
        if (currentFragment != null) {
            currentFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
