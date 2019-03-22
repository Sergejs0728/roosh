package android.feed.com.rssfeeds;

import android.content.Intent;
import android.net.Uri;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;

import java.util.List;

/**
 * Created by navi on 07/03/2018.
 */

public class AdapterFeeds extends BaseAdapter{

    private List<RssFeedModel> mFeedModelList;
    private static LayoutInflater inflater = null;
    private Boolean isPodcast;


    public AdapterFeeds(List<RssFeedModel> modelList,Boolean isPodcast){

        this.mFeedModelList = modelList;
        this.isPodcast = isPodcast;


//        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }



    @Override
    public int getCount() {

        return mFeedModelList.size();
//        return  10;


    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, final ViewGroup viewGroup) {

        final View vi;

        if(isPodcast){
            vi = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_podcast, viewGroup,false);

        }else{
            vi = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, viewGroup,false);
        }


        final Button btn_title = (Button) vi.findViewById(R.id.btn_title);
        final ImageButton img_photo = (ImageButton)vi.findViewById(R.id.img_image);

        final String txt_button = mFeedModelList.get(i).title;
        btn_title.setText(txt_button);



        btn_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mFeedModelList.get(i).link.equals("")) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mFeedModelList.get(i).link));
                    viewGroup.getContext().startActivity(browserIntent);
                }
            }
        });
        img_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_title.performClick();
            }
        });

//        if(Jsoup.parse(mFeedModelList.get(0).content).select("img").attr("src").equals("")){
//
//        }else{
//
//        }
//        String mailurl = Jsoup.parse(mFeedModelList.get(i).content).select("img").attr("src");
        Glide.with(viewGroup.getContext()).load(mFeedModelList.get(i).imgURL).into(img_photo);


        return vi;
    }
}
