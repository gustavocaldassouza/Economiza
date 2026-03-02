package com.example.economiza.ui.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.economiza.domain.usecase.ExportDataUseCase;

import java.io.File;

public class ExportViewModel extends ViewModel {

    public enum Status {
        IDLE, LOADING, SUCCESS, ERROR
    }

    public static class ExportResult {
        public final Status status;
        public final File file;
        public final String error;
        public final String format; // "pdf" or "csv"

        private ExportResult(Status status, File file, String error, String format) {
            this.status = status;
            this.file = file;
            this.error = error;
            this.format = format;
        }

        public static ExportResult idle() {
            return new ExportResult(Status.IDLE, null, null, null);
        }

        public static ExportResult loading() {
            return new ExportResult(Status.LOADING, null, null, null);
        }

        public static ExportResult success(File f, String fmt) {
            return new ExportResult(Status.SUCCESS, f, null, fmt);
        }

        public static ExportResult error(String msg) {
            return new ExportResult(Status.ERROR, null, msg, null);
        }
    }

    private final ExportDataUseCase exportDataUseCase;
    private final MutableLiveData<ExportResult> _state = new MutableLiveData<>(ExportResult.idle());
    public final LiveData<ExportResult> state = _state;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ExportViewModel(ExportDataUseCase exportDataUseCase) {
        this.exportDataUseCase = exportDataUseCase;
    }

    public void exportToPdf(File outFile) {
        _state.setValue(ExportResult.loading());
        new Thread(() -> {
            try {
                exportDataUseCase.executeToPdf(outFile);
                mainHandler.post(() -> _state.setValue(ExportResult.success(outFile, "pdf")));
            } catch (Exception e) {
                mainHandler.post(() -> _state.setValue(ExportResult.error(e.getMessage())));
            }
        }).start();
    }

    public void exportToCsv(File outFile) {
        _state.setValue(ExportResult.loading());
        new Thread(() -> {
            try {
                exportDataUseCase.executeToCsv(outFile);
                mainHandler.post(() -> _state.setValue(ExportResult.success(outFile, "csv")));
            } catch (Exception e) {
                mainHandler.post(() -> _state.setValue(ExportResult.error(e.getMessage())));
            }
        }).start();
    }

    public void resetState() {
        _state.setValue(ExportResult.idle());
    }
}
