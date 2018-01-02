package daniel.roomsocket;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private Button btnSend, btn1, btn2, btn3, btn4;
    private EditText textHost, textPort;

    public static final String PREFS_NAME = "MyPrefsFile";

    private String mHost;
    private int mPort;

    private boolean connected = false;

    JSONObject json = new JSONObject();

    public MainActivity() throws IOException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textHost = (EditText)findViewById(R.id.host);
        textPort = (EditText)findViewById(R.id.port);

        btnSend = (Button)findViewById(R.id.btnSave);

        btn1 = (Button)findViewById(R.id.btn1);
        btn2 = (Button)findViewById(R.id.btn2);
        btn3 = (Button)findViewById(R.id.btn3);
        btn4 = (Button)findViewById(R.id.btn4);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mHost = settings.getString("host", "");
        mPort = settings.getInt("port", 0);

        textHost.setText(mHost);
        textPort.setText(String.valueOf(mPort));

        try {
            json.accumulate("version", 0);
            json.accumulate("name", "android");
            json.accumulate("action", "");
            json.accumulate("argument", -1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHost = textHost.getText().toString();
                mPort = Integer.valueOf(textPort.getText().toString());
            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                toggleRelay(0);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                toggleRelay(1);
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                toggleRelay(2);
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                toggleRelay(3);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("host", mHost);
        editor.putInt("port", mPort);

        // Commit the edits!
        editor.commit();
    }

    public void toggleRelay(int n){
        Vibrator vi = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vi.vibrate(90);

        if(!connected){
            if(!mHost.equals("")){
                try{
                    json.put("action", "toggle");
                    json.put("argument", n);
                }catch (JSONException e){
                    e.printStackTrace();
                }

                Thread cThread = new Thread(new ClientThread(json.toString()));
                cThread.start();
            }
        }
    }

    public class ClientThread implements Runnable {

        String message = new String();

        ClientThread(String message){
            this.message = message;
        }

        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(mHost);
                Log.d("ClientActivity", "C: Connecting...");
                Socket socket = new Socket(serverAddr, mPort);
                connected = true;

                while (connected) {
                    try {
                        Log.d("ClientActivity", "C: Sending command.");
                        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
                                .getOutputStream())), true);
                        // WHERE YOU ISSUE THE COMMANDS
                        out.println(this.message);
                        Log.d("ClientActivity", "C: Sent.");
                        connected = false;
                    } catch (Exception e) {
                        Log.e("ClientActivity", "S: Error", e);
                    }
                }
                socket.close();
                Log.d("ClientActivity", "C: Closed.");
            } catch (Exception e) {
                Log.e("ClientActivity", "C: Error" + e.getMessage(), e);
                connected = false;
            }
        }
    }
}
