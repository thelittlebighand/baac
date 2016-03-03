package org.thelittlebighand.baac;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.bordylek.baac.R;
import org.thelittlebighand.baac.db.SubjectInfoDb;
import org.thelittlebighand.baac.fetch.DataFetcher;
import org.thelittlebighand.baac.model.BakaInfo;
import org.thelittlebighand.baac.model.RuleFile;
import org.thelittlebighand.baac.model.SubjectInfo;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {

    private SubjectInfoAdapter adapter;
    private AsyncTask mrfFinder;
    private AsyncTask fetcher;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        adapter = new SubjectInfoAdapter(this, R.layout.subject);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("SubjectName", adapter.getItem(position).getSubject());
            startActivity(intent);
            }
        });
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String user = prefs.getString("user", null);
        String url = prefs.getString("url", null);
        String username = prefs.getString("username", null);
        String password = prefs.getString("password", null);
        if (isEmpty(url) || isEmpty(username) || isEmpty(password)) {
            new AlertDialog.Builder(MainActivity.this)
                .setTitle(getString(R.string.first_run_title))
                .setMessage(getString(R.string.first_run_message))
                .setNeutralButton(getString(R.string.first_run_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    }
                }
            ).show();
        } else if (!isEmpty(user)) {
            setTitle(user);
        }

        mrfFinder = new FileFinder().execute(Environment.getExternalStorageDirectory());
    }

    protected void onResume() {
        super.onResume();
        adapter.loadData();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (mrfFinder.getStatus() != AsyncTask.Status.FINISHED) {
            this.mrfFinder.cancel(true);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        new MenuInflater(this).inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu (Menu menu) {
        return fetcher == null || fetcher.getStatus() == AsyncTask.Status.FINISHED;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.update:
                this.update();
                break;
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }

        return true;
    }

    private void update() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String url = prefs.getString("url", null);
        String username = prefs.getString("username", null);
        String password = prefs.getString("password", null);
        String rule = prefs.getString("rule", null);
        this.fetcher = new FetchOperation().execute(url, username, password, rule);
    }

    private boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    private class FetchOperation extends AsyncTask<String, Void, BakaInfo> {

        protected BakaInfo doInBackground(String... strings) {
            try {
                DataFetcher fetcher = new DataFetcher();
                fetcher.setRule((strings[3] != null) ? strings[3] : "classpath://default.mrf.json");
                return fetcher.fetch(strings[0], strings[1], strings[2]);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(BakaInfo data) {
            adapter.saveData(data.getSubjects());
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("user", data.getName());
            editor.commit();
            MainActivity.this.setTitle(data.getName());
        }
    }

    private class FileFinder extends AsyncTask<File, Void, List<RuleFile>> {

        protected List<RuleFile> doInBackground(File... roots) {
            List<RuleFile> files = new ArrayList<RuleFile>();
            for (File root : roots) iterate(root, files);
            return files;
        }

        protected void onPostExecute(List<RuleFile> data) {
            Log.d("FileFinder", "Found files: " + data);
        }

        private void iterate(File root, List<RuleFile> files) {
            Log.d("FileFinder", "Scanning " + root);
            FileFilter filter = new MRFFilter();
            File[] found = root.listFiles(filter);
            for (File file : found) try {
                if (file.isDirectory()) iterate(file, files);
                else files.add(new RuleFile(file.getName(), file.getAbsolutePath()));
            } catch (Exception ex) {
                Log.e("FileFinder", "Error reading file " + file, ex);
            }
        }
    }

    private static class MRFFilter implements FileFilter {
        public boolean accept(File file) {
            String name = file.getName();
            return (file.isFile() && name.endsWith(".mrf.json")) ||
                (file.isDirectory() && !name.startsWith(".") && !name.equals("Android"));
        }
    }

    private static class SubjectInfoAdapter extends ArrayAdapter<SubjectInfo> {

        private int resId;
        private Context context;
        private List<SubjectInfo> data;
        private SubjectInfoDb db;

        public SubjectInfoAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.resId = textViewResourceId;
            this.data = new ArrayList();
            this.context = context;
            this.db = new SubjectInfoDb(context);
        }

        public int getCount() {
            return data.size();
        }

        public SubjectInfo getItem(int position) {
            return data.get(position);
        }

        public void loadData() {
            Log.d("SubjectInfoAdapter", "Loading data...");
            this.data = db.loadSubjectInfo();
            notifyDataSetChanged();
        }

        public void saveData(List<SubjectInfo> data) {
            Log.d("SubjectInfoAdapter", "Saving data: " + data);
            this.data = this.db.saveSubjectInfo(data);
            notifyDataSetChanged();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = LayoutInflater.from(context).inflate(resId, parent, false);
            SubjectInfo subjectInfo = data.get(position);
            ((TextView)v.findViewById(R.id.subject)).setText(subjectInfo.getSubject());
            ((TextView)v.findViewById(R.id.message)).setText(subjectInfo.getMessage());
            ((TextView)v.findViewById(R.id.avg)).setText(String.format("%.1f", subjectInfo.getAvg()));
            return v;
        }
    }

}
