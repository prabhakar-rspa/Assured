package com.rspcaassured.sync.AssessmentQuestion;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * A dummy content provider that doesn't really do anything. Sync adapters
 * on Android require a content provider, which indirectly forces us to
 * work with cursors and use a cursor loader. Since we use a custom loader,
 * we simply declare this provider in the manifest, but use the loader
 * for all sync operations.
 *
 * @author shahedmiah
 */
public class AssessmentQuestionProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }
}
