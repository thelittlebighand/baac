package org.thelittlebighand.baac.fetch;

import android.util.Log;
import org.thelittlebighand.baac.model.SubjectInfo;

import java.util.ArrayList;
import java.util.List;

public class DataFetcher {

    public List<SubjectInfo> fetch(String url, String username, String password) {
        Log.d("FetchOperation", "Retrieving external data...");
        return new ArrayList<SubjectInfo>() {{
            add(new SubjectInfo("Anglický jazyk", "Well done...", 1.0));
        }};
    }
}
