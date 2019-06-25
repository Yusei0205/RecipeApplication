package com.example.cheeyung.recipeapp;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cheeyung.recipeapp.Adapter.RecipeAdapter;
import com.example.cheeyung.recipeapp.Model.RecipeModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import static android.content.ContentValues.TAG;

public class NewRecipe extends AppCompatActivity implements View.OnClickListener {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageReference;
    int recipe_ID;
    String readRecipeID;

    LinearLayout stepListLayout;
    LinearLayout ingredientListLayout;
    LinearLayout uploadLayout;
    LinearLayout editImage;
    RelativeLayout editLayout;
    String recipeType, recipeID;
    String imageType, imageUrl = "";
    EditText txtRecipeTitle;

    ImageView imgUpload;
    ImageView img1, img2, img3, img4, img5, img6;
    ImageView[] imageView = new ImageView[]{img1, img2, img3, img4, img5, img6};
    ArrayList<Uri> ImageList = new ArrayList<Uri>();
    ArrayList<String> ImageListUrl = new ArrayList<>();
    ArrayList<String> IngredientList = new ArrayList<>();
    ArrayList<String> StepList = new ArrayList<>();
    ImageButton btnDelImage;

    int imgID;
    Uri res, uri, recipeUri;

    ProgressDialog progressDialog;
    Button btnSave, btnUpload;
    TextView stepText;
    EditText txtInstruction, txtIngredient;
    View ingredientView, stepView, itemView, item2View;

    RecipeAdapter recipeAdapter;
    Spinner recipe_spinner;

    boolean update = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_recipe);
        progressDialog = new ProgressDialog(NewRecipe.this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        stepListLayout = findViewById(R.id.stepListLayout);
        ingredientListLayout = findViewById(R.id.ingredientListLayout);

        txtRecipeTitle = findViewById(R.id.txtRecipeTitle);
        txtInstruction = findViewById(R.id.txtInstruction);
        txtIngredient = findViewById(R.id.txtIngredient);
        editImage = findViewById(R.id.editImage);
        editLayout = findViewById(R.id.editLayout);
        imgUpload = findViewById(R.id.imgRecipe);
        uploadLayout = findViewById(R.id.upload_Layout);
        btnUpload = findViewById(R.id.btnUpload);
        btnDelImage = findViewById(R.id.delRecipeImage);

        res = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.add_image)
                + '/' + getResources().getResourceTypeName(R.drawable.add_image)
                + '/' + getResources().getResourceEntryName(R.drawable.add_image));

        TextView addStepText = findViewById(R.id.addStep);
        TextView addIngredientText = findViewById(R.id.addIngredient);
        addIngredientText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addIngredient("");
            }
        });

        addStepText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStep("");
            }
        });

        for (int i = 0; i < 6; i++) {
            int id = getResources().getIdentifier("img" + (i + 1), "id", getPackageName());
            imageView[i] = findViewById(id);
            imageView[i].setTag("default");
            imageView[i].setOnClickListener(this);
        }

        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IngredientList.clear();
                int count = ingredientListLayout.getChildCount();
                for (int i = 0; i < count; i++) {
                    item2View = ingredientListLayout.getChildAt(i);
                    txtIngredient = item2View.findViewById(R.id.txtIngredient);

                    if (item2View instanceof LinearLayout) {
                        if (!(txtIngredient.getText().toString().equals(""))) {
                            IngredientList.add(txtIngredient.getText().toString());
                        }
                    }
                }

                StepList.clear();
                int count2 = stepListLayout.getChildCount();
                for (int j = 0; j < count2; j++) {
                    itemView = stepListLayout.getChildAt(j);
                    txtInstruction = itemView.findViewById(R.id.txtInstruction);
                    if (itemView instanceof LinearLayout) {

                        if (!txtInstruction.getText().toString().equals("")) {
                            StepList.add(txtInstruction.getText().toString());
                        }
                    }
                }
                ////////// start perform save action
                if (!IngredientList.isEmpty() || StepList.isEmpty()) {
                    progressDialog.setMessage("Saving recipe...");
                    progressDialog.show();
                    progressDialog.setCancelable(false);

                    if (update) {
                        saveRecipe();
                    } else {
                        saveRecipeThumbnail();
                    }

                } else if (recipeType.equals("Select Recipe")) {
                    Toast.makeText(NewRecipe.this, "Please select recipeType type", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(NewRecipe.this, "Please enter step and ingredient", Toast.LENGTH_SHORT).show();

                }
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageType = "Upload";
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(i, 1);
            }
        });


        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageType = "Upload";
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(i, 1);
            }
        });


        btnDelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewRecipe.this);
                builder.setMessage("Confirm remove Image?")
                        .setTitle("Remove Image")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Picasso.get().load(res).noPlaceholder().centerCrop().fit()
                                        .into(imgUpload);

                                imgUpload.setTag("default");
                                recipeUri = null;
                                imageUrl = "";
                                editLayout.setVisibility(View.GONE);
                                uploadLayout.setVisibility(View.VISIBLE);

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                builder.show();
            }
        });


        /////////////   Spinner
        recipe_spinner = findViewById(R.id.spinner_recipe);
        final String[] recipe_type = getResources().getStringArray(R.array.recipe_type);
        recipeAdapter = new RecipeAdapter(NewRecipe.this, recipe_type);
        recipe_spinner.setAdapter(recipeAdapter);
        recipe_spinner.setSelection(recipeAdapter.getCount());

        recipe_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // spinnerAdapter.getItem(position);
                // Display the selected item into the TextView
                recipeType = (String) recipeAdapter.getRecipeType(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Bundle bundle = getIntent().getExtras();
        recipeID = bundle.getString("Recipe ID");

        if (recipeID.equals("")) {
            getDocumentID();
        } else {
            update = true;
            loadRecipeDetails();
        }
    }


    private void addIngredient(String ingredient) {
        ingredientView = View.inflate(NewRecipe.this, R.layout.new_textview, null);

        //// set tag for verification on saving to database
        int count = ingredientListLayout.getChildCount();
        txtIngredient = ingredientView.findViewById(R.id.txtIngredient);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins(0, 10, 0, 10);
        ingredientView.setLayoutParams(param);
        ingredientView.setTag("ingredient" + count);
        ingredientListLayout.addView(ingredientView);

        //// set function for delete button

        count = ingredientListLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            item2View = ingredientListLayout.getChildAt(i);
            if (item2View instanceof LinearLayout) {
                txtIngredient.setTag(i);
                ImageButton imgDel = ingredientView.findViewById(R.id.btnDel);
                imgDel.setTag(ingredientView);
                imgDel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeIngView((View) v.getTag());
                    }
                });
                if (!ingredient.equals("")) {
                    txtIngredient.setText(ingredient);
                }
            }
        }


    }

    private void addStep(String step) {
        ///////instantiate new row for steps
        stepView = View.inflate(NewRecipe.this, R.layout.new_step_layout, null);

        txtInstruction = stepView.findViewById(R.id.txtInstruction);

        //count current item
        int count = stepListLayout.getChildCount();
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins(0, 10, 0, 10);
        stepView.setLayoutParams(param);
        stepView.setTag("step" + count);

        stepListLayout.setBackgroundColor(Color.TRANSPARENT);
        stepListLayout.addView(stepView);

        //// set value after adding new row
        count = stepListLayout.getChildCount();
        stepText = stepView.findViewById(R.id.step);

        for (int i = 0; i < count; i++) {
            itemView = stepListLayout.getChildAt(i);
            if (itemView instanceof LinearLayout) {

                ImageButton imgDel = stepView.findViewById(R.id.imgDel);
                imgDel.setTag(stepView);
                imgDel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeView((View) v.getTag());
                    }
                });
                stepText.setText((i + 1) + ":");
                txtInstruction.setTag("Instruction" + (i + 1));
                if (!step.equals("")) {
                    txtInstruction.setText(step);
                }
            }
        }

    }

    public void removeIngView(View x) {
        String ingredientNo = x.getTag().toString();
        ingredientListLayout.removeView(x);
        int count = ingredientListLayout.getChildCount();

        for (int i = 0; i < count; i++) {
            ingredientView = ingredientListLayout.getChildAt(i);
            if (itemView instanceof TextView) {
                txtInstruction.setTag("Instruction" + i);
            }
        }
    }

    public void removeView(View x) {
        String instructionNo = x.getTag().toString();
        stepListLayout.removeView(x);
        int count = stepListLayout.getChildCount();

        for (int i = 0; i < count; i++) {
            itemView = stepListLayout.getChildAt(i);
            if (itemView instanceof LinearLayout) {
                stepText.setText(i + ":");
                txtInstruction.setTag("Instruction" + i);
            }
        }
    }


    @Override
    public void onClick(View v) {
        imageType = "";
        switch (v.getId()) {
            case R.id.img1:
                imgID = 0;
                break;

            case R.id.img2:
                imgID = 1;
                break;

            case R.id.img3:
                imgID = 2;
                break;
            case R.id.img4:
                imgID = 3;
                break;

            case R.id.img5:
                imgID = 4;
                break;

            case R.id.img6:
                imgID = 5;
                break;
        }


        ////// check tag name is default, if no then add new image to imageview
        if (imageView[imgID].getTag().equals("default")) {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(i, 1);

        } else {
            ///// if imageview not default image, confirm if user want to remove image
            AlertDialog.Builder builder = new AlertDialog.Builder(NewRecipe.this);
            builder.setMessage("Confirm remove Image?")
                    .setTitle("Remove Image")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //Toast.makeText(AddPropertyActivity.this, imageView[imgID].getTag().toString(), Toast.LENGTH_SHORT).show();

                            Picasso.get().load(res).noPlaceholder().centerCrop().fit()
                                    .into(imageView[imgID]);
                            Uri temp = Uri.parse(imageView[imgID].getTag().toString());
                            ImageList.remove(temp);
                            ImageList.remove(imageView[imgID].getTag().toString());
                            imageView[imgID].setTag("default");

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            builder.show();

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            uri = data.getData();

            if (imageType.equals("Upload")) {
                uploadLayout.setVisibility(View.GONE);
                editLayout.setVisibility(View.VISIBLE);
                imgUpload.setTag(uri);
                recipeUri = uri;
                Picasso.get().load(uri).noPlaceholder().centerCrop().fit()
                        .into(imgUpload);
            } else {
                //// check repeated image
                if (!ImageList.contains(uri)) {
                    imageView[imgID].setTag(uri);
                    ImageList.add(uri);
                    Picasso.get().load(uri).noPlaceholder().centerCrop().fit()
                            .into(imageView[imgID]);

                } else {
                    Toast.makeText(NewRecipe.this, "Image Repeated", Toast.LENGTH_SHORT).show();
                }
            }


        }

        /////// clear image type to prevent adding wrong data in imagelist
        imageType = "";
    }

    private void saveRecipeThumbnail() {
        if (recipeUri != null) {
            storageReference = FirebaseStorage.getInstance().getReference();
            final StorageReference ref_child = storageReference.child(recipeID + "/" + "Recipe" + ".jpg");

            ref_child.putFile(recipeUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ref_child.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageUrl = uri.toString();
                            saveImage();
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(NewRecipe.this, "Fail update", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        } else {
            imageUrl = "";
            saveImage();
        }
    }

    private void saveImage() {
        if (ImageList.isEmpty()) {
            saveRecipe();
        } else {
            int number = 1;

            for (Uri imgList : ImageList) {
                storageReference = FirebaseStorage.getInstance().getReference();
                final StorageReference ref_child = storageReference.child(recipeID + "/" + "image" + number + ".jpg");

                if (imgList != null) {
                    ref_child.putFile(imgList).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            ref_child.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //// check same url in array list
                                    if (!ImageListUrl.contains(uri.toString())) {
                                        ImageListUrl.add(uri.toString());

                                        /// add property if imageurl arraylist same with imagelist arraylist
                                        if (ImageListUrl.size() == ImageList.size()) {
                                            Toast.makeText(NewRecipe.this, ImageListUrl.toString(), Toast.LENGTH_SHORT).show();

                                            saveRecipe();

                                        }
                                    }
                                }

                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(NewRecipe.this, "Fail update", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    });
                }
                number += 1;
            }
        }
    }


    public void saveRecipe() {
        RecipeModel recipeModel = new RecipeModel();
        recipeModel.setRecipeID(recipeID);
        recipeModel.setIngredientList(IngredientList);
        recipeModel.setStepList(StepList);
        recipeModel.setRecipeTitle(txtRecipeTitle.getText().toString().trim());
        recipeModel.setRecipeType(recipeType);
        recipeModel.setRecipeImage(imageUrl);
        recipeModel.setImageList(ImageListUrl);

        db.collection("Recipe").document(recipeID)
                .set(recipeModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(NewRecipe.this, "Recipe added successfully", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(NewRecipe.this, "Fail adding property: " + e, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();

                    }
                });
    }

    public void getDocumentID() {
        final ArrayList<Integer> collectionID = new ArrayList<Integer>();

        /// declare default propertyID if no document in firestore
        recipe_ID = 1;

        /// read collection and assign new property id
        db.collection("Recipe")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                collectionID.add(Integer.parseInt(document.getId().substring(1)));
                                recipe_ID = Collections.max(collectionID);

                                recipe_ID += 1;
                                recipeID = String.format("R%03d", recipe_ID);
                            }
                        }
                    }
                });
        recipeID = String.format("R%03d", recipe_ID);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void loadRecipeDetails() {
        try {
            db.collection("Recipe").document(recipeID).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();

                                txtRecipeTitle.setText(document.get("recipeTitle").toString());

                                /// load Recipe main image
                                imageUrl = document.get("recipeImage").toString();
                                if (!imageUrl.equals("")) {
                                    Uri url = Uri.parse(imageUrl);
                                    Picasso.get().load(url).noPlaceholder().centerCrop().fit().into(imgUpload);
                                    uploadLayout.setVisibility(View.GONE);
                                    editLayout.setVisibility(View.VISIBLE);

                                }

                                ImageListUrl = (ArrayList<String>) document.get("imageList");
                                if (!ImageListUrl.isEmpty()) {
                                    for (int i = 0; i < ImageListUrl.size(); i++) {
                                        Uri imgUri = Uri.parse(ImageListUrl.get(i));

                                        Picasso.get().load(imgUri).noPlaceholder().centerCrop().fit()
                                                .into(imageView[i]);
                                        imageView[i].setTag(imgUri.toString());
                                        ImageList.add(imgUri);
                                    }
                                }


                                /////////load Ingredient and Step list
                                IngredientList = (ArrayList<String>) document.get("ingredientList");
                                if (!IngredientList.isEmpty()) {
                                    txtIngredient.setText(IngredientList.get(0));
                                    if (IngredientList.size() > 1) {
                                        for (int i = 0; i < IngredientList.size() - 1; i++) {
                                            addIngredient(IngredientList.get(i + 1));
                                        }
                                    }
                                }

                                StepList = (ArrayList<String>) document.get("stepList");
                                if (!StepList.isEmpty()) {
                                    txtInstruction.setText(StepList.get(0));
                                    if (StepList.size() > 1) {
                                        for (int j = 0; j < IngredientList.size() - 1; j++) {
                                            addStep(StepList.get(j + 1));
                                        }
                                    }

                                }

                                //////check db string equal with list in spinner adapter
                                recipe_spinner.setAdapter(recipeAdapter);
                                for (int i = 0; i < recipe_spinner.getAdapter().getCount(); i++) {
                                    if (recipeAdapter.getRecipeType(i).equals(document.get("recipeType").toString())) {
                                        recipe_spinner.setSelection(i);
                                        recipeType = (String) recipeAdapter.getRecipeType(i);
                                    }
                                }

                            }
                        }
                    });
        } catch (Exception ex) {
            Toast.makeText(NewRecipe.this, "Error loading detail", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                AlertDialog.Builder builder = new AlertDialog.Builder(NewRecipe.this);
                builder.setMessage("Confirm Delete?")
                        .setTitle("Delete")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                db.collection("Recipe").document(recipeID).delete();
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                builder.show();

                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

}


