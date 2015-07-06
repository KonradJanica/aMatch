package com.konradjanica.careercup.questions;

import com.konradjanica.careercup.urlParser.QuestionUrlParser;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Konrad on 16/06/2015.
 */
public class QuestionSearch {
    private static final String userAgent = "Mozilla/5.0 (jsoup)";
    private static final int timeout = 5 * 1000;

    private QuestionUrlParser urlParser;

    public QuestionSearch(QuestionUrlParser urlParser) {
        this.urlParser = urlParser;
    }

    public Question[] loadRecentQuestions() throws IOException {
        List<Question> questionsList = new ArrayList<Question>();
        String url = urlParser.ParseUrl();
        // fetch the specified URL and parse to a HTML DOM
        Document doc = Jsoup.connect(url).userAgent(userAgent).timeout(timeout).get();

        // POPULATE QUESTION TEXT
        String selector = "span[class=entry] > a";
        Elements elements = doc.select(selector); // get each element that matches the CSS selector
        for (Element element : elements) {
            String plainText = getPlainText(element);
            int lastNewLineIndex = plainText.lastIndexOf('\n');
            String questionText;
            if (lastNewLineIndex == -1) {
                questionText = "";
            } else {
                questionText = plainText.substring(0, plainText.lastIndexOf('\n'));
            }
            Question nextQuestion = new Question(questionText, urlParser.getParsedPageNumber());
            String[] lineCounter = questionText.split("\n");
            int lineCount = lineCounter.length + 1;
            nextQuestion.questionTextLineCount = lineCount;
            questionsList.add(nextQuestion);
//            System.out.println(plainText);
//            System.out.println(questionText);
//            System.out.println(lineCount);
//            System.out.println(questionsList.size());
        }
        // POPULATE ID
        selector = "span[class=entry] a[href~=/question\\?id]";
        elements = doc.select(selector); // get each element that matches the CSS selector
        int index = 0;
        for (Element element : elements) {
            String plainText = element.attr("href");
//            int idIndex = plainText.indexOf("=");
//            String id = plainText.substring(idIndex+1);
            Question nextQuestion = questionsList.get(index);
            nextQuestion.id = plainText;
            ++index;
//            System.out.println(plainText);
        }
        // POPULATE COMPANY
        selector = "span[class=company] img";
        elements = doc.select(selector); // get each element that matches the CSS selector
        index = 0;
        for (Element element : elements) {
            String companyTitle = element.attr("title");
            Question nextQuestion = questionsList.get(index);
            nextQuestion.company = companyTitle;
            ++index;
//            System.out.println(companyTitle);
        }
        // POPULATE COMPANY URL
        selector = "span[class=company] img";
        elements = doc.select(selector); // get each element that matches the CSS selector
        index = 0;
        for (Element element : elements) {
            String companyImgURL = element.attr("src");
            Question nextQuestion = questionsList.get(index);
            nextQuestion.companyImgURL = companyImgURL;
            ++index;
//            System.out.println(companyImgURL);
//            System.out.println(index + "size = " + questionsList.size());
        }
        // POPULATE DATES AND LOCATIONS
        selector = "abbr[class=timeago]";
        elements = doc.select(selector); // get each element that matches the CSS selector
        index = 0;
        for (Element element : elements) {
            Question nextQuestion = questionsList.get(index);
            nextQuestion.dateText = element.text();
            nextQuestion.location = element.nextSibling().toString();
            ++index;
        }
        // POPULATE TAGS
        selector = "span[class=tags]";
        elements = doc.select(selector); // get each element that matches the CSS selector
        index = 0;
        for (Element element : elements) {
            List<String> tagsList = new ArrayList<String>();
            for (Node child : element.childNodes()) {
                String tagsRaw = child.toString();
                int parseStart = tagsRaw.indexOf(">");
                int parseEnd = tagsRaw.lastIndexOf("<");
                if (parseEnd != -1) {
                    String tags = tagsRaw.substring(parseStart + 1, parseEnd);
//                    System.out.println(tags);
                    tagsList.add(tags);
                }
            }
            String[] tags = tagsList.toArray(new String[tagsList.size()]);
            Question nextQuestion = questionsList.get(index);
            nextQuestion.tags = tags;
            ++index;
        }

        return questionsList.toArray(new Question[questionsList.size()]);
    }

    /**
     * Format an Element to plain-text
     *
     * @param element the root element to format
     * @return formatted text
     */
    public String getPlainText(Element element) {
        FormattingVisitor formatter = new FormattingVisitor();
        NodeTraversor traversor = new NodeTraversor(formatter);
        traversor.traverse(element); // walk the DOM, and call .head() and .tail() for each node

        return formatter.toString();
    }

    // the formatting rules, implemented in a breadth-first DOM traverse
    private class FormattingVisitor implements NodeVisitor {
        private static final int maxWidth = 80;
        private int width = 0;
        private StringBuilder accum = new StringBuilder(); // holds the accumulated text

        // hit when the node is first seen
        public void head(Node node, int depth) {
            String name = node.nodeName();
            if (node instanceof TextNode)
                append(((TextNode) node).text()); // TextNodes carry all user-readable text in the DOM.
            else if (name.equals("li"))
                append("\n * ");
            else if (name.equals("dt"))
                append("  ");
            else if (StringUtil.in(name, "h1", "h2", "h3", "h4", "h5", "tr"))
//            else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5", "tr"))
                append("\n");
        }

        // hit when all of the node's children (if any) have been visited
        public void tail(Node node, int depth) {
            String name = node.nodeName();
            if (StringUtil.in(name, "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5")) {
                append("\n");
            } else if (name.equals("a"))
                append(String.format(" <%s>", node.absUrl("href")));
        }

        // appends text to the string builder with a simple word wrap method
        private void append(String text) {
            if (text.startsWith("\n"))
                width = 0; // reset counter if starts with a newline. only from formats above, not in natural text
            if (text.equals(" ") &&
                    (accum.length() == 0 || StringUtil.in(accum.substring(accum.length() - 1), " ", "\n")))
                return; // don't accumulate long runs of empty spaces

            if (text.length() + width > maxWidth) { // won't fit, needs to wrap
                String words[] = text.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    boolean last = i == words.length - 1;
                    if (!last) // insert a space if not the last word
                        word = word + " ";
                    if (word.length() + width > maxWidth) { // wrap and reset counter
                        accum.append("\n").append(word);
                        width = word.length();
                    } else {
                        accum.append(word);
                        width += word.length();
                    }
                }
            } else { // fits as is, without need to wrap text
                accum.append(text);
                width += text.length();
            }
        }

        @Override
        public String toString() {
            return accum.toString();
        }
    }
}
