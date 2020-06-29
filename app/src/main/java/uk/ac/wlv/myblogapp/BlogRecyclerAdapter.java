package uk.ac.wlv.myblogapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * This is where connection made with the Fragments
 * */


public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    private List<BlogPost> blog_list;
    private Context context;
    private FirebaseFirestore firebaseFirestore;

    private TextView blogUserName;
    private TextView blogDate;
    private CircleImageView blogUserImage;

    // receive list to the adapter
    public BlogRecyclerAdapter(List<BlogPost> blog_list){

        this.blog_list = blog_list;

    }

    // three methods are require for adapter
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item,parent, false );
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        // using View holder class we receive all data from the database and setting up values to the layout
        //String blogPostId = blog_list.get(position).BlogPostId;

        String desc_data = blog_list.get(position).getDesc();
        holder.setDescText(desc_data);

        String image_url = blog_list.get(position).getImage();
        holder.setBlogImage(image_url);


        // get the Date and Time from database and arrange new date time formate
        String postDate = blog_list.get(position).getDate();
        String postTime = blog_list.get(position).getTime();
        String dateTime = postDate + " at "+postTime;
        holder.setDate(dateTime);

        String user_id = blog_list.get(position).getUser_id();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    String username = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");

                    holder.setUserData(username,userImage);

                }else{


                }
            }
        });

        // Like Feature
        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView descView;
        private ImageView blogImageView;

        private ImageView blogLikeBtn;
        private TextView blogLikeCount;

        // this constructor require for this class
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            blogLikeBtn = mView.findViewById(R.id.blog_like_btn);
        }

        public void setDescText(String descText){

            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(descText);
        }

        public void setBlogImage(String downloadUri){

            blogImageView = mView.findViewById(R.id.blog_image);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.rectangle);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).into(blogImageView);
        }

        public void setDate(String postDate){

            blogDate = mView.findViewById(R.id.blog_date);
            blogDate.setText(postDate);

        }

        public void setUserData(String name, String image){

            blogUserName = mView.findViewById(R.id.blog_user_name);
            blogUserImage = mView.findViewById(R.id.blog_user_image);

            blogUserName.setText(name);

            RequestOptions placeHolderOptions = new RequestOptions();
            placeHolderOptions.placeholder(R.drawable.default_image);
            Glide.with(context).applyDefaultRequestOptions(placeHolderOptions).load(image).into(blogUserImage);

        }
    }
}
