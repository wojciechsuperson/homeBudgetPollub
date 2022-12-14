package com.example.homebudgetpollub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;
import org.joda.time.Weeks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BudgetActivity extends AppCompatActivity {
    
    private FloatingActionButton fab;

    private DatabaseReference budgetReference, personalRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loader;

    private TextView totalBudgetAmountTextView;
    private RecyclerView recyclerView;

    private String post_key = "";
    private String item = "";
    private int amount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        mAuth = FirebaseAuth.getInstance();
        budgetReference = FirebaseDatabase.getInstance().getReference().child("budget").child(mAuth.getCurrentUser().getUid());
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());
        loader = new ProgressDialog(this);
        
        totalBudgetAmountTextView = findViewById(R.id.totalBudgetAmountTextView);
        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem();
            }
        });

        budgetReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int pTotal = 0;
                int dayRatio = 0, weekRatio = 0, monthRatio = 0;
                if(snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Data data = snap.getValue(Data.class);
                        pTotal += data.getAmount();
                    }
                    String sTotal = String.valueOf("Month budget: $" + pTotal);
                    totalBudgetAmountTextView.setText(sTotal);

                    dayRatio = pTotal / 30;
                    weekRatio = pTotal / 4;
                    monthRatio = pTotal;
                }

                personalRef.child("dayBudget").setValue(dayRatio);
                personalRef.child("weekBudget").setValue(weekRatio);
                personalRef.child("budget").setValue(monthRatio);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        getMonthTransportBudgetRatios();
        getMonthFoodBudgetRatios();
        getMonthHouseBudgetRatios();
        getMonthEntertainmentBudgetRatios();
        getMonthEducationBudgetRatios();
        getMonthCharityBudgetRatios();
        getMonthApparelBudgetRatios();
        getMonthHealthBudgetRatios();
        getMonthPersonalBudgetRatios();
        getMonthOtherBudgetRatios();

    }


    private void addItem() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.input_layout, null);
        myDialog.setView(view);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final Spinner itemSpinner = view.findViewById(R.id.itemsSpinner);
        final EditText amount = view.findViewById(R.id.amount);
        final EditText note = view.findViewById(R.id.note);
        final Button save = view.findViewById(R.id.save);
        final Button cancel = view.findViewById(R.id.cancel);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String budgetAmount = amount.getText().toString();
                String budgetItem = itemSpinner.getSelectedItem().toString();

                if(TextUtils.isEmpty(budgetAmount)) {
                    amount.setError("Amount is required!");
                    return;
                }

                if("Select item".equals(budgetItem)) {
                    Toast.makeText(BudgetActivity.this, "Select a valid item", Toast.LENGTH_SHORT).show();
                    return;
                }

                loader.setMessage("adding a budget item");
                loader.setCanceledOnTouchOutside(false);
                loader.show();

                String id = budgetReference.push().getKey();
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Calendar cal = Calendar.getInstance();
                String date = dateFormat.format(cal.getTime());

                MutableDateTime epoch = new MutableDateTime();
                epoch.setDate(0);
                DateTime now = new DateTime();
                Weeks weeks = Weeks.weeksBetween(epoch,now);
                Months months = Months.monthsBetween(epoch, now);

                String itemNday = budgetItem + date;
                String itemNweek = budgetItem + weeks.getWeeks();
                String itemNmonth = budgetItem + months.getMonths();


                Data data = new Data(budgetItem, date, id, itemNday, itemNweek, itemNmonth,  Integer.parseInt(budgetAmount), months.getMonths(), weeks.getWeeks(),null);
                budgetReference.child(id).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(BudgetActivity.this, "Budget item added successfuly", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }

                        loader.dismiss();
                    }
                });
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(budgetReference, Data.class)
                .build();

        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Data model) {
                holder.setItemAmount("Allocated amount: $" + model.getAmount());
                holder.setItemDate("On: " + model.getDate());
                holder.setItemName("Category: " + model.getItem());

                holder.notes.setVisibility(View.GONE);

                switch (model.getItem()) {
                    case "Transport":
                        holder.imageView.setImageResource(R.drawable.ic_transport);
                        break;
                    case "Food":
                        holder.imageView.setImageResource(R.drawable.ic_food);
                        break;
                    case "House":
                        holder.imageView.setImageResource(R.drawable.ic_house);
                        break;
                    case "Entertainment":
                        holder.imageView.setImageResource(R.drawable.ic_entertainment);
                        break;
                    case "Education":
                        holder.imageView.setImageResource(R.drawable.ic_education);
                        break;
                    case "Charity":
                        holder.imageView.setImageResource(R.drawable.ic_consultancy);
                        break;
                    case "Apparel":
                        holder.imageView.setImageResource(R.drawable.ic_shirt);
                        break;
                    case "Health":
                        holder.imageView.setImageResource(R.drawable.ic_health);
                        break;
                    case "Personal":
                        holder.imageView.setImageResource(R.drawable.ic_personalcare);
                        break;
                    case "Other":
                        holder.imageView.setImageResource(R.drawable.ic_other);
                        break;
                }

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        post_key = getRef(position).getKey();
                        item = model.getItem();
                        amount= model.getAmount();
                        updateData();
                    }
                });


            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrieve_layout, parent, false);
                return new MyViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();
    }

    private void updateData() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View mView = inflater.inflate(R.layout.update_layout, null);

        myDialog.setView(mView);
        final AlertDialog dialog = myDialog.create();

        final TextView mItem = mView.findViewById(R.id.itemName);
        final EditText mAmount = mView.findViewById(R.id.amount);
        final EditText mNotes = mView.findViewById(R.id.note);

        mNotes.setVisibility(View.GONE);

        mItem.setText(item);

        mAmount.setText(String.valueOf(amount));
        mAmount.setSelection(String.valueOf(amount).length());

        Button deleteBtn = mView.findViewById(R.id.btnDelete);
        Button updateBtn = mView.findViewById(R.id.btnUpdate);

        dialog.show();

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                amount = Integer.parseInt(mAmount.getText().toString());
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Calendar cal = Calendar.getInstance();
                String date = dateFormat.format(cal.getTime());

                MutableDateTime epoch = new MutableDateTime();
                epoch.setDate(0);
                DateTime now = new DateTime();
                Weeks weeks = Weeks.weeksBetween(epoch,now);
                Months months = Months.monthsBetween(epoch, now);

                String itemNday = item + date;
                String itemNweek = item + weeks.getWeeks();
                String itemNmonth = item + months.getMonths();


                Data data = new Data(item, date, post_key, itemNday, itemNweek, itemNmonth,  amount, months.getMonths(), weeks.getWeeks(),null);
                budgetReference.child(post_key).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(BudgetActivity.this, "Budget item updated successfuly", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.dismiss();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                budgetReference.child(post_key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(BudgetActivity.this, "Budget item deleted successfuly", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.dismiss();
            }
        });
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public ImageView imageView;
        public TextView notes;

        public MyViewHolder(@NonNull View itemView){
            super(itemView);
            mView = itemView;
            imageView = itemView.findViewById(R.id.imageView);
            notes = itemView.findViewById(R.id.note);

        }

        public void setItemName(String itemName) {
            TextView item = mView.findViewById(R.id.item);
            item.setText(itemName);
        }

        public void setItemAmount(String itemAmount) {
            TextView amount = mView.findViewById(R.id.amount);
            amount.setText(itemAmount);
        }

        public void setItemDate(String itemDate) {
            TextView date = mView.findViewById(R.id.date);
            date.setText(itemDate);
        }

        public void setItemNote(String itemNote) {
            TextView note = mView.findViewById(R.id.note);
            note.setText(itemNote);
        }
    }

    private void getMonthTransportBudgetRatios() {
        setPersonalRefForCategory("Transport");
    }

    private void getMonthFoodBudgetRatios() {
        setPersonalRefForCategory("Food");
    }

    private void getMonthHouseBudgetRatios() {
        setPersonalRefForCategory("House");
    }

    private void getMonthEntertainmentBudgetRatios() {
        setPersonalRefForCategory("Entertainment");
    }

    private void getMonthEducationBudgetRatios() {
        setPersonalRefForCategory("Education");
    }

    private void getMonthCharityBudgetRatios() {
        setPersonalRefForCategory("Charity");
    }

    private void getMonthApparelBudgetRatios() {
        setPersonalRefForCategory("Apparel");
    }

    private void getMonthHealthBudgetRatios() {
        setPersonalRefForCategory("Health");
    }

    private void getMonthPersonalBudgetRatios() {
        setPersonalRefForCategory("Personal");
    }

    private void getMonthOtherBudgetRatios() {
        setPersonalRefForCategory("Other");
    }

    private void setPersonalRefForCategory(String nameOfCategory) {
        Query query = budgetReference.orderByChild("item").equalTo(nameOfCategory);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int dayRatio = 0, weekRatio = 0, monthRatio = 0;
                if(snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds: snapshot.getChildren()){
                        Data data = ds.getValue(Data.class);
                        assert data != null;
                        pTotal += data.getAmount();
                    }
                    dayRatio = pTotal/30;
                    weekRatio = pTotal/4;
                    monthRatio = pTotal;
                }
                personalRef.child("day"+ nameOfCategory +"Ratio").setValue(dayRatio);
                personalRef.child("week"+ nameOfCategory +"Ratio").setValue(weekRatio);
                personalRef.child("month"+ nameOfCategory +"Ratio").setValue(monthRatio);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}