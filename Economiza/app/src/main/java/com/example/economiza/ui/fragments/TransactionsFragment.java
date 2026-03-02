package com.example.economiza.ui.fragments;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.economiza.EconomizaApp;
import com.example.economiza.R;
import com.example.economiza.domain.model.Transaction;
import com.example.economiza.ui.activities.AddTransactionActivity;
import com.example.economiza.ui.adapter.TransactionAdapter;
import com.example.economiza.ui.viewmodel.TransactionViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

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

        EconomizaApp app = (EconomizaApp) requireActivity().getApplication();
        TransactionAdapter adapter = new TransactionAdapter(app.getCategoryRepository());
        adapter.setListener(t -> {
            Intent intent = new Intent(requireActivity(), AddTransactionActivity.class);
            intent.putExtra("EXTRA_TRANSACTION_ID", t.id);
            startActivity(intent);
        });
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        TransactionViewModel vm = new ViewModelProvider(this, app.getViewModelFactory())
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

        // Swipe left to delete, with Undo snackbar
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            private final ColorDrawable redBg = new ColorDrawable(Color.parseColor("#D32F2F"));
            private final Paint textPaint;

            {
                textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(42f);
                textPaint.setTextAlign(Paint.Align.RIGHT);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                    @NonNull RecyclerView.ViewHolder vh,
                    @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
                Transaction deleted = adapter.getTransactionAt(vh.getAdapterPosition());
                vm.deleteTransaction(deleted);

                Snackbar.make(view,
                        "\"" + (deleted.description != null ? deleted.description : "Transaction")
                                + "\" deleted",
                        Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> vm.addTransaction(deleted))
                        .setActionTextColor(Color.parseColor("#3D8BFF"))
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                    @NonNull RecyclerView.ViewHolder vh, float dX, float dY,
                    int actionState, boolean isCurrentlyActive) {
                View itemView = vh.itemView;
                if (dX < 0) {
                    redBg.setBounds(
                            (int) (itemView.getRight() + dX), itemView.getTop(),
                            itemView.getRight(), itemView.getBottom());
                    redBg.draw(c);
                    float textX = itemView.getRight() - 40f;
                    float textY = itemView.getTop() + itemView.getHeight() / 2f + 15f;
                    c.drawText("Delete", textX, textY, textPaint);
                }
                super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(rv);

        fab.setOnClickListener(v -> startActivity(
                new Intent(requireActivity(), AddTransactionActivity.class)));
    }
}
