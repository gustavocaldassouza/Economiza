package com.example.economiza.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.economiza.EconomizaApp;
import com.example.economiza.R;
import com.example.economiza.ui.activities.AddTransactionActivity;
import com.example.economiza.ui.adapter.TransactionAdapter;
import com.example.economiza.ui.viewmodel.TransactionViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TransactionsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_transactions);
        TextView txtEmpty = view.findViewById(R.id.txt_empty);
        FloatingActionButton fab = view.findViewById(R.id.fab_add);
        TextView txCount = view.findViewById(R.id.txt_tx_count);

        TransactionAdapter adapter = new TransactionAdapter();
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        TransactionViewModel vm = new ViewModelProvider(this,
                ((EconomizaApp) requireActivity().getApplication()).getViewModelFactory())
                .get(TransactionViewModel.class);

        vm.transactions.observe(getViewLifecycleOwner(), transactions -> {
            if (transactions == null || transactions.isEmpty()) {
                rv.setVisibility(View.GONE);
                txtEmpty.setVisibility(View.VISIBLE);
                txCount.setText("No transactions yet");
            } else {
                rv.setVisibility(View.VISIBLE);
                txtEmpty.setVisibility(View.GONE);
                txCount.setText(transactions.size() + " transactions");
                adapter.setTransactions(transactions);
            }
        });

        fab.setOnClickListener(v -> startActivity(new Intent(requireActivity(), AddTransactionActivity.class)));
    }
}
