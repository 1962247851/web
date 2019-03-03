package jn.mjz.web.DateBase.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;

import jn.mjz.web.R;

public class ListViewAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater;
    private String[] stringName;
    private IOnItemClickListener mIOnItemClickListener;
    private IOnItemLongClickListener mIOnItemLongClickListener;

    public ListViewAdapter(Context context, String[] stringsName, IOnItemClickListener iOnItemClickListener, IOnItemLongClickListener iOnItemLongClickListener) {
        this.stringName = stringsName;
        this.mIOnItemClickListener = iOnItemClickListener;
        this.mIOnItemLongClickListener = iOnItemLongClickListener;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (stringName != null) {
            return stringName.length;
        } else {
            return 1;
        }
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class MyViewHolder {
        private TextView mTv;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        MyViewHolder myViewHolder;
        if (convertView == null) {
            myViewHolder = new MyViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.layout_list_view_item, null);
            myViewHolder.mTv = convertView.findViewById(R.id.tv_layout_list_item);
            convertView.setTag(myViewHolder);
        } else {
            myViewHolder = (MyViewHolder) convertView.getTag();
        }
        //
        if (stringName != null) {
            myViewHolder.mTv.setText(stringName[position]);
            myViewHolder.mTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mIOnItemClickListener.onItemClick(position);
                }
            });
            myViewHolder.mTv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mIOnItemLongClickListener.onItemLongClick(position);
                    return true;
                }
            });
        }
        return convertView;
    }

    public interface IOnItemClickListener {
        void onItemClick(int index);
    }

    public interface IOnItemLongClickListener {
        void onItemLongClick(int index);
    }
}