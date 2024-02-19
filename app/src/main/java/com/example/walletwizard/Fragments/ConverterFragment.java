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
import com.example.walletwizard.Utils.LoadingScreen;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class ConverterFragment extends Fragment {
    private View rootView;
    private Context context;
    private LoadingScreen loadingScreen;

    private JSONArray devises = null;

    private EditText inputEditPrice;
    private TextView exchangeRateTextView;
    private TextView resultTextView;
    private Spinner fromCurrencySpinner;
    private Spinner toCurrencySpinner;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = requireContext();
        return rootView = inflater.inflate(R.layout.fragment_converter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        setFields();

        loadingScreen = new LoadingScreen(context);
        loadingScreen.show();
        handleAPICall();
    }

    protected void setFields() {
        inputEditPrice = rootView.findViewById(R.id.input_edit_price);

        fromCurrencySpinner = rootView.findViewById(R.id.spinner_from_currency);
        toCurrencySpinner = rootView.findViewById(R.id.spinner_to_currency);

        exchangeRateTextView = rootView.findViewById(R.id.exchange_rate);
        resultTextView = rootView.findViewById(R.id.price_after_exchange);
    }


    protected void handleAPICall() {
        String url = "https://happyapi.fr/api/devises";
        ApiCall.RequestType requestType = ApiCall.RequestType.OBJECT;

        ApiCall.handleRequest(requestType, url, new ApiCall.ApiCallback() {
            public void onSuccess(Object response) {
                if (response instanceof JSONObject) {
                    try {
                        JSONObject data = ((JSONObject) response);
                        setDevises(data);

                        setCurrencySpinner(devises, fromCurrencySpinner, "EUR");
                        setCurrencySpinner(devises, toCurrencySpinner, "USD");
                        setInputListener(inputEditPrice);

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                } else Toast.makeText(context, "", Toast.LENGTH_SHORT).show();

                loadingScreen.dismiss();
            }

            public void onError(String errorMessage) {
                loadingScreen.dismiss();
                Toast.makeText(context, "API call failed, please try again later" + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }, context);
    }


    protected void convert() {
        String selectedFromCurrency = (String) fromCurrencySpinner.getSelectedItem();
        String selectedToCurrency = (String) toCurrencySpinner.getSelectedItem();
        String inputValue = inputEditPrice.getText().toString();

        try {
            double exchangeRate = findExchangeRateFromDevises(selectedFromCurrency, selectedToCurrency);

            double value = 0;
            if (!inputValue.isEmpty()) {
                double inputDoubleValue = Double.parseDouble(inputValue);
                value = convertCurrency(inputDoubleValue, exchangeRate);
            }

            String exchangeRateResult = "Exchange Rate: " + exchangeRate;
            exchangeRateTextView.setText(exchangeRateResult);

            String amountAfterConversionResult = "Amount after conversion: " + value;
            resultTextView.setText(amountAfterConversionResult);

        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }



    private void setInputListener(EditText input) {
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                convert();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setCurrencySpinner(JSONArray devises, Spinner entitySpinner, String initialValue) throws JSONException {
        String[] currencies =  extractCodeISODevise(devises);
        Arrays.sort(currencies);

        int eurPosition = -1;
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].equals(initialValue)) {
                eurPosition = i;
                break;
            }
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, currencies);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        entitySpinner.setAdapter(spinnerAdapter);

        // Auto-select initialValue
        if (eurPosition != -1) {
            entitySpinner.setSelection(eurPosition);
        }

        setSpinnerListener(entitySpinner);
    }

    private void setSpinnerListener(Spinner spinner)  {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                convert();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
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

    private double findExchangeRateFromDevises(String fromCurrency, String toCurrency) throws JSONException {
        if (fromCurrency.equals(toCurrency)) {
            return 1.0;
        }

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

        return toRate / fromRate;
    }

    protected void setDevises(JSONObject data) throws JSONException {
        devises = data
                .getJSONObject("result")
                .getJSONObject("result")
                .getJSONArray("devises");


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("codeISODevise", "EUR");
        jsonObject.put("taux", 1.0);

        devises.put(jsonObject);
    }

    private double convertCurrency(double amount, double exchangeRate) {
        return amount * exchangeRate;
    }
}