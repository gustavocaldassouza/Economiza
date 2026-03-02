package com.example.economiza;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.economiza.ui.fragments.BudgetFragment;
import com.example.economiza.ui.fragments.DashboardFragment;
import com.example.economiza.ui.fragments.RecurringFragment;
import com.example.economiza.ui.fragments.SettingsFragment;
import com.example.economiza.ui.fragments.TransactionsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.economiza.ui.activities.BaseActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        // Show Dashboard on first load
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();
            if (id == R.id.nav_dashboard)
                selected = new DashboardFragment();
            else if (id == R.id.nav_transactions)
                selected = new TransactionsFragment();
            else if (id == R.id.nav_budgets)
                selected = new BudgetFragment();
            else if (id == R.id.nav_recurring)
                selected = new RecurringFragment();
            else if (id == R.id.nav_settings)
                selected = new SettingsFragment();
            if (selected != null)
                loadFragment(selected);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}