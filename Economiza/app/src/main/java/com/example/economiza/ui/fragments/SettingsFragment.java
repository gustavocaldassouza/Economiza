package com.example.economiza.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.economiza.EconomizaApp;
import com.example.economiza.R;
import com.example.economiza.domain.usecase.ExportDataUseCase;
import com.example.economiza.ui.activities.OnboardingActivity;

import java.io.File;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lock vault
        view.findViewById(R.id.btn_lock_vault).setOnClickListener(v -> {
            ((EconomizaApp) requireActivity().getApplication()).lockVault();
            Intent intent = new Intent(requireActivity(), OnboardingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Change password placeholder
        view.findViewById(R.id.btn_change_password).setOnClickListener(v -> Toast.makeText(requireContext(),
                "Change password: lock vault, then create a new one from onboarding.",
                Toast.LENGTH_LONG).show());

        // Export CSV
        view.findViewById(R.id.btn_export_csv).setOnClickListener(v -> exportData(true));

        // Export PDF
        view.findViewById(R.id.btn_export_pdf).setOnClickListener(v -> exportData(false));

        // Manage categories
        view.findViewById(R.id.btn_manage_categories).setOnClickListener(
                v -> Toast.makeText(requireContext(), "Category management coming soon!", Toast.LENGTH_SHORT).show());
    }

    private void exportData(boolean asCsv) {
        EconomizaApp app = (EconomizaApp) requireActivity().getApplication();
        ExportDataUseCase exportUseCase = app.getExportDataUseCase();
        if (exportUseCase == null) {
            Toast.makeText(requireContext(), "Export unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        File exportDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (exportDir == null) {
            Toast.makeText(requireContext(), "Storage unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        String filename = asCsv ? "economiza_export.csv" : "economiza_export.pdf";
        File outFile = new File(exportDir, filename);

        new Thread(() -> {
            try {
                if (asCsv) {
                    exportUseCase.executeToCsv(outFile);
                } else {
                    exportUseCase.executeToPdf(outFile);
                }
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Exported to " + outFile.getAbsolutePath(),
                            Toast.LENGTH_LONG).show();
                    shareFile(outFile, asCsv ? "text/csv" : "application/pdf");
                });
            } catch (Exception e) {
                requireActivity()
                        .runOnUiThread(() -> Toast.makeText(requireContext(), "Export failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void shareFile(File file, String mimeType) {
        Uri uri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                file);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(mimeType);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share Export"));
    }
}
