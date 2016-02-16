package org.thelittlebighand.baac;

import android.app.Activity;
import android.os.Bundle;
import org.bordylek.baac.R;

public class DetailActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        String subjectName = getIntent().getStringExtra("SubjectName");
        setTitle(subjectName);
    }
}