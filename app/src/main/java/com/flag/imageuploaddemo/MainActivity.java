package com.flag.imageuploaddemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private Button chooseImg, uploadImg;
    private ImageView selectedImg;
    private ProgressBar progressBar;
    private TextView showImgs;
    private Uri uri;

    private StorageReference sRef, fRef;
    private DatabaseReference dbRef;

    private MimeTypeMap mimeTypeMap;
    private ContentResolver contentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chooseImg = findViewById(R.id.ci);
        uploadImg = findViewById(R.id.upload);
        selectedImg = findViewById(R.id.image);
        progressBar = findViewById(R.id.pb);
        showImgs = findViewById(R.id.si);

        sRef = FirebaseStorage.getInstance().getReference("images/");
        dbRef = FirebaseDatabase.getInstance().getReference("images");

        mimeTypeMap = MimeTypeMap.getSingleton();
        contentResolver = getContentResolver();

        chooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        uploadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImg.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                fRef = sRef.child(System.currentTimeMillis()+"."+mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri)));
                fRef.putFile(uri).
                        addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                progressBar.setProgress((int)(100 * snapshot.getBytesTransferred()/snapshot.getTotalByteCount()));
                            }
                        }).
                        addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                fRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        dbRef.child(dbRef.push().getKey()).setValue(uri.toString());
                                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                                        Toast.makeText(MainActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                            }
                        });
            }
        });

        showImgs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){
            uri = data.getData();
            Picasso.get().load(uri).into(selectedImg);
            uploadImg.setEnabled(true);
        }
    }
}