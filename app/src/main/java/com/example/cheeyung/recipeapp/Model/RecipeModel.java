package com.example.cheeyung.recipeapp.Model;

import java.util.ArrayList;

public class RecipeModel {
    private String recipeID;
    private String recipeTitle;
    private String recipeImage;
    private String recipeType;
    private ArrayList<String> ingredientList;
    private ArrayList<String> imageList;
    private ArrayList<String> stepList;

    public String getRecipeID() {
        return recipeID;
    }

    public void setRecipeID(String recipeID) {
        this.recipeID = recipeID;
    }

    public String getRecipeType() {
        return recipeType;
    }

    public void setRecipeType(String recipeType) {
        this.recipeType = recipeType;
    }

    public String getRecipeTitle() {
        return recipeTitle;
    }

    public void setRecipeTitle(String recipeTitle) {
        this.recipeTitle = recipeTitle;
    }

    public String getRecipeImage() {
        return recipeImage;
    }

    public void setRecipeImage(String recipeImage) {
        this.recipeImage = recipeImage;
    }

    public ArrayList<String> getIngredientList() {
        return ingredientList;
    }

    public void setIngredientList(ArrayList<String> ingredientList) {
        this.ingredientList = ingredientList;
    }

    public ArrayList<String> getImageList() {
        return imageList;
    }

    public void setImageList(ArrayList<String> imageList) {
        this.imageList = imageList;
    }

    public ArrayList<String> getStepList() {
        return stepList;
    }

    public void setStepList(ArrayList<String> stepList) {
        this.stepList = stepList;
    }
}
