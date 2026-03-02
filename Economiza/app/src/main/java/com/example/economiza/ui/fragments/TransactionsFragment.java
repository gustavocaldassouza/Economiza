package com.example.economiza.ui.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
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
import com.example.economiza.ui.viewmodel.ExportViewModel;
import com.example.economiza.ui.viewmodel.TransactionViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransactionsFragment extends Fragment {

    private ExportViewModel exportVm;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.rootView = view;

        RecyclerView rv = view.findViewById(R.id.rv_transactions);
        TextView txtEmpty = view.findViewById(R.id.txt_empty);
        FloatingActionButton fab = view.findViewById(R.id.fab_add);
        TextView txCount = view.findViewById(R.id.txt_tx_count);
        ImageButton btnExport = view.findViewById(R.id.btn_export);

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
        exportVm = new ViewModelProvider(this, app.getViewModelFactory())
                .get(ExportViewModel.class);

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

        // Observe export state
        exportVm.state.observe(getViewLifecycleOwner(), result -> {
            if (result == null)
                return;
            switch (result.status) {
                case LOADING:
                    btnExport.setEnabled(false);
                    break;
                case SUCCESS:
                    btnExport.setEnabled(true);
                    saveToDownloads(result.file, result.format);
                    exportVm.resetState();
                    break;
                case ERROR:
                    btnExport.setEnabled(true);
                    Snackbar.make(view,
                            "Export failed: " + (result.error != null ? result.error : "unknown error"),
                            Snackbar.LENGTH_LONG).show();
                    exportVm.resetState();
                    break;
                default:
                    btnExport.setEnabled(true);
                    break;
            }
        });

        btnExport.setOnClickListener(v -> showExportDialog());

        // Swipe left to delete
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
                        "\"" + (deleted.description != null ? deleted.description : "Transaction") + "\" deleted",
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
                    redBg.setBounds((int) (itemView.getRight() + dX), itemView.getTop(),
                            itemView.getRight(), itemView.getBottom());
                    redBg.draw(c);
                    c.drawText("Delete", itemView.getRight() - 40f,
                            itemView.getTop() + itemView.getHeight() / 2f + 15f, textPaint);
                }
                super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(rv);

        fab.setOnClickListener(v -> startActivity(
                new Intent(requireActivity(), AddTransactionActivity.class)));
    }

    // ── Export dialog ────────────────────────────────────────────────────────

    private void showExportDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Export Transactions")
                .setMessage("Choose a format to save your transaction history to the Downloads folder.")
                .setPositiveButton("Save as PDF", (d, w) -> startExport("pdf"))
                .setNegativeButton("Save as CSV", (d, w) -> startExport("csv"))
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void startExport(String format) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
        String fileName = "economiza_" + timestamp + "." + format;

        // Write to a private temp file first (ExportDataUseCase writes to a File)
        File tempDir = new File(requireContext().getCacheDir(), "exports");
        // noinspection ResultOfMethodCallIgnored
        tempDir.mkdirs();
        File tempFile = new File(tempDir, fileName);

        if ("pdf".equals(format)) {
            exportVm.exportToPdf(tempFile);
        } else {
            exportVm.exportToCsv(tempFile);
        }
    }

    // ── Save to public Downloads ──────────────────────────────────────────────

    private void saveToDownloads(File sourceFile, String format) {
        String mimeType = "pdf".equals(format) ? "application/pdf" : "text/csv";
        String displayName = sourceFile.getName();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // API 29+: use MediaStore.Downloads (no WRITE_EXTERNAL_STORAGE needed)
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Downloads.DISPLAY_NAME, displayName);
                cv.put(MediaStore.Downloads.MIME_TYPE, mimeType);
                cv.put(MediaStore.Downloads.IS_PENDING, 1);

                Uri collectionUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                Uri itemUri = requireContext().getContentResolver().insert(collectionUri, cv);

                if (itemUri == null)
                    throw new IOException("MediaStore insert failed");

                try (OutputStream os = requireContext().getContentResolver().openOutputStream(itemUri);
                        InputStream is = new FileInputStream(sourceFile)) {
                    if (os == null)
                        throw new IOException("Cannot open output stream");
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = is.read(buf)) > 0)
                        os.write(buf, 0, len);
                }

                // Mark as complete
                cv.clear();
                cv.put(MediaStore.Downloads.IS_PENDING, 0);
                requireContext().getContentResolver().update(itemUri, cv, null, null);

                showSavedSnackbar(displayName, itemUri, mimeType);

            } else {
                // API 24–28: write directly to public Downloads directory
                File downloadsDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS);
                // noinspection ResultOfMethodCallIgnored
                downloadsDir.mkdirs();
                File destFile = new File(downloadsDir, displayName);

                try (InputStream is = new FileInputStream(sourceFile);
                        OutputStream os = new java.io.FileOutputStream(destFile)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = is.read(buf)) > 0)
                        os.write(buf, 0, len);
                }

                Uri fileUri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        destFile);
                showSavedSnackbar(displayName, fileUri, mimeType);
            }

            // Clean up temp file
            // noinspection ResultOfMethodCallIgnored
            sourceFile.delete();

        } catch (Exception e) {
            Snackbar.make(rootView,
                    "Could not save to Downloads: " + e.getMessage(),
                    Snackbar.LENGTH_LONG).show();
        }
    }

    private void showSavedSnackbar(String fileName, Uri fileUri, String mimeType) {
        Snackbar.make(rootView,
                "✓  Saved to Downloads: " + fileName,
                Snackbar.LENGTH_LONG)
                .setAction("Share", v -> shareFile(fileUri, mimeType))
                .setActionTextColor(Color.parseColor("#3D8BFF"))
                .show();
    }

    // ── Share via Intent ──────────────────────────────────────────────────────

    private void shareFile(Uri uri, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Economiza – Transaction History");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share via"));
    }
}
