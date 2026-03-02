package com.example.economiza.ui.fragments;

import android.graphics.Color;
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
import com.example.economiza.domain.model.Category;
import com.example.economiza.domain.model.CategoryTotal;
import com.example.economiza.domain.model.DayTotal;
import com.example.economiza.ui.activities.AddTransactionActivity;
import com.example.economiza.ui.adapter.CategoryAdapter;
import com.example.economiza.ui.viewmodel.DashboardViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;
    private TextView txtBalance, txtIncome, txtExpenses;
    private BarChart barChart;
    private PieChart pieChart;

    // Palette of 12 rich colors for chart slices / bars
    private static final int[] CHART_COLORS = {
            0xFF3D8BFF, 0xFF00D084, 0xFFFFD54F, 0xFFFF8A65,
            0xFFCE93D8, 0xFFF48FB1, 0xFF42A5F5, 0xFF26C6DA,
            0xFFEF5350, 0xFF66BB6A, 0xFFFFA726, 0xFF8E97A8
    };

    // Holds categories list for resolving category IDs → names
    private List<Category> allCategories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtBalance = view.findViewById(R.id.txt_balance_value);
        txtIncome = view.findViewById(R.id.txt_income_value);
        txtExpenses = view.findViewById(R.id.txt_expense_value);
        barChart = view.findViewById(R.id.bar_chart);
        pieChart = view.findViewById(R.id.pie_chart);
        ExtendedFloatingActionButton fab = view.findViewById(R.id.fab_smart_entry);

        // RecyclerView for top categories label strip (optional)
        RecyclerView rvCategories = view.findViewById(R.id.rv_categories);
        if (rvCategories != null) {
            rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
            rvCategories.setAdapter(new CategoryAdapter());
        }

        if (fab != null) {
            fab.setOnClickListener(
                    v -> startActivity(new android.content.Intent(requireActivity(), AddTransactionActivity.class)));
        }

        // Style charts
        styleBarChart();
        stylePieChart();

        // ViewModel
        ViewModelProvider.Factory factory = ((EconomizaApp) requireActivity().getApplication()).getViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(DashboardViewModel.class);

        // ── Balance card ──────────────────────────────────────────────────────
        viewModel.totalIncome.observe(getViewLifecycleOwner(), income -> {
            if (income != null && txtIncome != null)
                txtIncome.setText(String.format(Locale.getDefault(), "R$ %.2f", income / 100.0));
        });

        viewModel.totalExpenses.observe(getViewLifecycleOwner(), expenses -> {
            if (expenses != null && txtExpenses != null)
                txtExpenses.setText(String.format(Locale.getDefault(), "R$ %.2f", expenses / 100.0));
        });

        viewModel.netBalance.observe(getViewLifecycleOwner(), balance -> {
            if (balance != null && txtBalance != null) {
                double balanceReal = balance / 100.0;
                txtBalance.setText(String.format(Locale.getDefault(), "R$ %.2f", Math.abs(balanceReal)));
                txtBalance.setTextColor(balanceReal >= 0 ? 0xFF00D084 : 0xFFEF5350);
            }
        });

        // ── Categories (for slice labels) ─────────────────────────────────────
        viewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            if (categories != null)
                allCategories = categories;
        });

        // ── Pie chart ─────────────────────────────────────────────────────────
        viewModel.expensesByCategory.observe(getViewLifecycleOwner(), totals -> {
            if (totals == null || totals.isEmpty()) {
                pieChart.setNoDataText("No expenses yet");
                pieChart.invalidate();
                return;
            }
            populatePieChart(totals);
        });

        // ── Bar chart (last 7 days) ──────────────────────────────────────────
        viewModel.weeklyExpenses.observe(getViewLifecycleOwner(), dayTotals -> {
            if (dayTotals == null || dayTotals.isEmpty()) {
                barChart.setNoDataText("No data for this week");
                barChart.invalidate();
                return;
            }
            populateBarChart(dayTotals);
        });
    }

    // ── Chart population ─────────────────────────────────────────────────────

    private void populatePieChart(List<CategoryTotal> totals) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        Map<Integer, String> catNameMap = new HashMap<>();
        for (Category c : allCategories)
            catNameMap.put(c.id, c.name);

        for (int i = 0; i < totals.size(); i++) {
            CategoryTotal ct = totals.get(i);
            String label = catNameMap.containsKey(ct.categoryId)
                    ? catNameMap.get(ct.categoryId)
                    : "Cat " + ct.categoryId;
            entries.add(new PieEntry(ct.total, label));
            colors.add(CHART_COLORS[i % CHART_COLORS.length]);
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(11f);
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(6f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.animateY(700);
        pieChart.invalidate();
    }

    private void populateBarChart(List<DayTotal> dayTotals) {
        // Build a map day_bucket → total so we can fill in missing days
        Map<Long, Long> dayMap = new HashMap<>();
        long minDay = Long.MAX_VALUE, maxDay = Long.MIN_VALUE;
        for (DayTotal dt : dayTotals) {
            dayMap.put(dt.dayBucket, dt.total);
            if (dt.dayBucket < minDay)
                minDay = dt.dayBucket;
            if (dt.dayBucket > maxDay)
                maxDay = dt.dayBucket;
        }

        // Fill last 7 calendar days
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long today = cal.getTimeInMillis() / 86_400_000L;

        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            long dayBucket = today - i;
            float total = dayMap.containsKey(dayBucket) ? dayMap.get(dayBucket) / 100f : 0f;
            entries.add(new BarEntry(6 - i, total));

            cal.setTimeInMillis(dayBucket * 86_400_000L);
            labels.add(sdf.format(cal.getTime()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Daily Expenses (R$)");
        dataSet.setColor(0xFF3D8BFF);
        dataSet.setHighlightEnabled(true);
        dataSet.setHighLightColor(0xFF00D084);
//        dataSet.setBarBorderRadius(6f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(9f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.setData(data);
        barChart.animateY(600);
        barChart.invalidate();
    }

    // ── Chart styling ────────────────────────────────────────────────────────

    private void styleBarChart() {
        if (barChart == null)
            return;
        barChart.setBackgroundColor(Color.TRANSPARENT);
        barChart.setDrawGridBackground(false);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawBorders(false);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setNoDataTextColor(Color.parseColor("#8E97A8"));

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#8E97A8"));
        xAxis.setGranularity(1f);

        barChart.getAxisLeft().setTextColor(Color.parseColor("#8E97A8"));
        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setGridColor(0x22FFFFFF);
        barChart.getAxisRight().setEnabled(false);
    }

    private void stylePieChart() {
        if (pieChart == null)
            return;
        pieChart.setBackgroundColor(Color.TRANSPARENT);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.parseColor("#151C2C"));
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(11f);
        pieChart.getLegend().setTextColor(Color.parseColor("#8E97A8"));
        pieChart.getLegend().setTextSize(10f);
        pieChart.setNoDataTextColor(Color.parseColor("#8E97A8"));
        pieChart.setCenterText("Expenses");
        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.setCenterTextSize(14f);
    }
}
