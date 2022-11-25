package com.example.trymapbox;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mapbox.search.result.SearchSuggestion;

import java.util.ArrayList;

public class SearchedPlaceRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    ArrayList<SearchSuggestion> suggestionPlaceList;
    RecyclerViewAdapterOnItemClickListener onItemClickListener;

    public SearchedPlaceRecyclerViewAdapter(ArrayList<SearchSuggestion> list, RecyclerViewAdapterOnItemClickListener listener){
        suggestionPlaceList = list;
        onItemClickListener = listener;
    }

    @Override
    public SearchedPlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_name_and_address, parent, false);
        return new SearchedPlaceViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position){
        SearchSuggestion place = suggestionPlaceList.get(position);
        SearchedPlaceViewHolder viewHolder = (SearchedPlaceViewHolder) holder;
        viewHolder.setPlaceName(place.getName());
        if(place.getAddress() != null)
            viewHolder.setPlaceAddress(place.getAddress().formattedAddress());
        else
            viewHolder.setPlaceAddress("");
    }

    @Override
    public int getItemCount(){
        return suggestionPlaceList.size();
    }

    public void replaceDataList(ArrayList<SearchSuggestion> list){
        suggestionPlaceList = list;
    }

    class SearchedPlaceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final TextView placeName;
        public final TextView placeAddress;
        private RecyclerViewAdapterOnItemClickListener itemClickListener;

        public SearchedPlaceViewHolder(View view, RecyclerViewAdapterOnItemClickListener listener){
            super(view);
            placeName = (TextView) view.findViewById(R.id.name);
            placeAddress = (TextView) view.findViewById(R.id.address);
            itemClickListener = listener;
            view.setOnClickListener(this);
        }

        public void setPlaceName(String name){
            placeName.setText(name);
        }

        public void setPlaceAddress(String address){
            placeAddress.setText(address);
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onItemClick(v, getAdapterPosition());
        }
    }



}