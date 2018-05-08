package com.example.marcin.eventtracer;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    private ArrayList<EventClassItem> itemArrayListUp, itemArrayListOn, itemArrayListFi;
    private Spinner upcSpinner, ongSpinner, finSpinner;
    private FloatingActionButton camqr;
    private TabHost tabhost;
    private boolean success = false, done=false;
    private ConnectionClass connectionClass;
    private ListView upcomingList;
    private ListView ongoingList;
    private ListView finishedList;
    private SyncData orderData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSpinners();
        setCamQR();
        setTabs();

        upcomingList = findViewById(R.id.upcomingList);
        ongoingList = findViewById(R.id.ongoingList);
        finishedList = findViewById(R.id.finishedList);

        connectionClass = new ConnectionClass();

        itemArrayListUp = new ArrayList<EventClassItem>();
        itemArrayListOn = new ArrayList<EventClassItem>();
        itemArrayListFi = new ArrayList<EventClassItem>();

        orderData = new SyncData();
        orderData.execute("");

        setClickItem();
    }

    public void setAdapters(){
        if (success == false) {
            //do nothing
        } else {
            try {
                upcomingList.setAdapter(new ListViewAdapter(itemArrayListUp, this));
                ongoingList.setAdapter(new ListViewAdapter(itemArrayListOn, this));
                finishedList.setAdapter(new ListViewAdapter(itemArrayListFi, this));
            } catch (Exception ex) {
                // do nothing
            }
        }
    }

    private void setTabs() {
        tabhost = (TabHost) findViewById(R.id.menuWyboru);
        tabhost.setup();

        TabHost.TabSpec ts = tabhost.newTabSpec(getString(R.string.upcoming));
        ts.setContent(R.id.upcoming);
        ts.setIndicator(getString(R.string.upcoming).toUpperCase());
        tabhost.addTab(ts);

        ts = tabhost.newTabSpec(getString(R.string.ongoing));
        ts.setContent(R.id.ongoing);
        ts.setIndicator(getString(R.string.ongoing).toUpperCase());
        tabhost.addTab(ts);

        ts = tabhost.newTabSpec(getString(R.string.finished));
        ts.setContent(R.id.finished);
        ts.setIndicator(getString(R.string.finished).toUpperCase());
        tabhost.addTab(ts);
    }

    private void setCamQR() {
        camqr = (FloatingActionButton) findViewById(R.id.camQR);
        camqr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Choose action.");
                builder.setMessage("Kod QR pozwala na natychmiastowe przejscie do wydarzenia. Zeskanuj lub wgraj kod by przejsc do interesujacego Cie wydarzenia.");
                builder.setCancelable(true);
                builder.setPositiveButton("Use camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                        startActivity(intent);
                    }
                });
                builder.setNeutralButton("Select file", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
                        }
                        Toast.makeText(MainActivity.this, "Plik musi byc w formacie .png", Toast.LENGTH_SHORT);

                        new MaterialFilePicker()
                                .withActivity(MainActivity.this)
                                .withRequestCode(1000)
                                .withFilter(Pattern.compile(".*\\.png$"))
                                .withHiddenFiles(true)
                                .start();

                    }
                });
                AlertDialog dialog = builder.show();
                TextView messageText = (TextView) dialog.findViewById(android.R.id.message);
                messageText.setGravity(Gravity.CENTER);
                dialog.show();
            }
        });
    }

    private String readQR(String file) {
        Bitmap generatedQRCode = null;
        try {
            File f = new File(file);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            generatedQRCode = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        int width = generatedQRCode.getWidth();
        int height = generatedQRCode.getHeight();
        int[] pixels = new int[width * height];
        generatedQRCode.getPixels(pixels, 0, width, 0, 0, width, height);

        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);

        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        Reader reader = new MultiFormatReader();
        Result result = null;
        try {
            result = reader.decode(binaryBitmap);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (ChecksumException e) {
            e.printStackTrace();
            return null;
        } catch (FormatException e) {
            e.printStackTrace();
            return null;
        }
        return result.getText();
    }

    private void setSpinners() {
        upcSpinner = (Spinner) findViewById(R.id.spinnerUpcoming);
        ongSpinner = (Spinner) findViewById(R.id.spinnerOngoing);
        finSpinner = (Spinner) findViewById(R.id.spinnerFinish);

        String[] items = new String[]{"Verified", "City", "Date", "Category"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);

        upcSpinner.setAdapter(adapter);
        ongSpinner.setAdapter(adapter);
        finSpinner.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            // Do anything with file

            String message = readQR(filePath);
            if (message == null) message = "Can't read the file.";
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(message)
                    .setPositiveButton("OK", null)
                    .create()
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1001:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission granted!", Toast.LENGTH_SHORT);
                } else {
                    Toast.makeText(MainActivity.this, "Permission denied!", Toast.LENGTH_SHORT);
                    finish();
                }
                break;
        }
    }


    private void setClickItem() {
        upcomingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listItemClicked(itemArrayListUp, position);
               /* EventClassItem ev = findInList(itemArrayListUp, position);
                if(!(ev == null)){
                    Intent myIntent = new Intent(MainActivity.this, EventActivity.class);
                    myIntent.putExtra("EventClassItem", ev);
                } */
            }
        });

        ongoingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listItemClicked(itemArrayListOn, position);
                /*EventClassItem ev = findInList(itemArrayListOn, position);
                if(!(ev == null)){
                    Intent myIntent = new Intent(MainActivity.this, EventActivity.class);
                    myIntent.putExtra("EventClassItem", ev);
                }*/

            }
        });

        finishedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listItemClicked(itemArrayListFi, position);

            }
        });
    }

    private void listItemClicked(ArrayList<EventClassItem> itemArrayList, int position) {
        EventClassItem ev = findInList(itemArrayList, position);
        if (ev == null) {
            Log.e("Click null exceptionproblem", "Znaleziono nulla.");
        } else {
            Log.w("Starting event intent", "Uruchamianie intentu dla " + ev.getNazwa());
            Intent myIntent = new Intent(MainActivity.this, EventActivity.class);
            myIntent.putExtra("EventClassItem", ev);
            startActivity(myIntent);
        }
    }


    private EventClassItem findInList(ArrayList<EventClassItem> itemArrayList, int position) {
        EventClassItem ev = itemArrayList.get(position);
        return ev;
    }


    //Task to load ListViews
    private class SyncData extends AsyncTask<String, String, String> {
        String msg = "Internet/DB_Credentials/Windows_FireWall_TurnOn Error, See Android Monitor in the bottom For details!";
        ProgressDialog progress;


        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, "Collecting data",
                    "Starting...", true);
        }


        protected String doInBackground(String... strings) {
            try {
                Connection conn = connectionClass.CONN();
                if (conn == null) {
                    success = false;
                } else {
                    progress.setMessage("Preparing query...");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String currentDate = sdf.format(new Date());
                    String queryUp = "SELECT * FROM Wydarzenia WHERE DataOd > '" + currentDate + "' ORDER BY DataOd";
                    String queryOn = "SELECT * FROM wydarzenia WHERE '" + currentDate + "' BETWEEN DataOd AND DataDo ORDER BY DataOd";
                    String queryFi = "SELECT * FROM Wydarzenia WHERE DataDo < '" + currentDate + "' ORDER BY DataOd DESC";


                    Statement stmt = conn.createStatement();
                    doQuery(stmt, queryUp, itemArrayListUp);
                    doQuery(stmt, queryOn, itemArrayListOn);
                    doQuery(stmt, queryFi, itemArrayListFi);
                    progress.setMessage("Inserting records...");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Writer writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                msg = writer.toString();
                success = false;
            }
            return msg;
        }


        protected void onPostExecute(String msg) {
            progress.dismiss();
            Toast.makeText(MainActivity.this, msg + "", Toast.LENGTH_LONG).show();
            done=true;
            setAdapters();
        }

        private void doQuery(Statement stmt, String query, ArrayList<EventClassItem> itemArrayList) throws SQLException {
            ResultSet rs = stmt.executeQuery(query);
            if (rs != null) {
                while (rs.next()) {
                    try {

                        EventClassItem event = new EventClassItem(rs.getInt("ID"), rs.getString("Nazwa"),
                                rs.getString("Organizator"), rs.getString("Miasto"), rs.getString("Adres"),
                                rs.getString("DataOd"), rs.getString("DataDo"), rs.getString("Opis"),
                                rs.getString("Bilet"), rs.getString("SocialMedia"));

                        itemArrayList.add(event);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                msg = "Found";
                success = true;
            } else {
                msg = "No Data found!";
                success = false;
            }
        }

    }

    //Adapter for Event ListView
    public class ListViewAdapter extends BaseAdapter         //has a class viewholder which holds
    {
        public class ViewHolder {
            ImageView logo;
            TextView title;
            TextView place;
            TextView dates;
        }

        public List<EventClassItem> eventList;

        public Context context;
        ArrayList<EventClassItem> arraylist;

        protected ListViewAdapter(List<EventClassItem> apps, Context context) {
            this.eventList = apps;
            this.context = context;
            arraylist = new ArrayList<EventClassItem>();
            arraylist.addAll(eventList);
        }

        @Override
        public int getCount() {
            return eventList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) // inflating the layout and initializing widgets
        {

            View rowView = convertView;
            ViewHolder viewHolder = null;
            if (rowView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                rowView = inflater.inflate(R.layout.activity_event_list, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.logo = (ImageView) rowView.findViewById(R.id.eventListLogo);
                viewHolder.title = (TextView) rowView.findViewById(R.id.eventListTitle);
                viewHolder.place = (TextView) rowView.findViewById(R.id.eventListPlace);
                viewHolder.dates = (TextView) rowView.findViewById(R.id.eventListDate);
                rowView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            EventClassItem event = eventList.get(position);

            Picasso.with(context).load(R.drawable.iem_sydney).into(viewHolder.logo);
            viewHolder.title.setText(event.getNazwa() + "");
            viewHolder.place.setText("Adres: " + event.getAdres() + ", " + event.getMiasto() + "");

            String data = rozloz(event.getDataOd(), event.getDataDo());
            event.setDataOutput(data);

            viewHolder.dates.setText("Data: " + data + "");

            return rowView;
        }

        private String rozloz(String dataOd, String dataDo) {
            boolean bRok = false, bMsc = false, bDzien = false;

            String date;
            String rok = dataOd.substring(0, 4), rok1 = dataDo.substring(0, 4);
            ;
            String msc = dataOd.substring(5, 7), msc1 = dataDo.substring(5, 7);
            String dzien = dataOd.substring(8, 10), dzien1 = dataDo.substring(8, 10);


            if (rok.equals(rok1)) bRok = true;

            if (msc.equals(msc1)) bMsc = true;

            if (dzien.equals(dzien1)) bDzien = true;


            if (bRok) {

                if (bMsc) {

                    if (bDzien) {
                        date = dzien + "." + msc + "." + rok;
                    } else {
                        date = dzien + "-" + dzien1 + "." + msc + "." + rok;
                    }


                } else {
                    date = dzien + "." + msc + " - " + dzien1 + "." + msc1 + "." + rok;
                }

            } else {
                date = dzien + "." + msc + "." + rok + " - " + dzien1 + "." + msc1 + "." + rok1;
            }

            return date;
        }
    }
}