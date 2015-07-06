package com.konradjanica.careercup.unitTests.answers;

import com.konradjanica.careercup.answers.Answer;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Konrad on 16/06/2015.
 */
public class AnswerTest {
    /**
     * A Constructor Test for the Answer object
     * Tests direct member access.
     * @throws assertion exception when object members don't match
     */
    @Test
    public void fullCstorShouldHaveAllMembers() {
        // Test parameters
        String aText = "Answer Test";
        String upVotes = "Up Votes test";
        // Create Object for Testing
        Answer tester = new Answer(aText, upVotes);
        // Test parameters match members
        assertEquals("Member (answerText) failed in construction", aText, tester.answerText);
        assertEquals("Member (upVotes) failed in construction", upVotes, tester.upVotes);
    }

}