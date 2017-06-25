package com.phuang.pizzame;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.phuang.pizzame.model.Store;

import java.util.List;

public class StoreListAdapter extends RecyclerView.Adapter<StoreListAdapter.ViewHolder>
        implements View.OnClickListener {

    private RecyclerView mRecyclerView;

    private List<Store> mStores;

    private StoreListFragment.OnItemSelectedListener mItemSelectedListener;

    public StoreListAdapter(List<Store> stores,
                            StoreListFragment.OnItemSelectedListener itemSelectedListener) {
        mStores = stores;
        mItemSelectedListener = itemSelectedListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_store, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Store store = mStores.get(position);
        holder.mItem = store;
        holder.mTitle.setText(store.Title);
        holder.mAddress.setText(store.Address + " " + store.City + " " + store.State);
        holder.mPhone.setText(store.Phone);
        holder.mDistance.setText(store.Distance + " "
                + holder.mTitle.getContext().getResources().getString(R.string.distance_unit));
    }

    @Override
    public int getItemCount() {
        return mStores == null ? 0 : mStores.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }

    public void setItems(List<Store> stores) {
        mStores = stores;
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (mItemSelectedListener != null && mRecyclerView != null) {
            ViewHolder holder = (ViewHolder) mRecyclerView.getChildViewHolder(v);
            mItemSelectedListener.onItemSelected(holder.mItem);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        Store mItem;
        TextView mTitle;
        TextView mAddress;
        TextView mPhone;
        TextView mDistance;

        public ViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mAddress = (TextView) itemView.findViewById(R.id.address);
            mPhone = (TextView) itemView.findViewById(R.id.phone);
            mDistance = (TextView) itemView.findViewById(R.id.distance);
        }
    }
}
