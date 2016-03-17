package org.thelittlebighand.baac;

import org.apache.commons.jexl2.parser.ParseException;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.thelittlebighand.baac.model.Rules;
import org.thelittlebighand.baac.model.SubjectScore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 16)
public class RulesTest {

    private Rules rules;

    @Before
    public void before() throws JSONException, ParseException, IOException {
        rules = new Rules(getClass().getResourceAsStream("/default.mrf.json"));
    }

    @Test
    public void load() {
        assertNotNull(rules);
    }

    @Test
    public void defaultValue() {
        assertEquals("OK!", rules.getMessage("", null, 0.0));
    }

    @Test
    public void subject() {
        assertEquals("Whoa, math", rules.getMessage("Matematika", null, 2.0));
    }

    @Test
    public void avg() {
        assertEquals("Nice, math is important", rules.getMessage("Matematika", null, 1.1));
    }

    @Test
    public void min() {
        List<SubjectScore> scores = new ArrayList<SubjectScore>() {{
            add(new SubjectScore("", "4", 1.0, null, null));
        }};
        assertEquals("Anarchy!", rules.getMessage("", scores, 1.1));
    }

    @Test
    public void max() {
        List<SubjectScore> scores = new ArrayList<SubjectScore>() {{
            add(new SubjectScore("", "1", 1.0, null, null));
        }};
        assertEquals("Good boy!", rules.getMessage("", scores, 1.1));
    }

}
