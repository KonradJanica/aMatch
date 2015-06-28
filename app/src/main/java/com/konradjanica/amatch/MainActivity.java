package com.konradjanica.amatch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.andtinder.model.CardModel;
import com.andtinder.view.CardContainer;
import com.andtinder.view.SimpleCardStackAdapter;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.konradjanica.Utils;
import com.konradjanica.careercup.CareerCupAPI;
import com.konradjanica.careercup.questions.Question;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import at.markushi.ui.CircleButton;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class MainActivity extends Activity {
    private final int maxCards = 5;

    private CardContainer mCardContainerMain;
    private CardContainer mCardContainerFavorites;

    private SimpleCardStackAdapter adapterMain;
    private SimpleCardStackAdapter adapterFavorites;

    private CareerCupAPI careerCupAPI;
    private LinkedList<Question> questionsList;
    private Queue<CardModel> questionsCardQueue;
    private LinkedList<CardModel> favouritesList;
    private Queue<CardModel> favoritesCardQueue;

    // Number of cards displayed
    private int cardCount;
    private int cardCountFavorite;

    // From preferences/settings
    private int pageRaw;
    private String company;
    private String job;
    private String topic;

    private boolean aMatchButtonState;

    private boolean isQuestionsLoading;
    private boolean isErrorLoading;
    private SmoothProgressBar progressBar;
    private TextView noQuestionsText;

    private boolean isFavoriteMode;

    // Favourites card file
    public final static String favoritesFile = "favourites.sav";

    private static int settingsChangedIntent = 1;

    private void init() {
        careerCupAPI = new CareerCupAPI();
        questionsList = new LinkedList<>();
        questionsCardQueue = new LinkedList<>();
        adapterMain = new SimpleCardStackAdapter(this);

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

        mCardContainerMain = (CardContainer)     findViewById(R.id.main_cards);
        mCardContainerFavorites = (CardContainer)     findViewById(R.id.favorite_cards);
        progressBar     = (SmoothProgressBar) findViewById(R.id.dl_progress);
        noQuestionsText = (TextView)          findViewById(R.id.no_questions_found);

        aMatchButtonState  = true;
        isQuestionsLoading = false;
        isErrorLoading = false;
        isFavoriteMode = false;

        favouritesList = Utils.readLinkedListFromFile(getApplicationContext(), favoritesFile);
        favoritesCardQueue = new LinkedList<>(favouritesList);
        adapterFavorites = new SimpleCardStackAdapter(this);
        cardCountFavorite = 0;
        ensureFavoritesFull(true);
        mCardContainerFavorites.setAdapter(adapterFavorites);

        init();

        // Set aMatch button release
        final CircleButton aMatchButton = ((CircleButton) findViewById(R.id.amatchtoggle));
        aMatchButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (aMatchButton.repeatCount < aMatchButton.ANIMATION_REPEATS) {
                        if (mCardContainerMain.getTopCardView() == null && !isFavoriteMode) {
                                final String page = Integer.toString(pageRaw);
                                new DownloadRefillQuestions().execute(page, company, job, topic);
                        } else {
                            CardModel topCard = questionsCardQueue.peek();
                            View topCardView = mCardContainerMain.getTopCardView();
                            if (isFavoriteMode) {
                                if (favouritesList.size() > 0) {
                                    topCard = favouritesList.get(0);
                                }
                                topCardView = mCardContainerFavorites.getTopCardView();
                            }
                            if (topCardView != null) {
                                FrameLayout favView = ((FrameLayout) topCardView.findViewById(R.id.fav));
                                if (!topCard.isFavorite()) {
                                    favView.setVisibility(View.VISIBLE);
                                    favouritesList.add(topCard);
                                    Utils.writeLinkedListToFile(getApplicationContext(), favouritesList, favoritesFile);
                                } else {
                                    favView.setVisibility(View.INVISIBLE);
                                    if (favouritesList.size() > 0) {
                                        Iterator<CardModel> itr = favouritesList.iterator();
                                        do {
                                            CardModel cm = itr.next();
                                            if (cm.getId().equals(topCard.getId())) {
                                                itr.remove();
                                                break;
                                            }
                                        } while (itr.hasNext());
                                    }
                                    Utils.writeLinkedListToFile(getApplicationContext(), favouritesList, favoritesFile);
                                }
                                topCard.toggleFavorite();
                                System.out.println(favouritesList.size());
                            }
                        }
                    } else {
                        favouritesList = Utils.readLinkedListFromFile(getApplicationContext(), favoritesFile);
                        favoritesCardQueue = new LinkedList<>(favouritesList);
                        adapterFavorites = new SimpleCardStackAdapter(getApplicationContext());
                        cardCountFavorite = 0;
                        ensureFavoritesFull(true);
                        mCardContainerFavorites.setAdapter(adapterFavorites);

                        if (!isFavoriteMode) {
                            isFavoriteMode = true;
                            findViewById(R.id.main_cards).setVisibility(View.GONE);
                            findViewById(R.id.favorite_cards).setVisibility(View.VISIBLE);
                        } else {
                            isFavoriteMode = false;
                            findViewById(R.id.main_cards).setVisibility(View.VISIBLE);
                            findViewById(R.id.favorite_cards).setVisibility(View.GONE);
                            if (adapterMain.getCount() > 0) {
                                int end = 4;
                                if (adapterMain.getCount() < 5) {
                                    end = adapterMain.getCount();
                                }
                                for (int x = 0; x < end; ++x) {
                                    if (favouritesList.size() > 0) {
                                        Iterator<CardModel> itr = favouritesList.iterator();
                                        CardModel adapterCM = adapterMain.getCardModel(x);
                                        adapterCM.setFavorite(false);
                                        do {
                                            CardModel cm = itr.next();
                                            if (cm.getId().equals(adapterCM.getId())) {
                                                adapterCM.setFavorite(true);
                                                break;
                                            }
                                        } while (itr.hasNext());
                                    }
                                }
                                if (end == 4) {
                                    for (int x = end; x < adapterMain.getCount()-1; ++x) {
                                        adapterMain.pop();
                                    }
                                }
                                adapterMain.notifyDataSetChanged();
                                mCardContainerMain.refreshTopCard();
                            }
                        }
                        System.out.println(favouritesList.size());
                        System.out.println("queue =" + favoritesCardQueue.size());
                        System.out.println("adapter =" + adapterMain.getCount());
                    }
//                    Log.d("Released", "Button released");
                }

                return false;
            }
        });
    }

    /**
     * Sets up the adapterMain for first use and populates it with maxCards amount of cards
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
                mCardContainerMain.setAdapter(adapterMain);
                stopProgressBar();
                return;
            }
            Iterator<Question> itr = questionsList.iterator();
            // Add cards to adapterMain container
            addCardInitial(itr);
            while (itr.hasNext() && cardCount < maxCards) {
                addCardInitial(itr);
            }

            stopProgressBar();
            mCardContainerMain.setAdapter(adapterMain);
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
            mCardContainerMain.refreshTopCard();
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
        isErrorLoading = true;
        progressBar.progressiveStop();
        noQuestionsText.setText("Download Error has occurred!\nPlease Try Again.");
    }

    /**
     * Stop download progress bar and display question filter complete msg
     */
    private void stopProgressBar() {
        isQuestionsLoading = false;
        isErrorLoading = false;
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
     * Add a card without notifying adapterMain
     * This is the normal procedure for adding files after initial
     * @param itr an iterator to the questionsList
     */
    private void addCard(Iterator<Question> itr) {
        addCard(itr, false);
    }

    /**
     * Add a card while notifying adapterMain
     * This is the initial procedure for adding files
     * @param itr an iterator to the questionsList
     */
    private void addCardInitial(Iterator<Question> itr) {
        addCard(itr, true);
    }

    /**
     * Add card to adapterMain and add it's listener for adding more cards
     */
    private void addCard(Iterator<Question> itr, boolean isInitial) {
        Question q = itr.next();
        CardModel cardModel = new CardModel(q.company, q.questionText, q.companyImgURL,
                q.pageNumber, q.dateText + q.location,
                q.id, q.questionTextLineCount);
        if (favouritesList.size() > 0) {
            Iterator<CardModel> iter = favouritesList.iterator();
            do {
                CardModel cm = iter.next();
                if (cm.getId().equals(cardModel.getId())) {
                    cardModel.setFavorite(true);
                    break;
                }
            } while (iter.hasNext());
        }
        cardModel.setOnCardDimissedListener(new CardModel.OnCardDimissedListener() {
            @Override
            public void onLike() {
//                Log.i("Swipeable Cards", "I like the card");
                mainCardRemoval();
                ensureFull();
            }

            @Override
            public void onDislike() {
//                Log.i("Swipeable Cards", "I dislike the card");
                mainCardRemoval();
                ensureFull();
            }
        });
        cardModel.setOnClickListener(new CardModel.OnClickListener() {
            @Override
            public void OnClickListener() {
//                Log.i("Swipeable Cards", "I am pressing the card");
            }
        });
        if (isInitial) {
            adapterMain.addInitial(cardModel);
        } else {
            adapterMain.add(cardModel);
        }
        ++cardCount;
        itr.remove();
        questionsCardQueue.add(cardModel);
    }

    private void mainCardRemoval() {
        --cardCount;
        questionsCardQueue.remove();
    }

    /**
     * Add card to adapterMain and add it's listener for adding more cards
     */
    private void addCardFavorites(CardModel cardModel, boolean isInitial) {
        cardModel.setOnCardDimissedListener(new CardModel.OnCardDimissedListener() {
            @Override
            public void onLike() {
//                Log.i("Swipeable Cards", "I like the card");
                --cardCountFavorite;
                ensureFavoritesFull(false);
            }

            @Override
            public void onDislike() {
//                Log.i("Swipeable Cards", "I dislike the card");
                --cardCountFavorite;
                ensureFavoritesFull(false);
            }
        });
        cardModel.setOnClickListener(new CardModel.OnClickListener() {
            @Override
            public void OnClickListener() {
//                Log.i("Swipeable Cards", "I am pressing the card");
            }
        });
        if (isInitial) {
            adapterFavorites.addInitial(cardModel);
        } else {
            adapterFavorites.add(cardModel);
        }
        ++cardCountFavorite;
    }
    /**
     * Downloads next page if there's less than (maxCards * 2 - 1) in the list
     * Also fills 2 cards into adapterMain up to maxCards amount
     */
    private void ensureFull() {
        if (questionsList.size() < maxCards * 2) {
            if (!isQuestionsLoading) {
                if (!isErrorLoading) {
                    ++pageRaw;
                }
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

    /**
     * Fills 2 cards into adapterFavourites up to maxCards amount
     */
    private void ensureFavoritesFull(boolean isInitial) {
        if (favoritesCardQueue.size() > 0 && cardCountFavorite < maxCards) {
            // Add cards until full
            addCardFavorites(favoritesCardQueue.poll(), isInitial);
            if (favoritesCardQueue.size() > 0 && cardCountFavorite < maxCards) {
                addCardFavorites(favoritesCardQueue.poll(), isInitial);
            }
        }
//        System.out.println("page = " + pageRaw + " questionsList size = " + questionsList.size());
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
