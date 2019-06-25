package com.example.cheeyung.recipeapp.Adapter;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.cheeyung.recipeapp.Model.RecipeModel;
import com.example.cheeyung.recipeapp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import static android.view.View.GONE;

public class RecipeListAdapter extends FirestoreRecyclerAdapter<RecipeModel, RecipeListAdapter.RecipeHolder>
        implements View.OnClickListener {

    private ObservableSnapshotArray<RecipeModel> mSnapshots;

    OnItemClickListener listener;
    View view;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    FirestoreRecyclerOptions<RecipeModel> options;

    public RecipeListAdapter(FirestoreRecyclerOptions<RecipeModel> options) {
        super(options);
        this.options = options;
    }


    class RecipeHolder extends RecyclerView.ViewHolder {
        TextView txtRecipeTitle;
        ImageView imgRecipe;
        View frontLayout;

        public RecipeHolder(View itemView) {
            super(itemView);

            frontLayout = itemView.findViewById(R.id.front_layout);

            txtRecipeTitle=itemView.findViewById(R.id.txtRecipeTitle);
            imgRecipe=itemView.findViewById(R.id.imgRecipe);


            frontLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }

    }

    @Override
    protected void onBindViewHolder(@NonNull final RecipeHolder holder, int position, @NonNull final RecipeModel model) {


        holder.txtRecipeTitle.setText(model.getRecipeTitle());
        if(model.getRecipeTitle().equals(""))
        {
            holder.txtRecipeTitle.setText("Untitled");
        }
        Uri res = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + view.getContext().getResources().getResourcePackageName(R.drawable.no_image)
                + '/' + view.getContext().getResources().getResourceTypeName(R.drawable.no_image)
                + '/' + view.getContext().getResources().getResourceEntryName(R.drawable.no_image));

        Picasso.get().load(res).noPlaceholder().centerCrop().resize(200,150)
                .into(holder.imgRecipe);

        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        if (!model.getRecipeImage().isEmpty()) {
            holder.imgRecipe.setImageDrawable(null);
            ///// set Imageview to null for loading progress bar and load database image into Imageview
            progressBar.setVisibility(View.VISIBLE);

            Uri uri = Uri.parse(model.getRecipeImage());
            Picasso.get().load(uri).noPlaceholder().centerCrop().fit()
                    .into(holder.imgRecipe,
                            new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    progressBar.setVisibility(GONE);
                                }

                                @Override
                                public void onError(Exception e) {

                                }

                            });
        } else {
            progressBar.setVisibility(GONE);
        }
    }


    @NonNull
    @Override
    public RecipeHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recipe_list,
                viewGroup, false);

        return new RecipeHolder(view);
    }


    @Override
    public void onClick(View v) {

    }


    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void deleteRecipe(final int position) {

        DocumentReference docRef = getSnapshots().getSnapshot(position).getReference();
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        getSnapshots().getSnapshot(position).getReference().delete();
                        notifyItemChanged(position);


                    }
                }
            }
        });
    }


    @Override
    public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
    }


    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

}
