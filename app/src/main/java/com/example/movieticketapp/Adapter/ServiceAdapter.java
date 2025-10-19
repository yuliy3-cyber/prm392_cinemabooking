package com.example.movieticketapp.Adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieticketapp.Activity.Ticket.Service.AddService;
import com.example.movieticketapp.Model.Service;
import com.example.movieticketapp.Model.Users;
import com.example.movieticketapp.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {

    private List<Service> services;

    public ServiceAdapter(List<Service> services) {
        this.services = services;
    }

    @NonNull
    @Override
    public ServiceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.service_item, parent, false);
        return new ServiceAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Service service = services.get(position);
        holder.name.setText(service.getName());
        holder.price.setText(service.getPrice() + " VNĐ");
        holder.detail.setText(service.getDetail());

        Picasso.get().load(service.getImage()).fit().centerCrop().into(holder.image);

        try {
            Log.d("account type", Users.currentUser.getAccountType());
            if (Users.currentUser != null && !"admin".equals(Users.currentUser.getAccountType())) {
                holder.serviceMenu.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            holder.serviceMenu.setVisibility(View.INVISIBLE);
        }

        holder.serviceMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.serviceMenu);
            popup.inflate(R.menu.promo_menu);

            SpannableString edit = new SpannableString("Edit");
            edit.setSpan(new ForegroundColorSpan(Color.BLACK), 0, edit.length(), 0);
            popup.getMenu().getItem(0).setTitle(edit);

            SpannableString delete = new SpannableString("Delete");
            delete.setSpan(new ForegroundColorSpan(Color.RED), 0, delete.length(), 0);
            popup.getMenu().getItem(1).setTitle(delete);

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.promo_edit) {
                    Intent i = new Intent(holder.itemView.getContext(), AddService.class);
                    i.putExtra("service", service);
                    holder.itemView.getContext().startActivity(i);
                    return true;

                } else if (id == R.id.promo_delete) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(holder.itemView.getContext(), R.style.CustomAlertDialog);
                    LayoutInflater factory = LayoutInflater.from(holder.itemView.getContext());
                    final View deleteDialogView = factory.inflate(R.layout.yes_no_dialog, null);
                    TextView message = deleteDialogView.findViewById(R.id.message);
                    message.setText("Do you sure to delete the service ?");
                    alertDialog.setView(deleteDialogView);
                    AlertDialog OptionDialog = alertDialog.create();
                    OptionDialog.show();

                    TextView Cancel = deleteDialogView.findViewById(R.id.Cancel_Button);
                    TextView Delete = deleteDialogView.findViewById(R.id.DeleteButton);

                    Delete.setOnClickListener(view -> {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        CollectionReference ServiceRef = db.collection("Service");
                        ServiceRef.document(service.getID()).delete();
                        OptionDialog.dismiss();
                    });

                    Cancel.setOnClickListener(view -> OptionDialog.dismiss());

                    return true;
                } else {
                    return false;
                }
            });

            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView detail, price, name;
        ImageView image, serviceMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            detail = itemView.findViewById(R.id.serviceDetail);
            price = itemView.findViewById(R.id.servicePrice);
            name = itemView.findViewById(R.id.serviceName);
            image = itemView.findViewById(R.id.serviceImage);
            serviceMenu = itemView.findViewById(R.id.serviceMenu);
        }
    }
}
