package com.ctrctr.imgscramble;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class ScrambleFragment extends Fragment {
    String[] OptionList;
    TextView qualitytext;
    SeekBar seekBar;
    EditText rowedittext;
    EditText coledittext;
    LinearLayout seekBarlayout;
    EditText widthedittext;
    EditText heightedittext;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){
        View a = inflater.inflate(R.layout.fragment_scramble, container, false);
        Spinner optionspinner = (Spinner) a.findViewById(R.id.scramble_option_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> optionspinneradapter;
        optionspinneradapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.scramble_options_array,
                R.layout.spinner_textview);
        // Specify the layout to use when the list of choices appears
        optionspinneradapter.setDropDownViewResource(R.layout.spinner_dropdown);
        // Apply the adapter to the spinner
        optionspinner.setAdapter(optionspinneradapter);

        widthedittext = (EditText) a.findViewById(R.id.scramble_image_width);
        heightedittext = (EditText) a.findViewById(R.id.scramble_image_height);
        widthedittext.setEnabled(false);
        heightedittext.setEnabled(false);

        // Init Spinner
        AdapterView.OnItemSelectedListener scramblespinnerlistener = new SpinnerActivity();
        optionspinner.setOnItemSelectedListener(scramblespinnerlistener);


        OptionList = new String[]{getString(R.string.scramble_option_0),
                getString(R.string.scramble_option_1),
                getString(R.string.scramble_option_2),
                getString(R.string.scramble_option_3),
                getString(R.string.scramble_option_4)};

        // Init SeekBar
        qualitytext = (TextView) a.findViewById(R.id.scramble_image_quality_text);
        seekBar = (SeekBar) a.findViewById(R.id.scramble_image_quality_seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBarListener());
        seekBar.setProgress(3);
        qualitytext.setText(String.format(getString(R.string.scramble_option_pre), OptionList[3]));

        // Init Row Col
        rowedittext = (EditText) a.findViewById(R.id.scramble_row);
        coledittext = (EditText) a.findViewById(R.id.scramble_cols);
        coledittext.setVisibility(View.GONE);
        rowedittext.setVisibility(View.GONE);

        // Init SeekBar Layout
        seekBarlayout = (LinearLayout) a.findViewById(R.id.scramble_image_quality_layout);
        seekBarlayout.setVisibility(View.GONE);
        return a;
    }

    /**
     * SpinnerActivity -- Spinner Listener
     */
    class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)
            switch (pos) {
                case 0:
                    //Remove Rows and columns, remove seekBar
                    rowedittext.setVisibility(View.GONE);
                    coledittext.setVisibility(View.GONE);
                    seekBarlayout.setVisibility(View.GONE);
                    return;
                case 1:
                    // Remove Rows and columns
                    rowedittext.setVisibility(View.GONE);
                    coledittext.setVisibility(View.GONE);
                    // Set to visible seekBar
                    seekBarlayout.setVisibility(View.VISIBLE);
                    return;
               case 2:
                    rowedittext.setVisibility(View.VISIBLE);
                    coledittext.setVisibility(View.VISIBLE);
                    seekBarlayout.setVisibility(View.GONE);
                    return;
            }
            return;
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another Interface Callback
        }
    }

    /**
     * SeekBarListener
     */
    class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        int progress = 0;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
            progress = progresValue;
            // Change the Text
            qualitytext.setText(String.format(getString(R.string.scramble_option_pre),
                    OptionList[progress]));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}
