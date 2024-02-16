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
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.walletwizard.R;
import com.example.walletwizard.Utils.ApiCall;
import com.example.walletwizard.Utils.LocationHandler;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class HomeFragment extends Fragment {
    HashMap<String, JSONObject> stringJsonObject = new HashMap<>();

    List<BarEntry> barEntriesList = new ArrayList<>();;
    BarChart barChart;
    BarData barData ;
    BarDataSet barDataSet;

    private Context context;
    private View rootView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = requireContext();

        return rootView = inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        barChart = rootView.findViewById(R.id.idBarChart);
        barDataSet = new BarDataSet(barEntriesList, "Exchange rates against the euro");

        barData = new BarData(barDataSet);

        barChart.setData(barData);

        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        barDataSet.setValueTextColor(Color.BLACK);

        barDataSet.setValueTextSize(16f);
        barChart.getDescription().setEnabled(false);
        handleAPICall();
    }


    private void handleAPICall() {
        String url = "https://happyapi.fr/api/devises";
        ApiCall.RequestType requestType = ApiCall.RequestType.OBJECT;

        ApiCall.handleRequest(requestType, url, new ApiCall.ApiCallback() {
            public void onSuccess(Object response) {
                if (response instanceof JSONObject) {
                    try {

                        JSONObject data = ((JSONObject) response);

                        getBarEntriesFromAPI(data);
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

    private void getBarEntriesFromAPI(JSONObject data) throws JSONException {
        JSONArray devises = data
                .getJSONObject("result")
                .getJSONObject("result")
                .getJSONArray("devises");

        setupCurrencySpinner(devises);



        GridLayout checkboxLayout = rootView.findViewById(R.id.checkBoxLayout);


        List<JSONObject> devisesList = new ArrayList<>();
        List<CheckBox> checkBoxList = new ArrayList<>();
        // HashMap<String, JSONObject> stringJsonObject = new HashMap<>();


        for (int i = 0; i < devises.length(); i++) {
            JSONObject devise = devises.getJSONObject(i);
            stringJsonObject.put(devise.getString("codeISODevise"), devise);
            CheckBox checkBox = new CheckBox(context);
            checkBox.setId(View.generateViewId());
            checkBox.setText(devise.getString("codeISODevise"));

            checkBoxList.add(checkBox);

            if (i < 5) {
                checkBox.setChecked(true);
            }
            setCheckboxListener(checkBox);
            checkboxLayout.addView(checkBox);
        }

        // Sort the list based on the "taux" value

        for (CheckBox checkBox : checkBoxList) {
            if (checkBox.isChecked()) {
                devisesList.add(stringJsonObject.get(checkBox.getText()));
            }
        }

        Collections.sort(devisesList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                try {
                    // Compare based on the "taux" values
                    float taux1 = (float) o1.getDouble("taux");
                    float taux2 = (float) o2.getDouble("taux");
                    return Float.compare(taux1, taux2);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });

        // Clear existing entries
        barEntriesList.clear();

        // Adding new entries
        for (int i = 0; i < 5; i++) {
            JSONObject devise = devisesList.get(i);
            float taux = (float) devise.getDouble("taux");
            String codeISODevise = devise.getString("codeISODevise");
            barEntriesList.add(new BarEntry(i + 0f, taux, codeISODevise));
        }

        System.out.println("Size of barEntriesList: " + barEntriesList.size());

        XAxis xAxis = barChart.getXAxis();

        // Create a new BarDataSet
        barDataSet = new BarDataSet(barEntriesList, "Exchange rates against the euro");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);

        // Set the data to BarChart
        barData = new BarData(barDataSet);
        barChart.setData(barData);

        // Set custom labels for the legend
        Legend legend = barChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setTextSize(12f);
        legend.setTextColor(Color.BLACK);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(true);

        // Set custom labels for x-axis
        xAxis.setLabelCount(getLabels().size());
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getLabels()));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        // Refresh the chart
        barChart.invalidate();
    }

    private void setCheckboxListener(CheckBox checkBox) {
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {

                try {
                    addBarToChart(checkBox.getText().toString());
                    Toast.makeText(context,checkBox.getText() + " CheckBox Checked", Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(context, checkBox.getText() + " CheckBox Unchecked", Toast.LENGTH_SHORT).show();
                removeBarFromChart(checkBox.getText().toString());
            }
        });
    }

    private List<String> getLabels() {
        List<String> labels = new ArrayList<>();
        for (BarEntry entry : barEntriesList) {
            labels.add(entry.getData().toString());
        }
        System.out.println(labels);
        return labels;
    }








    private void setupCurrencySpinner(JSONArray devises) throws JSONException {
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




    private void addBarToChart(String codeISODevise) throws JSONException {
        for (BarEntry entry : barEntriesList) {
            // Si La barre est déjà présente, ne rien faire
            if (entry.getData().toString().equals(codeISODevise)) {
                return;
            }
        }

        JSONObject jsonObject = stringJsonObject.get(codeISODevise);

        float taux = 0f;
        taux = (float) jsonObject.getDouble("taux");

        barEntriesList.add(new BarEntry(barEntriesList.size(), taux, codeISODevise));
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