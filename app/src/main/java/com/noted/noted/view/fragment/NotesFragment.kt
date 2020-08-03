package com.noted.noted.view.fragment

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.chip.Chip
import com.mikepenz.fastadapter.*
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.drag.ItemTouchCallback
import com.mikepenz.fastadapter.drag.SimpleDragCallback
import com.mikepenz.fastadapter.select.getSelectExtension
import com.mikepenz.fastadapter.select.selectExtension
import com.mikepenz.fastadapter.utils.DragDropUtil
import com.noted.noted.MainActivity
import com.noted.noted.R
import com.noted.noted.databinding.FragmentNotesBinding
import com.noted.noted.repositories.NoteRepo
import com.noted.noted.utils.Utils
import com.noted.noted.view.activity.NoteAddActivity
import com.noted.noted.view.bindItem.NoteBinding
import com.noted.noted.viewmodel.NotesFragmentViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.parceler.Parcels

class NotesFragment : BaseFragment(), ItemTouchCallback {
    var actionMode: ActionMode? = null
    private lateinit var binding: FragmentNotesBinding
    private val itemAdapter = ItemAdapter<NoteBinding>()
    private val fastAdapter = FastAdapter.with(itemAdapter)
    private val viewModel: NotesFragmentViewModel by viewModel()
    private val noteRepo: NoteRepo by inject()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotesBinding.inflate(layoutInflater, container, false)

        initAdapter()

        val dragCallback = SimpleDragCallback()
        val touchHelper = ItemTouchHelper(dragCallback)
        touchHelper.attachToRecyclerView(binding.notesRecyclerView)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val gridValue = if (sharedPref!!.getString("notes_grid", "grid") == "grid" )  2 else 1
        val layoutManager = StaggeredGridLayoutManager(gridValue, 1)
        binding.notesRecyclerView.layoutManager = layoutManager
        binding.notesRecyclerView.adapter = fastAdapter

        return binding.root
    }


    private fun initAdapter(){
        val mainActivity = this.activity
        fastAdapter.selectExtension {
            isSelectable = true
            multiSelect = true
            selectOnLongClick = true
            selectionListener = object : ISelectionListener<NoteBinding> {
                override fun onSelectionChanged(item: NoteBinding, selected: Boolean) {

                }
            }
        }
        fastAdapter.onLongClickListener = object : LongClickListener<NoteBinding>{
            override fun invoke(
                v: View,
                adapter: IAdapter<NoteBinding>,
                item: NoteBinding,
                position: Int
            ): Boolean {
                val itemsSize = fastAdapter.getSelectExtension().selectedItems.size
                item.noteCard.isChecked = item.isSelected
                if (mainActivity is MainActivity) {
                    if (itemsSize > 0) {
                        if (actionMode == null) {
                            actionMode =
                                mainActivity.startSupportActionMode(object :
                                    ActionMode.Callback {

                                    override fun onCreateActionMode(
                                        mode: ActionMode?,
                                        menu: Menu?
                                    ): Boolean {
                                        mainActivity.menuInflater.inflate(
                                            R.menu.note_contextual_action_bar,
                                            menu
                                        )
                                        return true
                                    }

                                    override fun onPrepareActionMode(
                                        mode: ActionMode?,
                                        menu: Menu?
                                    ): Boolean {
                                        return false
                                    }

                                    override fun onActionItemClicked(
                                        mode: ActionMode?,
                                        menuItem: MenuItem?
                                    ): Boolean {
                                         when (menuItem?.itemId) {
                                            R.id.delete -> noteRepo.deleteNote(item.note)
                                             R.id.share -> Utils.shareText(requireContext(), "${item.noteTitle.text}\n\n${item.noteBody.text}")

                                        }
                                        mode!!.finish()
                                        return false
                                    }

                                    override fun onDestroyActionMode(mode: ActionMode?) {
                                        for (selectedItem : NoteBinding in fastAdapter.getSelectExtension().selectedItems){
                                            selectedItem.isSelected = false
                                            selectedItem.noteCard.isChecked = false
                                        }
                                        actionMode = null
                                    }
                                })
                            actionMode!!.title = "$itemsSize Items selected"
                        }else{
                            actionMode!!.title = "$itemsSize Items selected"
                        }

                    } else {
                        actionMode!!.finish()
                    }
                }
                return true
            }

        }

        fastAdapter.onClickListener = object :ClickListener<NoteBinding>{
            override fun invoke(
                v: View?,
                adapter: IAdapter<NoteBinding>,
                item: NoteBinding,
                position: Int
            ): Boolean {
                if (actionMode!=null){
                    v!!.performLongClick()
                }else{
                    val intent = Intent(activity, NoteAddActivity::class.java)

                    val options = ActivityOptions.makeSceneTransitionAnimation(
                        activity,
                        item.noteCard,
                        "note_shared_element_container" // The transition name to be matched in Activity B.
                    )

                    intent.putExtra("note", Parcels.wrap(item.note))
                    startActivity(intent, options.toBundle())
                }
                return true
            }

        }

        viewModel.getNotes().observe(viewLifecycleOwner){
            itemAdapter.setNewList(it)
            binding.notesPlaceholder.visibility = if (itemAdapter.adapterItemCount > 0)  View.GONE else View.VISIBLE

        }
    }


    private fun update(){
        itemAdapter.itemFilter.filterPredicate = {
                item: NoteBinding, constraint: CharSequence? ->
            item.noteBody.text.contains(constraint.toString(), true)
        }
        itemAdapter.filter("")
    }


    override fun itemTouchDropped(oldPosition: Int, newPosition: Int) {
        itemAdapter.getAdapterItem(newPosition).noteCard.isDragged = false

    }

    override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
        DragDropUtil.onMove(itemAdapter, oldPosition, newPosition)
        itemAdapter.getAdapterItem(oldPosition).noteCard.isDragged = true
        return true
    }


    override fun refresh() {
        update()
    }

    override fun filterCategories(categoryId : Int) {
        itemAdapter.itemFilter.filterPredicate = {
                item: NoteBinding, constraint: CharSequence? ->
            item.categoriesChipGroup.findViewById<Chip>(Integer.parseInt(constraint.toString())) != null
        }
        itemAdapter.filter(categoryId.toString())
    }

    override fun filterItem(string: String) {
        itemAdapter.itemFilter.filterPredicate = {
                item: NoteBinding, constraint: CharSequence? ->
            item.noteBody.text.contains(constraint.toString(), true)
        }
        itemAdapter.filter(string)
    }
}