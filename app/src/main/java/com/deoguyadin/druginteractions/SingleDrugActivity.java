package com.deoguyadin.druginteractions;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created By: Deo Guyadin
 *
 * Activity Description:
 * Takes the input drug name and searches for all drug with interactions with the
 * input drugs.
 */

public class SingleDrugActivity extends AppCompatActivity {

    public Spinner sourcesFilterSpinner;
    public Spinner drugListSpinner;

    public TextView inputDrugView;
    public TextView severityView;
    public TextView descriptionView;

    public AutoCompleteTextView searchView;

    public Button searchButtonView;

    public String inputDrugId; //RXNorm ID of the input drug
    public String inputDrug;
    public String trueDrugName; // This displays the drugs actual name if a product name is entered

    public int numOfInteractionsDrugBank; // Number of interactions returned from Drug Bank
    public int numOfInteractionsONCHigh; // Number of interactions returned from ONCHigh

    public boolean sourceUsedIsDrugBank; // Drug Bank: true, ONCHigh: false

    public ArrayList<String> drugInfoNameDrugBank; // Drug names from the DrugBank database
    public ArrayList<String> drugInfoSeverityDrugBank; // Severity of interactions from the Drug Bank database
    public ArrayList<String> drugInfoDescriptionDrugBank; // Description of interactions from the Drug Bank database
    public ArrayList<String> drugInfoNameONCHigh; // Drug names from the ONCHigh database
    public ArrayList<String> drugInfoSeverityONCHigh; // Severity of interactions from the ONCHigh database
    public ArrayList<String> drugInfoDescriptionONCHigh; // Description of interactions from the ONCHigh database

    public ArrayAdapter<String> drugBankAdapter;
    public ArrayAdapter<String> searchAdapter;
    public ArrayAdapter<String> ONCHighAdapter;
    public ArrayAdapter<String> searchAdapter2;

    public ProcessDrugData processDrugData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_drug);

        sourceUsedIsDrugBank = true;

        sourcesFilterSpinner = (Spinner) findViewById(R.id.sourcesFilter);
        drugListSpinner = (Spinner) findViewById(R.id.drugList);

        inputDrugView = (TextView) findViewById(R.id.inputDrug);
        severityView = (TextView) findViewById(R.id.severity);
        descriptionView = (TextView) findViewById(R.id.description);

        searchView = (AutoCompleteTextView) findViewById(R.id.search);

        searchButtonView = (Button) findViewById(R.id.searchButton);

        Intent intent = getIntent();
        inputDrug = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        inputDrugView.setText(inputDrug);

        String singleDrugURL = "https://rxnav.nlm.nih.gov/REST/rxcui?name=" + inputDrug + "&allsrc=1";

        processDrugData = new ProcessDrugData();
        processDrugData.execute(singleDrugURL);
    }

    @Override
    protected void onDestroy () {
        if (processDrugData.getStatus() == AsyncTask.Status.RUNNING) {
            processDrugData.cancel(true);
        }
        processDrugData = null;
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Separate thread to receive all drug interactions from both databases.
     */
    public class ProcessDrugData extends AsyncTask<String, Void, Integer> {

        private ProgressDialog dialog = new ProgressDialog(SingleDrugActivity.this);

        /* Displays a loading notification when drugs that interact with the input drug are being loaded. */
        @Override
        protected void onPreExecute () {
            this.dialog.setMessage("Loading Drug Information");
            this.dialog.show();
        }

        @Override
        protected Integer doInBackground (String... strings) {

            sourcesFilterSpinner.setEnabled(false);
            drugListSpinner.setEnabled(false);

            String stream;
            String urlString = strings[0];

            /* Get Drug Name From XML */
            HTTPDataHandler hh = new HTTPDataHandler();
            stream = hh.GetHTTPData(urlString);
            try {
                inputDrugId = parseXML(stream);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            /* Get Drug Info From JSON */
            urlString = "https://rxnav.nlm.nih.gov/REST/interaction/interaction.json?rxcui="
                    + inputDrugId;
            stream = hh.GetHTTPData(urlString);
            try {
                parseJSON(stream);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return 1;
        }

        @Override
        protected void onPostExecute (Integer result) {

            String setInputDrugView = inputDrug + " (" + trueDrugName + ")";
            inputDrugView.setText(setInputDrugView);

            sourcesFilterSpinner.setEnabled(true);
            drugListSpinner.setEnabled(true);

            /* set Spinner adapter (drugBankAdapter) */
            drugBankAdapter = new ArrayAdapter<String>(SingleDrugActivity.this,
                    android.R.layout.simple_spinner_item, drugInfoNameDrugBank);
            drugBankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            drugListSpinner.setAdapter(drugBankAdapter);

            /* set search adapter (DrugBank) */
            searchAdapter = new ArrayAdapter<String>(SingleDrugActivity.this,
                    android.R.layout.simple_spinner_item, drugInfoNameDrugBank);
            searchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            searchView.setAdapter(searchAdapter);

            /* Set Spinner adapter (ONCHigh) */
            ONCHighAdapter = new ArrayAdapter<String>(SingleDrugActivity.this,
                    android.R.layout.simple_spinner_item, drugInfoNameONCHigh);
            ONCHighAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            /* Set search adapter (ONCHigh) */
            searchAdapter2 = new ArrayAdapter<String>(SingleDrugActivity.this,
                    android.R.layout.simple_spinner_item, drugInfoNameONCHigh);
            searchAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            /* Set listener for the spinner displaying the list of drugs that interact with the input drug */
            AdapterView.OnItemSelectedListener initialListener = new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    severityView.setText(drugInfoSeverityDrugBank.get(position));
                    descriptionView.setText(drugInfoDescriptionDrugBank.get(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            };
            drugListSpinner.setOnItemSelectedListener(initialListener);

            /* Set the listener for the spinner determining the source for the list of drugs that interact
             * with the input drug.
             */
            AdapterView.OnItemSelectedListener sourceListener = new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    if (adapterView.getItemAtPosition(position).equals("Drug Bank")) {

                        sourceUsedIsDrugBank = true;

                        drugListSpinner.setAdapter(drugBankAdapter);
                        searchView.setAdapter(searchAdapter);

                        if (drugInfoNameDrugBank.isEmpty()) {
                            severityView.setText("N/A");
                            descriptionView.setText("N/A");
                        } else {
                            severityView.setText(drugInfoSeverityDrugBank.get(0));
                            descriptionView.setText(drugInfoDescriptionDrugBank.get(0));
                        }

                        AdapterView.OnItemSelectedListener drugListener = new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                                severityView.setText(drugInfoSeverityDrugBank.get(position));
                                descriptionView.setText(drugInfoDescriptionDrugBank.get(position));
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        };

                        drugListSpinner.setOnItemSelectedListener(drugListener);

                    } else if (adapterView.getItemAtPosition(position).equals("ONCHigh (Severity Listed as High)")) {

                        sourceUsedIsDrugBank = false;

                        drugListSpinner.setAdapter(ONCHighAdapter);
                        searchView.setAdapter(searchAdapter2);

                        if (drugInfoNameONCHigh.isEmpty()) {
                            severityView.setText("N/A");
                            descriptionView.setText("N/A");
                        } else {
                            severityView.setText(drugInfoSeverityONCHigh.get(0));
                            descriptionView.setText(drugInfoDescriptionONCHigh.get(0));
                        }

                        AdapterView.OnItemSelectedListener drugListener = new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                                severityView.setText(drugInfoSeverityONCHigh.get(position));
                                descriptionView.setText(drugInfoDescriptionONCHigh.get(position));
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        };
                        drugListSpinner.setOnItemSelectedListener(drugListener);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            };
            sourcesFilterSpinner.setOnItemSelectedListener(sourceListener);

            searchButtonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String searchEntry = searchView.getText().toString();

                    if (sourceUsedIsDrugBank) {
                        drugListSpinner.setSelection(drugBankAdapter.getPosition(searchEntry));
                        searchView.setAdapter(searchAdapter);
                    } else {
                        drugListSpinner.setSelection(ONCHighAdapter.getPosition(searchEntry));
                        searchView.setAdapter(searchAdapter2);
                    }
                }
            });

            /* Removes loading display when all drugs with interactions are loaded. */
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

        }

        /**
         * Parse data from XML stream.
         */
        private String parseXML (String stream) throws XmlPullParserException, IOException {

            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            xmlFactoryObject.setNamespaceAware(true);
            XmlPullParser myParser = xmlFactoryObject.newPullParser();
            myParser.setInput(new StringReader(stream));

            int eventType = myParser.getEventType();
            boolean shouldBreak = false;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (myParser.getName().equals("rxnormId")) {
                        eventType = myParser.next();
                        inputDrugId = myParser.getText();
                        shouldBreak = true;
                    }
                    if (shouldBreak) {
                        break;
                    }
                }
                eventType = myParser.next();
            }
            return inputDrugId;
        }

        /**
         * Parse data from JSON stream.
         */
        private void parseJSON (String stream) throws JSONException {

            JSONObject reader = new JSONObject(stream);

            drugInfoNameDrugBank = new ArrayList<>();
            drugInfoSeverityDrugBank = new ArrayList<>();
            drugInfoDescriptionDrugBank = new ArrayList<>();

            drugInfoNameONCHigh = new ArrayList<>();
            drugInfoSeverityONCHigh = new ArrayList<>();
            drugInfoDescriptionONCHigh = new ArrayList<>();

            // Source: Drug Bank
            JSONArray interactionPair = (JSONArray) reader.getJSONArray("interactionTypeGroup")
                    .getJSONObject(0).getJSONArray("interactionType").getJSONObject(0)
                    .getJSONArray("interactionPair");

            trueDrugName = interactionPair.getJSONObject(0)
                    .getJSONArray("interactionConcept").getJSONObject(0)
                    .getJSONObject("minConceptItem").getString("name");

            for (int i = 0; i < interactionPair.length(); i++) {
                numOfInteractionsDrugBank = i;

                // Interaction Drug Name
                drugInfoNameDrugBank.add(interactionPair.getJSONObject(i)
                        .getJSONArray("interactionConcept").getJSONObject(1)
                        .getJSONObject("minConceptItem").getString("name"));

                // Interaction Drug Severity
                drugInfoSeverityDrugBank.add(interactionPair.getJSONObject(i).getString("severity"));

                // Interaction Description
                drugInfoDescriptionDrugBank.add(interactionPair.getJSONObject(i).getString("description"));
                Log.e("WorksHereDB"+i, drugInfoNameDrugBank.get(i));
            }
            numOfInteractionsDrugBank++;

            // Source: ONCHigh
            interactionPair = (JSONArray) reader.getJSONArray("interactionTypeGroup")
                    .getJSONObject(1).getJSONArray("interactionType").getJSONObject(0)
                    .getJSONArray("interactionPair");

            for (int i = 0; i < interactionPair.length(); i++) {
                numOfInteractionsONCHigh = i;

                // Interaction Drug Name
                drugInfoNameONCHigh.add(interactionPair.getJSONObject(i)
                        .getJSONArray("interactionConcept").getJSONObject(1)
                        .getJSONObject("minConceptItem").getString("name"));

                // Interaction Drug Severity
                drugInfoSeverityONCHigh.add(interactionPair.getJSONObject(i).getString("severity"));

                // Interaction Description
                drugInfoDescriptionONCHigh.add(interactionPair.getJSONObject(i).getString("description"));
                Log.e("WorksHereONC"+i, drugInfoNameONCHigh.get(i));
            }
            numOfInteractionsONCHigh++;
        }
    }
}