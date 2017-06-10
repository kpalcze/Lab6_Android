package com.example.k.lab6;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * Created by K on 2017-06-08.
 */

public class Config extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    public Config(Context context) {
        super(context, "AppConfig",
                null, DATABASE_VERSION);
    }

    @Override //Creating new table config contains private and public key
    public void onCreate(SQLiteDatabase database) {
        String DATABASE_CREATE =
                "create table config " +
                        "(_id integer primary key autoincrement," +
                        "privKey blob not null," +
                        "pubKey string not null);";

        database.execSQL(DATABASE_CREATE);
        //Fill config database
        addKeys(database);
    }

    //method adding initial values to database
    private void addKeys(SQLiteDatabase db) {
        //Generate 2 RSA keys
        KeyPair keys = generateKeys();
        ContentValues values = new ContentValues();
        //Adding Keys into database
        values.put("privKey", keys.getPrivate().getEncoded());
        //Encode Key as String to sending this into Serwer
        values.put("pubKey", Base64.encodeToString(keys.getPublic().getEncoded(), Base64.DEFAULT));
        db.insert("config", null, values);
    }

    //Getting private RSA key as PrivateKey object for async cryptography
    private PrivateKey getPrivate(){
        SQLiteDatabase db =
                this.getReadableDatabase();
        Cursor cursor =
                db.query("config",
                        new String[] { "privKey",},
                        null,null,
                        null,null,
                        null,null);
        if (cursor != null)
            cursor.moveToFirst();
        //Convert bloob data into privateKey
        PrivateKey privateKey = null;
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(cursor.getBlob(0)));
        }catch(Exception ex){}
        db.close();
        return privateKey;
    }

    //Method returns encrypt sent message with RSA private key
    public String sign(String msg){
        try {
            //Create object for crypt
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getPrivate());
            //Convert encoded message into base64 format (for sending threw HTTP
            String sign = Base64.encodeToString(cipher.doFinal(msg.getBytes()), Base64.DEFAULT);
            return sign;
        }catch(Exception ex){}
        return null;
    }

    //Get public Key string for veryfi sign
    public String getPublic(){
        try {
            //Get data String from database
            SQLiteDatabase db =
                    this.getReadableDatabase();
            Cursor cursor =
                    db.query("config",
                            new String[]{"pubKey",},
                            null, null,
                            null, null,
                            null, null);
            if (cursor != null)
                cursor.moveToFirst();
            db.close();
            return cursor.getString(0);
        }catch(Exception ex){ }
        return null;
    }

    //Method recreate database with new keys
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS config");
        onCreate(db);
    }

    //RSA key pair generation
    public static KeyPair generateKeys() {
        KeyPair keyPair = null;
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            //Set key length
            keygen.initialize(512);
            keyPair = keygen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        return keyPair;
    }

}