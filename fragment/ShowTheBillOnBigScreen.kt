package com.yucox.splitwise.fragment


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.yucox.splitwise.R


class ShowTheBillOnBigScreen : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view =  inflater.inflate(R.layout.show_the_bill_on_big_screen_fragment, container, false)
        var imageofBill = arguments?.getString("imageofbill")
        var showtheBill = view.findViewById<ImageView>(R.id.imageofBill_showthebill)

        Glide.with(requireContext()).load(imageofBill).into(showtheBill)

        return view
    }
}