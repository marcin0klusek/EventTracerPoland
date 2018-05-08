package com.example.marcin.eventtracer;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionClass {
    String ip = "den1.mssql1.gear.host";
    String db = "mysimpleeventdb";
    String un = "mysimpleeventdb";
    String pass = "Dm3tL3!5~YcZ";

    @SuppressLint("NewApi")
    public Connection CONN() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String ConnURL = null;

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnURL = "jdbc:jtds:sqlserver://" + ip + "/" + db+ ";user=" + un + ";password=" + pass + ";";

            conn = DriverManager.getConnection(ConnURL);

        }catch (SQLException se)
        {
            Log.e("error here 1 : ", se.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            Log.e("error here 2 : ", e.getMessage());
        }
        catch (Exception e)
        {
            Log.e("error here 3 : ", e.getMessage());
        }

        return conn;
    }
}