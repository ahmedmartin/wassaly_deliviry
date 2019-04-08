package com.ahmed.martin.delivery_wassally;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class sign_in extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView password;
    private TextView email;
    private Intent sign_up;
    private Intent main;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        password=findViewById(R.id.sign_in_password);
        email = findViewById(R.id.sign_in_email);

        main= new Intent(this,Main.class);
    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        if(currentUser!=null ){

            check_email();

        }

    }

    public void sign_in(View view) {
        String Email = email.getText().toString();
        String Password = password.getText().toString();
        if(TextUtils.isEmpty(Email) || TextUtils.isEmpty(Password)){
            Toast.makeText(this,"should inter all entry ! ",Toast.LENGTH_LONG).show();
        }else {
            mAuth.signInWithEmailAndPassword(Email, Password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                check_email();
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(sign_in.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    void check_email(){
        currentUser = mAuth.getCurrentUser();
        String tid =currentUser.getUid().toString();
        final DatabaseReference myref = FirebaseDatabase.getInstance().getReference().child("orders").child("sending").child(tid);
        myref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    order w_order = dataSnapshot.getValue(order.class);
                    Intent order_info =new Intent(sign_in.this,order_info.class);
                    order_info.putExtra("order",w_order);
                    order_info.putExtra("key","");
                    order_info.putExtra("show_button","sent");
                    finish();
                    startActivity(order_info);
                }else {
                    finish();
                    startActivity(main);
                }
                myref.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
