package com.example.taskdeploymentscheduler.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.example.taskdeploymentscheduler.R;
import com.google.android.material.textfield.TextInputEditText;

public class welcomeFragment extends Fragment {

    String username;
    TextInputEditText usernameView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentWelcomeLayout =  inflater.inflate(R.layout.fragment_welcome, container, false);

        usernameView = fragmentWelcomeLayout.findViewById(R.id.username);

        Log.i("test", "test");
        usernameView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN){
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        username = usernameView.getText().toString();
                        // username cannot be empty
                        if (username.isEmpty()) {
                            Toast.makeText(getActivity(), "Let us know who you are", Toast.LENGTH_SHORT).show();
                        } else if (username.length() > 15) {
                            // username should have less than 15 character
                            Toast.makeText(getActivity(), "Username is too long", Toast.LENGTH_SHORT).show();
                        } else {
                            NavDirections action = welcomeFragmentDirections.actionWelcomeNameFragmentToWelcomeRoleFragment(username);
                            NavHostFragment.findNavController(welcomeFragment.this).navigate(action);
                        }
                        return true;
                    }
                }
                return false;
            }

        });
        return fragmentWelcomeLayout;
    }
}