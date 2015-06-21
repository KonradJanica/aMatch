package com.konradjanica.amatch;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.andtinder.model.CardModel;
import com.andtinder.view.CardContainer;
import com.andtinder.view.SimpleCardStackAdapter;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.konradjanica.careercup.CareerCupAPI;
import com.konradjanica.careercup.questions.Question;

import org.jsoup.Connection;

import java.util.Iterator;
import java.util.LinkedList;

import at.markushi.ui.CircleButton;

public class MainActivity extends Activity {
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

    private boolean aMatchButtonState;

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

        aMatchButtonState = true;

        mCardContainer = (CardContainer) findViewById(R.id.layoutview);
        r = getResources();
        adapter = new SimpleCardStackAdapter(this);

        final String page = Integer.toString(pageRaw);
        new DownloadInitialQuestions().execute(page);

        // Set aMatch button release
        final CircleButton aMatchButton = ((CircleButton) findViewById(R.id.amatchtoggle));
        aMatchButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (aMatchButton.repeatCount < aMatchButton.ANIMATION_REPEATS) {
                        CardModel topCard = adapter.getCardModel(0);
                        topCard.toggleFavorite();
                        View topCardView = mCardContainer.getTopCardView();
                        ImageView favView = ((ImageView) topCardView.findViewById(R.id.fav));
                        if (topCard.isFavorite()) {
                            favView.setVisibility(View.VISIBLE);
                        } else {
                            favView.setVisibility(View.INVISIBLE);
                        }
                    }
                    Log.d("Released", "Button released");
                }

                // TODO Auto-generated method stub
                return false;
            }
        });
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
                --cardCount;
                Log.i("Swipeable Cards", "I like the card");
                if (questionsList.size() > maxCards * 2) {
                    addCard(questionsList.iterator());
                } else {
                    ++pageRaw;
                    final String page = Integer.toString(pageRaw);
                    new DownloadQuestions().execute(page);
                }
            }

            @Override
            public void onDislike() {
                --cardCount;
                Log.i("Swipeable Cards", "I dislike the card");
                if (questionsList.size() > maxCards * 2) {
                    addCard(questionsList.iterator());
                } else {
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
//        return true;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        Drawable gearup = getResources().getDrawable(R.drawable.gearup);
//        item.setIcon(gearup);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
//            Drawable gearDown = getResources().getDrawable(R.drawable.ic_launcher);
//            item.setIcon(gearDown);
            Intent myIntent = new Intent(this, SettingsActivity.class);
            startActivity(myIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
