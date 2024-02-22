package com.example.walletwizard.Fragments;

import android.content.Context;
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

import com.example.walletwizard.MainActivity;
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
    // View and Context variables
    private View rootView;
    private Context context;
    private LoadingScreen loadingScreen;

    // HashMap to store JSON objects and list for bar entries
    private final HashMap<String, JSONObject> stringJsonObject = new HashMap<>();
    private final List<BarEntry> barEntriesList = new ArrayList<>();

    // Chart and data set variables
    private BarChart barChart;
    private BarDataSet barDataSet;

    // List of checkboxes and JSON array for currencies
    private List<CheckBox> checkBoxes;
    private JSONArray devises = null;

    // Base exchange rate variable
    private double baseExchangeRate = 1.0;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = requireContext();
        return rootView = inflater.inflate(R.layout.fragment_home, container, false);
    }

    // Called immediately after onCreateView()
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        barChart = rootView.findViewById(R.id.idBarChart);
        loadingScreen = new LoadingScreen(context);

        // If devises are not loaded, initiate API call, else generate chart
        if (devises == null) {
            // Show the loading screen and make the Call
            loadingScreen.show();
            handleAPICall();
        } else {
            try {
                generateChartAndCheckboxes();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        // Setup UI elements
        setCreditsButton();
        setConverterButton();
        setMapButton();
    }

    // Method to handle API call
    protected void handleAPICall() {
        // API options
        String url = "https://happyapi.fr/api/devises";
        ApiCall.RequestType requestType = ApiCall.RequestType.OBJECT;

        // Handling API call using custom API Class
        ApiCall.handleRequest(requestType, url, new ApiCall.ApiCallback() {
            public void onSuccess(Object response) {
                if (response instanceof JSONObject) {
                    try {
                        // set devises and generate the chart
                        JSONObject data = ((JSONObject) response);
                        setDevises(data);
                        generateChartAndCheckboxes();

                    } catch (JSONException e) {
                        // Inform user if there is a problem
                        Toast.makeText(context, "The Chart might not be generated, please try again later" , Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }

                // Remove Loading screen
                loadingScreen.dismiss();
            }

            public void onError(String errorMessage) {
                // Remove Loading screen and inform the user
                loadingScreen.dismiss();
                Toast.makeText(context, "API call failed, please try again later" + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }, context);
    }

    // Method to generate chart and checkboxes
    private void generateChartAndCheckboxes() throws JSONException {
        // Set spinner and checkboxes
        setCurrencySpinner(devises);
        checkBoxes = createCheckBoxes(devises);

        // Sort the devises checked
        List<CheckBox> checkCheckBoxes = getCheckBoxesEnable();
        List<JSONObject> currentDevises = sortCheckedDevicesByRate(checkCheckBoxes);

        // Create the bar Chart and setup the
        for (int i = 0; i < currentDevises.size(); i++) {
            JSONObject devise = currentDevises.get(i);
            float rate = (float) devise.getDouble("taux");
            String codeISODevise = devise.getString("codeISODevise");
            barEntriesList.add(new BarEntry(i + 0f, rate, codeISODevise));
        }

        setChart();
    }

    // Method to set up the bar chart
    private void setChart() {
        // Set the bar settings
        barDataSet = new BarDataSet(barEntriesList, "Exchange rates");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setValueTextColor(R.color.dark_color);
        barDataSet.setValueTextSize(16f);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        // Set the Chart parameters and update it
        barChart.getDescription().setEnabled(false);
        setLegendsParams();
        setXaxis();
        updateChart();
    }

    // Method to set legend parameters
    private void setLegendsParams() {
        Legend legend = barChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setTextSize(12f);
        legend.setTextColor(R.color.dark_color);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(true);
    }

    // Method to set X-axis parameters
    private void setXaxis() {
        List<String> labels = getLabels();
        XAxis xAxis = barChart.getXAxis();
        xAxis.setLabelCount(labels.size());
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
    }

    // Method to set up currency spinner
    protected void setCurrencySpinner(JSONArray devises) throws JSONException {
        // Get currencies Array and sort it
        Spinner entitySpinner = rootView.findViewById(R.id.entitySpinner);
        String[] currencies =  extractCodeISODevise(devises);
        Arrays.sort(currencies);

        // Get EUR index
        int eurPosition = -1;
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].equals("EUR")) {
                eurPosition = i;
                break;
            }
        }

        // Create the spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, currencies);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        entitySpinner.setAdapter(spinnerAdapter);

        // Auto-select EUR value
        if (eurPosition != -1) {
            entitySpinner.setSelection(eurPosition);
        }

        setSpinnerListener(entitySpinner);
    }

    // Method to get base exchange rate
    private double getBaseExchangeRate(String deviseSearched) throws JSONException {
        for (int i = 0; i < devises.length(); i++) {
            JSONObject devise = devises.getJSONObject(i);
            String codeISODevise = devise.getString("codeISODevise");

            // If find, return it
            if (codeISODevise.equals(deviseSearched)) {
                return devise.getDouble("taux");
            }
        }
        return 1.0;
    }

    // Method to create checkboxes
    protected List<CheckBox> createCheckBoxes(JSONArray devises) throws JSONException {
        // Get the Grid Layout to put them
        GridLayout checkBoxLayout = rootView.findViewById(R.id.checkBoxLayout);

        // Create a list to keep and return the checkboxes
        List<CheckBox> checkBoxes = new ArrayList<>();


        for (int i = 0; i < devises.length(); i++) {
            JSONObject devise = devises.getJSONObject(i);
            stringJsonObject.put(devise.getString("codeISODevise"), devise);

            // Create and set the Checkbox parameters
            CheckBox checkBox = new CheckBox(context);
            checkBox.setId(View.generateViewId());
            checkBox.setText(devise.getString("codeISODevise"));
            checkBox.setTextColor(getResources().getColor(R.color.dark_color));

            // Add the checkbox to the checkBoxes List
            checkBoxes.add(checkBox);

            // Check the 5 first
            if (i < 5) {
                checkBox.setChecked(true);
            }

            // Add the checkboxes into the grid Layout and set a Listener
            checkBoxLayout.addView(checkBox);
            setCheckboxListener(checkBox);
        }
        return checkBoxes;
    }

    // Method to sort checked devices by rate
    protected List<JSONObject> sortCheckedDevicesByRate(List<CheckBox> checkedCheckBoxes) {
        // Get the devises of the checked checkboxes;
        List<JSONObject> sortedDevises = new ArrayList<>();
        for (CheckBox checkBox : checkedCheckBoxes) {
            sortedDevises.add(stringJsonObject.get(checkBox.getText().toString()));
        }

        // Sort the List by exchange rate and return it
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

    // Method to get checked checkboxes
    private List<CheckBox> getCheckBoxesEnable() {
        List<CheckBox> checkBoxesEnabled = new ArrayList<>();
        for (CheckBox checkBoxUnit : checkBoxes) {
            if (checkBoxUnit.isChecked()) {
                checkBoxesEnabled.add(checkBoxUnit);
            }
        }
        return checkBoxesEnabled;
    }

    // Method to set spinner listener
    private void setSpinnerListener(Spinner entitySpinner)  {
        entitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                try {
                    // Change the base exchange rate and Regenerate the Chart
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

    // Method to set checkbox listener
    private void setCheckboxListener(@NonNull CheckBox checkBox) {
        checkBox.setOnCheckedChangeListener((buttonView, checked) -> {
            try {
                // Regenerate the chart and unchecked if there is already 5 checkboxes
                boolean isChecked = regenerateChartAccordingToCheckBoxes();
                if (!isChecked) buttonView.setChecked(false);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Method to regenerate chart based on checked checkboxes
    private boolean regenerateChartAccordingToCheckBoxes() throws JSONException{
        // Return false and indicate the user if there is already 5 checkboxes checked
        List<CheckBox> enabledCheckBoxes = getCheckBoxesEnable();
        if (enabledCheckBoxes.size() > 5) {
            Toast.makeText(context,"5 Currencies max", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Clear the Chart
        barEntriesList.clear();

        // Sort the exchange rate of checked checkboxes and Create a bar for each one in the Chart
        for (JSONObject checkBoxUnit : sortCheckedDevicesByRate(enabledCheckBoxes)) {
            addBarToChart(checkBoxUnit.getString("codeISODevise"));
        }

        // Set the X axis again to get the good labels and update the chart
        setXaxis();
        updateChart();
        return true;
    }

    // Method to extract codeISODevise from JSON array
    private static String[] extractCodeISODevise(JSONArray jsonArray) throws JSONException {
        String[] currencies = new String[jsonArray.length()];

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject devise = jsonArray.getJSONObject(i);
            String codeISODevise = devise.getString("codeISODevise");
            currencies[i] = codeISODevise;
        }

        return currencies;
    }

    // Method to get labels for X-axis
    private List<String> getLabels() {
        List<String> labels = new ArrayList<>();
        for (BarEntry entry : barEntriesList) {
            labels.add(entry.getData().toString());
        }
        return labels;
    }

    // Method to add a bar to the chart
    private void addBarToChart(String codeISODevise) throws JSONException {
        // Get exchange rate of the codeISODevise
        JSONObject jsonObject = stringJsonObject.get(codeISODevise);
        assert jsonObject != null;
        double rate = jsonObject.getDouble("taux");

        // Get the converted rate and add it to the chart
        float finalRate = (float) (rate / baseExchangeRate);
        barEntriesList.add(new BarEntry(barEntriesList.size(), finalRate, codeISODevise));
    }

    // Method to update the chart
    private void updateChart() {
        barDataSet.notifyDataSetChanged();
        barChart.invalidate();
    }

    // Method to set currencies received from API
    protected void setDevises(JSONObject data) throws JSONException {
        // Get devises Array from fetched data
        devises = data
                .getJSONObject("result")
                .getJSONObject("result")
                .getJSONArray("devises");

        // Add EUR to it because the Api doesn't provide it
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("codeISODevise", "EUR");
        jsonObject.put("taux", 1.0);
        devises.put(jsonObject);
    }


    // Method to set credits button click listener
    private void setCreditsButton() {
        rootView.findViewById(R.id.credits_button).setOnClickListener(v -> {
            // Check if activity is not null
            if (getActivity() == null) return;

            // Get reference to MainActivity
            MainActivity mainActivity = ((MainActivity) getActivity());

            // Change fragment to credits fragment
            mainActivity.changeFragment(
                    null,
                    null,
                    mainActivity.creditsFragment,
                    0
            );
        });
    }

    // Method to set currency button click listener
    private void setConverterButton() {
        rootView.findViewById(R.id.converter_button).setOnClickListener(v -> {
            // Check if activity is not null
            if (getActivity() == null) return;

            // Get reference to MainActivity
            MainActivity mainActivity = ((MainActivity) getActivity());

            // Change fragment to converter fragment
            mainActivity.changeFragment(
                    mainActivity.converterButtonContainer,
                    mainActivity.converterButton,
                    mainActivity.converterFragment,
                    mainActivity.toLeftAnimation
            );
        });
    }

    // Method to set map button click listener
    private void setMapButton() {
        rootView.findViewById(R.id.map_button).setOnClickListener(v -> {
            // Check if activity is not null
            if (getActivity() == null) return;

            // Get reference to MainActivity
            MainActivity mainActivity = ((MainActivity) getActivity());

            // Change fragment to home fragment
            mainActivity.changeFragment(
                    mainActivity.mapButtonContainer,
                    mainActivity.mapButton,
                    mainActivity.mapFragment,
                    mainActivity.toRightAnimation
            );
        });
    }
}
