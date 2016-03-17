package org.thelittlebighand.baac.fetch;

import android.util.Log;
import org.apache.commons.io.IOUtils;
import org.apache.commons.jexl2.parser.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.thelittlebighand.baac.model.BakaInfo;
import org.thelittlebighand.baac.model.Rules;
import org.thelittlebighand.baac.model.SubjectInfo;
import org.thelittlebighand.baac.model.SubjectScore;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataFetcher {

    private DefaultHttpClient client;
    private Rules rules;

    public static final String LOGIN = "http://skolar.duong.cz/api/login";
    public static final String ZNAMKY = "http://skolar.duong.cz/api/znamky";

    public DataFetcher() throws ParserConfigurationException, JSONException, ParseException, IOException {
        this.client = new DefaultHttpClient();
    }

    public void setRule(String ruleFile) throws IOException, JSONException, ParseException {
        this.rules = loadRules(ruleFile);
    }

    public BakaInfo fetch(String url, String username, String password) throws JSONException, IOException {
        Log.d("FetchOperation", "Authenticating user " + username);
        final JSONObject auth = new JSONObject();
        auth.put("url", url);
        auth.put("user", username);
        auth.put("pass", password);
        JSONObject response = readData(LOGIN, auth);
        if ("ok".equals(response.get("status"))) {
            String name = response.getJSONObject("data").getJSONObject("login").getString("name");
            return new BakaInfo(name, new ArrayList<SubjectInfo>() {{
                JSONObject data = readData(ZNAMKY, auth);
                JSONArray predmety = data.getJSONObject("data").getJSONArray("predmety");
                JSONArray znamky = data.getJSONObject("data").getJSONArray("znamky");
                for (int i = 0; i < predmety.length(); i++) {
                    String subject = predmety.getString(i);
                    List<SubjectScore> scores = readScores(znamky.getJSONArray(i));
                    SubjectInfo subjectInfo = new SubjectInfo(subject, scores);
                    subjectInfo.setMessage(createMessage(subject, scores, subjectInfo.getAvg()));
                    add(subjectInfo);
                }
            }});
        }

        throw new IllegalStateException("error reading data, status " + response.get("status"));
    }

    private Rules loadRules(String file) throws IOException, JSONException, ParseException {
        if (file.startsWith("classpath://")) {
            return new Rules(getClass().getResourceAsStream(file.substring(11)));
        } else if (file.startsWith("file://")) {
            return new Rules(new FileInputStream(new File(file.substring(6))));
        } else throw new IllegalArgumentException("cannot read file " + file);
    }

    private JSONObject readData(String endpoint, JSONObject body) throws JSONException, IOException {
        HttpPost post = new HttpPost(endpoint);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(body.toString()));
        Log.d("FetchOperation", "Request: " + endpoint);
        String content = IOUtils.toString(this.client.execute(post).getEntity().getContent());
        Log.d("FetchOperation", "Response: " + content);
        return new JSONObject(content);
    }

    private List<SubjectScore> readScores(final JSONArray znamky) throws JSONException {
        return new ArrayList<SubjectScore>() {{
            for (int i = 0; i < znamky.length(); i++) {
                JSONObject json = znamky.getJSONObject(i);
                add(new SubjectScore(
                    json.getString("caption"),
                    json.getString("mark"),
                    createWeight(json.getString("weight")),
                    new Date(json.getLong("date")),
                    json.getString("note")
                ));
            }
        }};
    }

    private Double createWeight(String code) {
        return Double.parseDouble(code.trim()) / 10;
    }

    private String createMessage(String subject, List<SubjectScore> scrores, Double avg) {
        return rules.getMessage(subject, scrores, avg);
    }

}
