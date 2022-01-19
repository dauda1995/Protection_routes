package com.example.ptapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class LayerDialog extends DialogFragment {

    private String[] arraylist;
    private String TAG;
    private OnLayerInteractionListener mListener;

    private Context getContext;
    public LayerDialog(String[] arrayId, String TAG) {
        this.arraylist = arrayId;
        this.TAG = TAG;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layer_dialog, container, false);
        final ListView lstView = (ListView) view.findViewById(R.id.listView);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_expandable_list_item_1, arraylist);
        lstView.setAdapter(adapter);

        lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String select = arraylist[position];
                mListener.onLayerInteraction(select, TAG);

                getDialog().dismiss();
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnLayerInteractionListener) {
            mListener = (OnLayerInteractionListener) context;
            this.getContext = context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public interface OnLayerInteractionListener {
        // TODO: Update argument type and name
        void onLayerInteraction(String resID, String TAGE);
    }

}
