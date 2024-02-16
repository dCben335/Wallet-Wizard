package com.example.walletwizard.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.walletwizard.R;
import com.example.walletwizard.Utils.ApiCall;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;


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

        TextInputLayout inputEditPrice = rootView.findViewById(R.id.input_price);
        TextView exchangeRateTextView = rootView.findViewById(R.id.exchange_rate);
        TextView resultTextView = rootView.findViewById(R.id.price_after_exchange);
        Spinner fromCurrencySpinner = rootView.findViewById(R.id.spinner_from_currency);
        Spinner toCurrencySpinner = rootView.findViewById(R.id.spinner_to_currency);


        fromCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {





                String selectedFromCurrency = (String) parentView.getItemAtPosition(position);
                String selectedToCurrency = (String) toCurrencySpinner.getSelectedItem();

                String textInputEditText = Objects.requireNonNull(inputEditPrice.getEditText()).getText().toString();
                Toast.makeText(context, "value: " + textInputEditText, Toast.LENGTH_SHORT).show();

                double inputValue = Double.parseDouble(textInputEditText);

                try {
                    double exchangeRate = findExchangeRate(devises, selectedFromCurrency, selectedToCurrency);
                    Toast.makeText(context, "e: " + exchangeRate, Toast.LENGTH_SHORT).show();

                    //resultTextView.setText((int) convertCurrency(inputValue,exchangeRate));
                    exchangeRateTextView.setText(String.valueOf(exchangeRate));
                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }

                Toast.makeText(context, "Currency selected: " + selectedFromCurrency, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Ne rien faire dans ce cas
            }
        });

        // Ajouter un écouteur d'événements au spinner de destination
        toCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedToCurrency = (String) parentView.getItemAtPosition(position);
                String selectedFromCurrency = (String) fromCurrencySpinner.getSelectedItem();

                try {
                    double exchangeRate = findExchangeRate(devises, selectedFromCurrency, selectedToCurrency);
                    exchangeRateTextView.setText(String.valueOf(exchangeRate));
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

            public void onError(String errorMessage) {
                // Handle error
            }
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

    // Supposons que 'exchangeRates' est votre JSONArray contenant les taux de change récupérés de votre API

    // Méthode pour trouver le taux de change entre deux devises


    private double findExchangeRate(JSONArray exchangeRates, String fromCurrency, String toCurrency) throws JSONException {

        double fromRate = 0.0;
        double toRate = 0.0;

        // Parcourez les taux de change pour trouver les taux des devises de départ et d'arrivée
        for (int i = 0; i < exchangeRates.length(); i++) {
            JSONObject currency = exchangeRates.getJSONObject(i);
            String codeISODevise = currency.getString("codeISODevise");
            double taux = currency.getDouble("taux");

            if (codeISODevise.equals(fromCurrency)) {
                fromRate = taux;
            } else if (codeISODevise.equals(toCurrency)) {
                toRate = taux;
            }

            // Si les deux taux ont été trouvés, sortez de la boucle
            if (fromRate != 0.0 && toRate != 0.0) {
                break;
            }
        }

        // Calcul du taux de change entre les deux devises
        return toRate / fromRate;
    }

    // Méthode pour convertir une devise source en une devise cible
    private double convertCurrency(double amount, double exchangeRate) {
        return amount * exchangeRate;
    }
}