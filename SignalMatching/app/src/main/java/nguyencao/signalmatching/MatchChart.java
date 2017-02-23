package nguyencao.signalmatching;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MatchChart extends AppCompatActivity {

    Button butView;
    Button butHome;
    Button gg;
    private View chart;
    private TableLayout tableLayout;
    private TableRow tr;
    private ListView listView;
    ArrayAdapter<String> adapt;
    private Parameters params = new Parameters();
    private TextView nameComp;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_chart);
        //txtv = (TextView) findViewById(R.id.textView);
        butView = (Button) findViewById(R.id.butView);
        final String spinner_value = getIntent().getStringExtra("spinner_value");

        ////_____________ BUTTON HOME__________________////
        butHome = (Button) findViewById(R.id.butHome);
        butHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ////_____________ BUTTON to VIEW__________________////
        butView = (Button) findViewById(R.id.butView);
        butView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ViewAct = new Intent(MatchChart.this, DrawChart.class);
                ViewAct.putExtra("spinner_value", spinner_value);
                finish();
                startActivity(ViewAct);
            }
        });

        ////____________ READ DATA FROM URL________////
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new MatchChart.readJSON().execute("http://signal25.000webhostapp.com/android/getdata.php?q="+spinner_value);
            }
        });
    }

    class readJSON extends AsyncTask<String,Integer,String>{
        @Override
        protected String doInBackground(String... params) {
            return readFromURL(params[0]);
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        protected void onPostExecute(String s) {
            ArrayList<String> arrNameComp = new ArrayList<String>();
            try {
                JSONArray arr= new JSONArray(s);
                ArrayList<String> allCompanyNames = new ArrayList<String>();
                ArrayList<double[]> ALMatchPrice = new  ArrayList<double[]>();
                double price[] = new double[25];
                double minY = 99999;
                double maxY = 0.0;
                String companyName = "Stock Not Found";
                String[] days =new String[25];
                for (int i =0 ; i< arr.length();i++){
                    JSONObject thisName = arr.getJSONObject(i);
                    //arrNameComp.add(aComp.getString("name"));
                    String namedep = thisName.getString("namedep");
                    if ( !namedep.equals("today")) {
                        allCompanyNames.add( thisName.getString("name")  );
                        String[] priceString =  thisName.getString("p").split(";");
                        double[] priceMatching = new double[priceString.length];
                        for ( int tmp=0;tmp < priceString.length;tmp++ ) {
                            double tmp1 = Double.parseDouble(priceString[tmp]);
                            priceMatching[tmp] = tmp1 ;
                            if ( minY > tmp1) { minY = tmp1;}
                            if ( maxY < tmp1) { maxY = tmp1;}
                        }
                        ALMatchPrice.add(priceMatching);
                    } else {
                        companyName =  thisName.getString("name");
                        String[] priceString =  thisName.getString("p").split(";");
                        days = thisName.getString("date").split(";");
                        for ( int tmp=0;tmp < priceString.length;tmp++ ) {
                            double tmp1 = Double.parseDouble(priceString[tmp]);
                            price[tmp] = tmp1;
                            if ( minY > tmp1) { minY = tmp1;}
                            if ( maxY < tmp1) { maxY = tmp1;}
                        }
                    }
                }
                if ( ALMatchPrice.size()>0 ) {
                    double[][] priceMatch = new double[ALMatchPrice.size()][ALMatchPrice.get(0).length];
                    for (int tmp = 0; tmp < ALMatchPrice.size(); tmp++) {
                        priceMatch[tmp] = ALMatchPrice.get(tmp);
                    }
                    String[] namesMatchCompanys = new String[allCompanyNames.size()];
                    for (int tmp = 0; tmp < allCompanyNames.size(); tmp++) {
                        namesMatchCompanys[tmp] = allCompanyNames.get(tmp);
                    }
                    minY = 0.99*minY;
                    maxY = 1.02*maxY;
                    for ( int tmp =0 ; tmp< days.length; tmp++){
                        String tmpS = days[tmp];
                        tmpS = tmpS.substring(0,2) +"\n"+tmpS.substring(2);
                        days[tmp] = tmpS;
                    }
                    drawChart1(companyName, price, days, minY, maxY, namesMatchCompanys, priceMatch); // OK  FOR DRAW 1 char only price!!!

                    ////______________ TABLE ROW _____________________///
                    tableLayout = (TableLayout) findViewById(R.id.tableLayout);
                    tr = new TableRow(getApplicationContext());
                    nameComp = new TextView(getApplicationContext());
                    nameComp.setText("REPORT");
                    nameComp.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    nameComp.setTextSize(20.0f);
                    nameComp.setTextColor(Color.WHITE);
                    nameComp.setHintTextColor(params.colorComparedPrices);
                    nameComp.setHighlightColor(params.colorComparedPrices);
                    nameComp.setLinkTextColor(params.colorComparedPrices);
                    tr.addView(nameComp);
                    tr.setBackgroundColor(params.colorScrollViewBG0);
                    tableLayout.addView(tr);

                    for (int i = 0; i < namesMatchCompanys.length; i++) {
                        tr = new TableRow(getApplicationContext());
                        nameComp = new TextView(getApplicationContext());
                        nameComp.setText(Integer.toString(i + 1) + " - " + namesMatchCompanys[i]);
                        nameComp.setTextSize(17.0f);
                        nameComp.setTextColor(params.colorAnnotation);
                        nameComp.setHintTextColor(params.colorComparedPrices);
                        nameComp.setHighlightColor(params.colorComparedPrices);
                        nameComp.setLinkTextColor(params.colorComparedPrices);
                        tr.addView(nameComp);
                        if (i % 2 == 0) {
                            tr.setBackgroundColor(params.colorScrollViewBG1);
                        } else {
                            tr.setBackgroundColor(params.colorScrollViewBG2);
                        }
                        tableLayout.addView(tr);
                    }
                }else {
                    ////______________ TABLE ROW _____________________///
                    tableLayout = (TableLayout) findViewById(R.id.tableLayout);
                    tr = new TableRow(getApplicationContext());
                    nameComp = new TextView(getApplicationContext());
                    nameComp.setText("NOT FOUND ANY SERIES MATCHING");
                    nameComp.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    nameComp.setTextSize(20.0f);
                    nameComp.setTextColor(Color.WHITE);
                    nameComp.setHintTextColor(params.colorComparedPrices);
                    nameComp.setHighlightColor(params.colorComparedPrices);
                    nameComp.setLinkTextColor(params.colorComparedPrices);
                    tr.addView(nameComp);
                    tr.setBackgroundColor(params.colorScrollViewBG0);
                    tableLayout.addView(tr);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public void drawChart1(String companyName, double[] p, String[] days, double minY, double maxY, String[] namesMatchCompanys, double[][] priceMatch) {
        //////____________________Creating an XYSeries + dataset__________________________
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        /// pricipal curve
        XYSeries pSeries = new XYSeries("Price");
        for (int i = 0; i < p.length; i++) {
            pSeries.add(i + 1, p[i]);
        }
        /// today line
        XYSeries todayLine = new XYSeries("todayLine");
        todayLine.add(p.length,maxY*0.975);
        todayLine.add(p.length,minY);
        dataset.addSeries(todayLine);
        /// matched curves
        XYSeries mSeries;
        for (int i = 0; i < priceMatch.length; i++) {
            mSeries = new XYSeries(namesMatchCompanys[i]);
            for (int j = 0; j < priceMatch[i].length; j++) {
                mSeries.add(j + 1, priceMatch[i][j]);
            }
            mSeries.addAnnotation(Integer.toString(i+1), priceMatch[0].length+0.4, priceMatch[i][ priceMatch[0].length -1 ] );
            dataset.addSeries(mSeries);
        }
        dataset.addSeries(pSeries);
        ////__________________________ Creating courbe __________________________
        XYSeriesRenderer Renderer = new XYSeriesRenderer();
        Renderer.setFillPoints(true);
        Renderer.setLineWidth(8); //lwd
        Renderer.setColor(params.colorPrice);   //color de la courbe
        Renderer.setDisplayChartValues(false);  // affiche le texte du point
        Renderer.setChartValuesTextSize(0);     // Taille du texte du point
        Renderer.setDisplayBoundingPoints(true);
        Renderer.setPointStyle(PointStyle.CIRCLE);
        Renderer.setPointStrokeWidth(25);
        ////for matched curves
        XYSeriesRenderer matchRenderer = new XYSeriesRenderer();
        matchRenderer.setFillPoints(true);
        matchRenderer.setLineWidth(7);                        //lwd
        matchRenderer.setColor(params.colorComparedPrices);    //color de la courbe
        matchRenderer.setDisplayChartValues(false);            // affiche le texte du point
        matchRenderer.setChartValuesTextSize(0);               // Taille du texte du point
        matchRenderer.setDisplayBoundingPoints(true);
        matchRenderer.setPointStyle(PointStyle.POINT);
        matchRenderer.setPointStrokeWidth(30);
        matchRenderer.setAnnotationsTextSize(55.5f);
        matchRenderer.setAnnotationsTextAlign( Paint.Align.CENTER );
        matchRenderer.setAnnotationsColor(params.colorAnnotation);
        ////for todayLine
        XYSeriesRenderer todayLineRenderer = new XYSeriesRenderer();
        todayLineRenderer.setFillPoints(true);
        todayLineRenderer.setLineWidth(4); //lwd
        todayLineRenderer.setColor(params.colorTodayLine); //color de la courbe
        todayLineRenderer.setAnnotationsTextSize(55.5f);
        todayLineRenderer.setAnnotationsColor(params.colorTodayLine);
        ////_________________ Creating a XYMultipleSeriesRenderer to CUSTOMIZE the whole chart______________
        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
        multiRenderer.addSeriesRenderer(todayLineRenderer);
        for (int i = 0; i < priceMatch.length; i++) {
            multiRenderer.addSeriesRenderer(matchRenderer);
        }
        multiRenderer.addSeriesRenderer(Renderer);
        multiRenderer.setPointSize(12.8f);         //  ___Point___ point size in chart
        multiRenderer.setMargins(new int[]{50, 50, 50, 10});
        multiRenderer.setMarginsColor(params.colorBG);  // to mau vien den ben ngoai
        multiRenderer.setPanEnabled(true);         //  ___Pan___ toucher la courbe  ???(true, true)
        multiRenderer.setShowGrid(true);           //  ___GRID___ display ?
        multiRenderer.setGridColor(params.colorGrid);   //  ___GRID___ color

        multiRenderer.setXLabels(0);               //  ___GRID___ number of lines
        multiRenderer.setYLabels(10);              //  ___GRID___ number of lines
        multiRenderer.setShowCustomTextGrid(false);//  ___GRID___
        multiRenderer.setChartTitle(companyName);     // LABEL MAIN __ title __name
        multiRenderer.setChartTitleTextSize(70);      // LABEL AXIS __ title __ size
        multiRenderer.setXTitle("");           // LABEL AXIS __ X
        multiRenderer.setYTitle("");           // LABEL AXIS __ Y
        multiRenderer.setAxisTitleTextSize(34);         // LABEL AXIS __ size texteTitle
        multiRenderer.setLabelsColor(params.colorAxisTitle);      // LABEL AXIS __ color texteTile
        multiRenderer.setYLabelsColor(0, params.colorAxisUnity);  // LABEL AXIS __ color texte Unity
        multiRenderer.setXLabelsColor(params.colorAxisUnity);     // LABEL AXIS __ color texte Unity
        multiRenderer.setLabelsTextSize(42);            // LABEL AXIS __  size texte Unity
        multiRenderer.setXLabelsAlign(Paint.Align.CENTER); // LABEL AXIS _X_ aligner Unity
        multiRenderer.setYLabelsAlign(Paint.Align.LEFT);   // LABEL AXIS _Y_ aligner Unity
        multiRenderer.setXAxisMax( priceMatch[0].length + 3);
        multiRenderer.setXAxisMin(0);
        multiRenderer.setYAxisMax(maxY);
        multiRenderer.setYAxisMin(minY);
        //multiRenderer.setXLabelsPadding(0.9f);
        multiRenderer.setYLabelsPadding(30.0f);
        DecimalFormat noFormat = new DecimalFormat("##0");
        noFormat.setMaximumFractionDigits(1);
        multiRenderer.setYLabelFormat(noFormat, 0);
        multiRenderer.setShowLegend(false);            //___Legend___


        //////__________Add X Label___________/////

        for (int i = 0; i < p.length; i++) {
            if (i %  ( p.length / 5 ) == 0){
                multiRenderer.addXTextLabel(i + 5, days[i+4]);
            }
        }

        ////___________________DRAW CHART_______________________
        //this part is used to display graph on the xml
        LinearLayout chartContainer = (LinearLayout) findViewById(R.id.chart);
        //remove any views before u paint the chart
        chartContainer.removeAllViews();
        chart = ChartFactory.getLineChartView(this, dataset, multiRenderer);
        chartContainer.addView(chart);
    }


    private static String readFromURL(String theUrl){
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
