package com.example.androidnotification;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidnotification.DataModel.DataModelAlgorithm;
import com.example.androidnotification.DataModel.DataModelArticles;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.messaging.FirebaseMessaging;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class Home extends AppCompatActivity {

    //FirebaseAuth
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    RecyclerView re_Article, re_OverAllArticle;

    ArrayList<DataModelArticles> ar_data = new ArrayList<>();
    ArrayList<DataModelArticles> ar_data_OverAll = new ArrayList<>();

    ArrayList<String> al_category = new ArrayList<>();

    HashMap<Integer, String> ar_like = new HashMap<>();
    HashMap<Integer, String> ar_view = new HashMap<>();
    HashMap<Integer, String> ar_comment = new HashMap<>();

    String docIdView = null;
    String docIdLike = null;
    String docIdComment = null;

    //1. Data read Algorithm Collection against user id


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //FireBase Auth getInstance
        mAuth = FirebaseAuth.getInstance();

        //Sign out

        //FirebaseMessaging
        FirebaseMessaging.getInstance().subscribeToTopic("test");

        //Find id of the components
        re_Article = findViewById(R.id.re_data);
        re_OverAllArticle = findViewById(R.id.re_data2);


        //Read data from Algo
        readDataAlgorithm(mAuth.getCurrentUser().getUid());

        /*//second Recycler view
        re_OverAllArticle.setAdapter(new AdataOverAllArticle());
        re_OverAllArticle.setLayoutManager(new LinearLayoutManager(Home.this, LinearLayoutManager.HORIZONTAL, false));*/


    }

    private void readDataAlgorithm(String uid) {

        al_category.clear();

        db.collection("Algorithm").whereEqualTo("uid", uid)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (queryDocumentSnapshots.isEmpty()) {

                    //Toast.makeText(Home.this, "Data is not found", Toast.LENGTH_SHORT).show();

                    //Call data if user can not any  interest of app
                    dataReadFirstTime();

                } else {

                    //Toast.makeText(Home.this, "Data is found", Toast.LENGTH_SHORT).show();

                    for (DocumentSnapshot ds : queryDocumentSnapshots) {

                        DataModelAlgorithm dataModelAlgorithm = ds.toObject(DataModelAlgorithm.class);


                        ar_like.put(dataModelAlgorithm.getLike(), dataModelAlgorithm.getCategory());
                        ar_view.put(dataModelAlgorithm.getView(), dataModelAlgorithm.getCategory());
                        ar_comment.put(dataModelAlgorithm.getComments(), dataModelAlgorithm.getCategory());

                        //al_data.add(dataModelAlgorithm);
                        //al_category.add(dataModelAlgorithm.getCategory());

                        //ar_like.put(ds.getString("category"), Integer.parseInt(ds.getString("like")));


                    }

                    //Call data if user can not any  interest of app

                    //MapValue
                    al_category.add(String.valueOf(ar_like.get(Collections.max(ar_like.keySet()))));
                    al_category.add(String.valueOf(ar_view.get(Collections.max(ar_view.keySet()))));
                    al_category.add(String.valueOf(ar_comment.get(Collections.max(ar_comment.keySet()))));

                    dataReadSecondTime();
                    //Toast.makeText(Home.this, String.valueOf(al_data.size()), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        db.collection("Article")
                .orderBy("date", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (queryDocumentSnapshots.isEmpty()) {

                    Toast.makeText(Home.this, "Data is not read", Toast.LENGTH_SHORT).show();

                } else {

                    //Toast.makeText(Home.this, "Data is read", Toast.LENGTH_SHORT).show();
                    for (DocumentSnapshot ds : queryDocumentSnapshots) {

                        DataModelArticles dataModelArticles = ds.toObject(DataModelArticles.class);

                        ar_data_OverAll.add(dataModelArticles);


                    }
                }

                //Toast.makeText(Home.this, String.valueOf(ar_data_OverAll.size()), Toast.LENGTH_SHORT).show();

                //second Recycler view
                re_OverAllArticle.setAdapter(new AdataOverAllArticle());
                re_OverAllArticle.setLayoutManager(new LinearLayoutManager(Home.this, LinearLayoutManager.HORIZONTAL, false));

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void dataReadSecondTime() {

        //new LinkedHashSet<>(al_data);

        //Set<String> s = new LinkedHashSet<String>(al_category);

        //ArrayList<String> listWithoutDuplicates = new ArrayList<>(hashSet);

        //ArrayList<String> category = Lists.newArrayList(Sets.newHashSet(al_category));

        //List<String> listWithoutDuplicates = hashSet.stream().distinct().collect(Collectors.toList());

        /*List<Object> listWithoutDuplicates = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            listWithoutDuplicates = al_category.stream()
                    .distinct()
                    .collect(Collectors.toList());
        }*/

        for (int i = 0; i < al_category.size(); i++) {

            for (int j = i + 1; j < al_category.size(); j++) {

                if (al_category.get(i).equals(al_category.get(j))) {

                    al_category.remove(j);
                    j--;
                }
            }
        }

        //int mapNumber = Collections.max(ar_like.keySet());

        //Toast.makeText(this, String.valueOf(mapNumber), Toast.LENGTH_SHORT).show();

        //Toast.makeText(this, String.valueOf(ar_like.get(Collections.max(ar_like.keySet()))), Toast.LENGTH_SHORT).show();

        for (int i = 0; i < al_category.size(); i++) {

            db.collection("Article")
                    .whereEqualTo("category", al_category.get(i))
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(3)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            if (queryDocumentSnapshots.isEmpty()) {

                                Toast.makeText(Home.this, "Data is not read", Toast.LENGTH_SHORT).show();

                            } else {

                                //Toast.makeText(Home.this, "Data is read", Toast.LENGTH_SHORT).show();

                                for (DocumentSnapshot ds : queryDocumentSnapshots) {

                                    DataModelArticles dataModelArticles = ds.toObject(DataModelArticles.class);

                                    ar_data.add(dataModelArticles);

                                }

                                // RecyclerView Code
                                re_Article.setAdapter(new AdapterArticle());
                                re_Article.setLayoutManager(new LinearLayoutManager(Home.this, LinearLayoutManager.HORIZONTAL, false));

                                // Toast.makeText(Home.this, String.valueOf(al_data.get(0).getLike()), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    //Log.e("Error", e.getMessage());

                }
            });
        }

    }

    private void dataReadFirstTime() {

        ar_data.clear();

        db.collection("Article")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            //Toast.makeText(Home.this, "Data is read", Toast.LENGTH_SHORT).show();
                            for (DocumentSnapshot ds : queryDocumentSnapshots) {

                                DataModelArticles dataModelArticles = ds.toObject(DataModelArticles.class);

                                ar_data.add(dataModelArticles);

                            }


                        } else {

                            Toast.makeText(Home.this, "Data is not read", Toast.LENGTH_SHORT).show();
                        }


                        // RecyclerView Code
                        re_Article.setAdapter(new AdapterArticle());
                        re_Article.setLayoutManager(new LinearLayoutManager(Home.this, LinearLayoutManager.HORIZONTAL, false));

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();


        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);

        builder.setCancelable(false);
        builder.setTitle("Exit");
        builder.setMessage("Are you sure you want to exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //super.onBackPressed();
                finish();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private class AdapterArticle extends RecyclerView.Adapter<AdapterArticle.ViewHolder> {
        @NonNull
        @Override
        public AdapterArticle.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.re_articles, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final AdapterArticle.ViewHolder holder, final int position) {

            holder.reCategory.setText(ar_data.get(position).getCategory());
            holder.reTitle.setText(ar_data.get(position).getTitle());
            holder.reDate.setText(ar_data.get(position).getDate().toDate().toString().substring(0, 10));
            holder.reView.setText(String.valueOf(ar_data.get(position).getView()));
            holder.reLike.setText(String.valueOf(ar_data.get(position).getLike()));

            //Button code
            holder.btn_heart.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {

                    //Toast.makeText(Home.this, "fill hart", Toast.LENGTH_SHORT).show();

                    db.collection("Algorithm")
                            .whereEqualTo("uid", mAuth.getCurrentUser().getUid())
                            .whereEqualTo("category", ar_data.get(position).getCategory())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    if (queryDocumentSnapshots.isEmpty()) {

                                        //Toast.makeText(Home.this, "Create new doc", Toast.LENGTH_SHORT).show();
                                        HashMap<String, Object> data = new HashMap<>();
                                        //data.put("articleRef", ar_data.get(position).getRef());
                                        data.put("category", ar_data.get(position).getCategory());
                                        data.put("uid", mAuth.getCurrentUser().getUid());
                                        data.put("like", 1);
                                        data.put("view", 0);
                                        data.put("comments", 0);

                                        db.collection("Algorithm").document()
                                                .set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Toast.makeText(Home.this, "Data is save in algorithm", Toast.LENGTH_SHORT).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    } else {

                                        for (DocumentSnapshot ds : queryDocumentSnapshots) {

                                            docIdLike = ds.getId();
                                        }

                                        //Toast.makeText(Home.this, documentId, Toast.LENGTH_SHORT).show();

                                        if (docIdLike != null) {

                                            final DocumentReference sfDocRef = db.collection("Algorithm")
                                                    .document(docIdLike);

                                            db.runTransaction(new Transaction.Function<Double>() {


                                                @Nullable
                                                @Override
                                                public Double apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                                                    DocumentSnapshot snapshot = transaction.get(sfDocRef);

                                                    double newPopulation = snapshot.getDouble("like") + 1;

                                                    if (newPopulation <= 1000000) {

                                                        transaction.update(sfDocRef, "like", newPopulation);
                                                        return newPopulation;

                                                    } else {

                                                        throw new FirebaseFirestoreException("Population too high",
                                                                FirebaseFirestoreException.Code.ABORTED);
                                                    }
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<Double>() {
                                                @Override
                                                public void onSuccess(Double aDouble) {

                                                    Toast.makeText(Home.this, "Data is save", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        } else {

                                            Toast.makeText(Home.this, "data is not update plz click again", Toast.LENGTH_SHORT).show();
                                        }

                                        //Toast.makeText(Home.this, "Update doc", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


                }

                @Override
                public void unLiked(LikeButton likeButton) {

                    //Toast.makeText(Home.this, "unfill hart", Toast.LENGTH_SHORT).show();

                    //holder.reLike.setText(String.valueOf(ar_data.get(position).getLike()));

                    db.collection("Algorithm")
                            .whereEqualTo("uid", mAuth.getCurrentUser().getUid())
                            .whereEqualTo("category", ar_data.get(position).getCategory())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    if (queryDocumentSnapshots.isEmpty()) {

                                        //There is no need to code
                                        Toast.makeText(Home.this, "Your no data about this article", Toast.LENGTH_SHORT).show();

                                    } else {

                                        for (DocumentSnapshot ds : queryDocumentSnapshots) {

                                            docIdLike = ds.getId();
                                        }

                                        //Toast.makeText(Home.this, documentId, Toast.LENGTH_SHORT).show();

                                        if (docIdLike != null) {

                                            final DocumentReference sfDocRef = db.collection("Algorithm")
                                                    .document(docIdLike);

                                            db.runTransaction(new Transaction.Function<Double>() {


                                                @Nullable
                                                @Override
                                                public Double apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                                                    DocumentSnapshot snapshot = transaction.get(sfDocRef);

                                                    double newPopulation = snapshot.getDouble("like") - 1;

                                                    if (newPopulation <= 1000000) {

                                                        transaction.update(sfDocRef, "like", newPopulation);
                                                        return newPopulation;

                                                    } else {

                                                        throw new FirebaseFirestoreException("Population too high",
                                                                FirebaseFirestoreException.Code.ABORTED);
                                                    }
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<Double>() {
                                                @Override
                                                public void onSuccess(Double aDouble) {

                                                    Toast.makeText(Home.this, "Data is save", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        } else {

                                            Toast.makeText(Home.this, "data is not update plz click again", Toast.LENGTH_SHORT).show();
                                        }

                                        //Toast.makeText(Home.this, "Update doc", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


                }
            });

            holder.btn_view.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {

                    //Toast.makeText(Home.this, "fill view", Toast.LENGTH_SHORT).show();
                    //holder.reView.setText(String.valueOf(ar_data.get(position).getView() + 1));

                    /*HashMap<String, Object> data = new HashMap<>();
                    data.put("uid", mAuth.getCurrentUser().getUid());
                    data.put("category", "third");
                    data.put("like", 0);
                    data.put("view", 1);
                    data.put("comments", 0);
                    data.put("title", "this is third 1");
                    data.put("date", new Timestamp(new Date()));

                    db.collection("Article").document()
                            .set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Toast.makeText(Home.this, "Data is save", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });*/

                    db.collection("Algorithm")
                            .whereEqualTo("uid", mAuth.getCurrentUser().getUid())
                            .whereEqualTo("category", ar_data.get(position).getCategory())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    if (queryDocumentSnapshots.isEmpty()) {

                                        //Toast.makeText(Home.this, "Create new doc", Toast.LENGTH_SHORT).show();
                                        HashMap<String, Object> data = new HashMap<>();
                                        //data.put("articleRef", ar_data.get(position).getRef());
                                        data.put("category", ar_data.get(position).getCategory());
                                        data.put("uid", mAuth.getCurrentUser().getUid());
                                        data.put("like", 0);
                                        data.put("view", 1);
                                        data.put("comments", 0);

                                        db.collection("Algorithm").document()
                                                .set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Toast.makeText(Home.this, "Data is save in algorithm", Toast.LENGTH_SHORT).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    } else {

                                        for (DocumentSnapshot ds : queryDocumentSnapshots) {

                                            docIdView = ds.getId();
                                        }

                                        //Toast.makeText(Home.this, documentId, Toast.LENGTH_SHORT).show();

                                        if (docIdView != null) {

                                            final DocumentReference sfDocRef = db.collection("Algorithm")
                                                    .document(docIdView);

                                            db.runTransaction(new Transaction.Function<Double>() {


                                                @Nullable
                                                @Override
                                                public Double apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                                                    DocumentSnapshot snapshot = transaction.get(sfDocRef);

                                                    double newPopulation = snapshot.getDouble("view") + 1;

                                                    if (newPopulation <= 1000000) {

                                                        transaction.update(sfDocRef, "view", newPopulation);
                                                        return newPopulation;

                                                    } else {

                                                        throw new FirebaseFirestoreException("Population too high",
                                                                FirebaseFirestoreException.Code.ABORTED);
                                                    }
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<Double>() {
                                                @Override
                                                public void onSuccess(Double aDouble) {

                                                    Toast.makeText(Home.this, "Data is save", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        } else {

                                            Toast.makeText(Home.this, "data is not update plz click again", Toast.LENGTH_SHORT).show();
                                        }

                                        //Toast.makeText(Home.this, "Update doc", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


                }

                @Override
                public void unLiked(LikeButton likeButton) {

                    //Toast.makeText(Home.this, "unfill view", Toast.LENGTH_SHORT).show();
                    //holder.reView.setText(String.valueOf(ar_data.get(position).getView()));

                    db.collection("Algorithm")
                            .whereEqualTo("uid", mAuth.getCurrentUser().getUid())
                            .whereEqualTo("category", ar_data.get(position).getCategory())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    if (queryDocumentSnapshots.isEmpty()) {

                                        //There is no need to code
                                        Toast.makeText(Home.this, "Your no data about this article", Toast.LENGTH_SHORT).show();

                                    } else {

                                        for (DocumentSnapshot ds : queryDocumentSnapshots) {

                                            docIdView = ds.getId();
                                        }

                                        //Toast.makeText(Home.this, documentId, Toast.LENGTH_SHORT).show();

                                        if (docIdView != null) {

                                            final DocumentReference sfDocRef = db.collection("Algorithm")
                                                    .document(docIdView);

                                            db.runTransaction(new Transaction.Function<Double>() {


                                                @Nullable
                                                @Override
                                                public Double apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                                                    DocumentSnapshot snapshot = transaction.get(sfDocRef);

                                                    double newPopulation = snapshot.getDouble("view") - 1;

                                                    if (newPopulation <= 1000000) {

                                                        transaction.update(sfDocRef, "view", newPopulation);
                                                        return newPopulation;

                                                    } else {

                                                        throw new FirebaseFirestoreException("Population too high",
                                                                FirebaseFirestoreException.Code.ABORTED);
                                                    }
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<Double>() {
                                                @Override
                                                public void onSuccess(Double aDouble) {

                                                    Toast.makeText(Home.this, "Data is save", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        } else {

                                            Toast.makeText(Home.this, "data is not update plz click again", Toast.LENGTH_SHORT).show();
                                        }

                                        //Toast.makeText(Home.this, "Update doc", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });

            holder.btn_comment.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {

                    //Toast.makeText(Home.this, "Comment on this article", Toast.LENGTH_SHORT).show();


                    db.collection("Algorithm")
                            .whereEqualTo("uid", mAuth.getCurrentUser().getUid())
                            .whereEqualTo("category", ar_data.get(position).getCategory())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    if (queryDocumentSnapshots.isEmpty()) {

                                        //Toast.makeText(Home.this, "Create new doc", Toast.LENGTH_SHORT).show();
                                        HashMap<String, Object> data = new HashMap<>();
                                        //data.put("articleRef", ar_data.get(position).getRef());
                                        data.put("category", ar_data.get(position).getCategory());
                                        data.put("uid", mAuth.getCurrentUser().getUid());
                                        data.put("like", 0);
                                        data.put("view", 0);
                                        data.put("comments", 1);

                                        db.collection("Algorithm").document()
                                                .set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Toast.makeText(Home.this, "Data is save in algorithm", Toast.LENGTH_SHORT).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    } else {

                                        for (DocumentSnapshot ds : queryDocumentSnapshots) {

                                            docIdComment = ds.getId();
                                        }

                                        //Toast.makeText(Home.this, documentId, Toast.LENGTH_SHORT).show();

                                        if (docIdComment != null) {

                                            final DocumentReference sfDocRef = db.collection("Algorithm")
                                                    .document(docIdComment);

                                            db.runTransaction(new Transaction.Function<Double>() {


                                                @Nullable
                                                @Override
                                                public Double apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                                                    DocumentSnapshot snapshot = transaction.get(sfDocRef);

                                                    double newPopulation = snapshot.getDouble("comments") + 1;

                                                    if (newPopulation <= 1000000) {

                                                        transaction.update(sfDocRef, "comments", newPopulation);
                                                        return newPopulation;

                                                    } else {

                                                        throw new FirebaseFirestoreException("Population too high",
                                                                FirebaseFirestoreException.Code.ABORTED);
                                                    }
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<Double>() {
                                                @Override
                                                public void onSuccess(Double aDouble) {

                                                    Toast.makeText(Home.this, "Data is save", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        } else {

                                            Toast.makeText(Home.this, "data is not update plz click again", Toast.LENGTH_SHORT).show();
                                        }

                                        //Toast.makeText(Home.this, "Update doc", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void unLiked(LikeButton likeButton) {

                    //Toast.makeText(Home.this, "remove the comment of this article", Toast.LENGTH_SHORT).show();


                    db.collection("Algorithm")
                            .whereEqualTo("uid", mAuth.getCurrentUser().getUid())
                            .whereEqualTo("category", ar_data.get(position).getCategory())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    if (queryDocumentSnapshots.isEmpty()) {

                                        //There is no need to code
                                        Toast.makeText(Home.this, "Your no data about this article", Toast.LENGTH_SHORT).show();

                                    } else {

                                        for (DocumentSnapshot ds : queryDocumentSnapshots) {

                                            docIdComment = ds.getId();
                                        }

                                        //Toast.makeText(Home.this, documentId, Toast.LENGTH_SHORT).show();

                                        if (docIdComment != null) {

                                            final DocumentReference sfDocRef = db.collection("Algorithm")
                                                    .document(docIdComment);

                                            db.runTransaction(new Transaction.Function<Double>() {


                                                @Nullable
                                                @Override
                                                public Double apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                                                    DocumentSnapshot snapshot = transaction.get(sfDocRef);

                                                    double newPopulation = snapshot.getDouble("comments") - 1;

                                                    if (newPopulation <= 1000000) {

                                                        transaction.update(sfDocRef, "comments", newPopulation);
                                                        return newPopulation;

                                                    } else {

                                                        throw new FirebaseFirestoreException("Population too high",
                                                                FirebaseFirestoreException.Code.ABORTED);
                                                    }
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<Double>() {
                                                @Override
                                                public void onSuccess(Double aDouble) {

                                                    Toast.makeText(Home.this, "Data is save", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        } else {

                                            Toast.makeText(Home.this, "data is not update plz click again", Toast.LENGTH_SHORT).show();
                                        }

                                        //Toast.makeText(Home.this, "Update doc", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });


        }

        @Override
        public int getItemCount() {

            return ar_data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView reCategory, reTitle, reDate, reLike, reView;
            LikeButton btn_heart, btn_view, btn_comment;

            ViewHolder(@NonNull View itemView) {
                super(itemView);

                reCategory = itemView.findViewById(R.id.tvCategory);
                reTitle = itemView.findViewById(R.id.tvTitle);
                reDate = itemView.findViewById(R.id.tvDate);
                btn_heart = itemView.findViewById(R.id.heart_button);
                btn_view = itemView.findViewById(R.id.view_button);
                btn_comment = itemView.findViewById(R.id.thumb_button);
                reLike = itemView.findViewById(R.id.tvLike);
                reView = itemView.findViewById(R.id.tvView);


            }
        }
    }

    private class AdataOverAllArticle extends RecyclerView.Adapter<AdataOverAllArticle.ViewHolder> {

        @NonNull
        @Override
        public AdataOverAllArticle.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.re_articles, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final AdataOverAllArticle.ViewHolder holder, final int position) {

            holder.reCategory.setText(ar_data_OverAll.get(position).getCategory());
            holder.reTitle.setText(ar_data_OverAll.get(position).getTitle());
            holder.reDate.setText(ar_data_OverAll.get(position).getDate().toDate().toString().substring(0, 10));
            holder.reView.setText(String.valueOf(ar_data_OverAll.get(position).getView()));
            holder.reLike.setText(String.valueOf(ar_data_OverAll.get(position).getLike()));


            //Button Code
            holder.btn_heart.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {

                    db.collection("Algorithm")
                            .whereEqualTo("uid", mAuth.getCurrentUser().getUid())
                            .whereEqualTo("category", ar_data_OverAll.get(position).getCategory())
                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            if (queryDocumentSnapshots.isEmpty()) {

                                //Toast.makeText(Home.this, "Create new doc", Toast.LENGTH_SHORT).show();
                                HashMap<String, Object> data = new HashMap<>();
                                //data.put("articleRef", ar_data.get(position).getRef());
                                data.put("category", ar_data_OverAll.get(position).getCategory());
                                data.put("uid", mAuth.getCurrentUser().getUid());
                                data.put("like", 1);
                                data.put("view", 0);
                                data.put("comments", 0);

                                db.collection("Algorithm").document()
                                        .set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(Home.this, "Data is save in algorithm", Toast.LENGTH_SHORT).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } else {

                                for (DocumentSnapshot ds : queryDocumentSnapshots) {

                                    docIdLike = ds.getId();
                                }

                                if (docIdLike != null) {

                                    final DocumentReference sfDocRef = db.collection("Algorithm")
                                            .document(docIdLike);

                                    db.runTransaction(new Transaction.Function<Double>() {


                                        @Nullable
                                        @Override
                                        public Double apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                                            DocumentSnapshot snapshot = transaction.get(sfDocRef);

                                            double newPopulation = snapshot.getDouble("like") + 1;

                                            if (newPopulation <= 1000000) {

                                                transaction.update(sfDocRef, "like", newPopulation);
                                                return newPopulation;

                                            } else {

                                                throw new FirebaseFirestoreException("Population too high",
                                                        FirebaseFirestoreException.Code.ABORTED);
                                            }
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<Double>() {
                                        @Override
                                        public void onSuccess(Double aDouble) {

                                            Toast.makeText(Home.this, "Data is save", Toast.LENGTH_SHORT).show();

                                            //documentIdSecond = null;
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                } else {

                                    Toast.makeText(Home.this, "Data is not Save plz click again", Toast.LENGTH_SHORT).show();
                                }


                                //Toast.makeText(Home.this, "Update doc", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


                }

                @Override
                public void unLiked(LikeButton likeButton) {

                    //holder.reLike.setText(String.valueOf(ar_data_OverAll.get(position).getLike()));

                    db.collection("Algorithm")
                            .whereEqualTo("uid", mAuth.getCurrentUser().getUid())
                            .whereEqualTo("category", ar_data.get(position).getCategory())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    if (queryDocumentSnapshots.isEmpty()) {

                                        //There is no need to code
                                        Toast.makeText(Home.this, "Your no data about this article", Toast.LENGTH_SHORT).show();

                                    } else {

                                        for (DocumentSnapshot ds : queryDocumentSnapshots) {

                                            docIdLike = ds.getId();
                                        }

                                        //Toast.makeText(Home.this, documentId, Toast.LENGTH_SHORT).show();

                                        if (docIdLike != null) {

                                            final DocumentReference sfDocRef = db.collection("Algorithm")
                                                    .document(docIdLike);

                                            db.runTransaction(new Transaction.Function<Double>() {


                                                @Nullable
                                                @Override
                                                public Double apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                                                    DocumentSnapshot snapshot = transaction.get(sfDocRef);

                                                    double newPopulation = snapshot.getDouble("like") - 1;

                                                    if (newPopulation <= 1000000) {

                                                        transaction.update(sfDocRef, "like", newPopulation);
                                                        return newPopulation;

                                                    } else {

                                                        throw new FirebaseFirestoreException("Population too high",
                                                                FirebaseFirestoreException.Code.ABORTED);
                                                    }
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<Double>() {
                                                @Override
                                                public void onSuccess(Double aDouble) {

                                                    Toast.makeText(Home.this, "Data is save", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        } else {

                                            Toast.makeText(Home.this, "data is not update plz click again", Toast.LENGTH_SHORT).show();
                                        }

                                        //Toast.makeText(Home.this, "Update doc", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


                }
            });

            holder.btn_view.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {


                    db.collection("Algorithm")
                            .whereEqualTo("uid", mAuth.getCurrentUser().getUid())
                            .whereEqualTo("category", ar_data_OverAll.get(position).getCategory())
                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            if (queryDocumentSnapshots.isEmpty()) {

                                //Toast.makeText(Home.this, "Create new doc", Toast.LENGTH_SHORT).show();
                                HashMap<String, Object> data = new HashMap<>();
                                //data.put("articleRef", ar_data.get(position).getRef());
                                data.put("category", ar_data_OverAll.get(position).getCategory());
                                data.put("uid", mAuth.getCurrentUser().getUid());
                                data.put("like", 0);
                                data.put("view", 1);
                                data.put("comments", 0);

                                db.collection("Algorithm").document()
                                        .set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(Home.this, "Data is save in algorithm", Toast.LENGTH_SHORT).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } else {

                                for (DocumentSnapshot ds : queryDocumentSnapshots) {

                                    docIdView = ds.getId();
                                }

                                if (docIdView != null) {

                                    final DocumentReference sfDocRef = db.collection("Algorithm")
                                            .document(docIdView);

                                    db.runTransaction(new Transaction.Function<Double>() {


                                        @Nullable
                                        @Override
                                        public Double apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                                            DocumentSnapshot snapshot = transaction.get(sfDocRef);

                                            double newPopulation = snapshot.getDouble("view") + 1;

                                            if (newPopulation <= 1000000) {

                                                transaction.update(sfDocRef, "view", newPopulation);
                                                return newPopulation;

                                            } else {

                                                throw new FirebaseFirestoreException("Population too high",
                                                        FirebaseFirestoreException.Code.ABORTED);
                                            }
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<Double>() {
                                        @Override
                                        public void onSuccess(Double aDouble) {

                                            Toast.makeText(Home.this, "Data is save", Toast.LENGTH_SHORT).show();

                                            //documentIdSecond = null;
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                } else {

                                    Toast.makeText(Home.this, "Data is not Save plz click again", Toast.LENGTH_SHORT).show();
                                }


                                //Toast.makeText(Home.this, "Update doc", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }

                @Override
                public void unLiked(LikeButton likeButton) {

                    db.collection("Algorithm")
                            .whereEqualTo("uid", mAuth.getCurrentUser().getUid())
                            .whereEqualTo("category", ar_data.get(position).getCategory())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    if (queryDocumentSnapshots.isEmpty()) {

                                        //There is no need to code
                                        Toast.makeText(Home.this, "Your no data about this article", Toast.LENGTH_SHORT).show();

                                    } else {

                                        for (DocumentSnapshot ds : queryDocumentSnapshots) {

                                            docIdView = ds.getId();
                                        }

                                        //Toast.makeText(Home.this, documentId, Toast.LENGTH_SHORT).show();

                                        if (docIdView != null) {

                                            final DocumentReference sfDocRef = db.collection("Algorithm")
                                                    .document(docIdView);

                                            db.runTransaction(new Transaction.Function<Double>() {

                                                @Nullable
                                                @Override
                                                public Double apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                                                    DocumentSnapshot snapshot = transaction.get(sfDocRef);

                                                    double newPopulation = snapshot.getDouble("view") - 1;

                                                    if (newPopulation <= 1000000) {

                                                        transaction.update(sfDocRef, "view", newPopulation);
                                                        return newPopulation;

                                                    } else {

                                                        throw new FirebaseFirestoreException("Population too high",
                                                                FirebaseFirestoreException.Code.ABORTED);
                                                    }
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<Double>() {
                                                @Override
                                                public void onSuccess(Double aDouble) {

                                                    Toast.makeText(Home.this, "Data is save", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        } else {

                                            Toast.makeText(Home.this, "data is not update plz click again", Toast.LENGTH_SHORT).show();
                                        }

                                        //Toast.makeText(Home.this, "Update doc", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


                }
            });

            holder.btn_comment.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {

                    //Toast.makeText(Home.this, "Comment on this article", Toast.LENGTH_SHORT).show();


                    db.collection("Algorithm")
                            .whereEqualTo("uid", mAuth.getCurrentUser().getUid())
                            .whereEqualTo("category", ar_data_OverAll.get(position).getCategory())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    if (queryDocumentSnapshots.isEmpty()) {

                                        //Toast.makeText(Home.this, "Create new doc", Toast.LENGTH_SHORT).show();
                                        HashMap<String, Object> data = new HashMap<>();
                                        //data.put("articleRef", ar_data.get(position).getRef());
                                        data.put("category", ar_data_OverAll.get(position).getCategory());
                                        data.put("uid", mAuth.getCurrentUser().getUid());
                                        data.put("like", 0);
                                        data.put("view", 0);
                                        data.put("comments", 1);

                                        db.collection("Algorithm").document()
                                                .set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                Toast.makeText(Home.this, "Data is save in algorithm", Toast.LENGTH_SHORT).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    } else {

                                        for (DocumentSnapshot ds : queryDocumentSnapshots) {

                                            docIdComment = ds.getId();
                                        }

                                        //Toast.makeText(Home.this, documentId, Toast.LENGTH_SHORT).show();

                                        if (docIdComment != null) {

                                            final DocumentReference sfDocRef = db.collection("Algorithm")
                                                    .document(docIdComment);

                                            db.runTransaction(new Transaction.Function<Double>() {


                                                @Nullable
                                                @Override
                                                public Double apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                                                    DocumentSnapshot snapshot = transaction.get(sfDocRef);

                                                    double newPopulation = snapshot.getDouble("comments") + 1;

                                                    if (newPopulation <= 1000000) {

                                                        transaction.update(sfDocRef, "comments", newPopulation);
                                                        return newPopulation;

                                                    } else {

                                                        throw new FirebaseFirestoreException("Population too high",
                                                                FirebaseFirestoreException.Code.ABORTED);
                                                    }
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<Double>() {
                                                @Override
                                                public void onSuccess(Double aDouble) {

                                                    Toast.makeText(Home.this, "Data is save", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        } else {

                                            Toast.makeText(Home.this, "data is not update plz click again", Toast.LENGTH_SHORT).show();
                                        }

                                        //Toast.makeText(Home.this, "Update doc", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void unLiked(LikeButton likeButton) {

                    //Toast.makeText(Home.this, "remove the comment of this article", Toast.LENGTH_SHORT).show();


                    db.collection("Algorithm")
                            .whereEqualTo("uid", mAuth.getCurrentUser().getUid())
                            .whereEqualTo("category", ar_data.get(position).getCategory())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    if (queryDocumentSnapshots.isEmpty()) {

                                        //There is no need to code
                                        Toast.makeText(Home.this, "Your no data about this article", Toast.LENGTH_SHORT).show();

                                    } else {

                                        for (DocumentSnapshot ds : queryDocumentSnapshots) {

                                            docIdComment = ds.getId();
                                        }

                                        //Toast.makeText(Home.this, documentId, Toast.LENGTH_SHORT).show();

                                        if (docIdComment != null) {

                                            final DocumentReference sfDocRef = db.collection("Algorithm")
                                                    .document(docIdComment);

                                            db.runTransaction(new Transaction.Function<Double>() {


                                                @Nullable
                                                @Override
                                                public Double apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                                                    DocumentSnapshot snapshot = transaction.get(sfDocRef);

                                                    double newPopulation = snapshot.getDouble("comments") - 1;

                                                    if (newPopulation <= 1000000) {

                                                        transaction.update(sfDocRef, "comments", newPopulation);
                                                        return newPopulation;

                                                    } else {

                                                        throw new FirebaseFirestoreException("Population too high",
                                                                FirebaseFirestoreException.Code.ABORTED);
                                                    }
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<Double>() {
                                                @Override
                                                public void onSuccess(Double aDouble) {

                                                    Toast.makeText(Home.this, "Data is save", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        } else {

                                            Toast.makeText(Home.this, "data is not update plz click again", Toast.LENGTH_SHORT).show();
                                        }

                                        //Toast.makeText(Home.this, "Update doc", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });


        }

        @Override
        public int getItemCount() {
            return ar_data_OverAll.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView reCategory, reTitle, reDate, reLike, reView;
            LikeButton btn_heart, btn_view, btn_comment;

            ViewHolder(View view) {
                super(view);

                reCategory = itemView.findViewById(R.id.tvCategory);
                reTitle = itemView.findViewById(R.id.tvTitle);
                reDate = itemView.findViewById(R.id.tvDate);
                btn_heart = itemView.findViewById(R.id.heart_button);
                btn_view = itemView.findViewById(R.id.view_button);
                btn_comment = itemView.findViewById(R.id.thumb_button);
                reLike = itemView.findViewById(R.id.tvLike);
                reView = itemView.findViewById(R.id.tvView);

            }
        }
    }


}
