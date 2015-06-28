package com.konradjanica;

import android.content.Context;

import com.andtinder.model.CardModel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

/**
 * Writes/reads an object to/from a private local file
 */
public class Utils {

    public static void writeLinkedListToFile(
            Context context, LinkedList<CardModel> cardList, String filename) {
        FileOutputStream fout;
        try {
            fout = context.openFileOutput(filename, context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            return;
        }
        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(fout);
            out.writeObject(cardList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static LinkedList<CardModel> readLinkedListFromFile(Context context, String filename) {
        FileInputStream fin;
        LinkedList<CardModel> cardList = new LinkedList<>();
        try {
            fin = context.openFileInput(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return cardList;
        }
        try {
            ObjectInputStream in = new ObjectInputStream(fin);
            cardList = (LinkedList<CardModel>) in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cardList;
    }
}
