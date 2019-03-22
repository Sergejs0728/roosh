package android.feed.com.rssfeeds;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PodcastFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PodcastFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PodcastFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    AppController appController = AppController.getInstance();

    ListView lst_podcast;

    private String mFeedTitle;
    private String mFeedLink;
    private String mFeedDescription;

    ArrayList<String> podcastTitleArray = new ArrayList<String>();

    String urlLink = "http://feeds.soundcloud.com/users/soundcloud:users:253651639/sounds.rss";
    private List<RssFeedModel> mFeedModelList;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public PodcastFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PodcaseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PodcastFragment newInstance(String param1, String param2) {
        PodcastFragment fragment = new PodcastFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_podcast, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        lst_podcast = (ListView)view.findViewById(R.id.lst_podcast);
        new FetchFeedTask().execute((Void) null);

    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {



        @Override
        protected void onPreExecute() {
//        mSwipeLayout.setRefreshing(true);
//        urlLink = mEditText.getText().toString();



        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (TextUtils.isEmpty(urlLink))
                return false;

            try {
                if(!urlLink.startsWith("http://") && !urlLink.startsWith("https://"))
                    urlLink = "http://" + urlLink;

                URL url = new URL(urlLink);
                InputStream inputStream = url.openConnection().getInputStream();
                mFeedModelList = parseFeed(inputStream);
                return true;
            } catch (IOException e) {
                Log.e("", "Error", e);
            } catch (XmlPullParserException e) {
                Log.e("", "Error", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {

            if (success) {


                appController.setPodcastFeedModelList(mFeedModelList);

                AdapterFeeds adapterFeeds = new AdapterFeeds(mFeedModelList,true);
                lst_podcast.setAdapter(adapterFeeds);


                SharedPreferences sharedPreferences = getContext().getSharedPreferences("", MODE_PRIVATE);
                int cnt = sharedPreferences.getInt("cnt_podcast",0);

                if(cnt == 0){
                    SharedPreferences.Editor editor = getContext().getSharedPreferences("", MODE_PRIVATE).edit();
                    editor.putInt("cnt_podcast", mFeedModelList.size());
                    editor.commit();

                    for(int i=0;i<mFeedModelList.size();i++){

                        podcastTitleArray.add(mFeedModelList.get(i).title);
                    }
                    saveArray(podcastTitleArray,"podcastArray",getContext());
                }





            } else {
//                Toast.makeText(getActivity(),
//                        "Enter a valid Rss feed url",
//                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean saveArray(ArrayList<String> array, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName +"_size", array.size());
        for(int i=0;i<array.size();i++)
            editor.putString(arrayName + "_" + i, array.get(i));
        return editor.commit();
    }
    public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException,
            IOException {
        String title = null;
        String link = null;
        String pubDate = null;
        String imgURL = null;
        boolean isItem = false;
        boolean flag = false;
        List<RssFeedModel> items = new ArrayList<>();
        List<String> hrArray = new ArrayList<>();

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if(name == null)
                    continue;

                if(eventType == XmlPullParser.END_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }
                Log.d("MyXmlParser", "Parsing name ==> " + name);
                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase("link")) {
                    link = result;

                }
                else if (name.equalsIgnoreCase("pubDate")) {
                    pubDate = result;
                }
                else if (name.equalsIgnoreCase("title")) {
                    title = result;
                }
                else if (name.equalsIgnoreCase("itunes:image")) {
                    imgURL = xmlPullParser.getAttributeValue(null, "href");
                }

                if (title != null && link != null && pubDate != null && imgURL != null) {
                    if(isItem) {
                        RssFeedModel item = new RssFeedModel(title, link, pubDate,imgURL);
                        items.add(item);


                    }
                    else {
                        mFeedTitle = title;
                        mFeedLink = link;
                        mFeedDescription = pubDate;
                    }

                    title = null;
                    link = null;
                    pubDate = null;
                    imgURL = null;
                    isItem = false;
                }
            }

            return items;
        } finally {
            inputStream.close();
        }
    }
}
