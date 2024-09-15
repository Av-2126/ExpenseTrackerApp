package com.projects.expensetrackerapp;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class BackupActivity extends AppCompatActivity {

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Button backupButton, restoreButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_backup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.backup), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        backupButton = findViewById(R.id.backupButton);
        restoreButton = findViewById(R.id.restoreButton);

        backupButton.setOnClickListener(v -> backupData());
        restoreButton.setOnClickListener(v -> restoreData());
    }

    private void restoreData() {
        StorageReference backupRef = storageRef.child("backups/data_backup.json");
        File localFile = new File(getFilesDir(), "restored_data.json");

        backupRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    // Restore data from local file
                    Toast.makeText(BackupActivity.this, "Restore Successful!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(BackupActivity.this, "Restore Failed.", Toast.LENGTH_SHORT).show());
    }

    private void backupData() {
        // Convert data to a format suitable for storage (e.g., CSV or JSON)
        File dataFile = new File(getFilesDir(), "data_backup.json");

        StorageReference backupRef = storageRef.child("backups/data_backup.json");
        backupRef.putFile(Uri.fromFile(dataFile))
                .addOnSuccessListener(taskSnapshot -> Toast.makeText(BackupActivity.this, "Backup Successful!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(BackupActivity.this, "Backup Failed.", Toast.LENGTH_SHORT).show());
    }
}

