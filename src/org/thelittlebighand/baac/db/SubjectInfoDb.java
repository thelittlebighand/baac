package org.thelittlebighand.baac.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.thelittlebighand.baac.model.SubjectInfo;
import org.thelittlebighand.baac.model.SubjectScore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SubjectInfoDb extends SQLiteOpenHelper {

    public SubjectInfoDb(Context context) {
        super(context, "baac", null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        Log.i("SubjectInfoDb", "Creating db...");
        try {
            for (String sql : readResource("/create.sql")) {
                Log.d("SubjectInfoDb", sql);
                db.execSQL(sql);
            }
        } catch (IOException ex) {
            Log.e(SubjectInfoDb.class.getSimpleName(), "error creating database", ex);
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("SubjectInfoDb", "Upgrading db...");
        try {
            for (int i = oldVersion; i <= newVersion; i++) {
                for (String sql : readResource("/upgrade-" + i + ".sql")) {
                    Log.d("SubjectInfoDb", sql);
                    db.execSQL(sql);
                }
            }
        } catch (IOException ex) {
            Log.e(SubjectInfoDb.class.getSimpleName(), "error upgrading database", ex);
        }
    }

    public List<SubjectInfo> loadSubjectInfo() {
        List<SubjectInfo> data = new ArrayList();
        Cursor cursor = getReadableDatabase().rawQuery("select * from subject", null);
        try {
            cursor.moveToFirst();
            int idIdx = cursor.getColumnIndex("id");
            int nameIdx = cursor.getColumnIndex("name");
            int messageIdx = cursor.getColumnIndex("message");
            int averageIdx = cursor.getColumnIndex("average");
            do {
                data.add(new SubjectInfo(
                    cursor.getString(nameIdx),
                    loadSubjectScores(cursor.getLong(idIdx)),
                    cursor.getString(messageIdx),
                    cursor.getDouble(averageIdx))
                );
            } while (cursor.moveToNext());

            return data;

        } finally {
            cursor.close();
        }
    }

    public List<SubjectInfo> saveSubjectInfo(List<SubjectInfo> data) {
        for (SubjectInfo info : data) {
            long id = 0L;
            ContentValues values = findSubject(info.getSubject());
            if (values != null) {
                Log.d("SubjectInfoDb", "Updating " + info.getSubject() + ": " + info.getAvg() + ", " + info.getMessage());
                id = values.getAsLong("id");
                values.put("message", info.getMessage());
                values.put("average", info.getAvg());
                getWritableDatabase().update("subject", values, "id = ?", new String[] {values.getAsString("id")});
            } else {
                Log.d("SubjectInfoDb", "Inserting " + info.getSubject() + ": " + info.getAvg() + ", " + info.getMessage());
                values = new ContentValues();
                values.put("name", info.getSubject());
                values.put("message", info.getMessage());
                values.put("average", info.getAvg());
                id = getWritableDatabase().insert("subject", null, values);
            }

            saveSubjectScores(id, info.getScores());
        }

        return data;
    }

    private List<SubjectScore> loadSubjectScores(long subjectId) {
        List<SubjectScore> data = new ArrayList();
        Cursor cursor = getReadableDatabase().rawQuery("select * from score where subjectId = ?",
            new String[] {String.valueOf(subjectId)});
        try {
            int nameIdx = cursor.getColumnIndex("name");
            int scoreIdx = cursor.getColumnIndex("score");
            int weightIdx = cursor.getColumnIndex("weight");
            int dateIdx = cursor.getColumnIndex("created");
            int noteIdx = cursor.getColumnIndex("note");
            if (cursor.moveToFirst()) do {
                data.add(new SubjectScore(
                    cursor.getString(nameIdx),
                    cursor.getString(scoreIdx),
                    cursor.getDouble(weightIdx),
                    new Date(cursor.getLong(dateIdx)),
                    cursor.getString(noteIdx))
                );
            } while (cursor.moveToNext());

            return data;

        } finally {
            cursor.close();
        }
    }

    private void saveSubjectScores(long subjectId, List<SubjectScore> scores) {
        getWritableDatabase().execSQL("delete from score where subjectId = " + subjectId);
        for (SubjectScore score : scores) {
            Log.d("SubjectInfoDb", "Saving score " + score.getName() + ": " + score.getScore() + ", " + score.getWeight());
            ContentValues values = new ContentValues();
            values.put("subjectId", subjectId);
            values.put("name", score.getName());
            values.put("score", score.getScore());
            values.put("weight", score.getWeight());
            values.put("created", score.getDate().getTime());
            values.put("note", score.getNote());
            getWritableDatabase().insert("score", null, values);
        }
    }

    private ContentValues findSubject(String name) {
        Cursor cursor = getReadableDatabase().rawQuery("select * from subject where name = ?", new String[] {name});
        try {
            if (cursor.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put("id", cursor.getString(cursor.getColumnIndex("id")));
                values.put("name", cursor.getString(cursor.getColumnIndex("name")));
                values.put("message", cursor.getString(cursor.getColumnIndex("message")));
                values.put("average", cursor.getDouble(cursor.getColumnIndex("average")));
                return values;
            }

            return null;

        } finally {
            cursor.close();
        }
    }

    private List<String> readResource(String url) throws IOException {
        List<String> lines = new ArrayList<String>();
        InputStream stream = getClass().getResourceAsStream(url);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.length() > 0) lines.add(line);
            }
        } finally {
            reader.close();
        }

        return lines;
    }

}
