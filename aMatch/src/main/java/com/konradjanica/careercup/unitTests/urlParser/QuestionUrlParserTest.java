package com.konradjanica.careercup.unitTests.urlParser;

import com.konradjanica.careercup.urlParser.QuestionUrlParser;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Konrad on 16/06/2015.
 */
public class QuestionUrlParserTest {

    @Test
    public void parseUrlShouldMakeFullUsableUrl(){
        // TEST MOST USED OPTION
        // Test parameters
        String[] filters = {"1", "microsoft-interview-questions", "c-plus-plus-interview-questions"};
        // Create Object for Testing
        QuestionUrlParser tester = new QuestionUrlParser(filters);
        // Check ParseUrl output
        String urlFromSite = "http://www.careercup.com/page?n=1&pid=microsoft-interview-questions&topic=c-plus-plus-interview-questions";
        assertEquals("ParseUrl output invalid", urlFromSite, tester.ParseUrl());
        // TEST ALL PARAM
        // Test parameters
        String[] filtersAllParams = {"1", "microsoft-interview-questions", "c-plus-plus-interview-questions", "software-engineer-interview-questions"};
        // Create Object for Testing
        QuestionUrlParser testerAllParams = new QuestionUrlParser(filtersAllParams);
        // Check ParseUrl output
        urlFromSite = "http://www.careercup.com/page?n=1&pid=microsoft-interview-questions&job=software-engineer-interview-questions&topic=c-plus-plus-interview-questions";
        assertEquals("ParseUrl output invalid", urlFromSite, testerAllParams.ParseUrl());
        // TEST 2 PARAM
        // Test parameters
        String[] filters2Params = {"1", "microsoft-interview-questions"};
        // Create Object for Testing
        QuestionUrlParser tester2Params = new QuestionUrlParser(filters2Params);
        // Check ParseUrl output
        urlFromSite = "http://www.careercup.com/page?n=1&pid=microsoft-interview-questions";
        assertEquals("ParseUrl output invalid", urlFromSite, tester2Params.ParseUrl());
        // TEST 1 PARAM
        // Test parameters
        String[] filters1Params = {"1"};
        // Create Object for Testing
        QuestionUrlParser tester1Params = new QuestionUrlParser(filters1Params);
        // Check ParseUrl output
        urlFromSite = "http://www.careercup.com/page?n=1";
        assertEquals("ParseUrl output invalid", urlFromSite, tester1Params.ParseUrl());
        // TEST NO PARAMS
        // Check ParseUrl output
        urlFromSite = "http://www.careercup.com/page";
        // Create Object for Testing
        QuestionUrlParser testerNoParam = new QuestionUrlParser();
        assertEquals("ParseUrl output invalid", urlFromSite, testerNoParam.ParseUrl());
    }
}