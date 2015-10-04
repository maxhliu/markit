package com.example.max.markit;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URLEncoder;


public class Results extends Activity {
    LinearLayout list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        ActionBar bar = getActionBar();
        //bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ef6c00")));
        bar.setTitle(Html.fromHtml("<font color=\"white\">" + "Search Results" + "</font>"));
        //View view = this.getWindow().getDecorView();
        //view.setBackgroundColor(Color.WHITE);
        list = (LinearLayout) findViewById(R.id.list);
        Intent intent = getIntent();
        new RetrieveProductsTask(this).execute(intent.getStringExtra("SEARCH_QUERY"));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Search/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_mark) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class RetrieveProductsTask extends AsyncTask<String, Void, String[][]> {

        private Context mContext;
        public RetrieveProductsTask(Context context) {
            mContext = context;
        }

        @Override
        protected String[][] doInBackground(String... params) {
            Document d = null;
            String searchTerms = "q={\"search\":\"";
            searchTerms += params[0];
            searchTerms += "\"}";
            String[][] result = null;
            try {
                d = Jsoup
                        .connect("https://api.semantics3.com/test/v1/products?" + URLEncoder.encode(searchTerms, "UTF-8"))
                        .header("api_key", "SEM3CCCD9E6B485D8A26EF6B4510F533915B")
                        .ignoreContentType(true)
                        .get();
                String name = d.body().toString().replace("<body>", "").replace("</body>", "");
                name = name.substring(2, name.length() - 1);
                JSONObject jo = new JSONObject(name);
                JSONArray results = jo.getJSONArray("results");
                String[] names = new String[results.length()];
                String[] prices = new String[results.length()];
                String[] images = new String[results.length()];
                String[] urls = new String[results.length()];
                String[] suppliers = new String[results.length()];
                //JSONArray fields = results.getJSONObject(0).names();
                for (int i = 0; i < results.length(); i++) {
                    JSONObject product = results.getJSONObject(i);
                    prices[i] = product.getString("price");
                    names[i] = product.getString("name");
                    images[i] = product.getString("images").replace("\\", "");
                    images[i] = images[i].substring(2, images[i].length() - 2);
                    if (product.optJSONArray("sitedetails") != null) {
                        urls[i] = product.getJSONArray("sitedetails")
                                .getJSONObject(0).getString("url");
                        suppliers[i] = product.getJSONArray("sitedetails")
                                .getJSONObject(0).getString("name");
                    }
                    //result += names[i] + "   " + prices[i] + "\r\n" + images[i] +
                    //        "\r\n" + cat_ids[i] + "\r\n\r\n";
                }

                //JSONArray ja = results.names();
                //result = fields.toString();
                result = new String[5][];
                result[0] = names;
                result[1] = prices;
                result[2] = images;
                result[3] = urls;
                result[4] = suppliers;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String[][] result) {
            if (result != null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                for (int i = 0; i < result[0].length; i++) {
                    RelativeLayout product = (RelativeLayout) inflater.inflate(R.layout.product, null);
                    ImageView image = (ImageView) product.getChildAt(0);
                    TextView name = (TextView) product.getChildAt(1);
                    TextView price = (TextView) product.getChildAt(2);
                    TextView supplier = (TextView) product.getChildAt(3);
                    name.setText(result[0][i]);
                    price.setText("$" + result[1][i]);
                    supplier.setText(result[4][i]);
                    Ion.with(image).load(result[2][i]);
                    list.addView(product);
                    product.setVisibility(View.VISIBLE);
                    product.setOnClickListener(new ProductURLListener(result, i));
                }
            } else {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                TextView product = (TextView) inflater.inflate(R.layout.no_search_results, null);
                list.addView(product);
                product.setVisibility(View.VISIBLE);
            }
        }

        class ProductURLListener implements View.OnClickListener {

            String[][] result;
            int i;

            ProductURLListener(String[][] result, int i) {
                this.result = result;
                this.i = i;
            }

            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(result[3][i]));
                startActivity(browserIntent);
            }
        }
    }
}

