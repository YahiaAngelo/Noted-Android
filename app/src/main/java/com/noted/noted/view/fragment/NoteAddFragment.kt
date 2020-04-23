package com.noted.noted.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.noted.noted.databinding.FragmentNoteAddBinding

open class NoteAddFragment(): BaseFragment() {

    lateinit var binding:FragmentNoteAddBinding

    private val args:NoteAddFragmentArgs? by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNoteAddBinding.inflate(inflater, container, false)
        if (args != null){
            binding.noteTitleEditText.setText(args!!.note!!.title)
            binding.noteBodyEditText.setText(args!!.note!!.body)
        }



        return binding.root
    }

}