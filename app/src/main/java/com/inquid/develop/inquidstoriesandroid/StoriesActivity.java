package com.inquid.develop.inquidstoriesandroid;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;

public class StoriesActivity extends Activity {
    private static final Integer[] XMEN = {R.drawable.story_1, R.drawable.story_3, R.drawable.story_4, R.drawable.story_5, R.drawable.story_6};
    private ArrayList<Integer> XMENArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories);

    }


    public void viewStory(View view) {
        startActivity(new Intent(StoriesActivity.this, MainActivity.class));
    }
}
