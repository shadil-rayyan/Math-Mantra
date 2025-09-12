//package com.zendalona.zmantra.presentation.multiplayer;
//
//import android.content.Context;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentTransaction;
//
//import com.zendalona.zmantra.presentation.features.landing.FragmentNavigation;
//
//public class MultiplayerFragment extends Fragment {
//
//    private FragmentMultiplayerBinding binding;
//    private FragmentNavigation navigationListener;
//
//    @Override
//    public void onAttach(@NonNull Context context) {
//        super.onAttach(context);
//        if (context instanceof FragmentNavigation) {
//            navigationListener = (FragmentNavigation) context;
//        } else {
//            throw new RuntimeException(context.toString() + " must implement FragmentNavigation");
//        }
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        binding = FragmentMultiplayerBinding.inflate(inflater, container, false);
//
//        binding.CreateRoomButton.setOnClickListener(v -> {
//            if (navigationListener != null) {
//                navigationListener.loadFragment(new CreateRoomFragment (), FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//            }
//        });
//
//        binding.RandomPlayerButton.setOnClickListener(v -> {
//            if (navigationListener != null) {
//                navigationListener.loadFragment(new RandomPlayerFragment(), FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//            }
//        });
//        binding.JoinRoomButton.setOnClickListener(v -> {
//            if (navigationListener != null) {
//                navigationListener.loadFragment(new JoinRoomFragment(), FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//            }
//        });
//
//        return binding.getRoot();
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null; // Prevent memory leaks
//    }
//}