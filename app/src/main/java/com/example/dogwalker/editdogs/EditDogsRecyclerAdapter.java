package com.example.dogwalker.editdogs;

import com.example.dogwalker.CircleTransform;
import com.example.dogwalker.R;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EditDogsRecyclerAdapter extends RecyclerView.Adapter<EditDogsRecyclerAdapter.DogDetailsViewHolder> {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private DatabaseReference currentUserRef;
    private DatabaseReference allDogsReference;
    private DatabaseReference myDogsReference;
    private ChildEventListener myDogsEventListener;
    private List<String> keyList;
    private DogDetailItemListener listener;
    private Resources resources;

    public EditDogsRecyclerAdapter(DogDetailItemListener listener, RecyclerView recyclerView, Resources resources) {

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        currentUserRef = database.getReference("Users/" + currentUser.getUid());
        allDogsReference = database.getReference("Dogs");
        myDogsReference = currentUserRef.child("dogs");

        keyList = new ArrayList<>();
        this.listener = listener;
        this.resources = resources;

        myDogsEventListener = myDogsReference.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot != null && snapshot.getKey() != null && snapshot.getValue() != null
                        && !keyList.contains(snapshot.getKey()) && Boolean.parseBoolean(snapshot.getValue().toString())) {
                    keyList.add(snapshot.getKey());
                    notifyItemInserted(keyList.size() - 1);
                    recyclerView.scrollToPosition(keyList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot != null && snapshot.getKey() != null && snapshot.getValue() != null) {

                    if (!keyList.contains(snapshot.getKey()) && Boolean.parseBoolean(snapshot.getValue().toString())) {
                        keyList.add(snapshot.getKey());
                        notifyItemInserted(keyList.size() - 1);
                        recyclerView.scrollToPosition(keyList.size() - 1);
                    } else {
                        int position = keyList.indexOf(snapshot.getKey());
                        if (position != -1 && !Boolean.parseBoolean(snapshot.getValue().toString())) {
                            keyList.remove(position);
                            notifyItemRemoved(position);
                            recyclerView.scrollToPosition(position);
                        }
                    }
                }
            }

            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) { }
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @NonNull @Override
    public DogDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DogDetailsViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_dog_details, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DogDetailsViewHolder holder, int position) {

        String currentDogKey = keyList.get(position);
        DatabaseReference currentDogReference = allDogsReference.child(currentDogKey);

        holder.moreButton.setVisibility(View.VISIBLE);
        holder.moreButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            MenuInflater menuInflater = popupMenu.getMenuInflater();
            menuInflater.inflate(R.menu.menu_dog_details_popup, popupMenu.getMenu());
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(item -> {
                final int editDogId = R.id.action_edit_dog;
                final int removeDogId = R.id.action_remove_dog;
                switch (item.getItemId()) {
                    case editDogId:
                        listener.startEditDogFragment(currentDogKey, "edit_dog");
                        return true;
                    case removeDogId:
                        currentUserRef.child("dogs/" + currentDogKey).setValue(false)
                                .addOnSuccessListener(aVoid1 ->
                                        Toast.makeText(v.getContext(), "Your dog was removed.", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(v.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        return true;
                    default: return false;
                }
            });
        });

        if (holder.profilePictureReference != null && holder.profilePictureListener != null)
            holder.profilePictureReference.removeEventListener(holder.profilePictureListener);
        if (holder.nameReference != null && holder.nameListener != null)
            holder.nameReference.removeEventListener(holder.nameListener);
        if (holder.breedReference != null && holder.breedListener != null)
            holder.breedReference.removeEventListener(holder.breedListener);
        if (holder.ageReference != null && holder.ageListener != null)
            holder.ageReference.removeEventListener(holder.ageListener);
        if (holder.aboutMeReference != null && holder.aboutMeListener != null)
            holder.aboutMeReference.removeEventListener(holder.aboutMeListener);
        if (holder.trainingReference != null && holder.trainingListener != null)
            holder.trainingReference.removeEventListener(holder.trainingListener);
        if (holder.walkLengthReference != null && holder.walkLengthListener != null)
            holder.walkLengthReference.removeEventListener(holder.walkLengthListener);
        if (holder.needsReference != null && holder.needsListener != null)
            holder.needsReference.removeEventListener(holder.needsListener);
        if (holder.requirementsReference != null && holder.requirementsListener != null)
            holder.requirementsReference.removeEventListener(holder.requirementsListener);

        holder.profilePictureReference = currentDogReference.child("profilePicture");
        holder.profilePictureListener = holder.profilePictureReference.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null && snapshot.getValue().toString().length() > 0) {
                    holder.profilePicture.setBackground(null);
                    Picasso.get().load(snapshot.getValue().toString()).transform(new CircleTransform()).into(holder.profilePicture);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        holder.nameReference = currentDogReference.child("name");
        holder.nameListener = holder.nameReference.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null && snapshot.getValue().toString().length() > 0)
                holder.name.setText(snapshot.getValue().toString());
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        holder.breedReference = currentDogReference.child("breed");
        holder.breedListener = holder.breedReference.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null && snapshot.getValue().toString().length() > 0)
                    holder.breed.setText(snapshot.getValue().toString());
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        holder.ageReference = currentDogReference.child("birthDate");
        holder.ageListener = holder.ageReference.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null && snapshot.getValue().toString().length() > 0) {
                    holder.age.setText(getAge(Long.parseLong(snapshot.getValue().toString())));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        holder.aboutMeReference = currentDogReference.child("profileAboutMe");
        holder.aboutMeListener = holder.aboutMeReference.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null && snapshot.getValue().toString().length() > 0)
                    holder.aboutMe.setText(snapshot.getValue().toString());
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        holder.trainingReference = currentDogReference.child("trainingLevel");
        holder.trainingListener = holder.trainingReference.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null && snapshot.getValue().toString().length() > 0)
                    holder.training.setText(snapshot.getValue().toString());
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        holder.walkLengthReference = currentDogReference.child("averageWalkLength");
        holder.walkLengthListener = holder.walkLengthReference.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null && snapshot.getValue().toString().length() > 0)
                    holder.walkLength.setText(snapshot.getValue().toString());
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        holder.needsReference = currentDogReference.child("infoAndHealthNeeds");
        holder.needsListener = holder.needsReference.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    StringBuilder needsText = new StringBuilder();
                    for (int i = 0; i < snapshot.getChildrenCount(); i++) {
                        DataSnapshot child = snapshot.child(Integer.toString(i));
                        if (child.exists() &&  child.getValue() != null && child.getValue().toString().length() > 0) {
                            if (i != 0) needsText.append(", ");
                            needsText.append(child.getValue().toString());
                        }
                    }
                    holder.needs.setText(needsText.toString());
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        holder.requirementsReference = currentDogReference.child("walkerRequirements");
        holder.requirementsListener = holder.requirementsReference.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    StringBuilder requirementsText = new StringBuilder();
                    for (int i = 0; i < snapshot.getChildrenCount(); i++) {
                        DataSnapshot child = snapshot.child(Integer.toString(i));
                        if (child.exists() &&  child.getValue() != null && child.getValue().toString().length() > 0) {
                            if (i != 0) requirementsText.append(", ");
                            requirementsText.append(child.getValue().toString());
                        }
                    }
                    holder.requirements.setText(requirementsText.toString());
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private String getAge(long birthTimestamp) {

        String age = "";
        long ageTime = Calendar.getInstance().getTimeInMillis() - birthTimestamp;
        long totalDays = ageTime / 86400000;
        long totalMonths = (long) (totalDays / 30.4375);
        long days = (long) (totalDays % 30.4375);
        long years = totalMonths / 12;
        long months = totalMonths % 12;

        if (totalDays < 1)
            age = "< 1 " + resources.getString(R.string.days);

        else if (totalMonths < 1) {
            age = totalDays + " ";
            if (totalDays == 1) age = age + resources.getString(R.string.day);
            else age = age + resources.getString(R.string.days);

        } else if (years < 1) {
            age = totalMonths + " ";
            if (totalMonths == 1) age = age + resources.getString(R.string.month);
            else age = age + resources.getString(R.string.months);
            if (days > 0) {
                age = age + resources.getString(R.string.comma) + " " + days + " ";
                if (days == 1) age = age + resources.getString(R.string.day);
                else age = age + resources.getString(R.string.days);
            }

        } else {
            age = years + " ";
            if (years == 1) age = age + resources.getString(R.string.year);
            else age = age + resources.getString(R.string.years);
            if (months > 0) {
                age = age + resources.getString(R.string.comma) + " " + months + " ";
                if (months == 1) age = age + resources.getString(R.string.month);
                else age = age + resources.getString(R.string.months);
            }
            if (days > 0) {
                age = age + resources.getString(R.string.comma) + " " + days + " ";
                if (days == 1) age = age + resources.getString(R.string.day);
                else age = age + resources.getString(R.string.days);
            }
        }

        return age;
    }

    public void removeListener() {
        if (myDogsReference != null && myDogsEventListener != null)
            myDogsReference.removeEventListener(myDogsEventListener);
    }

    @Override
    public int getItemCount() { return keyList.size(); }

    public static class DogDetailsViewHolder extends RecyclerView.ViewHolder {

        public ImageButton moreButton;
        public ImageView profilePicture;
        public TextView name;
        public TextView breed;
        public TextView age;
        public TextView aboutMe;
        public TextView training;
        public TextView walkLength;
        public TextView needs;
        public TextView requirements;

        public DatabaseReference profilePictureReference;
        public DatabaseReference nameReference;
        public DatabaseReference breedReference;
        public DatabaseReference ageReference;
        public DatabaseReference aboutMeReference;
        public DatabaseReference trainingReference;
        public DatabaseReference walkLengthReference;
        public DatabaseReference needsReference;
        public DatabaseReference requirementsReference;

        public ValueEventListener profilePictureListener;
        public ValueEventListener nameListener;
        public ValueEventListener breedListener;
        public ValueEventListener ageListener;
        public ValueEventListener aboutMeListener;
        public ValueEventListener trainingListener;
        public ValueEventListener walkLengthListener;
        public ValueEventListener needsListener;
        public ValueEventListener requirementsListener;

        public DogDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            moreButton = itemView.findViewById(R.id.dog_more_button);
            profilePicture = itemView.findViewById(R.id.dog_profile_picture);
            name = itemView.findViewById(R.id.dog_name);
            breed = itemView.findViewById(R.id.dog_breed);
            age = itemView.findViewById(R.id.dog_age);
            aboutMe = itemView.findViewById(R.id.dog_about_me);
            training = itemView.findViewById(R.id.dog_training);
            walkLength = itemView.findViewById(R.id.dog_walk_length);
            needs = itemView.findViewById(R.id.dog_needs);
            requirements = itemView.findViewById(R.id.dog_requirements);
        }
    }
}
