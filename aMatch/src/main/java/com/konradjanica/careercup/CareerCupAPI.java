package com.konradjanica.careercup;

import com.konradjanica.careercup.questions.Question;
import com.konradjanica.careercup.questions.QuestionSearch;
import com.konradjanica.careercup.urlParser.QuestionUrlParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by Konrad on 17/06/2015.
 */
public class CareerCupAPI {
    private QuestionUrlParser questionURL;
    private QuestionSearch questionSearch;

    public LinkedList<Question> loadRecentQuestions(String page, String company, String job, String topic) throws IOException {
        String[] filters = new String[]{page, company, topic, job};
        questionURL = new QuestionUrlParser(filters);
        questionSearch = new QuestionSearch(questionURL);
        Question[] questionList = questionSearch.loadRecentQuestions();
        return new LinkedList(Arrays.asList(questionList));
    }

    public LinkedList<Question> loadRecentQuestions(String... filters) throws IOException {
        questionURL = new QuestionUrlParser(filters);
        questionSearch = new QuestionSearch(questionURL);
        Question[] questionList = questionSearch.loadRecentQuestions();
        return new LinkedList(Arrays.asList(questionList));
    }
}
