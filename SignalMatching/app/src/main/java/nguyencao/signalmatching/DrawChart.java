package nguyencao.signalmatching;

import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.CombinedXYChart;
import org.achartengine.chart.LineChart;
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


public class DrawChart extends AppCompatActivity {
    Button butHome;
    Button butMatch;
    private View chart;
    private Parameters params = new Parameters();

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_chart);
        final String spinner_value = getIntent().getStringExtra("spinner_value");


        ////_____________ BUTTON HOME__________________////
        butHome = (Button) findViewById(R.id.butHome);
        butHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ////_____________ BUTTON MATCH__________________////
        butMatch = (Button) findViewById(R.id.butMatch);

        butMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent MatchAct = new Intent(DrawChart.this, MatchChart.class);
                MatchAct.putExtra("spinner_value", spinner_value);
                finish();
                startActivity(MatchAct);
            }
        });

        ////_____________ DRAW CHART HOME__________________////
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new DrawChart.readJSON().execute("http://signal25.000webhostapp.com/android/getdata.php?q="+spinner_value);
            }
        });


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    class readJSON extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            return readContentURL(params[0]);
        }
        @Override
        protected void  onPostExecute(String s) {
            try {
                JSONArray array = new JSONArray(s);
                String companyName = "None";
                for (int i=0; i<(array.length());i++){
                    JSONObject thisName = array.getJSONObject(i);
                    String namedep = thisName.getString("namedep");
                    if ( namedep.equals("today")) {
                        companyName = thisName.getString("name");
                        String[] days = thisName.getString("date").split(";");
                        String[] priceString =  thisName.getString("p").split(";");
                        String[] volString = thisName.getString("v").split(";");
                        double price[] = new double[priceString.length];
                        double minY = 99999;
                        double maxY = 0.0;
                        for ( int tmp=0;tmp < priceString.length;tmp++ ) {
                            double tmp1 = Double.parseDouble(priceString[tmp]);
                            price[tmp] = tmp1;
                            if ( minY > tmp1) { minY = tmp1;}
                            if ( maxY < tmp1) { maxY = tmp1;}
                        }
                        double vol[] = new double[volString.length];
                        for ( int tmp=0;tmp < volString.length;tmp++ ) {
                            vol[tmp] = Double.parseDouble(volString[tmp]) ;
                        }
                        minY = 0.985*minY;
                        maxY = 1.015*maxY;
                        for ( int tmp =0 ; tmp< days.length; tmp++) {
                            String tmpS = days[tmp];
                            tmpS = tmpS.substring(0,2) +"\n"+tmpS.substring(2);
                            days[tmp] = tmpS;
                        }
                        drawChartPV(companyName,price,vol,days,minY,maxY);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static String readContentURL(String theUrl){
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(theUrl);
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null){
                content.append(line + "\n");
            }
            bufferedReader.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return content.toString();
    }



    private void drawChartPV(String companyName,double[] p, double[] v,String[] days, double minY,double maxY) {
        ////____________________Creating an XYSeries + dataset__________________________
        XYSeries priceSeries = new XYSeries("Price");
        for (int i = 0; i < p.length ; i++) {
            priceSeries.add( i+1 , p[i]);
        }
        XYSeries volSeries = new XYSeries("       ");

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(volSeries);
        dataset.addSeries(priceSeries);

        ////__________________________ Creating courbe __________________________
        XYSeriesRenderer priceRenderer = new XYSeriesRenderer();
        priceRenderer.setFillPoints(true);
        priceRenderer.setLineWidth(4); //lwd
        priceRenderer.setColor(params.colorPrice); //color de la courbe
        //Renderer.setColor(Color.GREEN); //color de la courbe
        priceRenderer.setDisplayChartValues(false); // affiche le texte du point
        priceRenderer.setChartValuesTextSize(0); // Taille du texte du point
        priceRenderer.setDisplayBoundingPoints(true);
        priceRenderer.setPointStyle(PointStyle.CIRCLE);
        priceRenderer.setPointStrokeWidth(25);

        XYSeriesRenderer volRenderer = new XYSeriesRenderer();
        volRenderer.setColor(params.colorVol);
        ////_________________ Creating a XYMultipleSeriesRenderer to CUSTOMIZE the whole chart______________
        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
        multiRenderer.addSeriesRenderer(volRenderer);
        multiRenderer.addSeriesRenderer(priceRenderer);
        multiRenderer.setPointSize(10.8f);         //  ___Point___ point size in chart
        multiRenderer.setMargins(new int[] {50, 50, 300, 50}); // top - left - bottom - right);
        multiRenderer.setMarginsColor(params.colorBG);
        multiRenderer.setPanEnabled(true);         //  ___Pan___ toucher la courbe  ???(true, true)
        multiRenderer.setShowGrid(true);           //  ___GRID___ display ?
        multiRenderer.setGridColor(params.colorGrid );    //  ___GRID___ color
        multiRenderer.setGridLineWidth(2f);
        multiRenderer.setXLabels(0);               //  ___GRID___ number of lines
        multiRenderer.setYLabels(10);              //  ___GRID___ number of lines
        multiRenderer.setShowCustomTextGrid(false);//  ___GRID___ number of lines
        multiRenderer.setChartTitle("Price Curve : "+companyName);     // LABEL MAIN __ title __name
        multiRenderer.setChartTitleTextSize(70);                       // LABEL AXIS __ title __ size
        multiRenderer.setXTitle("");                                   // LABEL AXIS __ X
        multiRenderer.setYTitle("");                                   // LABEL AXIS __ Y
        multiRenderer.setAxisTitleTextSize(55);                   // LABEL AXIS __ size texteTitle
        multiRenderer.setLabelsColor(params.colorAxisTitle);      // LABEL AXIS __ color texteTile
        multiRenderer.setYLabelsColor(0, params.colorAxisUnity);  // LABEL AXIS __ color texte Unity
        multiRenderer.setXLabelsColor(params.colorAxisUnity);     // LABEL AXIS __ color texte Unity
        multiRenderer.setLabelsTextSize(42);                      // LABEL AXIS __  size texte Unity
        multiRenderer.setXLabelsAlign(Paint.Align.CENTER);        // LABEL AXIS _X_ aligner Unity
        multiRenderer.setYLabelsAlign(Paint.Align.LEFT);          // LABEL AXIS _Y_ aligner Unity
        multiRenderer.setXAxisMax(p.length+1);
        multiRenderer.setXAxisMin(0);
        multiRenderer.setYAxisMax(maxY);
        multiRenderer.setYAxisMin(minY);
        //multiRenderer.setXLabelsPadding(0.9f);
        multiRenderer.setYLabelsPadding(30.0f);
        DecimalFormat noFormat = new DecimalFormat("##0");
        noFormat.setMaximumFractionDigits(1);
        multiRenderer.setYLabelFormat(noFormat,0);
        multiRenderer.setShowLegend(false);              //___Legend___
        multiRenderer.setLegendTextSize(45);             //___Legend___
        //multiRenderer.setLegendHeight(55);
        multiRenderer.setFitLegend(true);
        multiRenderer.setBarSpacing(0.85);
        //multiRenderer.setZoomButtonsVisible(true);
        //multiRenderer.setYAxisMax(35);
        //multiRenderer.setYAxisMin(0);
        //////__________Add X Label___________/////
        multiRenderer.addXTextLabel( 1 , days[0]);
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
        ////drawing bar chart//chart = ChartFactory.getBarChartView(DrawChart.this, dataset, multiRenderer, BarChart.Type.DEFAULT);
        CombinedXYChart.XYCombinedChartDef[] types = new CombinedXYChart.XYCombinedChartDef[] {
                new CombinedXYChart.XYCombinedChartDef(BarChart.TYPE, 0),
                new CombinedXYChart.XYCombinedChartDef(LineChart.TYPE, 1)
        };
        chart = ChartFactory.getCombinedXYChartView(this, dataset, multiRenderer, types );
        chartContainer.addView(chart);
    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    /*
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("DrawChart Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
    */
}
