package com.example.taskdeploymentscheduler.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.example.taskdeploymentscheduler.R;
import com.example.taskdeploymentscheduler.roomdb.Executors;
import com.example.taskdeploymentscheduler.roommodel.Task;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class leaderHomeFragment extends Fragment {

    TextView loginMessageView;
    Button addButton;
    String username;
    LinearLayout tasksLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View lhfLayout = inflater.inflate(R.layout.fragment_leader_home, container, false);
        username = leaderHomeFragmentArgs.fromBundle(getArguments()).getUsername();
        loginMessageView = lhfLayout.findViewById(R.id.login_message);
        tasksLayout = lhfLayout.findViewById(R.id.leader_tasks);
        addButton = lhfLayout.findViewById(R.id.add_button);
        String text = String.format(getString(R.string.top_message), username);
        loginMessageView.setText(text);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavDirections action = leaderHomeFragmentDirections.actionLeaderHomeFragmentToAddtaskFragment(username);
                NavHostFragment.findNavController(leaderHomeFragment.this).navigate(action);
            }
        });

        OkHttpClient client = new OkHttpClient();
        // ibahin yung 0.0.0.0 ng ip address ng wifi nyo
        HttpUrl url = HttpUrl.parse("http://" + getString(R.string.ip_address) + "/leader/" + username);
        MediaType mediaType  = MediaType.parse("application/json; charset=utf-8");
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
                    Log.i("MESSAGE", "Success");
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
        return lhfLayout;
    }



    public View createView(final Task t){
        Log.i("Task title", t.getTitle());
        final View taskView = getLayoutInflater().inflate(R.layout.task_row_leader, null, false);
        final TextView taskTitle = taskView.findViewById(R.id.row_title_leader);
        final TextView taskStatus = taskView.findViewById(R.id.row_status_leader);
        final TextView taskDescription = taskView.findViewById(R.id.row_description_leader);
        final TextView assignedTo = taskView.findViewById(R.id.row_member_leader);
        final TextView deadline = taskView.findViewById(R.id.row_deadline_leader);

        taskTitle.setText(t.getTitle());
        if(t.getStatus().equals("Active")){
            taskStatus.setBackground(AppCompatResources.getDrawable(getActivity(), R.mipmap.task_ip));
        }else if(t.getStatus().equals("Completed")){
            taskStatus.setBackground(AppCompatResources.getDrawable(getActivity(), R.mipmap.task_complete));
        }else if(t.getStatus().equals("Failed")){
            taskStatus.setBackground(AppCompatResources.getDrawable(getActivity(), R.mipmap.task_failed));
        }
        taskDescription.setText(t.getAssignment());
        assignedTo.setText("For: " + t.getMember());
        deadline.setText("Deadline: " + t.getDeadline());

        taskTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Confirmation");
                builder.setMessage("Are you sure you want to delete?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        OkHttpClient client = new OkHttpClient();
                        // ibahin yung 0.0.0.0 ng ip address ng wifi nyo
                        HttpUrl url = HttpUrl.parse("http://" + getString(R.string.ip_address) + "/delete/" + t.getId());
                        Request req = new Request.Builder()
                                .url(url)
                                .delete()
                                .build();

                        client.newCall(req).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                                if(response.isSuccessful()){
                                    Executors.getInstance().mainThread().execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Toast.makeText(getActivity(), response.body().string(), Toast.LENGTH_SHORT).show();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            taskView.setVisibility(View.GONE);
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
                        /* Do nothing */
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        return taskView;
    }
}