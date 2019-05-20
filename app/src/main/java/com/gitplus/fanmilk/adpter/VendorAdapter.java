package com.gitplus.fanmilk.adpter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gitplus.fanmilk.AddNewVendorActivity;
import com.gitplus.fanmilk.DailyReportActivity;
import com.gitplus.fanmilk.R;
import com.gitplus.fanmilk.model.Vendor;

import java.util.List;

public class VendorAdapter extends RecyclerView.Adapter<VendorAdapter.MyViewHolder> {


    private List<Vendor> list;
    private Activity context;

    public interface IProcessFilter {
        void onProcessFilter(String vendor_code, int position);
    }

    private IProcessFilter mCallback;

    public VendorAdapter(Activity context, List<Vendor> list, IProcessFilter callback) {
        this.context = context;
        this.list = list;
        this.mCallback = callback;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vendor, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final Vendor vendor = list.get(position);

        holder.name.setText(vendor.getVendor_name());
        holder.phone.setText(vendor.getPhone_main());
        holder.amount.setText(vendor.getAmount());

        Glide.with(context)
                .load(vendor.getImage())
                .into(holder.image);

        holder.sale.setOnClickListener(v -> {
            Intent intent = new Intent(context, DailyReportActivity.class);
            intent.putExtra("vendor_code",vendor.getVendor_code());
            intent.putExtra("vendor_name",vendor.getVendor_name());
            intent.putExtra("vendor_image",vendor.getImage());
            intent.putExtra("position",holder.getAdapterPosition());
            context.startActivityForResult(intent,1);

        });


        holder.edit.setOnClickListener(v -> context.startActivity(new Intent(context, AddNewVendorActivity.class)));

        holder.delete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        mCallback.onProcessFilter(vendor.getVendor_code(), holder.getAdapterPosition());
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            };

            builder.setMessage("Are you to delete ?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener)
                    .show();
        });
    }

    public Vendor getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView phone;
        private TextView amount;
        private ImageView image;
        private RelativeLayout sale, edit, delete;
        private View view;

        MyViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            phone = itemView.findViewById(R.id.phone);
            phone = itemView.findViewById(R.id.phone);
            amount = itemView.findViewById(R.id.amount);
            image = itemView.findViewById(R.id.image);
            sale = itemView.findViewById(R.id.sale);
            edit = itemView.findViewById(R.id.edit);
            delete = itemView.findViewById(R.id.delete);
            view = itemView;
        }
    }
}



