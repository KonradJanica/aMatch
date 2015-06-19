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
    /**
     * This variable is the container that will host our cards
     */
    private CardContainer mCardContainer;

    private Resources r;
    private SimpleCardStackAdapter adapter;

    private LinkedList<Question> questionsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);

        String page = "1";
        String company = "microsoft-interview-questions";
        String id = "software-engineer-intern-interview-questions";
        String topic = "algorithm-interview-questions";

        mCardContainer = (CardContainer) findViewById(R.id.layoutview);
        r = getResources();
        adapter = new SimpleCardStackAdapter(this);

        new DownloadQuestions().execute(page);

    }

    private class DownloadQuestions extends AsyncTask<String , Void, Void> {

        private Exception exception;

        protected Void doInBackground(String... filters) {
            try {
                CareerCupAPI cc = new CareerCupAPI();
//                LinkedList<Question> questionsList;
                questionsList = cc.loadRecentQuestions(filters);
            } catch (Exception e) {
                this.exception = e;
            }
            return null;
        }

        protected void onPostExecute(Void dummy) {
            String imgUrl = "http://1-ps.googleusercontent.com/xk/JJeiMfWcqZ0jQjTeTmLh_Jvy8i/s.careercup-hrd.appspot.com/www.careercup.com/attributeimages/xmicrosoft-interview-questions.png.pagespeed.ic.7T_HnafFtwFLzs6HLjzN.png";
            // TODO: check this.exception
            // TODO: do something with the feed
            Iterator<Question> itr = questionsList.descendingIterator();
            while (itr.hasNext()) {
                Question q = itr.next();
                CardModel cardModel = new CardModel(q.company, q.questionText, q.companyImgURL, q.questionTextLineCount);
                cardModel.setOnCardDimissedListener(new CardModel.OnCardDimissedListener() {
                    @Override
                    public void onLike() {
                        Log.i("Swipeable Cards", "I like the card");
                    }

                    @Override
                    public void onDislike() {
                        Log.i("Swipeable Cards", "I dislike the card");
                    }
                });
                cardModel.setOnClickListener(new CardModel.OnClickListener() {
                    @Override
                    public void OnClickListener() {
                        Log.i("Swipeable Cards", "I am pressing the card");
                    }
                });
                adapter.add(cardModel);
            }
//            CardModel cardModel = new CardModel("Title1", "Description goes here", r.getDrawable(R.drawable.picture1));

            mCardContainer.setAdapter(adapter);
        }
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
