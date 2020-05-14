package com.noted.noted.view.fragment

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.appcompat.view.ActionMode
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
import com.noted.noted.model.Note
import com.noted.noted.view.activity.NoteAddActivity
import com.noted.noted.view.bindItem.NoteBinding
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import org.parceler.Parcels

class NotesFragment : BaseFragment(), ItemTouchCallback {
    private var searchView : TextInputEditText? = null
    var actionMode: ActionMode? = null
    private val itemAdapter = ItemAdapter<NoteBinding>()
    private val fastAdapter = FastAdapter.with(itemAdapter)
    private lateinit var mRealm: Realm
    private var searchTextWatcher : TextWatcher? = null


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
        mRealm = Realm.getDefaultInstance()


        val dragCallback = SimpleDragCallback()
        val touchHelper = ItemTouchHelper(dragCallback)
        touchHelper.attachToRecyclerView(binding.notesRecyclerView)
        val layoutManager = StaggeredGridLayoutManager(2, 1)
        binding.notesRecyclerView.layoutManager = layoutManager
        binding.notesRecyclerView.adapter = fastAdapter


        //itemAdapter.add(generateItems())





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
        itemAdapter.itemFilter.filterPredicate = {
                item: NoteBinding, constraint: CharSequence? ->
            item.noteBody.text.contains(constraint.toString(), true)
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

                    intent.putExtra("note", Parcels.wrap(item.note))
                    startActivity(intent, options.toBundle())
                }
                return true
            }

        }

        initSearch()
    }

    private fun initSearch(){
        val activity = this.activity
        if(activity is MainActivity){
             searchView = activity.findViewById(R.id.main_search)
            searchTextWatcher = object : TextWatcher{
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        itemAdapter.filter(s)
                }


            }
            searchView!!.addTextChangedListener(searchTextWatcher)
        }
    }

  /*  private fun generateItems(): List<NoteBinding> {
        val noteBinding =
            NoteBinding(Note(1234658, "Test note", "Test note", System.currentTimeMillis(), R.color.card_blue))
        val noteBinding2 =
            NoteBinding(Note(1234659, "Another test note", "Yup, that's another test note", System.currentTimeMillis(), R.color.card_green))
        val noteBinding3 =
            NoteBinding(Note(12346510, "Test note", "Another test note... \nAGAIN!!", System.currentTimeMillis(), R.color.card_purple))
        val noteBinding4 =
            NoteBinding(Note(12346511, "Test note", "You what's up, this is a test note :)", System.currentTimeMillis(), R.color.card_red))
        val noteBinding5 =
            NoteBinding(Note(12346512, "Test note", "Test note", System.currentTimeMillis(), R.color.card_violet))
        val noteBinding6 =
            NoteBinding(Note(12346513, "Test note", "Test note", System.currentTimeMillis(), R.color.card_yellow))

        return listOf(noteBinding, noteBinding2, noteBinding3, noteBinding4, noteBinding5, noteBinding6)
    }

   */
    private fun update(){
        val notesList = mRealm.where(Note::class.java).sort("date", Sort.DESCENDING).findAll()
            itemAdapter.setNewList(notesList.toBinding())

    }


    private fun RealmResults<Note>.toBinding():List<NoteBinding>{
        val noteBindingList : MutableList<NoteBinding> = mutableListOf()
        for (note in this){
            noteBindingList.add(NoteBinding(note))
        }
        return noteBindingList
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
}