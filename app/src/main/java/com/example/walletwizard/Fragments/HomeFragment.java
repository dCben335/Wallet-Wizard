package com.example.walletwizard.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.walletwizard.R;
import com.example.walletwizard.Utils.ApiCall;
import com.example.walletwizard.Utils.LoadingScreen;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class HomeFragment extends Fragment {
    private final HashMap<String, JSONObject> stringJsonObject = new HashMap<>();
    private final List<BarEntry> barEntriesList = new ArrayList<>();
    private LoadingScreen loadingScreen;

    private BarChart barChart;
    private BarDataSet barDataSet;

    private List<CheckBox> checkBoxes;
    private JSONArray devises;
    private double baseExchangeRate = 1.0;

    private Context context;
    private View rootView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = requireContext();
        return rootView = inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        barChart = rootView.findViewById(R.id.idBarChart);

        loadingScreen = new LoadingScreen(context);
        loadingScreen.show();

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
                        loadingScreen.dismiss();

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
        devises = data
                .getJSONObject("result")
                .getJSONObject("result")
                .getJSONArray("devises");


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("codeISODevise", "EUR");
        jsonObject.put("taux", 1.0);

        devises.put(jsonObject);

        setCurrencySpinner(devises);

        checkBoxes = createCheckBoxes(devises);
        List<JSONObject> devisesList = sortCheckedDevicesByRate(checkBoxes);

        for (int i = 0; i < 5; i++) {
            JSONObject devise = devisesList.get(i);
            float rate = (float) devise.getDouble("taux");
            String codeISODevise = devise.getString("codeISODevise");
            barEntriesList.add(new BarEntry(i + 0f, rate, codeISODevise));
        }

        setChart();
    }


    private void setChart() {
        barDataSet = new BarDataSet(barEntriesList, "Exchange rates");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        barChart.getDescription().setEnabled(false);

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
        List<String> labels = getLabels();

        XAxis xAxis = barChart.getXAxis();
        xAxis.setLabelCount(labels.size());
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
    }



    protected void setCurrencySpinner(JSONArray devises) throws JSONException {
        Spinner entitySpinner = rootView.findViewById(R.id.entitySpinner);
        String[] currencies =  extractCodeISODevise(devises);
        Arrays.sort(currencies);

        int eurPosition = -1;
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].equals("EUR")) {
                eurPosition = i;
                break;
            }
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, currencies);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        entitySpinner.setAdapter(spinnerAdapter);

        // Auto-select EUR value
        if (eurPosition != -1) {
            entitySpinner.setSelection(eurPosition);
        }

        setSpinnerListener(entitySpinner);
    }



    private double getBaseExchangeRate(String deviseSearched) throws JSONException {
        for (int i = 0; i < devises.length(); i++) {
            JSONObject devise = devises.getJSONObject(i);
            String codeISODevise = devise.getString("codeISODevise");
            if (codeISODevise.equals(deviseSearched)) {
                return devise.getDouble("taux");
            }
        }
        return 1.0;
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

    private List<CheckBox> getCheckBoxesEnable() {
        List<CheckBox> checkBoxesEnabled = new ArrayList<>();

        for (CheckBox checkBoxUnit : checkBoxes) {
            if (checkBoxUnit.isChecked()) {
                checkBoxesEnabled.add(checkBoxUnit);
            }
        }

        return checkBoxesEnabled;
    }

    private void setSpinnerListener(Spinner entitySpinner)  {
        entitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                try {
                    String deviseSearched = (String) parentView.getItemAtPosition(position);
                    baseExchangeRate = getBaseExchangeRate(deviseSearched);
                    regenerateChartAccordingToCheckBoxes();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }


    private void setCheckboxListener(@NonNull CheckBox checkBox) {
        checkBox.setOnCheckedChangeListener((buttonView, checked) -> {
            try {
                boolean isChecked = regenerateChartAccordingToCheckBoxes();
                if (!isChecked) {
                    buttonView.setChecked(false);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private boolean regenerateChartAccordingToCheckBoxes() throws JSONException{
        List<CheckBox> enabledCheckBoxes = getCheckBoxesEnable();
        if (enabledCheckBoxes.size() > 5) {
            Toast.makeText(context,"5 Currencies max", Toast.LENGTH_SHORT).show();
            return false;
        }

        barEntriesList.clear();

        for (JSONObject checkBoxUnit : sortCheckedDevicesByRate(enabledCheckBoxes)) {
            addBarToChart(checkBoxUnit.getString("codeISODevise"));
        }

        setXaxis();
        updateChart();

        return true;
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

    private List<String> getLabels() {
        List<String> labels = new ArrayList<>();

        for (BarEntry entry : barEntriesList) {
            labels.add(entry.getData().toString());
        }

        return labels;
    }

    private void addBarToChart(String codeISODevise) throws JSONException {
        for (BarEntry entry : barEntriesList) {
            if (entry.getData().toString().equals(codeISODevise)) {
                return;
            }
        }

        JSONObject jsonObject = stringJsonObject.get(codeISODevise);
        assert jsonObject != null;
        double rate = jsonObject.getDouble("taux");

        float finalRate = (float) (rate / baseExchangeRate);
        barEntriesList.add(new BarEntry(barEntriesList.size(), finalRate, codeISODevise));
    }


    private void updateChart() {
        barDataSet.notifyDataSetChanged();
        barChart.invalidate();
    }
}