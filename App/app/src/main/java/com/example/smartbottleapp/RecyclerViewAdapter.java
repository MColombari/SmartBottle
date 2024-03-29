package com.example.smartbottleapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    ArrayList<ElementRecycleView> elementRecycleViewArrayList;

    public RecyclerViewAdapter() {
        this.elementRecycleViewArrayList = new ArrayList<>();
    }

    public RecyclerViewAdapter(ArrayList<ElementRecycleView> elementRecycleViewArrayList) {
        this.elementRecycleViewArrayList = elementRecycleViewArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ElementRecycleView elementRecycleView = elementRecycleViewArrayList.get(position);
        holder.getNameView().setText(elementRecycleView.name);
        holder.getLocationView().setText(elementRecycleView.location);
        holder.getIsBusyView().setText(elementRecycleView.is_busy? "Is Busy" : "");
    }

    @Override
    public int getItemCount() {
        return elementRecycleViewArrayList.size();
    }

    public void addElement(ElementRecycleView e){
        elementRecycleViewArrayList.add(e);
        notifyItemInserted(getItemCount() - 1);
    }

    public void removeAllElement(){
        elementRecycleViewArrayList = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addElements(ArrayList<ElementRecycleView> es){
        elementRecycleViewArrayList.addAll(es);
        notifyDataSetChanged();
    }

    public void setNewData(ArrayList<ElementRecycleView> es){
        elementRecycleViewArrayList = es;
        notifyDataSetChanged();
    }

    public void setElementBusy(int position){
        if(getItemCount() > position){
            // Check for range.
            elementRecycleViewArrayList.get(position).is_busy = true;
            notifyItemChanged(position);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameView;
        private TextView locationView;
        private TextView isBusyView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameView = (TextView) itemView.findViewById(R.id.RR_name);
            locationView = (TextView) itemView.findViewById(R.id.RR_location);
            isBusyView = (TextView) itemView.findViewById(R.id.RR_is_busy);
        }

        public TextView getNameView() { return nameView; }

        public TextView getLocationView() { return locationView; }

        public TextView getIsBusyView() { return isBusyView; }
    }
}
