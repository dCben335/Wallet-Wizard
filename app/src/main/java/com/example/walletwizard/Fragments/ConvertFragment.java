package com.example.walletwizard.Fragments;

import android.content.Context;
import android.os.Bundle;

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

public class ConvertFragment extends Fragment {
    private Context context;
    private View rootView;
    private JSONArray devises = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = requireContext();
        handleAPICall();
        rootView = inflater.inflate(R.layout.fragment_convert, container, false);

        EditText inputEditPrice = rootView.findViewById(R.id.input_edit_price);
        TextView exchangeRateTextView = rootView.findViewById(R.id.exchange_rate);
        TextView resultTextView = rootView.findViewById(R.id.price_after_exchange);
        Spinner fromCurrencySpinner = rootView.findViewById(R.id.spinner_from_currency);
        Spinner toCurrencySpinner = rootView.findViewById(R.id.spinner_to_currency);

        fromCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                String selectedFromCurrency = (String) parentView.getItemAtPosition(position);
                String selectedToCurrency = (String) toCurrencySpinner.getSelectedItem();
                String inputValue = inputEditPrice.getText().toString();

                try {
                    double exchangeRate = findExchangeRate(devises, selectedFromCurrency, selectedToCurrency);
                    double value = 0;
                    if (!inputValue.isEmpty()) {
                        double inputDoubleValue = Double.parseDouble(inputValue);
                        value = convertCurrency(inputDoubleValue, exchangeRate);
                    }

                    String exchangeRateResult = "Exchange Rate: " + String.format("%.2f", exchangeRate);
                    String amountAfterConversionResult = "Amount after conversion: " + String.format("%.2f", value);
                    exchangeRateTextView.setText(exchangeRateResult);
                    resultTextView.setText(amountAfterConversionResult);

                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }

                Toast.makeText(context, "Currency selected: " + selectedFromCurrency, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        toCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedToCurrency = (String) parentView.getItemAtPosition(position);
                String selectedFromCurrency = (String) fromCurrencySpinner.getSelectedItem();
                String inputValue = inputEditPrice.getText().toString();

                try {
                    double exchangeRate = findExchangeRate(devises, selectedFromCurrency, selectedToCurrency);
                    double value = 0;
                    if (!inputValue.isEmpty()) {
                        double inputDoubleValue = Double.parseDouble(inputValue);
                        value = convertCurrency(inputDoubleValue, exchangeRate);
                    }

                    String exchangeRateResult = "Exchange Rate: " + String.format("%.2f", exchangeRate);
                    String amountAfterConversionResult = "Amount after conversion: " + String.format("%.2f", value);
                    exchangeRateTextView.setText(exchangeRateResult);
                    resultTextView.setText(amountAfterConversionResult);

                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }

                Toast.makeText(context, "Currency selected: " + selectedToCurrency, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Ne rien faire dans ce cas
            }
        });

        inputEditPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Avant que le texte ne change
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String selectedFromCurrency = (String) fromCurrencySpinner.getSelectedItem();
                String selectedToCurrency = (String) toCurrencySpinner.getSelectedItem();
                String inputValue = String.valueOf(s);

                System.out.println(inputValue);

                try {
                    double exchangeRate = findExchangeRate(devises, selectedFromCurrency, selectedToCurrency);
                    double value = 0;

                    if (!inputValue.isEmpty()) {
                        double inputDoubleValue = Double.parseDouble(inputValue);
                        value = convertCurrency(inputDoubleValue, exchangeRate);
                    }

                    String exchangeRateResult = "Exchange Rate: " + String.format("%.2f", exchangeRate);
                    String amountAfterConversionResult = "Amount after conversion: " + String.format("%.2f", value);
                    exchangeRateTextView.setText(exchangeRateResult);
                    resultTextView.setText(amountAfterConversionResult);

                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return rootView;
    }

    protected void handleAPICall() {
        String url = "https://happyapi.fr/api/devises";
        ApiCall.RequestType requestType = ApiCall.RequestType.OBJECT;

        ApiCall.handleRequest(requestType, url, new ApiCall.ApiCallback() {
            public void onSuccess(Object response) {
                if (response instanceof JSONObject) {
                    try {
                        devises = ((JSONObject) response)
                                .getJSONObject("result")
                                .getJSONObject("result")
                                .getJSONArray("devises");
                        System.out.println(devises);
                        setupCurrencySpinner(devises, R.id.spinner_from_currency);
                        setupCurrencySpinner(devises, R.id.spinner_to_currency);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
            }

            public void onError(String errorMessage) {}
        }, context);
    }

    private void setupCurrencySpinner(JSONArray devises, int id) throws JSONException {
        Spinner entitySpinner = rootView.findViewById(id);

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

    private double findExchangeRate(JSONArray exchangeRates, String fromCurrency, String toCurrency) throws JSONException {

        if (fromCurrency.equals(toCurrency)) {
            return 1.0;
        }

        double fromRate = 0.0;
        double toRate = 0.0;

        for (int i = 0; i < exchangeRates.length(); i++) {
            JSONObject currency = exchangeRates.getJSONObject(i);
            String codeISODevise = currency.getString("codeISODevise");
            double taux = currency.getDouble("taux");

            if (codeISODevise.equals(fromCurrency)) {
                fromRate = taux;
            } else if (codeISODevise.equals(toCurrency)) {
                toRate = taux;
            }

            if (fromRate != 0.0 && toRate != 0.0) {
                break;
            }
        }

        return toRate / fromRate;
    }

    private double convertCurrency(double amount, double exchangeRate) {
        return amount * exchangeRate;
    }

}