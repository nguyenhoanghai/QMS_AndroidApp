package com.gpro.admin.qmsevaluateonly;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {
    public ArrayList<ServiceModel> listService;
    private Context context;

    public ListAdapter(Context _context,ArrayList<ServiceModel> _listService) {
        this.context = _context;
        this.listService = _listService;
    }

    @Override
    public int getCount() {
        return listService.size();
    }

    @Override
    public Object getItem(int position) {
        return listService.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        final ListViewHolder listViewHolder;
        if(convertView == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.activity_custom_list_view,parent,false);
            listViewHolder = new ListViewHolder();
            listViewHolder.lbName = row.findViewById(R.id.lbName);
            listViewHolder.txtTime = row.findViewById(R.id.txtTime);
            listViewHolder.btPrint = row.findViewById(R.id.btPrint);
            row.setTag(listViewHolder);
        }
        else
        {
            row=convertView;
            listViewHolder= (ListViewHolder) row.getTag();
        }
        final ServiceModel serviceObj = (ServiceModel) getItem(position);

        listViewHolder.lbName.setText(serviceObj.ServiceName);
        listViewHolder.txtTime.setText(serviceObj.TimeServe);
        listViewHolder.btPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //updateQuantity(position,listViewHolder.edTextQuantity,1);
                //Toast.makeText(context,"Id :"+serviceObj.Id +" -> Name :"+serviceObj.ServiceName, Toast.LENGTH_SHORT).show();
                  if(context instanceof PrintTicket_2Activity){
                    ((PrintTicket_2Activity)context).GridButton_Click((serviceObj.Id+""),serviceObj.TimeServe);
                }
                else {
                      Toast.makeText(context,"(ListView_Click) Gửi yêu cầu cấp phiếu thất bại.", Toast.LENGTH_SHORT).show();
                  }
            }
        });
        return row;
    }
}
