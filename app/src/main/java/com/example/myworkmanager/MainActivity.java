package com.example.myworkmanager;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.os.Build;
import android.Manifest;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.myworkmanager.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityMainBinding binding;
    private WorkManager workManager;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    new ActivityResultCallback<Boolean>() {
                        @Override
                        public void onActivityResult(Boolean isGranted) {
                            if (isGranted) {
                                Toast.makeText(MainActivity.this, "Notifications permission granted", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Notifications permission rejected", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        workManager = WorkManager.getInstance(this);
        binding.btnOneTimeTask.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnOneTimeTask) {
            startOneTimeTask();
        }
    }

    private void startOneTimeTask() {
        binding.textStatus.setText(getString(R.string.status));

        // Create input data for the worker
        Data data = new Data.Builder()
                .putString(MyWorker.EXTRA_CITY, binding.editCity.getText().toString())
                .build();

        // Build the one-time work request
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(MyWorker.class)
                .setInputData(data)
                .build();

        // Enqueue the work request
        workManager.enqueue(oneTimeWorkRequest);

        // Observe work info
        workManager.getWorkInfoByIdLiveData(oneTimeWorkRequest.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo != null) {
                            String status = workInfo.getState().name();
                            binding.textStatus.append("\n" + status);
                        }
                    }
                });
    }
}