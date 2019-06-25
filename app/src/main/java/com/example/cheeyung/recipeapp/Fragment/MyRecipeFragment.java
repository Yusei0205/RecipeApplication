package com.example.cheeyung.recipeapp.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.cheeyung.recipeapp.Adapter.RecipeAdapter;
import com.example.cheeyung.recipeapp.Adapter.RecipeListAdapter;
import com.example.cheeyung.recipeapp.Model.RecipeModel;
import com.example.cheeyung.recipeapp.NewRecipe;
import com.example.cheeyung.recipeapp.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;

public class MyRecipeFragment extends Fragment {
    View view;
    TextView txtEmptyView, txtTotalRecipe;

    String recipeType;
    RecipeAdapter recipeAdapter;
    Spinner recipe_spinner;

    ////// firebase setup
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference recipeRef = db.collection("Recipe");

    ////// recyclerview setup
    private RecipeListAdapter adapter;
    private RecyclerView recyclerView;
    FirestoreRecyclerOptions<RecipeModel> options;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.my_recipe_fragment, container, false);
        } catch (Exception ex) {

        }

        txtEmptyView = view.findViewById(R.id.txtEmptyView);
        txtTotalRecipe = view.findViewById(R.id.txtTotalRecipe);

        ArrayList<String> recipe_list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.recipe_type)));
        recipe_list.add(0, "Any Recipe");
        String[] recipe_type= Arrays.copyOf(recipe_list.toArray(), recipe_list.size(), String[].class);

        recipe_spinner = view.findViewById(R.id.spinner_recipe);
        recipeAdapter = new RecipeAdapter(getActivity(), recipe_type);
        recipe_spinner.setAdapter(recipeAdapter);
        recipe_spinner.setSelection(0);

        recipe_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // spinnerAdapter.getItem(position);
                // Display the selected item into the TextView
                recipeType = (String) recipeAdapter.getRecipeType(position);
                if (recipeType.equals("Any Recipe")) {
                    recipeType = "all";
                }
                filterRecipe(recipeType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        setUpRecyclerView();
        return view;
    }

    private void setUpRecyclerView() {

        Query query = recipeRef.orderBy("recipeID", Query.Direction.DESCENDING);

        options = new FirestoreRecyclerOptions.Builder<RecipeModel>()
                .setQuery(query, RecipeModel.class)
                .build();
        adapter = new RecipeListAdapter(options);

        recyclerView = view.findViewById(R.id.recycler_view_property);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager((new LinearLayoutManager(getActivity().getApplicationContext())));
        recyclerView.setAdapter(adapter);
        txtEmptyView.setText("No Recipe");

        setAdapterListener();
    }

    private void filterRecipe(String recipeType) {
        adapter.stopListening();

        Query query;
        if (recipeType.equals("all")) {
            query = recipeRef.orderBy("recipeID", Query.Direction.DESCENDING);
        } else {
            query = recipeRef.orderBy("recipeID", Query.Direction.DESCENDING)
                    .whereEqualTo("recipeType", recipeType);
        }

        options = new FirestoreRecyclerOptions.Builder<RecipeModel>()
                .setQuery(query, RecipeModel.class)
                .build();
        adapter = new RecipeListAdapter(options);
        recyclerView.setAdapter(adapter);
        setAdapterListener();
        adapter.startListening();

    }


    private void setAdapterListener() {

        ///// get total number of item in adapter through synchronized data
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            public void onChanged() {
                checkEmpty();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                checkEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                checkEmpty();
            }

            void checkEmpty() {
                recyclerView.setVisibility(View.VISIBLE);
                txtEmptyView.setVisibility(View.GONE);
                if (adapter.getItemCount() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    txtEmptyView.setVisibility(View.VISIBLE);
                }
                txtTotalRecipe.setText(String.valueOf(adapter.getItemCount()));
            }
        });

        adapter.setOnItemClickListener(new RecipeListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

                RecipeModel recipeModel = documentSnapshot.toObject(RecipeModel.class);
                String recipe_id = documentSnapshot.getId();

                ////// Pass variable to next view
                Bundle bundle = new Bundle();
                bundle.putString("Recipe ID", recipe_id);

                Intent intent;
                intent = new Intent(getActivity().getApplicationContext(), NewRecipe.class);
                intent.putExtras(bundle);
                startActivity(intent);
                //Toast.makeText(getContext(),recipe_id,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}
