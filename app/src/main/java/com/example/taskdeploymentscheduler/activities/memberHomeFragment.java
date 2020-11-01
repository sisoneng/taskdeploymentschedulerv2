package com.example.taskdeploymentscheduler.activities;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.example.taskdeploymentscheduler.R;
import com.example.taskdeploymentscheduler.roomdb.Executors;
import com.example.taskdeploymentscheduler.roommodel.Task;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class memberHomeFragment extends Fragment {

    TextView notifyView;
    TextView loginMessageView;
    LinearLayout tasksLayout;
    int notifyCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mfhActivity = inflater.inflate(R.layout.fragment_member_home, container, false);

        final String username = memberHomeFragmentArgs.fromBundle(getArguments()).getUsername();
        loginMessageView = mfhActivity.findViewById(R.id.login_message_member);
        notifyView = mfhActivity.findViewById(R.id.member_notif);
        String text = String.format(getString(R.string.top_message), username);
        loginMessageView.setText(text);
        tasksLayout = mfhActivity.findViewById(R.id.member_tasks);

        //  (required for android version >= 8)
        createNotificationChannel();

        OkHttpClient client = new OkHttpClient();
        // ibahin yung 0.0.0.0 ng ip address ng wifi nyo
        HttpUrl url = HttpUrl.parse("http://" + getString(R.string.ip_address) + "/member/" + username);
        Request req = new Request.Builder()
                .url(url)
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                if(response.isSuccessful()){
                    Log.i("MESSAGE", "Get member task Success");
                    Executors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            Executors.getInstance().mainThread().execute(new Runnable() {
                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                @Override
                                public void run() {
                                    try {
                                        JSONArray data = new JSONArray(response.body().string());
                                        int length = data.length();
                                        List<Task> Tasks = new ArrayList<Task>();
                                        for(int i = 0; i < length; i++){
                                            JSONObject obj = data.getJSONObject(i);
                                            String id = obj.getString("_id");
                                            String leader = obj.getString("leader");
                                            String member = obj.getString("member");
                                            String title = obj.getString("title");
                                            String description = obj.getString("description");
                                            String deadline = obj.getString("deadline");
                                            String status = obj.getString("status");
                                            Task t = new Task(id, leader, member, title, description, deadline, status);
                                            Tasks.add(t);
                                        }
                                        notifyCount = 0;
                                        int i = 0;
                                        for(Task t: Tasks){
                                            String status = t.getStatus();
                                            if(isUpcomingDeadline(t.getDeadline(), status)){
                                                notifyCount += 1;

                                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), "app1channel")
                                                        .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                                                        .setContentTitle(t.getTitle())
                                                        .setContentText(t.getAssignment())
                                                        .setContentText(t.getDeadline())
                                                        .setAutoCancel(true)
                                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                                                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getActivity());
                                                managerCompat.notify(i, builder.build());
                                            }
                                            i++;
                                        }
                                        notifyView.setText("You have " + notifyCount + " upcoming deadlines");
                                        if(notifyCount == 0){
                                            notifyView.setBackgroundColor(Color.parseColor("#008000"));
                                        }else if(notifyCount < 0){
                                            notifyCount = 0;
                                            notifyView.setBackgroundColor(Color.parseColor("#008000"));
                                        }
                                        for(Task t: Tasks){
                                            tasksLayout.addView(createView(t));
                                        }
                                    } catch (JSONException | IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
        return mfhActivity;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "app1";
            String description = "app1 channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("app1channel", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    public View createView(final Task t){
        final View taskView = getLayoutInflater().inflate(R.layout.task_row_member, null, false);
        final TextView taskTitle = taskView.findViewById(R.id.row_title_member);
        final TextView taskStatus = taskView.findViewById(R.id.row_status_member);
        final TextView taskDescription = taskView.findViewById(R.id.row_description_member);
        final TextView assignedTo = taskView.findViewById(R.id.row_member_member);
        final TextView deadline = taskView.findViewById(R.id.row_deadline_member);
        final Button updateButton = taskView.findViewById(R.id.update_button);

        taskDescription.setVisibility(View.GONE);
        assignedTo.setVisibility(View.GONE);
        deadline.setVisibility(View.GONE);
        updateButton.setVisibility(View.GONE);

        taskTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(taskDescription.getVisibility() == View.GONE && assignedTo.getVisibility() == View.GONE &&
                deadline.getVisibility() == View.GONE && updateButton.getVisibility() == View.GONE){
                    taskDescription.setVisibility(View.VISIBLE);
                    assignedTo.setVisibility(View.VISIBLE);
                    deadline.setVisibility(View.VISIBLE);
                    updateButton.setVisibility(View.VISIBLE);
                }else{
                    taskDescription.setVisibility(View.GONE);
                    assignedTo.setVisibility(View.GONE);
                    deadline.setVisibility(View.GONE);
                    updateButton.setVisibility(View.GONE);
                }
            }
        });

        taskTitle.setText(t.getTitle());
        if(isUpcomingDeadline(t.getDeadline(), t.getStatus())){
            taskTitle.setTextColor(Color.parseColor("#FF0000"));
        }
        switch (t.getStatus()) {
            case "Active":
                taskStatus.setBackground(AppCompatResources.getDrawable(getActivity(), R.mipmap.task_ip));
                break;
            case "Completed":
                taskStatus.setBackground(AppCompatResources.getDrawable(getActivity(), R.mipmap.task_complete));
                break;
            case "Failed":
                taskStatus.setBackground(AppCompatResources.getDrawable(getActivity(), R.mipmap.task_failed));
                break;
        }

        taskDescription.setText(t.getAssignment());
        assignedTo.setText("For: " + t.getMember());
        deadline.setText("Deadline: " + t.getDeadline());

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Update Status");
                builder.setMessage("Please update your status");

                builder.setPositiveButton("Mark as Complete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        Executors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    final String username = memberHomeFragmentArgs.fromBundle(getArguments()).getUsername();

                                    OkHttpClient client = new OkHttpClient();
                                    // ibahin yung 0.0.0.0 ng ip address ng wifi nyo
                                    HttpUrl url = HttpUrl.parse("http://" + getString(R.string.ip_address) + "/update/" + t.getId());
                                    JSONObject jsonObject = new JSONObject();
                                    try {
                                        jsonObject.put("status", "Complete");
                                        jsonObject.put("member", username);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    MediaType mediaType  = MediaType.parse("application/json; charset=utf-8");
                                    RequestBody formBody = RequestBody.create(jsonObject.toString(), mediaType );
                                    Request req = new Request.Builder()
                                            .url(url)
                                            .put(formBody)
                                            .build();

                                    client.newCall(req).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                            e.printStackTrace();
                                        }

                                        @Override
                                        public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                                            if (response.isSuccessful()){
                                                Log.i("MESSAGE", "Update Success");
                                                Executors.getInstance().diskIO().execute(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        // run on main thread
                                                        Executors.getInstance().mainThread().execute(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    JSONArray data = new JSONArray(response.body().string());
                                                                    int length = data.length();
                                                                    List<Task> Tasks = new ArrayList<Task>();
                                                                    for (int i = 0; i < length; i++){
                                                                        JSONObject obj = data.getJSONObject(i);
                                                                        String id = obj.getString("_id");
                                                                        String leader = obj.getString("leader");
                                                                        String member = obj.getString("member");
                                                                        String title = obj.getString("title");
                                                                        String description = obj.getString("description");
                                                                        String deadline = obj.getString("deadline");
                                                                        String status = obj.getString("status");
                                                                        Task t = new Task(id, leader, member, title, description, deadline, status);
                                                                        Tasks.add(t);
                                                                    }
                                                                    notifyCount = 0;
                                                                    for(Task t: Tasks){
                                                                        String status = t.getStatus();
                                                                        if(isUpcomingDeadline(t.getDeadline(), status)){
                                                                            notifyCount += 1;
                                                                        }else if(!status.equals("Active")){
                                                                            taskTitle.setTextColor(Color.parseColor("#000a12"));
                                                                        }
                                                                    }
                                                                    notifyView.setText("You have " + notifyCount + " upcoming deadlines");
                                                                    taskStatus.setBackground(AppCompatResources.getDrawable(getActivity(), R.mipmap.task_complete));
                                                                    if(notifyCount == 0){
                                                                        notifyView.setBackgroundColor(Color.parseColor("#008000"));
                                                                    }else if(notifyCount < 0){
                                                                        notifyCount = 0;
                                                                        notifyView.setBackgroundColor(Color.parseColor("#008000"));
                                                                    }
                                                                    Log.i("length", String.valueOf(Tasks.size()));
                                                                    for(Task t: Tasks){
                                                                        tasksLayout.addView(createView(t));
                                                                    }
                                                                } catch (IOException | JSONException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }catch (Exception e){
                                    Log.i("Exception", e.getMessage());
                                }
                            }
                        });
                    }
                });

                builder.setNegativeButton("Mark as Failed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Executors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    final String username = memberHomeFragmentArgs.fromBundle(getArguments()).getUsername();

                                    OkHttpClient client = new OkHttpClient();
                                    // ibahin yung 0.0.0.0 ng ip address ng wifi nyo
                                    HttpUrl url = HttpUrl.parse("http://" + getString(R.string.ip_address) + "/update/" + t.getId());
                                    JSONObject jsonObject = new JSONObject();
                                    try {
                                        jsonObject.put("status", "Failed");
                                        jsonObject.put("member", username);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    MediaType mediaType  = MediaType.parse("application/json; charset=utf-8");
                                    RequestBody formBody = RequestBody.create(jsonObject.toString(), mediaType );
                                    Request req = new Request.Builder()
                                            .url(url)
                                            .put(formBody)
                                            .build();

                                    client.newCall(req).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                            e.printStackTrace();
                                        }

                                        @Override
                                        public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                                            if(response.isSuccessful()){
                                                Log.i("MESSAGE", "update task Success");
                                                Executors.getInstance().diskIO().execute(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        // run on main thread
                                                        Executors.getInstance().mainThread().execute(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    JSONArray data = new JSONArray(response.body().string());
                                                                    int length = data.length();
                                                                    List<Task> Tasks = new ArrayList<Task>();
                                                                    for(int i = 0; i < length; i++){
                                                                        JSONObject obj = data.getJSONObject(i);
                                                                        String id = obj.getString("_id");
                                                                        String leader = obj.getString("leader");
                                                                        String member = obj.getString("member");
                                                                        String title = obj.getString("title");
                                                                        String description = obj.getString("description");
                                                                        String deadline = obj.getString("deadline");
                                                                        String status = obj.getString("status");
                                                                        Task t = new Task(id, leader, member, title, description, deadline, status);
                                                                        Tasks.add(t);
                                                                    }
                                                                    notifyCount = 0;
                                                                    for(Task t: Tasks){
                                                                        String status = t.getStatus();
                                                                        if(isUpcomingDeadline(t.getDeadline(), status)){
                                                                            notifyCount += 1;
                                                                        }else if(!status.equals("Active")){
                                                                            taskTitle.setTextColor(Color.parseColor("#000a12"));
                                                                        }
                                                                    }
                                                                    notifyView.setText("You have " + notifyCount + " upcoming deadlines");
                                                                    taskStatus.setBackground(AppCompatResources.getDrawable(getActivity(), R.mipmap.task_failed));
                                                                    if(notifyCount == 0){
                                                                        notifyView.setBackgroundColor(Color.parseColor("#008000"));
                                                                    }else if(notifyCount < 0){
                                                                        notifyCount = 0;
                                                                        notifyView.setBackgroundColor(Color.parseColor("#008000"));
                                                                    }
                                                                    Log.i("length", String.valueOf(Tasks.size()));
                                                                    for(Task t: Tasks){
                                                                        tasksLayout.addView(createView(t));
                                                                    }
                                                                } catch (IOException | JSONException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }catch (Exception e){
                                    Log.i("Exception", e.getMessage());
                                }
                            }
                        });
                    }
                });
                builder.create().show();
            }
        });
        return taskView;
    }

    public boolean isUpcomingDeadline(String deadline, String status){
        // get date today
        Date today = new Date();
        Date _deadline = null;
        try {
            _deadline = new SimpleDateFormat("MM/dd/yyyy").parse(deadline);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long differenceInTime  = _deadline.getTime() - today.getTime();

        // formula for calculating difference in days
        long differenceInDays
                = (differenceInTime
                / (1000 * 60 * 60 * 24))
                % 365 + 1;

        Log.i("diff", String.valueOf(differenceInDays));
        // display notification if deadline due date is in 3 days (notify 0-3)
        if(differenceInDays <= 3 && differenceInDays >= 0 && status.equals("Active")){
            return true;
        }else{
            return false;
        }
    }
}