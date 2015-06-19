package com.konradjanica.amatch;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.andtinder.model.CardModel;
import com.andtinder.view.CardContainer;
import com.andtinder.view.SimpleCardStackAdapter;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.konradjanica.careercup.CareerCupAPI;
import com.konradjanica.careercup.questions.Question;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

public class MainActivity extends ActionBarActivity {
    private final int maxCards = 5;

    /**
     * This variable is the container that will host our cards
     */
    private CardContainer mCardContainer;

    private Resources r;
    private SimpleCardStackAdapter adapter;

    private CareerCupAPI careerCupAPI;
    private LinkedList<Question> questionsList;
    private int cardCount;
    private int pageRaw;
    private boolean isFirstRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);

        String company = "microsoft-interview-questions";
        String id = "software-engineer-intern-interview-questions";
        String topic = "algorithm-interview-questions";

        careerCupAPI = new CareerCupAPI();
        questionsList = new LinkedList<>();
        cardCount = 0;
        pageRaw = 1;
        isFirstRun = true;

        mCardContainer = (CardContainer) findViewById(R.id.layoutview);
        r = getResources();
        adapter = new SimpleCardStackAdapter(this);

        final String page = Integer.toString(pageRaw);
        new DownloadInitialQuestions().execute(page);
    }

    private class DownloadInitialQuestions extends AsyncTask<String, Void, Void> {
        private Exception exception;

        protected Void doInBackground(String... filters) {
            try {
                questionsList.addAll(careerCupAPI.loadRecentQuestions(filters));
            } catch (Exception e) {
                this.exception = e;
            }
            return null;
        }

        protected void onPostExecute(Void dummy) {
            if (questionsList.size() == 0) {
                mCardContainer.setAdapter(adapter);
                return;
            }
            Iterator<Question> itr = questionsList.iterator();
            // Add cards to adapter container
            while (itr.hasNext() && cardCount < maxCards) {
                addCardInitial(itr);
            }
//            CardModel cardModel = new CardModel("Title1", "Description goes here", r.getDrawable(R.drawable.picture1));

            mCardContainer.setAdapter(adapter);
        }
    }

    private class DownloadQuestions extends AsyncTask<String, Void, Void> {
        private Exception exception;

        protected Void doInBackground(String... filters) {
            try {
                questionsList.addAll(careerCupAPI.loadRecentQuestions(filters));
            } catch (Exception e) {
                this.exception = e;
            }
            return null;
        }

        protected void onPostExecute(Void dummy) {
            if (questionsList.size() == 0) {
                Log.i("CareerCupAPI", "No more questions found");
                return;
            }
            Iterator<Question> itr = questionsList.iterator();
            // Add cards to adapter container
            while (itr.hasNext() && cardCount < maxCards) {
                addCard(itr);
            }
        }
    }

    private void addCard(Iterator<Question> itr) {
        addCard(itr, false);
    }

    private void addCardInitial(Iterator<Question> itr) {
        addCard(itr, true);
    }

    private void addCard(Iterator<Question> itr, boolean isInitial) {
        Question q = itr.next();
        final CardModel cardModel = new CardModel(q.company, q.questionText, q.companyImgURL, q.questionTextLineCount);
        cardModel.setOnCardDimissedListener(new CardModel.OnCardDimissedListener() {
            @Override
            public void onLike() {
                Log.i("Swipeable Cards", "I like the card");
                if (questionsList.size() > maxCards) {
                    addCard(questionsList.iterator());
                } else {
                    --cardCount;
                    ++pageRaw;
                    final String page = Integer.toString(pageRaw);
                    new DownloadQuestions().execute(page);
                }
            }

            @Override
            public void onDislike() {
                Log.i("Swipeable Cards", "I dislike the card");
                if (questionsList.size() > maxCards) {
                    addCard(questionsList.iterator());
                } else {
                    --cardCount;
                    ++pageRaw;
                    final String page = Integer.toString(pageRaw);
                    new DownloadQuestions().execute(page);
                }
            }
        });
        cardModel.setOnClickListener(new CardModel.OnClickListener() {
            @Override
            public void OnClickListener() {
                Log.i("Swipeable Cards", "I am pressing the card");
            }
        });
        if (isInitial) {
            adapter.addInitial(cardModel);
        } else {
            adapter.add(cardModel);
        }
        ++cardCount;
        itr.remove();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
