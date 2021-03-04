package com.example.pettrackerapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AddPetDialogFragment extends DialogFragment implements View.OnClickListener{

    private EditText editTextName;
    private EditText editTextType;
    private Button buttonAdd;
    private Button buttonCancel;

    public AddPetDialogFragment(){

    }

    public static AddPetDialogFragment newInstance(String title){
        AddPetDialogFragment fragment = new AddPetDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_dialog, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextName = view.findViewById(R.id.editPetName);
        editTextType = view.findViewById(R.id.editTextType);
        buttonAdd = view.findViewById(R.id.buttonAdd);
        buttonCancel = view.findViewById(R.id.buttonCancel);
        String title = getArguments().getString("title", "Enter name");
        getDialog().setTitle(title);

        buttonAdd.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);

        editTextName.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.buttonAdd:
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra("name", editTextName.getText().toString());
                intent.putExtra("type", editTextType.getText().toString());
                startActivity(intent);
                break;
            case R.id.buttonCancel:
                dismiss();
                break;
        }
    }

}