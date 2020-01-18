package com.example.navigationdrawer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardFragment extends Fragment {

    Button btn_signOut, btn_delete;
    TextView text_name;
    FirebaseUser user;
    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        user = getArguments().getParcelable("user");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btn_signOut = view.findViewById(R.id.btn_signOut);
        text_name = view.findViewById(R.id.text_username);
        btn_delete = view.findViewById(R.id.btn_delete);

        FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
        fireStore.collection("users").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String name = documentSnapshot.get("name").toString();
                text_name.setText(name);
            }
        });

        btn_signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.signOut();
                NavController controller = Navigation.findNavController(getActivity(), R.id.hostFragment);
                controller.navigate(R.id.loginFragment);
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser(view);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public void deleteUser(final View view){

        final View authenticationView = getActivity().getLayoutInflater().inflate(R.layout.auth_layout, null);

        final PopupWindow popupWindow = new PopupWindow(authenticationView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        if(Build.VERSION.SDK_INT >= 21){
            popupWindow.setElevation(5.0f);
        }

        final EditText auth_email = authenticationView.findViewById(R.id.edit_auth_email);
        final EditText auth_password = authenticationView.findViewById(R.id.edit_auth_password);
        Button btn_auth = authenticationView.findViewById(R.id.btn_auth);

        btn_auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = auth_email.getText().toString();
                String password = auth_password.getText().toString();

                Toast.makeText(getActivity().getApplicationContext(), email, Toast.LENGTH_LONG).show();
                if(auth_password.getText().toString().length()   < 6){
                    auth_password.setError("Invalid Password, Password should be at least 6 character long");
                    auth_password.requestFocus();
                }else{

                    if(TextUtils.isEmpty(email)){
                        auth_email.setError("Email is required!");
                        auth_email.requestFocus();
                    }else if(TextUtils.isEmpty(password)){
                        auth_password.setError("Password is required!");
                        auth_password.requestFocus();
                    }else{

                        AuthCredential credential = EmailAuthProvider.getCredential(email,password);
                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful()){
                                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                                    firestore.collection("users").document(user.getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful()){
                                                            NavOptions navOptions = new NavOptions.Builder()
                                                                    .setPopUpTo(R.id.dashboardFragment, false).build();
                                                            NavController navController = Navigation.findNavController(getActivity(), R.id.hostFragment);
                                                            navController.navigate(R.id.loginFragment);
                                                            popupWindow.dismiss();
                                                        }else{
                                                            System.out.println("Delete user failed: " + task.getException().getMessage());
                                                        }
                                                    }
                                                });
                                            }
                                            else{
                                                System.out.println("Delete failed: " + task.getException().getMessage());
                                            }
                                        }
                                    });

                                }else{
                                    System.out.println("Delete failed: " + task.getException().getMessage());
                                }
                            }
                        });
                    }
                }
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.showAtLocation(getView(), Gravity.CENTER, 0,0);

    }
}
