package com.example.marcin.eventtracer;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class EventActivity extends AppCompatActivity {

    private EventClassItem ev;
    private TextView title;
    private TextView org;
    private TextView street;
    private TextView city;
    private TextView startDate;
    private TextView endDate;
    private TextView desc;
    private ImageView qrcode;
    private ImageView ticket;
    private ImageView maps;
    private ImageView social;

    private int width;
    private int height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay()
                .getMetrics(metrics);
        width = metrics.widthPixels;
        height = metrics.heightPixels;

        int qrbok = (int)(height * 0.20);
        int ico = (int) (height * 0.08);
        int titlesize = (int) (height * 0.01);


        ev = (EventClassItem) getIntent().getSerializableExtra("EventClassItem");
        findElements();

        title.setText(ev.getNazwa());
        title.setTextSize(titlesize);
        org.setText("Organizator: " + ev.getOrganizator());
        street.setText("Ulica: " + ev.getAdres());
        city.setText("Miasto: " + ev.getMiasto());
        startDate.setText("Data rozpoczecia: " + ev.getDataOd());
        endDate.setText("Data zakonczenia: " + ev.getDataDo());
        if(ev.getOpis().length() > 350)
                desc.setText("Opis: \n" + ev.getOpis().substring(0,350) + "...");
        else
                desc.setText("Opis: \n" + ev.getOpis());

        if (ev.getBilety().equals("") || ev.getBilety().equals(null)) {
            ticket.setImageResource(R.drawable.ticket_free);
            ticket.setEnabled(false);
        } else {
            ticket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = ev.getBilety();
                    if (!url.startsWith("htt")) url = "http://" + url;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);

                }
            });
        }

        maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(EventActivity.this).create();
                alertDialog.setTitle("Przekierowanie do Maps");
                alertDialog.setMessage("Brak implementacji :(");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });

        if (ev.getSocial().equals("") || ev.getSocial().equals(null)) {
            social.setImageResource(R.drawable.fb_logo_disable);
            social.setEnabled(false);
        } else {
            social.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = ev.getSocial();
                    if (!url.startsWith("htt")) url = "http://" + url;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);

                }
            });
        }


        ticket.getLayoutParams().height = ico;
        ticket.getLayoutParams().width = ico;
        ticket.requestLayout();

        maps.getLayoutParams().height = ico;
        maps.getLayoutParams().width = ico;
        maps.requestLayout();

        social.getLayoutParams().height = ico;
        social.getLayoutParams().width = ico;
        social.requestLayout();

        qrcode.getLayoutParams().height = qrbok;
        qrcode.getLayoutParams().width = qrbok;
        qrcode.requestLayout();


    }

    private void findElements() {
        title = findViewById(R.id.eventTitle);
        org = findViewById(R.id.eventOrg);
        street = findViewById(R.id.adresText);
        city = findViewById(R.id.cityText);
        startDate = findViewById(R.id.dateStartText);
        endDate = findViewById(R.id.dateToText);
        desc = findViewById(R.id.descText);
        qrcode = findViewById(R.id.eventQR);
        ticket = findViewById(R.id.eventTicket);
        maps = findViewById(R.id.eventMaps);
        social = findViewById(R.id.eventFb);
    }
}
