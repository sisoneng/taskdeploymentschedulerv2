package com.example.taskdeploymentscheduler.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.example.taskdeploymentscheduler.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class welcomeRoleFragment extends Fragment {

    String username;
    String role = "";
    String[] roles = new String[]{
            "I'm a...",
            "Group Leader",
            "Group Member"
    };

    TextView welcomeUsernameView;
    Spinner roleSpinnerView;
    Button nextButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate layout (convert xml to view objects)
        View fragmentWelcomeRoleLayout =  inflater.inflate(R.layout.fragment_welcome_role, container, false);

        // get argument from first activity
        username = welcomeRoleFragmentArgs.fromBundle(getArguments()).getUsername();
        welcomeUsernameView = fragmentWelcomeRoleLayout.findViewById(R.id.welcome_role_name);
        roleSpinnerView = fragmentWelcomeRoleLayout.findViewById(R.id.role_spinner);
        nextButton = fragmentWelcomeRoleLayout.findViewById(R.id.role_next_button);
        nextButton.setVisibility(View.GONE);
        welcomeUsernameView.setText(username);

        final List<String> rolesList = new ArrayList<>(Arrays.asList(roles));

        // setup adapter
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_role, rolesList){
            @Override
            public boolean isEnabled(int position){
                /* disable first item */
                return position != 0;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView item = (TextView) view;
                if(position == 0){
                    item.setVisibility(View.GONE);
                }
                else {
                    item.setTextColor(getResources().getColor(R.color.secondaryDarkColor));
                }
                return view;
            }
        };

        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_role);

        // add adapter to the dialog spinner
        roleSpinnerView.setAdapter(spinnerArrayAdapter);

        roleSpinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 1){
                    role = "Leader";
                    nextButton.setVisibility(View.VISIBLE);
                }else if(position == 2){
                    role = "Member";
                    nextButton.setVisibility(View.VISIBLE);
                }else{
                    role = "";
                    nextButton.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                /* required to ilagay kasi interface yung method pag tinanggal mageerror */
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(role.equals("Leader")){
                    // navigate to leader homepage and pass username as argument
                    NavDirections action = welcomeRoleFragmentDirections.actionWelcomeRoleFragmentToLeaderHomeFragment(username);
                    NavHostFragment.findNavController(welcomeRoleFragment.this).navigate(action);
                }else if(role.equals("Member")){
                    // navigate to member homepage and pass username as argument
                    Log.i("username", username);
                    NavDirections action = welcomeRoleFragmentDirections.actionWelcomeRoleFragmentToMemberHomeFragment(username);
                    NavHostFragment.findNavController(welcomeRoleFragment.this).navigate(action);
                }else{
                    makeText(getActivity(), "Let us know your role", LENGTH_SHORT).show();
                }
            }
        });
        return fragmentWelcomeRoleLayout;
    }
}