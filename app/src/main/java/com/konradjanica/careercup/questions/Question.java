package com.konradjanica.careercup.questions;

/**
 * Created by Konrad on 16/06/2015.
 */
public class Question {
    public String questionText;
    public int questionTextLineCount;
    public String id;
    public String company;
    public String companyImgURL;
    public String[] tags;

    /**
     * Constructor with one parameter
     * @param questionText The actual question text
     */
    public Question(String questionText) {
        this.questionText = questionText;
    }

    /**
     * Constructor with all parameters
     * @param questionText The actual question text
     * @param id The url query ID
     * @param company The company the question is from
     * @param companyImgURL The URL of the company picture
     * @param tags Any tags the question has. E.g. C++
     */
    public Question(String questionText, String id,
                    String company, String companyImgURL,
                    String... tags) {
        this.questionText = questionText;
        this.id = id;
        this.company = company;
        this.companyImgURL = companyImgURL;
        this.tags = tags;
    }
}
