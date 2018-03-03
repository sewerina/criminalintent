package com.example.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.util.Date;
import java.util.UUID;

import static android.support.v4.content.ContextCompat.checkSelfPermission;

public class CrimeFragment extends Fragment {

    public static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String TAG = "CrimeFragment";
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mSuspectButton;
    private Button mCallButton;
    private Button mReportButton;
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mCrime = new Crime();

        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        Log.d(TAG, "onCreate: call with crimeId = " + crimeId);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        returnResult();
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        Log.d(TAG, "onCreateView: call with crimeId = " + mCrime.getId());
        mTitleField = v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        TextWatcher titleFieldListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        mTitleField.addTextChangedListener(titleFieldListener);

        mDateButton = v.findViewById(R.id.crime_date);
        updateDate();
//        mDateButton.setEnabled(false);

        View.OnClickListener listenerDateButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(fragmentManager, DIALOG_DATE);
            }
        };
        mDateButton.setOnClickListener(listenerDateButton);


        mSolvedCheckBox = v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        CompoundButton.OnCheckedChangeListener solvedCheckBoxListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        };
        mSolvedCheckBox.setOnCheckedChangeListener(solvedCheckBoxListener);

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//        pickContact.addCategory(Intent.CATEGORY_HOME);
        mSuspectButton = v.findViewById(R.id.crime_suspect);
        View.OnClickListener listenerSuspectButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        };
        mSuspectButton.setOnClickListener(listenerSuspectButton);

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }


        mCallButton = v.findViewById(R.id.crime_call);
        View.OnClickListener listenerCallButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
                }
                    String contactNumber = null;

                    Cursor cursorPhone = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                                    ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                            new String[]{mCrime.getSuspectId()},
                            null);

                    if (cursorPhone.moveToFirst()) {
                        contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }

                    cursorPhone.close();

                    Uri number = Uri.parse("tel:" + contactNumber);

                    final Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                    startActivity(callIntent);
            }
        };
        mCallButton.setOnClickListener(listenerCallButton);

        mReportButton = v.findViewById(R.id.crime_report);
        View.OnClickListener listenerReportButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Create intent - var2
                Intent i = ShareCompat.IntentBuilder.
                        from((Activity) v.getContext()).
                        setSubject(getString(R.string.crime_report_subject)).
                        setType("text/plain").
                        setText(getCrimeReport()).
                        getIntent();

                i = Intent.createChooser(i, getString(R.string.send_report));

//                Create intent - var1
//                Intent i = new Intent(Intent.ACTION_SEND);
//                i.setType("text/plain");
//                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
//                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
//                i = Intent.createChooser(i, getString(R.string.send_report));

                startActivity(i);
            }
        };
        mReportButton.setOnClickListener(listenerReportButton);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_crime:
                CrimeLab crimeLab = CrimeLab.get(getActivity());
                crimeLab.deleteCrime(mCrime);
                getActivity().finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();

            String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID};

            Cursor c = getActivity().getContentResolver().query(contactUri, queryFields, null, null, null);

            try {
                if (c.getCount() == 0) {
                    return;
                }
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                String suspectId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                mCrime.setSuspectId(suspectId);
                mSuspectButton.setText(suspect + "#" + suspectId);
            } finally {
                c.close();
            }
        }
    }

    public void returnResult() {
        Intent data = new Intent();
        data.putExtra(ARG_CRIME_ID, mCrime.getId());
        getActivity().setResult(Activity.RESULT_OK, data);
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

}
