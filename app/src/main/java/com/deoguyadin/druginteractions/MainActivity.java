package com.deoguyadin.druginteractions;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created By: Deo Guyadin
 *
 * Application Description:
 * This program takes the name of an input drug and returns a list of drug names that
 * interact with the input drug along with the severity of the interactions and a description of
 * the interactions from the DrugBank database and the ONCHigh database. This product uses publicly
 * available data from the U.S. National Library of Medicine (NLM), National Institutes of Health,
 * Department of Health and Human Services; NLM is not responsible for the product and does not
 * endorse or recommend this or any other product.
 *
 * Activity Description:
 * This activity prompts the user to enter the name of an input drug.
 *
 * Disclaimer:
 * This app is for general educational purposes and should not be used as a definitive source of
 * information on drug interactions. Use of this app as such is at your own risk. This app should
 * not be seen as a replacement for the advice of medical professionals.
 *
 * DrugBank Disclaimer:
 * DrugBank is intended for educational and scientific research purposes only and you expressly
 * acknowledge and agree that use of DrugBank is at your sole risk. The accuracy of DrugBank
 * information is not guaranteed and reliance on DrugBank shall be at your sole risk. DrugBank is
 * not intended as a substitute for professional medical advice, diagnosis or treatment.
 *
 * Citing DrugBank:
 * Wishart DS, Knox C, Guo AC, Shrivastava S, Hassanali M, Stothard P, Chang Z, Woolsey J. DrugBank:
 * a comprehensive resource for in silico drug discovery and exploration. Nucleic Acids Res. 2006
 * Jan 1;34(Database issue):D668-72. 16381955
 *
 * Article Web Link for the Source of the ONCHigh database:
 * https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3422823/
 */

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.deoguyadin.druginteractions.MESSAGE";

    public EditText singleDrugEntry;

    public Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView disclaimerView = (TextView) findViewById(R.id.disclaimer);
        disclaimerView.setMovementMethod(new ScrollingMovementMethod());
        disclaimerView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void goToSingleDrugActivity(View view) {
        intent = new Intent(this, SingleDrugActivity.class);
        singleDrugEntry = (EditText) findViewById(R.id.singleDrugEntry);
        String singleDrug = singleDrugEntry.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, singleDrug);
        startActivity(intent);
    }
}
