package com.example.walletwizard.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.walletwizard.R;
import com.example.walletwizard.Utils.ApiCall;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class HomeFragment extends Fragment {
    private HashMap<String, JSONObject> stringJsonObject = new HashMap<>();
    private List<BarEntry> barEntriesList = new ArrayList<>();

    private BarChart barChart;
    private BarDataSet barDataSet;
    private BarData barData;

    private Context context;
    private View rootView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = requireContext();
        return rootView = inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        barChart = rootView.findViewById(R.id.idBarChart);
        handleAPICall();
    }


    protected void handleAPICall() {
        String url = "https://happyapi.fr/api/devises";
        ApiCall.RequestType requestType = ApiCall.RequestType.OBJECT;

        ApiCall.handleRequest(requestType, url, new ApiCall.ApiCallback() {
            public void onSuccess(Object response) {
                if (response instanceof JSONObject) {
                    try {
                        JSONObject data = ((JSONObject) response);
                        generateChartAndCheckboxes(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
            }

            public void onError(String errorMessage) {
                // Handle error
            }
        }, context);
    }

    private void generateChartAndCheckboxes(JSONObject data) throws JSONException {
        JSONArray devises = data
                .getJSONObject("result")
                .getJSONObject("result")
                .getJSONArray("devises");

        setCurrencySpinner(devises);

        List<CheckBox> checkBoxList = createCheckBoxes(devises);
        List<JSONObject> devisesList = sortCheckedDevicesByRate(checkBoxList);

        // Adding new entries
        for (int i = 0; i < 5; i++) {
            JSONObject devise = devisesList.get(i);
            float rate = (float) devise.getDouble("taux");
            String codeISODevise = devise.getString("codeISODevise");
            barEntriesList.add(new BarEntry(i + 0f, rate, codeISODevise));
        }

        setChart();
    }


    private void setChart() {
        barDataSet = new BarDataSet(barEntriesList, "Exchange rates according to Euro");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);

        barData = new BarData(barDataSet);
        barChart.setData(barData);

        setLegendsParams();
        setXaxis();

        updateChart();
    }

    private void setLegendsParams() {
        Legend legend = barChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setTextSize(12f);
        legend.setTextColor(Color.BLACK);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(true);
    }

    private void setXaxis() {
        XAxis xAxis = barChart.getXAxis();
        xAxis.setLabelCount(getLabels().size());
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getLabels()));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
    }


    private List<String> getLabels() {
        List<String> labels = new ArrayList<>();

        for (BarEntry entry : barEntriesList) {
            labels.add(entry.getData().toString());
        }

        return labels;
    }



    protected void setCurrencySpinner(JSONArray devises) throws JSONException {
        Spinner entitySpinner = rootView.findViewById(R.id.entitySpinner);
        String[] currencies = extractCodeISODevise(devises);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, currencies);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        entitySpinner.setAdapter(spinnerAdapter);
    }



    private static String[] extractCodeISODevise(JSONArray jsonArray) throws JSONException {
        String[] currencies = new String[jsonArray.length()];

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject devise = jsonArray.getJSONObject(i);
            String codeISODevise = devise.getString("codeISODevise");
            currencies[i] = codeISODevise;
        }

        return currencies;
    }


    protected List<CheckBox> createCheckBoxes(JSONArray devises) throws JSONException {
        GridLayout checkBoxLayout = rootView.findViewById(R.id.checkBoxLayout);
        List<CheckBox> checkBoxes = new ArrayList<>();


        for (int i = 0; i < devises.length(); i++) {
            JSONObject devise = devises.getJSONObject(i);
            stringJsonObject.put(devise.getString("codeISODevise"), devise);

            CheckBox checkBox = new CheckBox(context);
            checkBox.setId(View.generateViewId());
            checkBox.setText(devise.getString("codeISODevise"));

            //insert in the view
            checkBoxes.add(checkBox);

            if (i < 5) {
                checkBox.setChecked(true);
            }

            checkBoxLayout.addView(checkBox);
            setCheckboxListener(checkBox);
        }

        return checkBoxes;
    }


    protected List<JSONObject> sortCheckedDevicesByRate(List<CheckBox> checkBoxList) {
        List<JSONObject> sortedDevises = new ArrayList<>();
        for (CheckBox checkBox : checkBoxList) {
            if (checkBox.isChecked()) {
                sortedDevises.add(stringJsonObject.get(checkBox.getText().toString()));
            }
        }

        sortedDevises.sort((o1, o2) -> {
            try {
                float firstRate = (float) o1.getDouble("taux");
                float secondRate = (float) o2.getDouble("taux");

                return Float.compare(firstRate, secondRate);

            } catch (JSONException e) {
                e.printStackTrace();
                return 0;
            }
        });

        return sortedDevises;
    }





    private void setCheckboxListener(@NonNull CheckBox checkBox) {
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                try {
                    addBarToChart(checkBox.getText().toString());
                    Toast.makeText(context,checkBox.getText() + " CheckBox Checked", Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }


            } else {
                Toast.makeText(context, checkBox.getText() + " CheckBox Unchecked", Toast.LENGTH_SHORT).show();
                removeBarFromChart(checkBox.getText().toString());
            }
        });
    }

    private void addBarToChart(String codeISODevise) throws JSONException {
        // Si La barre est déjà présente, ne rien faire
        for (BarEntry entry : barEntriesList) {
            if (entry.getData().toString().equals(codeISODevise)) {
                return;
            }
        }

        JSONObject jsonObject = stringJsonObject.get(codeISODevise);
        float rate = (float) jsonObject.getDouble("taux");

        barEntriesList.add(new BarEntry(barEntriesList.size(), rate, codeISODevise));
        updateChart();
    }

    private void removeBarFromChart(String codeISODevise) {
        for (int i = 0; i < barEntriesList.size(); i++) {
            BarEntry entry = barEntriesList.get(i);
            if (entry.getData().toString().equals(codeISODevise)) {
                barEntriesList.remove(i);
                break;
            }
        }

        updateChart();
    }

    private void updateChart() {
        barDataSet.notifyDataSetChanged();
        barChart.invalidate();
    }
}