package com.example.cheeyung.recipeapp.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.example.cheeyung.recipeapp.R;

public class RecipeAdapter extends BaseAdapter implements SpinnerAdapter {
    Activity activity;
    String[] recipe_type;
    LayoutInflater inflater;
    View row;

    public RecipeAdapter(Activity activity, String[] recipe_type) {
        this.activity = activity;
        this.recipe_type = recipe_type;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        int count = recipe_type.length;
        return count -1;
    }




    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    public String getRecipeType(int position) {
        return recipe_type[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        row = inflater.inflate(R.layout.spinner, null);
        //CustomFont fontChanger = new CustomFont(row.getContext().getAssets(), "font/nunito_sans.ttf");
        // fontChanger.replaceFonts((ViewGroup)convertView);
        TextView recipe = (TextView) row.findViewById(R.id.recipe_type);
        recipe.setText(recipe_type[position]);
        return row;
    }
}
