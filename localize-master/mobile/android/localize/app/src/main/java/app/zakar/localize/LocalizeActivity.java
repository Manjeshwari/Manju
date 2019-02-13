package app.zakar.localize;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import static java.text.DateFormat.getDateInstance;

public class LocalizeActivity extends Activity {

    // Google Firebase:
    private FirebaseDatabase firebaseDatabase;

    // Android WIFI Manager:
    private WifiManager wifiManager;

    private Timer timerUpload;
    private Timer timerLocate;

    // UI Components:
    // Constraint Layouts:
    private ConstraintLayout layoutHome;
    private ConstraintLayout layoutSample;
    private ConstraintLayout layoutLocate;
    // Home Layout:
    private Button buttonHomeSample;
    private Button buttonHomeLocate;
    private Button buttonHomeEnableLocationServices;
    // Sample Layout:
    private Button buttonSampleBack;
    private Button buttonSampleScan;
    private EditText editTextSampleCountry;
    private EditText editTextSamplePostCode;
    private EditText editTextSampleRegion;
    private EditText editTextSampleTown;
    private EditText editTextSampleBuildingName;
    private EditText editTextSampleFloor;
    private EditText editTextSampleStreetType;
    private EditText editTextSampleStreetName;
    private EditText editTextSampleStreetNumber;
    private EditText editTextSampleLocationName;
    // Locate Layout:
    private Button buttonLocateBack;
    private Button buttonLocateLocate;
    private Switch switchLocateAutoLocate;
    private TextView textViewLocateResults;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localize);

        // Create reference to Google Firebase database:
        firebaseDatabase = FirebaseDatabase.getInstance();

        // Create reference to device WIFI Manager Service:
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Create reference to UI components:
        // Constraint Layouts:
        layoutHome = findViewById(R.id.layout_home);
        layoutSample = findViewById(R.id.layout_sample);
        layoutLocate = findViewById(R.id.layout_locate);

        // Home Components:
        // Button:
        buttonHomeSample = findViewById(R.id.button_home_sample);
        buttonHomeLocate = findViewById(R.id.button_home_locate);
        buttonHomeEnableLocationServices = findViewById(R.id.button_home_enable_location_services);
        // Sample Components:
        // Button:
        buttonSampleBack = findViewById(R.id.button_sample_back);
        buttonSampleScan = findViewById(R.id.button_sample_scan);
        // EditText:
        editTextSampleCountry = findViewById(R.id.editText_sample_country);
        editTextSamplePostCode = findViewById(R.id.editText_sample_post_code);
        editTextSampleRegion = findViewById(R.id.editText_sample_region);
        editTextSampleTown = findViewById(R.id.editText_sample_town);
        editTextSampleBuildingName = findViewById(R.id.editText_sample_building_name);
        editTextSampleFloor = findViewById(R.id.editText_sample_floor);
        editTextSampleStreetType = findViewById(R.id.editText_sample_street_type);
        editTextSampleStreetName = findViewById(R.id.editText_sample_street_name);
        editTextSampleStreetNumber = findViewById(R.id.editText_sample_street_number);
        editTextSampleLocationName = findViewById(R.id.editText_sample_location_name);

        // Locate Components:
        // Button:
        buttonLocateBack = findViewById(R.id.button_locate_back);
        buttonLocateLocate = findViewById(R.id.button_locate_locate);
        // Switch:
        switchLocateAutoLocate = findViewById(R.id.switch_path_auto_locate);
        // TextView:
        textViewLocateResults = findViewById(R.id.textView_locate_results);

        //Button On Click Listeners:
        // Home Buttons:
        buttonHomeSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change View.
                layoutHome.setVisibility(View.GONE);
                layoutSample.setVisibility(View.VISIBLE);
            }
        });
        buttonHomeLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change View.
                layoutHome.setVisibility(View.GONE);
                layoutLocate.setVisibility(View.VISIBLE);
                uploadDeviceData();
            }
        });
        buttonHomeEnableLocationServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Activity to have user enable location services for this application.
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        });
        // Sample Buttons:
        buttonSampleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change View.
                layoutHome.setVisibility(View.VISIBLE);
                layoutSample.setVisibility(View.GONE);
            }
        });
        buttonSampleScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadSampleData();
            }
        });
        // Locate Buttons:
        buttonLocateBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutHome.setVisibility(View.VISIBLE);
                layoutLocate.setVisibility(View.GONE);
                if (timerUpload != null) {
                    timerUpload.cancel();
                    timerUpload.purge();
                }
                if (timerLocate != null) {
                    timerLocate.cancel();
                    timerLocate.purge();
                }
                switchLocateAutoLocate.setChecked(false);
                textViewLocateResults.setText("Searching...");
            }
        });
        buttonLocateLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });

        switchLocateAutoLocate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if (isChecked) {
                    int delay = 5 * 1000;
                    int period = 5 * 1000;
                    timerLocate = new Timer();
                    timerLocate.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            getDeviceLocation();
                        }
                    }, delay, period);
                } else {
                    if (timerLocate != null) {
                        timerLocate.cancel();
                        timerLocate.purge();
                    }
                }
            }
        });
    }

    private void uploadSampleData() {
        // Validate and process fields
        final String sampleLocation = getSampleFields();
        if (sampleLocation.equals("")) {
            return;
        }

        // Check WIFI state.
        if (!getWifiState()) {
            Toast.makeText(getApplicationContext(), "Error: Please enable WIFI on your device.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // WIFI manager start scan.
        wifiManager.startScan();

        // Create a new broadcast receiver that will execute upon receiving scan results.
        new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(getLocalClassName(), "No. Results: " + wifiManager.getScanResults().size());

                if (wifiManager.getScanResults().size() == 0) {
                    Toast.makeText(getApplicationContext(), "Fail: No scan results. Enable WIFI and location services for this app if disabled.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Get scan results from the receiver.
                List<ScanResult> sample = wifiManager.getScanResults();

                // Create Map with data.
                try {
                    // Create sampled data to save to Firebase
                    Map<String, Object> sampleData = createSampleData(sample);

                    // Create reference path to Firebase database.
                    String firebaseInstanceId = FirebaseInstanceId.getInstance().getId();
                    String referencePath = String.format("user/%s/location/%s", firebaseInstanceId, sampleLocation);
                    DatabaseReference databaseReference = firebaseDatabase.getReference(referencePath);

                    // Save sample to Firebase database at the reference path.
                    databaseReference.getRef().push().setValue(sampleData).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), "Succees: Data was saved",
                                    Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(getLocalClassName(), "onFailure: " + e);
                            Toast.makeText(getApplicationContext(), "Fail:" + e.toString(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Fail:" + e.toString(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }.onReceive(getApplicationContext(), getIntent());
    }


    private String getSampleFields() {
        String locationName = editTextSampleLocationName.getText().toString();
        if (locationName.equals("")) {
            Toast.makeText(getApplicationContext(), "Location name is required.",
                    Toast.LENGTH_LONG).show();
            return "";
        }
        HashMap<Integer, String> SampleFields = new HashMap<>();
        SampleFields.put(0, editTextSampleLocationName.getText().toString());
        SampleFields.put(1, editTextSampleStreetNumber.getText().toString());
        SampleFields.put(2, editTextSampleStreetName.getText().toString());
        SampleFields.put(3, editTextSampleStreetType.getText().toString());
        SampleFields.put(4, editTextSampleFloor.getText().toString());
        SampleFields.put(5, editTextSampleBuildingName.getText().toString());
        SampleFields.put(6, editTextSampleTown.getText().toString());
        SampleFields.put(7, editTextSampleRegion.getText().toString());
        SampleFields.put(8, editTextSamplePostCode.getText().toString());
        SampleFields.put(9, editTextSampleCountry.getText().toString());
        if (validateFields(SampleFields)) {
            return processFields(SampleFields);
        }
        Toast.makeText(getApplicationContext(), "Remove commas ',' from all fields.", Toast.LENGTH_LONG).show();
        return "";
    }

    private void uploadDeviceData() {
        int delay = 31 * 1000;
        int period = 31 * 1000;
        timerUpload = new Timer();
        timerUpload.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Check WIFI state.
                if (!getWifiState()) {
                    Toast.makeText(getApplicationContext(), "Error: Please enable WIFI on your device.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // WIFI manager start scan.
                wifiManager.startScan();

                // Create a new broadcast receiver that will execute upon receiving scan results.
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Log.d(getLocalClassName(), "No. Results: " + wifiManager.getScanResults().size());

                        if (wifiManager.getScanResults().size() == 0) {
                            Toast.makeText(getApplicationContext(), "Fail: No scan results. Enable WIFI and location services for this app if disabled.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Get scan results from the receiver.
                        List<ScanResult> sample = wifiManager.getScanResults();

                        // Create Map with data.
                        try {
                            // Create sampled data to save to Firebase
                            Map<String, Object> sampleData = createSampleData(sample);

                            // Create reference path to Firebase database.
                            String firebaseInstanceId = FirebaseInstanceId.getInstance().getId();
                            String referencePath = String.format("device/%s/", firebaseInstanceId);
                            DatabaseReference databaseReference = firebaseDatabase.getReference(referencePath);

                            // Save sample to Firebase database at the reference path.
                            databaseReference.getRef().updateChildren(sampleData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "Succees: Data was saved",
                                            Toast.LENGTH_LONG).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(getLocalClassName(), "onFailure: " + e);
                                    Toast.makeText(getApplicationContext(), "Fail:" + e.toString(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Fail:" + e.toString(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }.onReceive(getApplicationContext(), getIntent());
            }
        }, delay, period);
    }

    private void getDeviceLocation() {
        // Get the device location when it is update in Firebase.
        String firebaseInstanceId = FirebaseInstanceId.getInstance().getId();
        String referencePath = String.format("device/%s/location", firebaseInstanceId);
        DatabaseReference deviceLocation = firebaseDatabase.getReference(referencePath);
        ValueEventListener deviceListener = new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                try {
                    textViewLocateResults.setText(dataSnapshot.getValue().toString());
                } catch (Exception e) {
                    textViewLocateResults.setText("Searching...");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        deviceLocation.addValueEventListener(deviceListener);
    }

    private Map<String, Object> createSampleData(List<ScanResult> sample) {
        Map<String, Object> sampleData = new HashMap<>();
        String time_stamp = getDateUTC();
        sampleData.put("time", time_stamp);
        Map<String, Object> data = new HashMap<>();
        for (ScanResult modem : sample) {
            data.put(modem.BSSID, modem.level);
        }
        sampleData.put("data", data);
        return sampleData;
    }


    private boolean validateFields(HashMap<Integer, String> fields) {
        for (Integer id : fields.keySet()) {
            String value = fields.get(id);
            if (value == null) {
                continue;
            }
            // Check if character ',' exists in String value.
            if (value.indexOf(',') != -1) {
                return false;
            }
        }
        return true;
    }

    private String processFields(HashMap<Integer, String> fields) {
        ArrayList<String> processedFields = new ArrayList<>();
        for (Integer id : fields.keySet()) {
            String value = fields.get(id);
            if (value == null) {
                continue;
            }
            if (value.equals("")) {
                processedFields.add("_");
            } else {
                value = value.replace(" ", "_");
                processedFields.add(value);
            }
        }
        // Return concatenated list delimited with character ",".
        return String.join(",", processedFields);
    }

    private String getDateUTC() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date) + " UTC";
    }


    private Boolean getWifiState() {
        if (wifiManager.isWifiEnabled()) {
            return true;
        }
        Toast.makeText(getApplicationContext(),
                "WIFI is disabled. Please enable.",
                Toast.LENGTH_LONG).show();
        return false;
    }
}
