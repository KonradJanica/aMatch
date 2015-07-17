package com.konradjanica.amatch;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
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
import java.util.Locale;
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
    private int cardCountMain;
    private int cardCountFavorite;
    private int indexFavorite;

    // From preferences/settings
    public static int maxFlingSensitivity;
    private int pageRaw;
    private String company;
    private String job;
    private String topic;

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

        cardCountMain = 0;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        maxFlingSensitivity = Integer.parseInt(preferences.getString("fling_speed", "3"));

        String page = preferences.getString("page_number", "1");
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

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        mCardContainerMain = (CardContainer) findViewById(R.id.main_cards);
        mCardContainerFavorites = (CardContainer) findViewById(R.id.favorite_cards);
        progressBar = (SmoothProgressBar) findViewById(R.id.dl_progress);
        noQuestionsText = (TextView) findViewById(R.id.no_questions_found);

        isQuestionsLoading = false;
        isErrorLoading = false;
        isFavoriteMode = false;

        favouritesList = Utils.readLinkedListFromFile(getApplicationContext(), favoritesFile);
        favoritesCardQueue = new LinkedList<>(favouritesList);
        adapterFavorites = new SimpleCardStackAdapter(this);
        cardCountFavorite = 0;
        indexFavorite = 0;
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
                            // Reload and parse url when main list is empty
                            final String page = Integer.toString(pageRaw);
                            new DownloadRefillQuestions().execute(page, company, job, topic);
                        } else if (mCardContainerFavorites.getTopCardView() == null && isFavoriteMode) {
                            // Refresh favourites list upon button press
                            favouritesList = Utils.readLinkedListFromFile(getApplicationContext(), favoritesFile);
                            adapterFavorites = new SimpleCardStackAdapter(getApplicationContext());
                            cardCountFavorite = 0;
                            indexFavorite = 0;
                            favoritesCardQueue = new LinkedList<>(favouritesList);
                            ensureFavoritesFull(true);
                            mCardContainerFavorites.setAdapter(adapterFavorites);
                        } else {
                            // Toggle favourite card
                            CardModel topCard = questionsCardQueue.peek();
                            View topCardView = mCardContainerMain.getTopCardView();
                            if (isFavoriteMode) {
                                if (adapterFavorites.getCount() > indexFavorite) {
                                    topCard = adapterFavorites.getCardModel(indexFavorite);
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
                        // Toggle favourite mode
                        favouritesList = Utils.readLinkedListFromFile(getApplicationContext(), favoritesFile);

                        if (!isFavoriteMode) {
                            // Change to favourite mode
                            isFavoriteMode = true;

                            adapterFavorites = new SimpleCardStackAdapter(getApplicationContext());
                            cardCountFavorite = 0;
                            indexFavorite = 0;
                            favoritesCardQueue = new LinkedList<>(favouritesList);
                            ensureFavoritesFull(true);
                            mCardContainerFavorites.setAdapter(adapterFavorites);

                            // Change background color
//                            findViewById(R.id.main_background).setBackgroundColor(getResources().getColor(R.color.amatch));

                            // Change actionbar color
                            ActionBar bar = getActionBar();
                            bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionbar_bg_fav)));
                            if (Locale.getDefault().getCountry().equals("US")) {
                                bar.setTitle("Favorites");
                            } else {
                                bar.setTitle("Favourites");
                            }

                            // Toggle views
                            findViewById(R.id.main_cards).setVisibility(View.GONE);
                            findViewById(R.id.favorite_cards).setVisibility(View.VISIBLE);
                        } else {
                            // Change to main mode
                            isFavoriteMode = false;

                            adapterMain = new SimpleCardStackAdapter(getApplicationContext());
                            ensureMainFull();
                            mCardContainerMain.setAdapter(adapterMain);

                            // Change actionbar
                            ActionBar bar = getActionBar();
                            bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionbar_bg)));
                            bar.setTitle("aMatch");

                            // Toggle views
                            findViewById(R.id.main_cards).setVisibility(View.VISIBLE);
                            findViewById(R.id.favorite_cards).setVisibility(View.GONE);
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
            while (itr.hasNext() && cardCountMain < maxCards) {
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
            try {
                mCardContainerMain.refreshTopCard();
            } catch (Exception e) {
                errorProgressBar();
                return;
            }
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
     *
     * @param itr an iterator to the questionsList
     */
    private void addCard(Iterator<Question> itr) {
        addCard(itr, false);
    }

    /**
     * Add a card while notifying adapterMain
     * This is the initial procedure for adding files
     *
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
//        cardModel.setOnClickListener(new CardModel.OnClickListener() {
//            @Override
//            public void OnClickListener() {
//                Log.i("Swipeable Cards", "I am pressing the card");
//            }
//        });
        if (isInitial) {
            adapterMain.addInitial(cardModel);
        } else {
            adapterMain.add(cardModel);
        }
        ++cardCountMain;
        itr.remove();
        questionsCardQueue.add(cardModel);
    }

    /**
     * Add cards from queue to adapterMain
     * Used when changing from favourites to main
     */
    private void addCardFromFav(Iterator<CardModel> itr) {
        CardModel cardModel = itr.next();
        cardModel.setFavorite(false);
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
//        cardModel.setOnClickListener(new CardModel.OnClickListener() {
//            @Override
//            public void OnClickListener() {
//                Log.i("Swipeable Cards", "I am pressing the card");
//            }
//        });
        adapterMain.addInitial(cardModel);
    }

    private void mainCardRemoval() {
        --cardCountMain;
        if (questionsCardQueue.size() > 0) {
            questionsCardQueue.remove();
        }
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
                ++indexFavorite;
                ensureFavoritesFull(false);
            }

            @Override
            public void onDislike() {
//                Log.i("Swipeable Cards", "I dislike the card");
                --cardCountFavorite;
                ++indexFavorite;
                ensureFavoritesFull(false);
            }
        });
//        cardModel.setOnClickListener(new CardModel.OnClickListener() {
//            @Override
//            public void OnClickListener() {
//                Log.i("Swipeable Cards", "I am pressing the card");
//            }
//        });
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
            if (itr.hasNext() && cardCountMain < maxCards) {
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

    /**
     * Fills cards into adapterMain from cardQueue
     */
    private void ensureMainFull() {
        if (questionsCardQueue.size() > 0) {
            Iterator<CardModel> itr = questionsCardQueue.iterator();
            do {
                addCardFromFav(itr);
            } while (itr.hasNext());
        }

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

    @Override
    public void onBackPressed() {
        // RetrieveLastCard will put previously destroyed card
        //   on the top of current card container stack
        //   else just use normal back button
        if (isFavoriteMode) {
//            if (!mCardContainerFavorites.retrieveLastCard()) {
//                return; //do nothing
//            }
            mCardContainerFavorites.retrieveLastCard();
        } else {
//            if (!mCardContainerMain.retrieveLastCard()) {
//                return; //do nothing
//            }
            mCardContainerMain.retrieveLastCard();
        }

    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            super.onBackPressed();
        }
        return super.onKeyLongPress(keyCode, event);
    }
}
