package com.example.cyclesearch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cyclesearch.databinding.FragmentWekaBinding;


public class WekaFragment extends Fragment {

    private FragmentWekaBinding binding;
    private final String TAG = "[SYSTEM]: ";


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentWekaBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //Go back button
        binding.button3.setOnClickListener
                (v -> NavHostFragment.findNavController(WekaFragment.this)
                .navigate(R.id.walkingButton));

        //Reset Button
        binding.button4.setOnClickListener
                (v -> System.out.println(TAG + "The measurement was reset"));

        //Start button
        binding.startButton.setOnClickListener
                (v -> System.out.println(TAG+ "The measurement started"));

        //Done button
        binding.doneButton.setOnClickListener
                (v -> System.out.println(TAG + "The measurement ended"));

     //Radio group
        RadioGroup radioGroup =((RadioGroup) view.findViewById(R.id.radioGroup));
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.bikingButton){
                    System.out.println("You are now biking");
                }
                else if(checkedId == R.id.walkingButton){
                    System.out.println("You are now walking");
                }
                else if(checkedId == R.id.standingButton){
                    System.out.println("You are standing");
                }
                else if(checkedId == R.id.sittingButton){
                    System.out.println("You are now sitting");
                }

            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void onRadioButtonClick(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.bikingButton:
                if(checked)
                    //Biking
                    System.out.println(TAG + "The biking button was selected");
                    break;

            case R.id.walkingButton:
                if(checked)
                    //Biking
                    break;

            case R.id.standingButton:
                if(checked)
                    //Biking
                    break;

            case R.id.sittingButton:
                if(checked)
                    //Biking
                    break;
        }
    }


}