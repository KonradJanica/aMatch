package com.konradjanica.careercup.unitTests.questions;

import com.konradjanica.careercup.questions.QuestionSearch;
import com.konradjanica.careercup.urlParser.QuestionUrlParser;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Konrad on 17/06/2015.
 */
public class QuestionSearchTest {

    @Test
    public void doesUrlParseQuestions() throws IOException {
        String[] filters = {"1"};
        QuestionUrlParser questionURL = new QuestionUrlParser(filters);
        QuestionSearch tester = new QuestionSearch(questionURL);
        tester.loadRecentQuestions();

        filters = new String[]{"2", "microsoft-interview-questions"};
        questionURL = new QuestionUrlParser(filters);
        tester = new QuestionSearch(questionURL);
        tester.loadRecentQuestions();

        filters = new String[]{"2", "facebook-interview-questions"};
        questionURL = new QuestionUrlParser(filters);
        tester = new QuestionSearch(questionURL);
        tester.loadRecentQuestions();

//        System.out.println("size = " + tester.loadRecentQuestions().length);
    }

}