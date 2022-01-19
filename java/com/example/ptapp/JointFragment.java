package com.example.ptapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.ptapp.Joints.Joints;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JointFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JointFragment extends DialogFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private OnJointInteractionListener mListener;
    private Context getContext;
    EditText location, typejoint;
    ImageButton jointImage;
    Button submit;
    private Uri mSelectedImage = null;

    Dialog mDialog;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private LatLng mParam2;

    public JointFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment JointFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static JointFragment newInstance(String param1, LatLng param2) {
        JointFragment fragment = new JointFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putParcelable(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getParcelable(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_joint, container, false);
        location = mView.findViewById(R.id.joint_location);
        typejoint = mView.findViewById(R.id.joint_type);
        jointImage = mView.findViewById(R.id.joint_image);
        submit = mView.findViewById(R.id.joint_save);

        jointImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(getContext);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSubmit();
            }
        });
        return mView;

    }

    public interface OnJointInteractionListener {
        // TODO: Update argument type and name
        void onJointInteraction(Joints joints);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof OnJointInteractionListener) {
            mListener = (OnJointInteractionListener) context;
            this.getContext = context;
        }else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    private void selectImage(Context context) {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, (dialog, item) -> {

            if (options[item].equals("Take Photo")) {
                Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 0);

            } else if (options[item].equals("Choose from Gallery")) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);

            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_CANCELED) {
            mSelectedImage = null;
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        mSelectedImage = data.getData();
//                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
//                        imageView.setImageBitmap(bitmap);
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        mSelectedImage =  data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    }
                    break;
            }
        }
    }

    public String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        String time = sdf.format(new Date());
        return time;
    }


    public void startSubmit(){
        if(location.getText().toString().isEmpty() && typejoint.getText().toString().isEmpty()){
            return;
        }
        String location = this.location.getText().toString();
        String type = typejoint.getText().toString();


        Joints joints = new Joints();
        joints.feeder = mParam1;
        joints.location = location;
        joints.latitude = mParam2.latitude;
        joints.longitude = mParam2.longitude;
        joints.type = type;
        joints.time = getTime();

        DatabaseReference mref = FirebaseDatabase.getInstance().getReference();
        String databaseRef = mref.child("Joints").child(mParam1).push().getKey();
        joints.Imageid = databaseRef;
        String imageref = databaseRef;
        DatabaseReference upload = mref.child("Joints").child(mParam1);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(databaseRef, joints );

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference("images/" + mParam1 + "/" + imageref);


        upload.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if(mSelectedImage != null) {
                    storageReference.putFile(mSelectedImage).addOnSuccessListener(taskSnapshot -> Toast.makeText(getContext, "Image Successfully Uploaded", Toast.LENGTH_SHORT).show());
                }
                mListener.onJointInteraction(joints);
                getDialog().dismiss();
            }
        });



    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }


}