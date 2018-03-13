package mobileapps.aau.at.ab02.listview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import mobileapps.aau.at.ab02.R;

/**
 * Custom adapter for the list view.
 */
public class ListViewAdapter extends ArrayAdapter<String> {

    private final LayoutInflater layoutInflater;
    private final ArrayList<String> values = new ArrayList<>();
    private RowOnClickListener onClickListener;

    public ListViewAdapter(@NonNull Activity context) {
        this(context, new NullRowOnClickListener());
    }

    public ListViewAdapter(@NonNull Activity context, RowOnClickListener onClickListener) {
        super(context, R.layout.listview_layout, R.id.listViewLabel, new ArrayList<String>());

        this.layoutInflater = context.getLayoutInflater();
        setRowOnClickListener(onClickListener);
    }

    public void setRowOnClickListener(RowOnClickListener onClickListener) {
        if (onClickListener == null) {
            throw new IllegalArgumentException("onClickListener is null");
        }

        this.onClickListener = onClickListener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        if (rowView == null) {
            rowView = layoutInflater.inflate(R.layout.listview_layout, parent, false);
        }


        if (rowView.getTag() == null) {
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = rowView.findViewById(R.id.listViewLabel);
            viewHolder.image = rowView.findViewById(R.id.listViewIcon);
            viewHolder.edit = rowView.findViewById(R.id.editText);
            rowView.setTag(viewHolder);
        }

        final ViewHolder viewHolder = (ViewHolder)rowView.getTag();

        final String text = getItem(position);
        if (text == null || text.isEmpty()) {
            viewHolder.editMode = true;
            viewHolder.text.setVisibility(View.INVISIBLE);
            viewHolder.image.setVisibility(View.INVISIBLE);
            viewHolder.edit.setVisibility(View.VISIBLE);
            viewHolder.edit.setFocusableInTouchMode(true);
            viewHolder.edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    String input;


                    if(actionId == EditorInfo.IME_ACTION_DONE)
                    {
                        input= v.getText().toString();
                        if (TextUtils.isEmpty(input.trim())) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setMessage("Input must not be empty.");
                            builder.setCancelable(true);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            builder.create().show();
                        } else {
                            // this is a complete hack, but i guess that's the Android way :)
                            values.set(position, input); // update values in our own backing field
                            setNotifyOnChange(false); // temporarily disable notifications
                            clear(); // remove all old records
                            addAll(values); // add all records
                            setNotifyOnChange(true);
                            notifyDataSetChanged();
                        }
                    }
                    return false;
                }
            });
            if (!viewHolder.edit.hasFocus()) {
                if (viewHolder.edit.requestFocus()) {
                    viewHolder.edit.setCursorVisible(true);
                }
            }

        } else {
            viewHolder.editMode = false;
            viewHolder.text.setVisibility(View.VISIBLE);
            viewHolder.image.setVisibility(View.VISIBLE);
            viewHolder.edit.setVisibility(View.INVISIBLE);
            viewHolder.text.setText(text);
            rowView.setClickable(true);
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String s = viewHolder.text.getText().toString();
                    onClickListener.onClick(s);
                }
            });
        }

        return rowView;
    }

    public void addNewItem() {
        values.add("");
        add(""); // add pseudo element at the end
    }

    private static final class ViewHolder {
        public TextView text;
        EditText edit;
        ImageView image;
        boolean editMode = false;
    }
}
