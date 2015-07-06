package com.konradjanica.careercup.urlParser;

/**
 * Created by Konrad on 16/06/2015.
 */
public class QuestionUrlParser extends UrlParser{
    /**
     * The question search page filters to apply (to the url)
     *   filters[0] = page
     *   filters[1] = company
     *   filters[2] = topic
     *   filters[3] = job
     * @warn remaining indexes are undefined
      */
    private String[] filters;

    /**
     * Construct with the required page url filters (to find questions)
     * @param filters The question search filters
     */
    public QuestionUrlParser(String... filters) {
        this.filters = filters;
    }

    /**
     * Returns a string representing the Url to the questions search with required filters
     * @return An url to the questions search page including filters
     */
    public String ParseUrl() {
        String questionsUrl = super.url;
        // Initialize empty strings
        String page;
        String company = "";
        String job = "";
        String topic = "";
        // Make Url components as necessary
        switch(filters.length) {
            default:
                System.out.println("Too many params in QuestionUrlParser cstor... Ignoring remainder");
            case 4:
                job = "&job=" + filters[3];
            case 3:
                topic = "&topic=" + filters[2];
            case 2:
                company = "&pid=" + filters[1];
            case 1:
                page = "/page?n=" + filters[0];
                break;
            case 0:
                questionsUrl += "/page";
                return questionsUrl;
        }
        // Combine url components
        return questionsUrl + page + company + job + topic;
    }

    /**
     * Accesses the parsed questions page number
     * @return A string representing the page number containing the questions
     */
    public String getParsedPageNumber() {
        return filters[0];
    }
}
