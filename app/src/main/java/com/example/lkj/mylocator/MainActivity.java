package com.example.lkj.mylocator;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lkj.mylocator.model.User;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.FirebaseRecyclerAdapter;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "MainActivityLog";
    Firebase firebase;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseRecyclerAdapter<User, RecycleViewHolder> recyclerAdapter;

    private static void lookItem(Double lat, Double lng, View v) {

        Intent intent = new Intent(v.getContext(), MapsActivity.class);
        intent.putExtra(Constants.LOCATION_LAT, lat);
        intent.putExtra(Constants.LOCATION_LNG, lng);
        v.getContext().startActivity(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lookItem(0.0, 0.0, view);
                }
            });
        }

        firebase = new Firebase(Constants.FIREBASE_URL);

        if (firebase.getAuth() == null || isExpired(firebase.getAuth())) {
            logInUser();
        } else {
            Firebase fireBaseUser = new Firebase(Constants.FIREBASE_USERS + firebase.getAuth().getUid());

            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycleView);
            layoutManager = new LinearLayoutManager(this);
            if (recyclerView != null) {
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(layoutManager);
            }

            recyclerAdapter = new FirebaseRecyclerAdapter<User, RecycleViewHolder>(User.class,
                    R.layout.location_list, RecycleViewHolder.class, fireBaseUser) {
                @Override
                protected void populateViewHolder(RecycleViewHolder recycleViewHolder, User s, int i) {

                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    String label = "Unknown location ".concat(new Date().toString());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(s.getL().get(0), s.getL().get(1), 1);

                        if (addressList != null && addressList.size() > 0) {
                            label = addressList.get(0).getAddressLine(0);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, label);
                    Log.i(TAG, s.toString());
                    recycleViewHolder.textView1.setText(label);
                    recycleViewHolder.textViewLat.setText(s.getL().get(0).toString());
                    recycleViewHolder.textViewLng.setText(s.getL().get(1).toString());

                }
            };

            if (recyclerView != null) {
                recyclerView.setAdapter(recyclerAdapter);
            }
        }
    }

    private void logInUser() {

        firebase.authAnonymously(new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Toast.makeText(getApplicationContext(),
                        Constants.FIREBASE_USERS + "/" + authData.getUid(),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                switch (firebaseError.getCode()) {
                    case FirebaseError.INVALID_CREDENTIALS: {
                        Toast.makeText(getApplicationContext(), "INVALID CREDENTIALS", Toast.LENGTH_LONG).show();
                        break;
                    }
                    case FirebaseError.USER_DOES_NOT_EXIST: {
                        Toast.makeText(getApplicationContext(), "USER DOES NOT EXIST", Toast.LENGTH_LONG).show();
                        break;
                    }
                    default: {
                        Toast.makeText(getApplicationContext(), "Authenticate Failed", Toast.LENGTH_LONG).show();
                        break;
                    }
                }
            }
        });

    }

    private boolean isExpired(AuthData authData) {
        return (System.currentTimeMillis() / 1000) >= authData.getExpires();
    }


    public static class RecycleViewHolder extends RecyclerView.ViewHolder {
        TextView textView1, textViewLat, textViewLng;

        public RecycleViewHolder(View itemView) {
            super(itemView);
            textView1 = (TextView) itemView.findViewById(R.id.list_title);
            textViewLat = (TextView) itemView.findViewById(R.id.text1at);
            textViewLng = (TextView) itemView.findViewById(R.id.text1ng);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "position = " + getAdapterPosition() + " is " + textViewLng.getText() + "", Toast.LENGTH_SHORT).show();
                    lookItem(Double.parseDouble(textViewLat.getText().toString()), Double.parseDouble(textViewLng.getText().toString()), v);


                }
            });

        }

    }
}
