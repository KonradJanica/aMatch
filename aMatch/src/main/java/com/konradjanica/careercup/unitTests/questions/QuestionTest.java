package com.konradjanica.careercup.unitTests.questions;

import com.konradjanica.careercup.questions.Question;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Konrad on 16/06/2015.
 */
public class QuestionTest {
    /**
     * A Constructor Test for the Question object
     * Tests direct member access.
     * @throws java.io.IOException When object members don't match
     */
    @Test
    public void fullCstorShouldHaveAllMembers() {
        // Test parameters
        String qText = "Question Test";
        String id = "ID test";
        String company = "Company test";
        String companyImgURL = "www.companyimgurl.com test";
        String[] tags = {"Tag1 test", "Tag2 test", "Tag3 test"};
        // Create Object for Testing
        Question tester = new Question(qText, id, company, companyImgURL, tags);
        // Test parameters match members
        assertEquals("Member (questionText) failed in construction", qText, tester.questionText);
        assertEquals("Member (id) failed in construction", id, tester.id);
        assertEquals("Member (company) failed in construction", company, tester.company);
        for (int x = 0; x < tags.length; ++x) {
            assertEquals("Member ([]tag) failed in construction", tags[x], tester.tags[x]);
        }
    }

}