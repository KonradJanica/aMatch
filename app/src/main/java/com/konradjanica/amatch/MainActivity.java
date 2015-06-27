package com.konradjanica.amatch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class MainActivity extends Activity {
    private final int maxCards = 5;

    /**
     * This variable is the container that will host our cards
     */
    private CardContainer mCardContainer;

    private SimpleCardStackAdapter adapter;

    private CareerCupAPI careerCupAPI;
    private LinkedList<Question> questionsList;

    // Number of cards displayed
    private int cardCount;

    // From preferences/settings
    private int pageRaw;
    private String company;
    private String job;
    private String topic;

    private boolean aMatchButtonState;

    private boolean isQuestionsLoading;
    private SmoothProgressBar progressBar;
    private TextView noQuestionsText;

    private static int settingsChangedIntent = 1;

    private void init() {
        careerCupAPI = new CareerCupAPI();
        questionsList = new LinkedList<>();
        adapter = new SimpleCardStackAdapter(this);

        cardCount = 0;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String page = preferences.getString("page_number", "");
        company = preferences.getString("company_list", "");
        job = preferences.getString("job_list", "");
        topic = preferences.getString("topic_list", "");

        pageRaw = Integer.parseInt(page);

        new DownloadInitialQuestions().execute(page, company, job, topic);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);

        mCardContainer  = (CardContainer)     findViewById(R.id.layoutview);
        progressBar     = (SmoothProgressBar) findViewById(R.id.dl_progress);
        noQuestionsText = (TextView)          findViewById(R.id.no_questions_found);

        aMatchButtonState  = true;
        isQuestionsLoading = false;

        init();

        // Set aMatch button release
        final CircleButton aMatchButton = ((CircleButton) findViewById(R.id.amatchtoggle));
        aMatchButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (aMatchButton.repeatCount < aMatchButton.ANIMATION_REPEATS) {
                        if (mCardContainer.getTopCardView() == null) {
                                final String page = Integer.toString(pageRaw);
                                new DownloadRefillQuestions().execute(page, company, job, topic);
                            System.out.println("page = " + pageRaw + " questionsList size = " + questionsList.size());
                        } else {
                            CardModel topCard = adapter.getCardModel(0);
                            topCard.toggleFavorite();
                            View topCardView = mCardContainer.getTopCardView();
                            FrameLayout favView = ((FrameLayout) topCardView.findViewById(R.id.fav));
                            if (topCard.isFavorite()) {
                                favView.setVisibility(View.VISIBLE);
                            } else {
                                favView.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                    Log.d("Released", "Button released");
                }

                return false;
            }
        });
    }

    /**
     * Sets up the adapter for first use and populates it with maxCards amount of cards
     */
    private class DownloadInitialQuestions extends AsyncTask<String, Void, Void> {
        private Exception exception;

        protected void onPreExecute() {
            startProgressBar();
        }

        protected Void doInBackground(String... filters) {
            try {
                questionsList.addAll(careerCupAPI.loadRecentQuestions(filters));
            } catch (Exception e) {
                this.exception = e;
            }
            return null;
        }

        protected void onPostExecute(Void dummy) {
            if (exception != null) {
                errorProgressBar();
                return;
            }
            if (questionsList.size() == 0) {
                mCardContainer.setAdapter(adapter);
                stopProgressBar();
                return;
            }
            Iterator<Question> itr = questionsList.iterator();
            // Add cards to adapter container
            addCardInitial(itr);
            while (itr.hasNext() && cardCount < maxCards) {
                addCardInitial(itr);
            }

            stopProgressBar();
            mCardContainer.setAdapter(adapter);
        }
    }

    /**
     * Used to refill the cards upon pressing aMatch button on connection error
     */
    private class DownloadRefillQuestions extends AsyncTask<String, Void, Void> {
        private Exception exception;

        protected void onPreExecute() {
            startProgressBar();
        }

        protected Void doInBackground(String... filters) {
            try {
                questionsList.addAll(careerCupAPI.loadRecentQuestions(filters));
            } catch (Exception e) {
                this.exception = e;
            }
            return null;
        }

        protected void onPostExecute(Void dummy) {
            if (exception != null) {
                errorProgressBar();
                return;
            }
            ensureFull();
            stopProgressBar();
        }
    }

    /**
     * Normal download operation, next page is loaded into questionList
     */
    private class DownloadQuestions extends AsyncTask<String, Void, Void> {
        private Exception exception;

        protected void onPreExecute() {
            startProgressBar();
        }

        protected Void doInBackground(String... filters) {
            try {
                questionsList.addAll(careerCupAPI.loadRecentQuestions(filters));
            } catch (Exception e) {
                this.exception = e;
            }
            return null;
        }

        protected void onPostExecute(Void dummy) {
            if (exception != null) {
                errorProgressBar();
                return;
            }
            stopProgressBar();
        }
    }

    /**
     * Display download error msg
     */
    private void errorProgressBar() {
        isQuestionsLoading = false;
        progressBar.progressiveStop();
        noQuestionsText.setText("Download Error has occurred!\nPlease Try Again.");
    }

    /**
     * Stop download progress bar and display question filter complete msg
     */
    private void stopProgressBar() {
        isQuestionsLoading = false;
        progressBar.progressiveStop();
        noQuestionsText.setText("Sorry - no more questions!\nTry new filters");
    }

    /**
     * Start download progress bar and display loading msg
     */
    private void startProgressBar() {
        isQuestionsLoading = true;
        progressBar.progressiveStart();
        noQuestionsText.setText("Loading Questions...");
    }

    /**
     * Add a card without notifying adapter
     * This is the normal procedure for adding files after initial
     * @param itr an iterator to the questionsList
     */
    private void addCard(Iterator<Question> itr) {
        addCard(itr, false);
    }

    /**
     * Add a card while notifying adapter
     * This is the initial procedure for adding files
     * @param itr an iterator to the questionsList
     */
    private void addCardInitial(Iterator<Question> itr) {
        addCard(itr, true);
    }

    /**
     * Add card to adapter and add it's listener for adding more cards
     */
    private void addCard(Iterator<Question> itr, boolean isInitial) {
        Question q = itr.next();
        final CardModel cardModel = new CardModel(q.company, q.questionText, q.companyImgURL,
                q.pageNumber, q.dateText + q.location, q.questionTextLineCount);
        cardModel.setOnCardDimissedListener(new CardModel.OnCardDimissedListener() {
            @Override
            public void onLike() {
                Log.i("Swipeable Cards", "I like the card");
                --cardCount;
                ensureFull();
            }

            @Override
            public void onDislike() {
                Log.i("Swipeable Cards", "I dislike the card");
                --cardCount;
                ensureFull();
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

    /**
     * Downloads next page if there's less than (maxCards * 2 - 1) in the list
     * Also fills 2 cards into adapter up to maxCards amount
     */
    private void ensureFull() {
        if (questionsList.size() < maxCards * 2) {
            if (!isQuestionsLoading) {
                ++pageRaw;
                final String page = Integer.toString(pageRaw);
                new DownloadQuestions().execute(page, company, job, topic);
            }
        }
        if (questionsList.size() > 0) {
            // Add cards until full
            Iterator<Question> itr = questionsList.iterator();
            addCard(itr);
            if (itr.hasNext() && cardCount < maxCards) {
                addCard(itr);
            }
        }
        System.out.println("page = " + pageRaw + " questionsList size = " + questionsList.size());
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
            SettingsActivity.applyPressed = false;
            Intent myIntent = new Intent(this, SettingsActivity.class);
            startActivityForResult(myIntent, settingsChangedIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (SettingsActivity.applyPressed) {
            init();
        }
    }
}
