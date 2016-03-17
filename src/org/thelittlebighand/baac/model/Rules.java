package org.thelittlebighand.baac.model;

import android.util.Log;
import org.apache.commons.io.IOUtils;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.jexl2.parser.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Rules {

    private List<Rule> rules;
    private Random random;

    public Rules(InputStream stream) throws IOException, JSONException, ParseException {
        JSONObject json = new JSONObject(IOUtils.toString(stream));
        JSONArray ruleDefs = json.getJSONArray("rules");
        this.rules = new ArrayList<>(ruleDefs.length());
        this.random = new Random();
        JexlEngine engine = new JexlEngine();
        engine.setStrict(true);
        engine.setLenient(true);
        for (int i = 0; i < ruleDefs.length(); i++) try {
            JSONObject ruleDef = ruleDefs.getJSONObject(i);
            String when = ruleDef.getString("when");
            Integer weight = (ruleDef.has("weight")) ? ruleDef.getInt("weight") : 0;
            Object then = ruleDef.get("then");
            this.rules.add(new Rule(engine.createExpression(when), weight, readMessages(then)));
        } catch (Exception ex) {
            Log.e("Rules", "Error reading rule def: " + ruleDefs.getJSONObject(i), ex);
        }
    }

    public String getMessage(String subject, List<SubjectScore> scores, Double avg) {
        double maxWeight = 0;
        List<String> messages = new ArrayList<>();
        JexlContext context = new MapContext();
        context.set("subject", subject);
        context.set("avg", avg);
        context.set("min", findMinScore(scores));
        context.set("max", findMaxScore(scores));
        for (Rule rule : rules) {
            if (rule.getWeight() > maxWeight) {
                if (((Boolean)rule.getExpression().evaluate(context))) {
                    messages = new ArrayList<>(rule.getMessages());
                    maxWeight = rule.getWeight();
                }
            } else if (rule.getWeight() == maxWeight) {
                if (((Boolean) rule.getExpression().evaluate(context))) {
                    messages.addAll(rule.getMessages());
                }
            }
        }

        return (messages.size() > 0) ? messages.get(random.nextInt(messages.size())) : "OK!";
    }

    private Double findMaxScore(List<SubjectScore> scores) {
        Double max = 0.0;
        if (scores != null) for (SubjectScore score : scores) {
            if (max == 0.0 || score.getScoreValue() > max) {
                max = score.getScoreValue();
            }
        }

        return max;
    }

    private Double findMinScore(List<SubjectScore> scores) {
        Double min = 0.0;
        if (scores != null) for (SubjectScore score : scores) {
            if (min == 0.0 || score.getScoreValue() < min) {
                min = score.getScoreValue();
            }
        }

        return min;
    }

    private List<String> readMessages(final Object messages) throws JSONException {
        if (messages instanceof JSONArray) {
            return new ArrayList<String>() {{
                JSONArray array = (JSONArray) messages;
                for (int i = 0; i < array.length(); i++) {
                    add(array.get(i).toString());
                }
            }};
        }

        return Collections.singletonList(messages.toString());
    }

    private static class Rule {

        private Expression expression;
        private Integer weight;
        private List<String> messages;

        public Rule(Expression expression, Integer weight, List<String> messages) {
            this.expression = expression;
            this.weight = weight;
            this.messages = messages;
        }

        public Expression getExpression() {
            return expression;
        }

        public Integer getWeight() {
            return weight;
        }

        public List<String> getMessages() {
            return messages;
        }
    }

}
