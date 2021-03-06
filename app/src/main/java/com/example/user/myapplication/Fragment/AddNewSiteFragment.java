package com.example.user.myapplication.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.user.myapplication.Fragment.Interface.IAddNewSiteListener;
import com.example.user.myapplication.Fragment.Interface.IRegisterListener;
import com.example.user.myapplication.R;
import com.example.user.myapplication.Activity.MainActivity;
import com.example.user.myapplication.Model.RSSFeed;
import com.example.user.myapplication.Utils.Parser;
import com.example.user.myapplication.Utils.RSSParser;

import java.util.List;

import androidx.fragment.app.Fragment;
import es.dmoral.toasty.Toasty;


public class AddNewSiteFragment extends Fragment {

    private View view;
    private EditText editTextUrl;

    private ProgressDialog pDialog;

    private RSSFeed rssFeed;

    private IAddNewSiteListener addNewSiteListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_new_site, container, false);
        initializeView();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            addNewSiteListener = (IAddNewSiteListener) context;
        } catch (ClassCastException ignored) {
        }
    }

    private void initializeView(){
        Button submit = view.findViewById(R.id.new_site_btnSubmit);
        Button cancel = view.findViewById(R.id.new_site_btnCancel);

        editTextUrl = view.findViewById(R.id.new_site_txtUrl);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = editTextUrl.getText().toString();

                int valid_val = Parser.ValidUrl(url);

                if(valid_val == 0){
                    new loadRSSFeed().execute(url);
                } else if(valid_val == 1){
                    onErrorMessage("Please enter a valid url");
                } else {
                    onErrorMessage("Please enter website url");
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewSiteListener.onSubmitSiteClick();
            }
        });

    }


    private void onErrorMessage(String message) {
        Toasty.error(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    class loadRSSFeed extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getContext());
            pDialog.setMessage("Fetching RSS Information ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }


        @Override
        protected String doInBackground(String... args) {
            String url = args[0];
            rssFeed = RSSParser.getRSSFeed(url);
            if (rssFeed == null) {
                onErrorMessage("Rss url not found. Please check the url or try again");
            }
            return null;
        }


        protected void onPostExecute(String args) {
            if (rssFeed != null) {
                List<RSSFeed> rssFeeds = RSSFeed.find(RSSFeed.class, "rsslink = ?", rssFeed.getRSSLink());

                if (rssFeeds.size() == 0)
                    rssFeed.save();
            }
            addNewSiteListener.onSubmitSiteClick();
            pDialog.dismiss();
        }

    }

}
