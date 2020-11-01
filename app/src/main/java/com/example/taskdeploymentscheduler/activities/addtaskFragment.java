package com.example.taskdeploymentscheduler.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.taskdeploymentscheduler.R;
import com.example.taskdeploymentscheduler.roomdb.Executors;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class addtaskFragment extends Fragment {

    // default to "Active"
    String defaultStatus = "Active";

    // views
    CalendarView calendar;
    TextView dateNow;
    TextView dateDeadline;
    TextInputEditText taskTitle;
    TextInputEditText taskAssignment;
    TextInputEditText assignedTo;
    Button saveButton;
    String leaderName;

    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // take xml and convert to view objects
        View atfLayout =  inflater.inflate(R.layout.fragment_addtask, container, false);

        leaderName = addtaskFragmentArgs.fromBundle(getArguments()).getUsername(); // get argument from previous activity
        dateNow = atfLayout.findViewById(R.id.dateToday);
        dateDeadline = atfLayout.findViewById(R.id.deadline);
        taskTitle = atfLayout.findViewById(R.id.task_title);
        taskAssignment = atfLayout.findViewById(R.id.task_description);
        assignedTo = atfLayout.findViewById(R.id.task_assignTo);
        saveButton = atfLayout.findViewById(R.id.save_button);
        calendar = atfLayout.findViewById(R.id.calendarView);

        dateNow.setText(formatter.format(new Date()));

        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                month = month + 1;
                dateDeadline.setText(month + "/" + dayOfMonth + "/" + year);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(leaderName.isEmpty() || assignedTo.getText().toString().isEmpty() || taskTitle.getText().toString().isEmpty() ||
                taskAssignment.getText().toString().isEmpty() || dateDeadline.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(), "All fields are required.", Toast.LENGTH_SHORT).show();
                }else {
                    try {
                        if(new SimpleDateFormat("MM/dd/yyyy").parse(dateDeadline.getText().toString()).before(new SimpleDateFormat("MM/dd/yyyy").parse(dateNow.getText().toString()))){Toast.makeText(getActivity(), "Invalid deadline", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Confirmation");
                            builder.setMessage("Are you sure you want to save?");

                            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {

                                    // create http client
                                    OkHttpClient client = new OkHttpClient();
                                    // ibahin yung 0.0.0.0 ng ip address ng wifi nyo
                                    HttpUrl url = HttpUrl.parse("http://" + getString(R.string.ip_address) + "/save");
                                    JSONObject jsonObject = new JSONObject();
                                    try {
                                        jsonObject.put("leader", leaderName.trim());
                                        jsonObject.put("member", assignedTo.getText().toString().trim());
                                        jsonObject.put("title", taskTitle.getText().toString().trim());
                                        jsonObject.put("description", taskAssignment.getText().toString().trim());
                                        jsonObject.put("deadline", dateDeadline.getText().toString());
                                        jsonObject.put("status", defaultStatus);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Log.i("MESSAGE", "Task Saved Successfully");
                                    //Log.i("test", String.valueOf(jsonObject));
                                    MediaType mediaType  = MediaType.parse("application/json; charset=utf-8");
                                    RequestBody formBody = RequestBody.create(jsonObject.toString(), mediaType );
                                    Request req = new Request.Builder()
                                            .url(url)
                                            .post(formBody)
                                            .build();
                                    client.newCall(req).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                            e.printStackTrace();
                                        }
                                        @Override
                                        public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                                            if(response.isSuccessful()){
                                                Executors.getInstance().diskIO().execute(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        // run on main thread
                                                        Executors.getInstance().mainThread().execute(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(getActivity(), "Task saved successfully!", Toast.LENGTH_SHORT).show();
                                                                assignedTo.setText("");
                                                                taskTitle.setText("");
                                                                taskAssignment.setText("");
                                                                dateDeadline.setText("");
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Do nothing
                                    dialog.dismiss();
                                }
                            });
                        builder.create().show();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return atfLayout;
    }
}