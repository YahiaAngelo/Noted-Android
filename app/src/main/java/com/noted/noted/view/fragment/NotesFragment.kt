package com.noted.noted.view.fragment

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialContainerTransform
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
import com.noted.noted.view.activity.NoteAddActivity
import com.noted.noted.view.bindItem.NoteBinding
import com.noted.noted.viewmodel.NotesFragmentViewModel
import org.parceler.Parcels

class NotesFragment : BaseFragment(), ItemTouchCallback {
    private var searchView : TextInputEditText? = null
    var actionMode: ActionMode? = null
    private val itemAdapter = ItemAdapter<NoteBinding>()
    private val fastAdapter = FastAdapter.with(itemAdapter)
    private var searchTextWatcher : TextWatcher? = null
    private val viewModel: NotesFragmentViewModel by viewModels(factoryProducer = { SavedStateViewModelFactory(requireActivity().application, this)})


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = Hold()
        sharedElementEnterTransition = MaterialContainerTransform()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentNotesBinding.inflate(layoutInflater, container, false)

        initAdapter()

        val dragCallback = SimpleDragCallback()
        val touchHelper = ItemTouchHelper(dragCallback)
        touchHelper.attachToRecyclerView(binding.notesRecyclerView)
        val layoutManager = StaggeredGridLayoutManager(2, 1)
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
                                        item: MenuItem?
                                    ): Boolean {
                                        return when (item?.itemId) {

                                            else -> false
                                        }
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
        }
    }


    private fun update(){

    }


    override fun itemTouchDropped(oldPosition: Int, newPosition: Int) {
        itemAdapter.getAdapterItem(newPosition).noteCard.isDragged = false

    }

    override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
        DragDropUtil.onMove(itemAdapter, oldPosition, newPosition)
        itemAdapter.getAdapterItem(oldPosition).noteCard.isDragged = true
        return true
    }

    override fun onResume() {
        super.onResume()
        update()
    }
    override fun onPause() {
        if (searchView != null && searchTextWatcher != null){
            searchView!!.removeTextChangedListener(searchTextWatcher)
        }
        super.onPause()
    }

    override fun refresh() {
        update()
    }

    override fun filterCategories(categoryName : String) {
        itemAdapter.itemFilter.filterPredicate = {
                item: NoteBinding, constraint: CharSequence? ->
            item.note.categories.any {
                return@any it.title == constraint
            }
        }
        itemAdapter.filter(categoryName)
    }

    override fun filterItem(string: String) {
        itemAdapter.itemFilter.filterPredicate = {
                item: NoteBinding, constraint: CharSequence? ->
            item.noteBody.text.contains(constraint.toString(), true)
        }
        itemAdapter.filter(string)
    }
}