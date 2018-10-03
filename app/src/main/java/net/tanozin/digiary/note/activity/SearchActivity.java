package net.tanozin.digiary.note.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import net.tanozin.digiary.R;
import net.tanozin.digiary.note.NoteItem;
import net.tanozin.digiary.note.fragment.SearchFragment;

public class SearchActivity extends AppCompatActivity implements SearchFragment.OnListFragmentInteractionListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_start);

    }


    @Override
    public void onListFragmentInteraction(NoteItem item) {

    }


}
