package com.noted.noted.view.fragment

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.view.ActionMode
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
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
import com.noted.noted.model.Note
import com.noted.noted.view.activity.NoteAddActivity
import com.noted.noted.view.bindItem.NoteBinding
import io.realm.Realm

class NotesFragment : BaseFragment(), ItemTouchCallback {

    var actionMode: ActionMode? = null
    private val itemAdapter = ItemAdapter<NoteBinding>()
    private val fastAdapter = FastAdapter.with(itemAdapter)
    private lateinit var mRealm: Realm
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentNotesBinding.inflate(layoutInflater, container, false)


        initAdapter()
        mRealm = Realm.getDefaultInstance()


        val dragCallback = SimpleDragCallback()
        val touchHelper = ItemTouchHelper(dragCallback)
        touchHelper.attachToRecyclerView(binding.notesRecyclerView)

        val layoutManager = GridLayoutManager(this.context, 2)
        binding.notesRecyclerView.layoutManager = layoutManager
        binding.notesRecyclerView.adapter = fastAdapter
        itemAdapter.add(generateItems())





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
                            mainActivity.hideMainSearch(true)
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
                                        mainActivity.hideMainSearch(false)
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
                    Log.e("Noted", "U selected item")
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
                    /* val extras = FragmentNavigatorExtras(item.noteCard to "note_shared_element_container")
                     val note = NotesFragmentDirections.actionNotesFragmentToNoteAddFragment(item.note)
                     findNavController().navigate(note, extras)

                     */

                    val intent = Intent(activity, NoteAddActivity::class.java)

                    val options = ActivityOptions.makeSceneTransitionAnimation(
                        activity,
                        item.noteCard,
                        "note_shared_element_container" // The transition name to be matched in Activity B.
                    )
                    intent.putExtra("title", item.note.title)
                    intent.putExtra("body", item.note.body)
                    intent.putExtra("color", item.note.color)
                    startActivity(intent, options.toBundle())
                }
                return true
            }

        }
    }

    private fun generateItems(): List<NoteBinding> {
        val noteBinding =
            NoteBinding(Note(1234658, "Test note", "Test note", System.currentTimeMillis(), "#EA5455"))
        val noteBinding2 =
            NoteBinding(Note(1234659, "Another test note", "Yup, that's another test note", System.currentTimeMillis(), "#0396FF"))
        val noteBinding3 =
            NoteBinding(Note(12346510, "Test note", "Another test note... \nAGAIN!!", System.currentTimeMillis(), "#F8D800"))
        val noteBinding4 =
            NoteBinding(Note(12346511, "Test note", "You what's up, this is a test note :)", System.currentTimeMillis(), "#7367F0"))
        val noteBinding5 =
            NoteBinding(Note(12346512, "Test note", "Test note", System.currentTimeMillis(), "#F6416C"))
        val noteBinding6 =
            NoteBinding(Note(12346513, "Test note", "Test note", System.currentTimeMillis(), "#28C76F"))
        val noteBinding7 =
            NoteBinding(Note(12346514, "Test note", "Test note", System.currentTimeMillis(), "#9F44D3"))
        return listOf(noteBinding, noteBinding2, noteBinding3, noteBinding4, noteBinding5, noteBinding6, noteBinding7)
    }

    override fun itemTouchDropped(oldPosition: Int, newPosition: Int) {
        itemAdapter.getAdapterItem(newPosition).noteCard.isDragged = false

    }

    override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
        DragDropUtil.onMove(itemAdapter, oldPosition, newPosition)
        itemAdapter.getAdapterItem(oldPosition).noteCard.isDragged = true
        return true
    }

}