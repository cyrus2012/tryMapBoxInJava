package com.example.trymapbox;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.mapbox.geojson.Point;
import com.mapbox.search.MapboxSearchSdk;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.SearchEngineSettings;
import com.mapbox.search.common.AsyncOperationTask;
import com.mapbox.search.result.SearchAddress;
import com.mapbox.search.result.SearchResult;
import com.mapbox.search.result.SearchSuggestion;


import com.mapbox.search.SearchEngine;
import com.mapbox.search.SearchOptions;
import com.mapbox.search.SearchSelectionCallback;

import com.example.trymapbox.databinding.FragmentSearchPlaceBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

//refer to https://docs.mapbox.com/android/search/examples/search/
public class SearchPlaceFragment extends Fragment {
    private FragmentSearchPlaceBinding binding;

    private SearchEngine searchEngine;
    private SearchOptions searchOption;
    private ArrayList<SearchSuggestion> suggestionPlacesList = new ArrayList<>();
    private AsyncOperationTask suggestionSearchRequestTask;
    private SearchedPlaceRecyclerViewAdapter searchPlaceAdapter;


    public SearchPlaceFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchEngine = MapboxSearchSdk.createSearchEngineWithBuiltInDataProviders(
                new SearchEngineSettings(getString(R.string.mapbox_access_token))
        );

        searchOption = new SearchOptions.Builder()
                .limit(6)   //set maximum 6 place of suggestions return
                .build();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSearchPlaceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.searchTextInput.addTextChangedListener(textChangedListener);
        int scrollPosition = 0;
        RecyclerView recyclerView = binding.suggestionPlaceRecyclerView;
        if(recyclerView.getLayoutManager() != null){
            scrollPosition = ((LinearLayoutManager) recyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.scrollToPosition(scrollPosition);

        searchPlaceAdapter = new SearchedPlaceRecyclerViewAdapter(suggestionPlacesList, searchedPlaceAdapterOnItemClickListener);
        recyclerView.setAdapter(searchPlaceAdapter);
    }


    @Override
    public void onDestroy(){
        suggestionSearchRequestTask.cancel();
        super.onDestroy();
    }

    TextWatcher textChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //searchResultsView.search(s.toString());
            if(suggestionSearchRequestTask != null)
                suggestionSearchRequestTask.cancel();
            String newText = s.toString().trim();
            if(!TextUtils.isEmpty(newText)) {
                if(TextUtils.getTrimmedLength(newText) > 2) { //only search where 3 or more characters have been input
                    Log.d("SearchPlace", "start to search keywords");
                    suggestionSearchRequestTask = searchEngine.search(newText, searchOption, searchCallback);
                }
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private final SearchSelectionCallback searchCallback = new
            SearchSelectionCallback(){
                @Override
                public void onSuggestions(@NonNull List<SearchSuggestion> suggestions, @NonNull  ResponseInfo responseInfo){
                    suggestionPlacesList.clear();
                    if(suggestions.isEmpty()){
                        Log.i("Search Fragment", "No suggestions found");
                    }else{
                        System.out.println("Number of suggestion got: " + suggestions.size());
                        suggestionPlacesList.addAll(suggestions);
                        searchPlaceAdapter.replaceDataList(suggestionPlacesList);
                        searchPlaceAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onResult(@NonNull SearchSuggestion suggestion, @NonNull SearchResult result, @NonNull ResponseInfo info) {
                    Point selectPoint = null;
                    if(result.getRoutablePoints() != null) {
                        if(result.getRoutablePoints().size() != 0){
                            selectPoint = result.getRoutablePoints().get(0).getPoint();

                        }
                    }
                    if(selectPoint == null)
                        selectPoint = result.getCoordinate();

                    System.out.println(result.getName() + " Latitude: " + selectPoint.latitude() + ", Longitude: " + selectPoint.longitude() );
                    Toast.makeText(getContext(),result.getName() + " Latitude: " + selectPoint.latitude() +
                            ", Longitude: " + selectPoint.longitude(), Toast.LENGTH_SHORT ).show();
                }

                @Override
                public void onCategoryResult(@NonNull SearchSuggestion suggestion, @NonNull List<? extends SearchResult> results, @NonNull ResponseInfo responseInfo) {
                    Log.i("SearchApiExample", "Category search results: " + results);

                }

                @Override
                public void onError(@NonNull Exception e) {
                    Log.i("SearchApiExample", "Search error: ", e);
                }
            };

    RecyclerViewAdapterOnItemClickListener searchedPlaceAdapterOnItemClickListener = new RecyclerViewAdapterOnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if(suggestionPlacesList == null)
                return;

           if(position == RecyclerView.NO_POSITION)
               return;

           System.out.println(suggestionPlacesList.get(position).getName() + " is clicked");
           suggestionSearchRequestTask.cancel();
           searchEngine.select(suggestionPlacesList.get(position), searchCallback);

        }
    };


}