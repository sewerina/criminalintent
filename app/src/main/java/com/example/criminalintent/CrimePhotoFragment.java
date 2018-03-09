package com.example.criminalintent;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class CrimePhotoFragment extends DialogFragment {
    private Bitmap bitmap;
    private ImageView mBigCrimePhoto;

    public static CrimePhotoFragment newInstance(Bitmap bm) {
        CrimePhotoFragment fragment = new CrimePhotoFragment();
        fragment.bitmap = bm;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_photo, null);
        mBigCrimePhoto = v.findViewById(R.id.big_crime_photo);
        mBigCrimePhoto.setImageBitmap(bitmap);

        return new AlertDialog.Builder(getActivity()).
                setView(v).
                create();
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        Dialog dialog = getDialog();
//        if (dialog != null) {
//            int width = ViewGroup.LayoutParams.MATCH_PARENT;
//            int height = ViewGroup.LayoutParams.MATCH_PARENT;
//            dialog.getWindow().setLayout(width, height);
//        }
//    }

}
