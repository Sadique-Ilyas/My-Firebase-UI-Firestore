package com.example.myfirebaseuifirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("Notebook");
    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton buttonAddNote = findViewById(R.id.button_add_note);
        buttonAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NewNoteActivity.class));
            }
        });
        setUpRecyclerView();
    }

    private void setUpRecyclerView()
    {
        Query query = notebookRef.orderBy("priority", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();
        adapter= new NoteAdapter(options);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter );

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
        {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Note note = documentSnapshot.toObject(Note.class);
                String id = documentSnapshot.getId();
                String title = note.getTitle();
                int prioirty = note.getPriority();
                String description = note.getDescription();
                updateNoteAlertDialog(title, description, prioirty, documentSnapshot);
                /*
                    We can update here using the id.
                    documentSnapshot.getReference.
                 */
                Toast.makeText(MainActivity.this, "Position: " +(position+1)+ "\nID: " +id, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNoteAlertDialog(String title, String description, int priority, final DocumentSnapshot documentSnapshot)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_alert_dialogue_box,null);
        builder.setView(dialogView);

        final EditText updateTitle = dialogView.findViewById(R.id.edit_text_updateTitle);
        final EditText updateDescription = dialogView.findViewById(R.id.edit_text_updateDescription);
        final NumberPicker updatePriority = dialogView.findViewById(R.id.updateNumber_picker_priority);

        updateTitle.setText(title);
        updateDescription.setText(description);
        updatePriority.setMinValue(1);
        updatePriority.setMaxValue(100);
        updatePriority.setValue(priority);

        builder.setTitle("Update " +title+ " Note");
        builder.setPositiveButton("Update Note", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = updateTitle.getText().toString().trim();
                String description = updateDescription.getText().toString().trim();
                int priority = updatePriority.getValue();

                updateNote(title, description, priority, documentSnapshot);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void updateNote(String title, String description, int priority, DocumentSnapshot documentSnapshot)
    {
        documentSnapshot.getReference().update("title", title);
        documentSnapshot.getReference().update("description", description);
        documentSnapshot.getReference().update("priority", priority);
        Toast.makeText(this, "Note Updated !!!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}