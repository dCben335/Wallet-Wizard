package com.example.walletwizard.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.walletwizard.R;
import com.example.walletwizard.Utils.ApiCall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

// Fragment responsible for currency conversion
public class ConverterFragment extends Fragment {
    // View and Context variables
    private View rootView;
    private Context context;

    // JSON array to store currency data
    private JSONArray devises = null;

    // UI elements
    private EditText inputEditPrice;
    private TextView exchangeRateTextView;
    private TextView resultTextView;
    private Spinner fromCurrencySpinner;
    private Spinner toCurrencySpinner;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = requireContext();
        return rootView = inflater.inflate(R.layout.fragment_converter, container, false);
    }

    // Called immediately after onCreateView()
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements and handle API call
        setFields();

        // Check if currency data is already fetched, if not, make API call
        if (devises == null) {
            handleAPICall();
        } else {
            try {
                // Set up currency spinners and input listener with fetched data
                setCurrencySpinner(devises, fromCurrencySpinner, "EUR");
                setCurrencySpinner(devises, toCurrencySpinner, "USD");
                setInputListener(inputEditPrice);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Method to initialize UI elements
    protected void setFields() {
        inputEditPrice = rootView.findViewById(R.id.input_edit_price);

        fromCurrencySpinner = rootView.findViewById(R.id.spinner_from_currency);
        toCurrencySpinner = rootView.findViewById(R.id.spinner_to_currency);

        exchangeRateTextView = rootView.findViewById(R.id.exchange_rate);
        resultTextView = rootView.findViewById(R.id.price_after_exchange);
    }

    // Method to handle API call to fetch currency data
    protected void handleAPICall() {
        // API options
        String url = "https://happyapi.fr/api/devises";
        ApiCall.RequestType requestType = ApiCall.RequestType.OBJECT;

        // Handling API call using custom API Class
        ApiCall.handleRequest(requestType, url, new ApiCall.ApiCallback() {
            public void onSuccess(Object response) {
                if (response instanceof JSONObject) {
                    try {
                        // Set the devises
                        JSONObject data = ((JSONObject) response);
                        setDevises(data);

                        // Set up currency spinners and input listener with fetched data
                        setCurrencySpinner(devises, fromCurrencySpinner, "EUR");
                        setCurrencySpinner(devises, toCurrencySpinner, "USD");
                        setInputListener(inputEditPrice);

                    } catch (JSONException e) {
                        // Inform user if there is no bank Array
                        Toast.makeText(context, "Converter Unavailable, please try again later", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }

            public void onError(String errorMessage) {
                // Display error message if API call fails
                Toast.makeText(context, "API call failed, please try again later", Toast.LENGTH_SHORT).show();
            }
        }, context);
    }

    // Method to perform currency conversion and display the result
    protected void convert() {
        // Get UI fields value
        String selectedFromCurrency = (String) fromCurrencySpinner.getSelectedItem();
        String selectedToCurrency = (String) toCurrencySpinner.getSelectedItem();
        String inputValue = inputEditPrice.getText().toString();

        try {
            // get exchangeRate
            double exchangeRate = getExchangeRate(selectedFromCurrency, selectedToCurrency);

            double value = 0;
            if (!inputValue.isEmpty()) {
                double inputDoubleValue = Double.parseDouble(inputValue);
                value = convertCurrency(inputDoubleValue, exchangeRate);
            }

            // Display exchange rate and converted amount
            String exchangeRateResult = "Exchange Rate: " + exchangeRate;
            exchangeRateTextView.setText(exchangeRateResult);

            // Display converted amount
            String amountAfterConversionResult = "Amount after conversion: " + value;
            resultTextView.setText(amountAfterConversionResult);

        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    // Method to listen for input changes in the EditText
    private void setInputListener(EditText input) {
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Trigger currency conversion when input changes
                convert();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // Method to set up currency spinners
    private void setCurrencySpinner(JSONArray devises, Spinner entitySpinner, String initialValue) throws JSONException {
        // Get currencies Array and sort it
        String[] currencies =  extractCodeISODevise(devises);
        Arrays.sort(currencies);

        // Get initialValue index
        int initialValuePosition = -1;
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].equals(initialValue)) {
                initialValuePosition = i;
                break;
            }
        }

        // Create the spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, currencies);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        entitySpinner.setAdapter(spinnerAdapter);

        // Auto-select initialValue
        if (initialValuePosition != -1) {
            entitySpinner.setSelection(initialValuePosition);
        }

        setSpinnerListener(entitySpinner);
    }

    // Method to set spinner item selection listener
    private void setSpinnerListener(Spinner spinner)  {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Trigger currency conversion when spinner selection changes
                convert();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    // Method to extract currency codes from JSON array
    private static String[] extractCodeISODevise(JSONArray jsonArray) throws JSONException {
        String[] currencies = new String[jsonArray.length()];

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject devise = jsonArray.getJSONObject(i);
            String codeISODevise = devise.getString("codeISODevise");
            currencies[i] = codeISODevise;
        }

        return currencies;
    }

    // Method to find exchange rate between two currencies
    private double getExchangeRate(String fromCurrency, String toCurrency) throws JSONException {
        if (fromCurrency.equals(toCurrency)) {
            return 1.0;
        }

        // Get rates
        double fromRate = 0.0;
        double toRate = 0.0;
        for (int i = 0; i < devises.length(); i++) {
            JSONObject currency = devises.getJSONObject(i);
            String codeISODevise = currency.getString("codeISODevise");
            double rate = currency.getDouble("taux");


            if (codeISODevise.equals(fromCurrency)) {
                fromRate = rate;
            } else if (codeISODevise.equals(toCurrency)) {
                toRate = rate;
            }

            if (fromRate != 0.0 && toRate != 0.0) {
                break;
            }
        }

        // return exchange rate
        return toRate / fromRate;
    }

    // Method to set the retrieved currency data
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

    // Method to convert currency amount
    private double convertCurrency(double amount, double exchangeRate) {
        return amount * exchangeRate;
    }
}
