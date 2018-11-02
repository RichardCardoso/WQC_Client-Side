package com.richard.weger.wqc.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.helper.ReportHelper;

import java.util.List;

public class ReportAdapter extends ArrayAdapter<Report> {

    private final List<Report> reportList;
    private final Context context;
    private boolean enabled;

    private ChangeListener listener;

    public void setChangeListener(ReportAdapter.ChangeListener listener) {
        this.listener = listener;
    }

    public interface ChangeListener {
        void reportListClick(Report report, int position);
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    public ReportAdapter(@NonNull Context context, @NonNull List<Report> objects) {
        super(context, R.layout.report_row_layout, objects);
        this.reportList = objects;
        this.context = context;
    }

    @Override
    public View getView(@NonNull final int position, @NonNull View convertView, @NonNull ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.report_row_layout, parent, false);

        TextView textView = rowView.findViewById(R.id.tvRep);
        CheckBox checkBox = rowView.findViewById(R.id.chkRep);
        ConstraintLayout constraintLayout = rowView.findViewById(R.id.conRep);
        LinearLayout linearLayout = rowView.findViewById(R.id.linRep);

        textView.setEnabled(enabled);
        constraintLayout.setEnabled(enabled);
        linearLayout.setEnabled(enabled);

        final Report report = reportList.get(position);

        textView.setText((new ReportHelper()).getReportLabel(report.getReference()));
        checkBox.setText("");

        if (report instanceof ItemReport){
            checkBox.setChecked(((ItemReport) report).getPendingItemsCount() == 0);
        } else if (report instanceof CheckReport){
            int marks = ((CheckReport)report).getMarksCount();
            checkBox.setChecked(marks > 0);
        }

        final View.OnClickListener l = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.reportListClick(report, position);
            }
        };

        constraintLayout.setOnClickListener(l);
        linearLayout.setOnClickListener(l);
        textView.setOnClickListener(l);

        return rowView;
    }
}
