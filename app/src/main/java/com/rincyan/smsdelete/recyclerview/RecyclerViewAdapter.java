package com.rincyan.smsdelete.recyclerview;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rincyan.smsdelete.R;

import java.util.List;

/**
 * Created by rin on 2017/6/15.
 *
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.SmsViewHolder> {
    private List<SMS> sms;
    private OnItemClickListener onItemClickListener;

    public RecyclerViewAdapter(List<SMS> sms) {
        this.sms = sms;
    }

    //item click
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
    //end item click

    static class SmsViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView smsNum;
        TextView smsBody;
        TextView smsDate;

        SmsViewHolder(final View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardview);
            smsNum = itemView.findViewById(R.id.smsnumber);
            smsBody = itemView.findViewById(R.id.smsbody);
            smsDate = itemView.findViewById(R.id.smsdate);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public SmsViewHolder onCreateViewHolder(final ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.smslist, viewGroup, false);
        SmsViewHolder pvh = new SmsViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final SmsViewHolder holder, final int i) {
        if (onItemClickListener != null) {
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(holder.itemView, i);
                }
            });
        }
        holder.smsNum.setText(sms.get(i).getNum());
        holder.smsBody.setText(sms.get(i).getBody());
        holder.smsDate.setText(sms.get(i).getDate());
    }

    @Override
    public int getItemCount() {
        return sms.size();
    }
}
