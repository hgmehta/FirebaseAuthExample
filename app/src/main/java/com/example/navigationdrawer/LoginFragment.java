package com.example.navigationdrawer;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginFragment extends Fragment implements View.OnClickListener {

    private FirebaseAuth auth;
    FirebaseUser user;
    EditText edit_email, edit_password;
    Button btn_login;
    TextView text_register;



    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edit_email = view.findViewById(R.id.edit_login_email);
        edit_password = view.findViewById(R.id.edit_login_password);
        btn_login = view.findViewById(R.id.btn_login);
        text_register = view.findViewById(R.id.text_register);

        btn_login.setOnClickListener(this);
        text_register.setOnClickListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.text_register){
            NavController navController = Navigation.findNavController(getActivity(), R.id.hostFragment);

            navController.navigate(R.id.registerFragment);

        }else if(id == R.id.btn_login){
            if(TextUtils.isEmpty(edit_email.getText().toString())){
                edit_email.setError("Email is required!");
                edit_email.requestFocus();
            }else if(TextUtils.isEmpty(edit_password.getText().toString())){
                edit_password.setError("Password is required!");
                edit_password.requestFocus();
            }else{
                String email = edit_email.getText().toString();
                String password = edit_password.getText().toString();
                loginUser(email, password);
            }

        }

    }

    @Override
    public void onStart() {
        super.onStart();

        user = auth.getCurrentUser();
        if(user != null){
            Toast.makeText(getActivity().getApplicationContext(), "Already LoggedIn!", Toast.LENGTH_LONG).show();
            updateUI(user);
        }
    }

    public void loginUser(String email, String password){

        auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    user = auth.getCurrentUser();
                    Toast.makeText(getActivity().getApplicationContext(), "Login Successful!", Toast.LENGTH_LONG).show();
                    updateUI(user);

                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "Login Failed!", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUI(FirebaseUser user){
        NavController navController = Navigation.findNavController(getActivity(), R.id.hostFragment);

        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);

        navController.navigate(R.id.dashboardFragment, bundle);
    }
}
