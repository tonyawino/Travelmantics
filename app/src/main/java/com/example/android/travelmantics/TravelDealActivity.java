package com.example.android.travelmantics;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.Arrays;

import static com.example.android.travelmantics.MainActivity.admin;

public class TravelDealActivity extends AppCompatActivity {

    private static final int PICTURE_RESULT = 10;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private TravelDeal travelDeal;

    private TextInputEditText textTitle;
    private TextInputEditText textPrice;
    private TextInputEditText textDescription;
    private Button buttonImage;
    private ImageView imageView;
    private ProgressBar progressBar;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_deal);
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        Serializable serializableExtra = getIntent().getSerializableExtra("travelDeal");
        textTitle = findViewById(R.id.text_traveldeal_title);
        textPrice = findViewById(R.id.text_traveldeal_price);
        textDescription = findViewById(R.id.text_traveldeal_description);
        buttonImage = findViewById(R.id.button_traveldeal_image);
        imageView = findViewById(R.id.imageView_traveldeal);
        progressBar = findViewById(R.id.progressBar_traveldeal);
        progressBar.setVisibility(View.GONE);
        if (serializableExtra != null) {
            this.travelDeal = (TravelDeal) serializableExtra;
            textTitle.setText(travelDeal.getTitle());
            textPrice.setText(travelDeal.getPrice());
            textDescription.setText(travelDeal.getDescription());
            Picasso.get().load(travelDeal.getImageUrl()).into(imageView);
        }

        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent, "Insert Picture"), PICTURE_RESULT);
            }
        });

        setEditable(admin);
    }

    private void setEditable(boolean admin) {
        for (TextView textView : Arrays.asList(textTitle, textPrice, textDescription)) {
            textView.setEnabled(admin);
        }
        if (admin)
            buttonImage.setVisibility(View.VISIBLE);
        else
            buttonImage.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (admin) {
            getMenuInflater().inflate(R.menu.activity_travel_deal, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DatabaseReference databaseReference = database.getReference().child("deals");
        switch (item.getItemId()) {
            case R.id.action_traveldeal_save:
                saveDeal(databaseReference);
                return true;
            case R.id.action_traveldeal_delete:
                deleteDeal(databaseReference);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteDeal(DatabaseReference databaseReference) {
        databaseReference.child(travelDeal.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(TravelDealActivity.this, "Deal deleted", Toast.LENGTH_SHORT).show();
                    storage.getReference("deals").child(travelDeal.getId()).delete();
                    finish();
                }
                else {
                    Toast.makeText(TravelDealActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveDeal(final DatabaseReference databaseReference) {
        for (TextView textView : Arrays.asList(textTitle, textPrice, textDescription)) {
            if (textView.getText().toString().trim().isEmpty()) {
                textView.setError("Please fill this field");
                return;
            }
        }
        progressBar.setVisibility(View.VISIBLE);
        final TravelDeal travelDeal = new TravelDeal();
        travelDeal.setDescription(textDescription.getText().toString());
        travelDeal.setPrice(textPrice.getText().toString());
        travelDeal.setTitle(textTitle.getText().toString());
        if (imageUri == null && TravelDealActivity.this.travelDeal != null) {
            travelDeal.setImageUrl(TravelDealActivity.this.travelDeal.getImageUrl());
        }
        Task<Void> task;
        if (TravelDealActivity.this.travelDeal == null) {
            DatabaseReference push = databaseReference.push();
            travelDeal.setId(push.getKey());
            task = push.setValue(travelDeal);
        }
        else {
            travelDeal.setId(TravelDealActivity.this.travelDeal.getId());
            task = databaseReference.child(TravelDealActivity.this.travelDeal.getId()).setValue(travelDeal);
        }
        task.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(TravelDealActivity.this, "Deal saved", Toast.LENGTH_SHORT).show();
                    TravelDealActivity.this.travelDeal = travelDeal;
                    if (imageUri != null) {
                        StorageReference ref = storage.getReference("deals").child(travelDeal.getId());
                        ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String url = uri.toString();
                                        travelDeal.setImageUrl(url);
                                        databaseReference.child(travelDeal.getId()).child("imageUrl").setValue(url);
                                    }
                                });
                            }
                        });
                    }
                    finish();
                }
                else {
                    Toast.makeText(TravelDealActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (travelDeal == null) {
            menu.removeItem(R.id.action_traveldeal_delete);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(imageView);
        }
    }
}
