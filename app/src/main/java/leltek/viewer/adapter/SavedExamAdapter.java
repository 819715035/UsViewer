package leltek.viewer.adapter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import leltek.viewer.model.PatientModel;
import leltek.viewer.R;

/**
 * Created by puma on 9/5/2017.
 */

public class SavedExamAdapter extends BaseAdapter {

    private Context _context;
    private ArrayList<PatientModel> patientModels = new ArrayList<>();

    public SavedExamAdapter(Context _context, ArrayList<PatientModel> patientModels){

        super();
        this._context = _context;
        this.patientModels.clear();
        this.patientModels.addAll(patientModels);
    }

    @Override
    public int getCount() {
        return patientModels.size();
    }

    @Override
    public Object getItem(int i) {
        return patientModels.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        PatientHolder patientHolder;

        if (convertView == null){

            patientHolder = new PatientHolder();

            LayoutInflater inflater = (LayoutInflater)_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_patient, null);

            patientHolder.txvId = (TextView) convertView.findViewById(R.id.txvId);
            patientHolder.txvMRN = (TextView) convertView.findViewById(R.id.txvMRN);
            patientHolder.txvDate = (TextView) convertView.findViewById(R.id.txvDate);
            patientHolder.txvDuration = (TextView) convertView.findViewById(R.id.txvDuration);
            patientHolder.txvDob = (TextView) convertView.findViewById(R.id.txvDOB);
            patientHolder.txvTime = (TextView) convertView.findViewById(R.id.txvTime);
            patientHolder.txvPerformedBy = (TextView) convertView.findViewById(R.id.txvPerformed);
            patientHolder.selected = false;

            convertView.setTag(patientHolder);
        }else {
            patientHolder = (PatientHolder)convertView.getTag();
        }

        final PatientModel pm = patientModels.get(position);

        String id = pm.get_lastName();
        if (pm.get_firstName().length() > 0)
            id += ", " + pm.get_firstName();
        patientHolder.txvId.setText(id);
        patientHolder.txvMRN.setText("MRN: " + pm.get_MRN());
        patientHolder.txvDate.setText("Date: " + pm.get_date());
        if (pm.get_duration() == 0)
            patientHolder.txvDuration.setText("Duration: " + "In Progress");
        else
            patientHolder.txvDuration.setText("Duration: " + pm.get_duration()/1000/60 + " minutes");
        patientHolder.txvDob.setText("DOB: " + pm.get_birthday());
        patientHolder.txvTime.setText("Time: " + pm.get_time());
        patientHolder.txvPerformedBy.setText("Performed By: " + pm.get_performedBy());
        Drawable background = convertView.getBackground();
        patientHolder.background =  ((ColorDrawable)background).getColor();

        return convertView;
    }

    public void refreshView(ArrayList<PatientModel> patientModels) {
        this.patientModels.clear();
        this.patientModels.addAll(patientModels);
        notifyDataSetChanged();
    }

    public class PatientHolder {
        public TextView txvId;
        public TextView txvMRN;
        public TextView txvDate;
        public TextView txvDuration;
        public TextView txvDob;
        public TextView txvTime;
        public TextView txvPerformedBy;
        public boolean selected;
        public int background;
    }
}
