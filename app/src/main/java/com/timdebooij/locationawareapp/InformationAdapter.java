package com.timdebooij.locationawareapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timdebooij.locationawareapp.Entities.DepartureInformation;

import java.util.ArrayList;

public class InformationAdapter extends RecyclerView.Adapter<InformationAdapter.InformationViewHolder> {

    public Context context;
    public ArrayList<DepartureInformation> departureInformation;

    public InformationAdapter(Context context, ArrayList<DepartureInformation> departureInformation){
        this.context = context;
        this.departureInformation = departureInformation;
    }

    @NonNull
    @Override
    public InformationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_recyclerview_item, parent, false);
        return new InformationAdapter.InformationViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull InformationViewHolder holder, int position) {
        DepartureInformation dep = departureInformation.get(position);
        holder.time.setText(dep.getTime());
        holder.endstation.setText(dep.endStation);
        holder.stops.setText("via: " + dep.stopStations);
        holder.delay.setText(dep.departureDelay);
        holder.track.setText("Track: " + dep.departureTrack);
    }

    @Override
    public int getItemCount() {
        return departureInformation.size();
    }

    public class InformationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView time;
        public TextView endstation;
        public TextView stops;
        public TextView delay;
        public TextView track;

        public InformationViewHolder(View itemView, final Context context) {
            super(itemView);
            time = itemView.findViewById(R.id.timeInformationTextView);
            endstation = itemView.findViewById(R.id.informationEndstationTextView);
            stops = itemView.findViewById(R.id.informationStopsTextView);
            delay = itemView.findViewById(R.id.informationDelayTextView);
            track = itemView.findViewById(R.id.informationTrackTextView);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
