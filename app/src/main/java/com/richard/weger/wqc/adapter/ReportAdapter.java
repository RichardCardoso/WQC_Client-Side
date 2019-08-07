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

import java.util.Comparator;
import java.util.List;

import static com.richard.weger.wqc.appconstants.AppConstants.*;

public class ReportAdapter extends ArrayAdapter<Report> {

    private final List<Report> reportList;
    private final Context context;
    private boolean enabled = true;

    private ChangeListener listener;

    public void setChangeListener(ReportAdapter.ChangeListener listener) {
        this.listener = listener;
    }

    public interface ChangeListener {
        void reportListClick(Long id);
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

        reportList.sort(Comparator.comparing(Report::getType).reversed().thenComparing(Report::getId));

        TextView textView = rowView.findViewById(R.id.tvRep);
        CheckBox checkBox = rowView.findViewById(R.id.chkRep);
        ConstraintLayout constraintLayout = rowView.findViewById(R.id.conRep);
        LinearLayout linearLayout = rowView.findViewById(R.id.linRep);

        textView.setEnabled(this.enabled);
        constraintLayout.setEnabled(this.enabled);
        linearLayout.setEnabled(this.enabled);

        final Report report = reportList.get(position);

        textView.setText(report.toString()); //.concat(" - Z").concat(String.valueOf(report.getParent().getNumber()))
        checkBox.setText("");

        checkBox.setChecked(report.isFinished());

        final View.OnClickListener l = v -> listener.reportListClick(report.getId());

        constraintLayout.setOnClickListener(l);
        linearLayout.setOnClickListener(l);
        textView.setOnClickListener(l);

        return rowView;
    }
}
