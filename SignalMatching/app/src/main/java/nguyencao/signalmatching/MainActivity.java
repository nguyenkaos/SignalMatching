package nguyencao.signalmatching;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    /////DECLARATION VARIABLES
    TextView txtv;
    Button but1;
    Button but2;
    Button butView;
    Button butMatch;
    ImageView myPic;
    Spinner spinner;
    LineGraphSeries<DataPoint> series;
    String colors1[]=new String[1];
    ArrayList<String> mang  ;
    int spinner_pos;
    String spinner_value;
    SearchView sv;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /////_____REFLEXION VARIABLES_____
        txtv = (TextView) findViewById(R.id.textView2);
        //myPic = (ImageView) findViewById(R.id.imageView);
        //but1 = (Button) findViewById(R.id.button1);
        //but2 = (Button) findViewById(R.id.button2);
        butView = (Button) findViewById(R.id.butView);
        butMatch = (Button) findViewById(R.id.butMatch);
        spinner = (Spinner) findViewById(R.id.spinner);
        sv = (SearchView) findViewById(R.id.searchview);
        //////____get content from url_______________
        mang = new ArrayList<String>();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //System.out.println("11111111");
                new readJSON().execute("http://signal25.000webhostapp.com/android/index.php");
                //System.out.println("222222");
            }
        });
        txtv.setText("Your Choice : None...");



        ////______________________________click button___________________________________
        butView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent drawChartAct = new Intent(MainActivity.this,DrawChart.class);
                drawChartAct.putExtra("spinner_value", spinner_value);
                startActivity(drawChartAct);

            }
        });
        butMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent matchAct = new Intent(MainActivity.this,MatchChart.class);
                matchAct.putExtra("spinner_value", spinner_value);
                startActivity(matchAct);

            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */


    class readJSON extends AsyncTask<String, Integer, String>{
        @Override
        protected String doInBackground(String... params) {
            return readContentURL(params[0]);
        }
        @Override
        protected void  onPostExecute(String s) {
            try {
                mang.add(" choose a stock...");
                JSONArray array = new JSONArray(s);
                for (int i=0; i<(array.length()-1);i++){
                    JSONObject thisName = array.getJSONObject(i);
                    mang.add(thisName.getString("name"));
                }

                final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>( getApplicationContext(),   android.R.layout.simple_spinner_item, mang);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                spinner.setAdapter(spinnerArrayAdapter);
                spinner_pos = spinner.getSelectedItemPosition();

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        txtv.setText( "Your Choice : " +spinner.getSelectedItem().toString() );
                        spinner_value= spinner.getSelectedItem().toString();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // TODO Auto-generated method stub
                    }
                });

                ////_______________ FOR SearchView ______________
                sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        newText=newText.toUpperCase();
                        spinnerArrayAdapter.getFilter().filter(newText);
                        int spinnerPosition = spinnerArrayAdapter.getPosition(newText);
                        if (spinnerPosition > 0 ) {
                            spinner.setSelection(spinnerPosition);
                        }

                        if ( spinner.getSelectedItem() != null ){
                            spinner_value= spinner.getSelectedItem().toString();
                            txtv.setText( "Your Choice : " + spinner_value );
                        } else {
                            txtv.setText( "Your Choice : ..." );
                        }
                        return false;
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private static String readContentURL(String theUrl)
    {
        StringBuilder content = new StringBuilder();
        try
        {
            // create a url object
            URL url = new URL(theUrl);
            // create a urlconnection object
            URLConnection urlConnection = url.openConnection();
            // wrap the urlconnection in a bufferedreader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            // read from the urlconnection via the bufferedreader
            while ((line = bufferedReader.readLine()) != null)
            {
                content.append(line + "\n");
            }
            bufferedReader.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return content.toString();
    }

}
