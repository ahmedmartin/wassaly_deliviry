package com.ahmed.martin.delivery_wassally;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main extends AppCompatActivity {

    private ListView order_list;
    private ArrayAdapter<Integer> adapter;


    private DatabaseReference myref;

    private String tid;

    private ProgressBar bar;

    private ValueEventListener value_listen;


    private ArrayList<Integer> kms = new ArrayList<>();
    private ArrayList<String> keys = new ArrayList<>();
    private ArrayList<order> orders =new ArrayList<>();

    private LocationManager lm;
    private LocationListener l;

    private Double latitude;
    private Double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bar =findViewById(R.id.progressBar2);

        /* find current location for delivery every 5 m */
        l = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // every time location change get data and calculate every thing for new location
                // and put location info in locate
               // locat.setText(location.getLatitude()+" : "+location.getLongitude()+"\n");
                bar.setVisibility(View.INVISIBLE);
                Geocoder geo =new Geocoder(Main.this);
                List<Address> list =new ArrayList<>();
                try {
                    list=geo.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                } catch (IOException e) {

                }
                if(list.size()>0)
                   // locat.append(list.get(0).getAddressLine(0));

                latitude=location.getLatitude();
                longitude=location.getLongitude();
                get_data();
            }
            public void onStatusChanged(String s, int i, Bundle bundle) {}
            public void onProviderEnabled(String s) {
                Toast.makeText(Main.this, " the location is opened ", Toast.LENGTH_LONG).show();
            }
            public void onProviderDisabled(String s) {
                // if location turn off show location setting
                Intent it = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(it);
                Toast.makeText(Main.this, "must open the location", Toast.LENGTH_LONG).show();
            }
        };

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
               if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                     ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},10);

        }else
            //do change every 5 s or 10 m
            lm.requestLocationUpdates(lm.GPS_PROVIDER, 60*5000, 1000, l);
        /**/



        tid=FirebaseAuth.getInstance().getCurrentUser().getUid().toString();


        myref = FirebaseDatabase.getInstance().getReference().child("orders").child("wait");

        value_listen = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                orders.clear(); // clear data and put new data
                kms.clear();
                keys.clear();
                int i=0;
                for(DataSnapshot data:dataSnapshot.getChildren()){
                    String key =data.getKey().toString();
                    float result[]=new float[10];
                    order ord=data.getValue(order.class);
                    Location.distanceBetween(latitude,longitude,ord.getS_lat(),ord.getS_long(),result);
                    int distance = round_up(result[0]/1000);
                    kms.add(distance);
                    orders.add(ord);
                    keys.add(key);
                    i++;
                }
                if(i==dataSnapshot.getChildrenCount()) {
                    sort_show_data();
                }
                // adapter.notifyDataSetChanged();
            }
            public void onCancelled(DatabaseError databaseError) {}
        };
        // list view show km in nearst to delivery
        order_list = findViewById(R.id.order_list);
        adapter=new ArrayAdapter<>(Main.this,R.layout.list_show,R.id.list_text,kms);
        order_list.setAdapter(adapter);
        order_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // go to order info to how order details
                // put order & key & show accept button
                  Intent order_info = new Intent(Main.this,order_info.class);
                  order_info.putExtra("order",orders.get(i));
                  order_info.putExtra("key",keys.get(i));
                  order_info.putExtra("show_button","accept");
                  order_info.putExtra("km",kms.get(i));
                  startActivity(order_info);
            }
        });



    }


    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions, int[] grantResults) {
        if (requestCode == 10) {

            if (grantResults.length >0&&grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                location();
            }else{
                Toast.makeText(Main.this, "must get us location permission to can get orders", Toast.LENGTH_LONG).show();
            }
        }
    }



    public void location  (){
                 bar.setVisibility(View.VISIBLE);
                if (ActivityCompat.checkSelfPermission(Main.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Main.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(Main.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},10);

                else
                    lm.requestLocationUpdates(lm.GPS_PROVIDER, 5000, 1000, l);
               /* Location ll =lm.getLastKnownLocation(lm.GPS_PROVIDER);
                if(ll==null)
                    ll=lm.getLastKnownLocation(lm.PASSIVE_PROVIDER);
                if(ll!=null) {
                    latitude = ll.getLatitude();
                    longitude = ll.getLongitude();
                    //  locat.setText(longitude+" : "+latitude+"\n");
                    Geocoder geo =new Geocoder(Main.this);
                    List<Address> list =new ArrayList<>();
                    try {
                        list=geo.getFromLocation(latitude,longitude,1);
                    } catch (IOException e) {

                    }
                    if(list.size()>0)
                        // locat.append(list.get(0).getAddressLine(0));

                        get_data();
                }*/


    }



    public void get_data(){
        myref.addValueEventListener(value_listen);

    }

    private void sort_show_data() {
        for(int i=0;i<kms.size()-1;i++){
            for(int j=i+1;j<kms.size();j++){
                if(kms.get(i)>kms.get(j)){
                    int temp_distance = kms.get(i);
                    String temp_key = keys.get(i);
                    order temp_ord = orders.get(i);

                    kms.set(i,kms.get(j));
                    orders.set(i,orders.get(j));
                    keys.set(i,keys.get(j));

                    kms.set(j,temp_distance);
                    orders.set(j,temp_ord);
                    keys.set(j,temp_key);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // return km in int number
    public int round_up(double number){

         int round =(int) number;
         if(number%round==0){
             return round;
         }else
             return round+1;
    }



 // log out from the app
    public void log_out(View view) {
        FirebaseAuth mauth = FirebaseAuth.getInstance();
        mauth.signOut();
        Intent sign_in=new Intent(Main.this,sign_in.class);
        sign_in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
        startActivity(sign_in);
    }

    @Override
    protected void onStop() {
        super.onStop();
        myref.removeEventListener(value_listen);
        if(lm!=null) {
            lm.removeUpdates(l);
            lm = null;
        }
        bar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    }

    public void onBackPressed() {
        Intent t=new Intent(this,sign_in.class);
        t.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
    }


    public void get_location(View view) {
        location();
    }
}
