package uk.ac.wlv.myblogapp;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 *
 * this is the class which we retrieve all Blog Post
 */
public class HomeFragment extends Fragment {

    private RecyclerView blog_list_view;
    private List<BlogPost> blog_List;
    private FirebaseAuth  firebaseAuth;

    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home,container,false);

        blog_List = new ArrayList<>();
        blog_list_view = view.findViewById(R.id.blog_list_view);

        firebaseAuth = FirebaseAuth.getInstance();

        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_List);

        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        // setting adapter to view
        blog_list_view.setAdapter(blogRecyclerAdapter);

        if(firebaseAuth.getCurrentUser() != null) {


            firebaseFirestore = FirebaseFirestore.getInstance();

            Query querySnapshot = firebaseFirestore.collection("Posts").orderBy("time", Query.Direction.DESCENDING);

            querySnapshot.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String blogPostId = doc.getDocument().getId();

                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);
                            blog_List.add(blogPost);

                            blogRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }

        // Inflate the layout for this fragment
        return view;
    }

}
