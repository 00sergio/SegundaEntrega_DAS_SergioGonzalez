package com.example.cookit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "CookItDB";
    private static final String TABLE_RECIPES = "recipes";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_INGREDIENTS = "ingredientes";

    // User table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_RECIPES_TABLE = "CREATE TABLE " + TABLE_RECIPES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_INGREDIENTS + " TEXT)";
        db.execSQL(CREATE_RECIPES_TABLE);

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT,"
                + COLUMN_PASSWORD + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public long addRecipe(String name, String ing) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_INGREDIENTS, ing);
        long id = db.insert(TABLE_RECIPES, null, values);
        db.close();
        return id;
    }

    public int updateRecipe(Receta receta) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, receta.getNombre());
        values.put(COLUMN_INGREDIENTS, receta.getIngredientes());
        int rowsAffected = db.update(TABLE_RECIPES, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(receta.getId())});
        db.close();
        return rowsAffected;
    }

    public void deleteRecipe(long recipeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RECIPES, COLUMN_ID + " = ?",
                new String[]{String.valueOf(recipeId)});
        db.close();
    }

    public List<Receta> getAllRecipes() {
        List<Receta> recipeList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RECIPES, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int columnId = cursor.getColumnIndex(COLUMN_ID);
                    int columnName = cursor.getColumnIndex(COLUMN_NAME);
                    int columnIngredientes = cursor.getColumnIndex(COLUMN_INGREDIENTS);
                    do {
                        long id = cursor.getInt(columnId);
                        String name = cursor.getString(columnName);
                        String ing = cursor.getString(columnIngredientes);
                        Receta receta = new Receta(id,name,ing);
                        recipeList.add(receta);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        db.close();
        return recipeList;
    }



}
