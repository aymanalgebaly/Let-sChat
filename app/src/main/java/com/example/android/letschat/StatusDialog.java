package com.example.android.letschat;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusDialog extends AppCompatDialogFragment {
    private DatabaseReference database;
    private FirebaseUser firebaseUser;
    private EditText changeStatusText;
    private StatusDialogListener lisener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = firebaseUser.getUid();

        database = FirebaseDatabase.getInstance().getReference().child("users").child(current_uid);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View v = layoutInflater.inflate(R.layout.status_main,null);

        changeStatusText = v.findViewById(R.id.dialogstatus);
        builder.setView(v);
               builder.setTitle("Change Your Status");
               builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton("save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String statusView = changeStatusText.getText().toString();
                database.child("status").setValue(statusView);
                lisener.applyText(statusView);
            }
        });
        v.findViewById(R.id.dialogstatus);
        return builder.create();
    }

    // ----------- Stop it man ------------------------------------
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            lisener = (StatusDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement StatusDialogListener");
        }
    }

    public interface StatusDialogListener{
        void applyText(String statusText);
    }
}
