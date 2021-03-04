package com.example.pettrackerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase sqLiteDatabase;
    PetDatabaseHelper petDatabaseHelper;
    RecyclerView recyclerView;
    PetAdapter petAdapter;
    ArrayList<PetEntry> petEntryArrayList = new ArrayList<PetEntry>();
    int petNumber = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //PetEntry pet1 = new PetEntry("hello", "cat");
        //PetEntry pet2 = new PetEntry("whatup", "Dog");
        recyclerView = findViewById(R.id.recyclerView);
        petAdapter = new PetAdapter();
        recyclerView.setAdapter(petAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        petDatabaseHelper = new PetDatabaseHelper(getApplicationContext());
        SQLiteDatabase sqLiteDatabase = petDatabaseHelper.getReadableDatabase();
        //petDatabaseHelper.insertData(pet1);
        //petDatabaseHelper.insertData(pet2);

        Intent intent = getIntent();
        if(intent.getStringExtra("name")!=null){
            PetEntry petEntry = new PetEntry(intent.getStringExtra("name"), intent.getStringExtra("type"));
            /*ContentValues newValues = new ContentValues();
            newValues.put("name", petEntry.getName());
            newValues.put("type", petEntry.getType());
            petDatabaseHelper = new PetDatabaseHelper(getApplicationContext());
            sqLiteDatabase = petDatabaseHelper.getWritableDatabase();
            sqLiteDatabase.insert("pets", null, newValues);
            //petEntryArrayList.add(petEntry);
            //petAdapter.notifyDataSetChanged();*/
            petDatabaseHelper.insertData(petEntry);
        }

        setup();
    }

    public void setup(){
        SQLiteDatabase sqLiteDatabase = petDatabaseHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query("pets", new String[] {"_id", "name", "type", "drawable", "homeLat", "homeLong", "petLat", "petLong"}
        , null, null, null,null,null);

        if(cursor.moveToLast()){
            int id = cursor.getInt(0);
            petNumber = id + 1;
        }

        if(cursor.moveToFirst()){
            PetEntry petEntry = createPet(cursor);
            petEntryArrayList.add(petEntry);

        }

        boolean keepgoing = true;

        while(keepgoing == true){
            if(cursor.moveToNext()){
                PetEntry petEntry = createPet(cursor);
                petEntryArrayList.add(petEntry);
            }
            else{
                keepgoing = false;
            }
        }
    }

    public PetEntry createPet(Cursor cursor){
        String name = cursor.getString(1);
        String type = cursor.getString(2);
        PetEntry petEntry = new PetEntry(name, type);
        return petEntry;
    }

    public class PetAdapter extends RecyclerView.Adapter{

        class PetViewHolder extends RecyclerView.ViewHolder{
            TextView nameTextView;
            TextView ageTextView;

            public PetViewHolder(@NonNull View itemView) {
                super(itemView);
                this.nameTextView = itemView.findViewById(R.id.nameTextView);
                this.ageTextView = itemView.findViewById(R.id.ageTextView);
            }
        }


        View.OnClickListener myOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildLayoutPosition(v);
                PetEntry petEntry = petEntryArrayList.get(position);

                Intent intent = new Intent(getApplicationContext(), DetailViewActivity.class);
                intent.putExtra("_id", position+1);
                intent.putExtra("name", petEntry.name);
                intent.putExtra("type", petEntry.type);
                startActivity(intent);

            }
        };

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_layout, parent, false);
            view.setOnClickListener(myOnClickListener);
            return new PetViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            PetViewHolder petViewHolder = (PetViewHolder) holder;
            PetEntry petEntry = petEntryArrayList.get(position);

            petViewHolder.ageTextView.setText(petEntry.type);
            petViewHolder.nameTextView.setText(petEntry.name);
        }

        @Override
        public int getItemCount() {
            return petEntryArrayList.size();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_layout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.menuAddPet:
                showAddDialog();
                return true;
            case R.id.menuHome:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddDialog(){
        FragmentManager fm = getSupportFragmentManager();
        AddPetDialogFragment addPetDialogFragment = AddPetDialogFragment.newInstance("Add New Pet");
        addPetDialogFragment.show(fm, "fragment_add_pet");
    }
}