package com.ahmed.martin.delivery_wassally;

import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;

public class order_info extends AppCompatActivity {

    private order w_order;
    private String key;
    private String tid;
    private String show_button;
    private int km;

    private TextView sender_fname;
    private TextView sender_lname;
    private TextView sender_phone ;
    private TextView sender_address;
    private TextView receiver_fname ;
    private TextView receiver_lname ;
    private TextView receiver_phone ;
    private TextView receiver_address;
    private TextView price;
    private TextView pounce;
    private TextView description;

    private Button accept;
    private Button sent;

    private DatabaseReference myref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_info);

        w_order= (order) getIntent().getSerializableExtra("order");
        key=getIntent().getStringExtra("key");
        show_button=getIntent().getStringExtra("show_button");
        km=getIntent().getIntExtra("km",0);

        sender_fname=findViewById(R.id.sender_fname);
        sender_lname=findViewById(R.id.sender_lname);
        sender_phone=findViewById(R.id.sender_phone);
        sender_address=findViewById(R.id.sender_address);
        sender_address.setMovementMethod(new ScrollingMovementMethod());
        receiver_fname=findViewById(R.id.receiver_fname);
        receiver_lname=findViewById(R.id.receiver_lname);
        receiver_phone=findViewById(R.id.receiver_phone);
        receiver_address=findViewById(R.id.receiver_address);
        receiver_address.setMovementMethod(new ScrollingMovementMethod());
        description=findViewById(R.id.description);
        description.setMovementMethod(new ScrollingMovementMethod());
        price=findViewById(R.id.price);
        pounce=findViewById(R.id.pounce);

        accept=findViewById(R.id.accept);
        sent=findViewById(R.id.sent);

        //if deleviry come from main .... wait for accept
        // don't show all order details just address only
        if(show_button.equals("accept")){
            sent.setVisibility(View.INVISIBLE);
            sender_fname.setVisibility(View.INVISIBLE);
            sender_lname.setVisibility(View.INVISIBLE);
            sender_phone.setVisibility(View.INVISIBLE);
            receiver_fname.setVisibility(View.INVISIBLE);
            receiver_lname.setVisibility(View.INVISIBLE);
            receiver_phone.setVisibility(View.INVISIBLE);
            pounce.setText(""+km*1.5);

        }else if(show_button.equals("sent")){ // delivery in sending state then show every thing except accept button
           accept.setVisibility(View.INVISIBLE);
            pounce.setText(""+w_order.getPounce());
        }

        tid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString(); // delivery id
        /* show order details */
        myref= FirebaseDatabase.getInstance().getReference().child("user").child(w_order.getUid()).child("personalinfo");
        myref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    user use = dataSnapshot.getValue(user.class);
                    sender_fname.setText(use.getFname());
                    sender_lname.setText(use.getLname());
                }
                myref.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        sender_phone.setText(w_order.getS_phone());
        sender_address.setText(w_order.getS_address());
        receiver_fname .setText(w_order.getR_first_name());
        receiver_lname .setText(w_order.getR_last_name());
        receiver_phone .setText(w_order.getR_phone());
        receiver_address.setText(w_order.getR_address());
        description.setText(w_order.getDescription());
        price.setText(""+w_order.getPrice());

       /**/
    }



    // delivery press accept button
    public void accept(View view) {

        Intent t=new Intent(this,sign_in.class);
        t.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        show_button="sent"; // make state sent

        // set in class delivery id && start time
        Date s_dt=new Date();
        SimpleDateFormat f=new SimpleDateFormat("hh:mm:ss");
        w_order.setTid(tid);    // set delivery id
        w_order.setStart_time(f.format(s_dt).toString()); // set start date
        w_order.setPounce(km*1.5);

        /* show hidden fieldes ,sent button && hide accept button*/
        sent.setVisibility(View.VISIBLE);
        sender_fname.setVisibility(View.VISIBLE);
        sender_lname.setVisibility(View.VISIBLE);
        sender_phone.setVisibility(View.VISIBLE);
        receiver_fname.setVisibility(View.VISIBLE);
        receiver_lname.setVisibility(View.VISIBLE);
        receiver_phone.setVisibility(View.VISIBLE);
        accept.setVisibility(View.INVISIBLE);
        /**/

        // add class to sending
        myref=FirebaseDatabase.getInstance().getReference().child("orders").child("sending").child(tid);
        myref.setValue(w_order);
        // remove class from wait
        myref=FirebaseDatabase.getInstance().getReference().child("orders").child("wait").child(key);
        myref.removeValue();

    }

    //press sent button
    public void sent(View view) {
        Date e_dt=new Date();
        SimpleDateFormat form =new SimpleDateFormat("hh:mm:ss");
        w_order.setEnd_time(form.format(e_dt).toString()); // set end date

        Date date=new Date();
        SimpleDateFormat f =new SimpleDateFormat("dd-MM-yy");

        //add class to day date
        myref=FirebaseDatabase.getInstance().getReference().child("orders").child(f.format(date).toString());
        myref.push().setValue(w_order);

        //remove from sending
        myref=FirebaseDatabase.getInstance().getReference().child("orders").child("sending").child(tid);
        myref.removeValue();

        // start main to choose another order
        Intent main =new Intent(order_info.this,Main.class);
        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ActivityCompat.finishAffinity(order_info.this);
        startActivity(main);
        finish();
    }

    @Override
    public void onBackPressed() {
        if(show_button.equals("accept")) { // if accept state  can press back elss stay untill order sent
            super.onBackPressed();
        }else{
            Intent t=new Intent(this,sign_in.class);
            t.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            t.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            ActivityCompat.finishAffinity(order_info.this);
            finish();
        }
    }

    public void show_map(View view) {

        Intent map=new Intent(order_info.this,MapsActivity.class);
        map.putExtra("order",w_order);
        startActivity(map);
    }
}
