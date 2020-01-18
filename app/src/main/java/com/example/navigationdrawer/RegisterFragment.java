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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.core.FirestoreClient;

import org.w3c.dom.Document;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class RegisterFragment extends Fragment implements  View.OnClickListener {

    FirebaseAuth auth;
    FirebaseUser user;

    EditText edit_email, edit_password, edit_confirmPassword, edit_name;
    Button btn_register;

    public RegisterFragment() {

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edit_email = view.findViewById(R.id.edit_email);
        edit_password = view.findViewById(R.id.edit_password);
        edit_confirmPassword = view.findViewById(R.id.edit_confirmpassword);
        btn_register = view.findViewById(R.id.btn_register);
        edit_name = view.findViewById(R.id.edit_name);

        btn_register.setOnClickListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onClick(View v) {

        if(!checkEmptyFields()){
            if(edit_password.getText().toString().length() < 6){
                edit_password.setError("Password length must be greater than 6!");
                edit_password.requestFocus();
            } else if(!edit_password.getText().toString().equals(edit_confirmPassword.getText().toString())){
                edit_confirmPassword.setError("Password doesn't match!");
                edit_confirmPassword.requestFocus();
            }else{
                String email = edit_email.getText().toString();
                String password = edit_password.getText().toString();
                String name = edit_name.getText().toString();

                createUser(email, password, name);
            }
        }
    }

    private boolean checkEmptyFields() {
        if (TextUtils.isEmpty(edit_name.getText().toString())){
            edit_name.setError("Name is required!");
            edit_name.requestFocus();
        }else if(TextUtils.isEmpty(edit_email.getText().toString())){
            edit_email.setError("Email is required!");
            edit_email.requestFocus();
            return true;
        }else if(TextUtils.isEmpty(edit_password.getText().toString())){
            edit_password.setError("Password is required!");
            edit_password.requestFocus();
            return true;
        }else if(TextUtils.isEmpty(edit_confirmPassword.getText().toString())){
            edit_confirmPassword.setError("Confirm Password is required!");
            edit_confirmPassword.requestFocus();
            return true;
        }
        return false;
    }

    private void createUser(String email, String password, String name){

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    FirebaseUser user = auth.getCurrentUser();
                    if(user != null){
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                        Map<String, Object> fields = new HashMap<>();

                        fields.put("name", edit_name.getText().toString());
                        fields.put("email", edit_email.getText().toString());

                        firestore.collection("users").document(user.getUid()).set(fields).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(getActivity().getApplicationContext(), "Register Successful!", Toast.LENGTH_LONG).show();
                                auth.signOut();
                                NavController navController = Navigation.findNavController(getActivity(), R.id.hostFragment);
                                navController.navigate(R.id.loginFragment);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity().getApplicationContext(), "Unable to register user!", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
