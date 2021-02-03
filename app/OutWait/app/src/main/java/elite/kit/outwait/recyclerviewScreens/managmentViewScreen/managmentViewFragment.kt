package elite.kit.outwait.recyclerviewScreens.managmentViewScreen

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import elite.kit.outwait.R
import elite.kit.outwait.databinding.ManagmentViewFragmentBinding
import elite.kit.outwait.recyclerviewSetUp.functionality.SlotAdapter
import elite.kit.outwait.recyclerviewSetUp.functionality.SlotItemTouchHelper
import elite.kit.outwait.waitingQueue.timeSlotModel.*

class managmentViewFragment : Fragment(), ItemActionListener {


    private lateinit var viewModel: ManagmentViewViewModel
    private lateinit var binding: ManagmentViewFragmentBinding
    private lateinit var slotAdapter: SlotAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(ManagmentViewViewModel::class.java)
        binding =DataBindingUtil.inflate(inflater,R.layout.managment_view_fragment, container, false)
        binding.viewModel=this.viewModel
        //RecyclerView SetUp
        binding.slotList.layoutManager = LinearLayoutManager(activity)
        binding.slotList.setHasFixedSize(true)

        //TODO change for slotList
//        viewModel.weatherLocations.observe(viewLifecycleOwner) {
//            weatherOverviewAdapter.updateWeatherLocations(it)
//        }

        //Add listener for recyclerview
        slotAdapter = SlotAdapter(fakeSlotList(),this)
        var callback: ItemTouchHelper.Callback = SlotItemTouchHelper(slotAdapter)
        var itemTouchHelper: ItemTouchHelper = ItemTouchHelper(callback)
        slotAdapter.setTouchHelper(itemTouchHelper)
        itemTouchHelper.attachToRecyclerView(binding.slotList)
        binding.slotList.adapter=slotAdapter


        return binding.root
    }

    private fun fakeSlotList(): MutableList<TimeSlot> {
        var slotList = mutableListOf<TimeSlot>()

        for (i in 1..1) {
            slotList!!.add(FixedTimeSlot(21,"4321", "Müller", 601))
        }

        for (i in 1..3) {
            slotList!!.add(Pause(33))
        }

        for (i in 1..1) {
            slotList!!.add(FixedTimeSlot(21,"4321", "Müller", 601))
        }

        for (i in 1..3) {
            slotList!!.add(SpontaneousTimeSlot(11, "33$i", "Frank"))
        }
        return slotList
    }

    override fun onItemClicked(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onItemSwiped(position: Int, removedSlot: TimeSlot) {

        Snackbar.make(binding.slotList, "${getIdentifier(removedSlot)}", Snackbar.LENGTH_LONG).setAction(getString(
                    R.string.undo)) {
           slotAdapter.slotList.add(position,removedSlot)
            slotAdapter.notifyItemInserted(position)
            slotAdapter.notifyItemRangeChanged(0, slotAdapter.slotList.size -1)

        }.show()
    }

    private fun getIdentifier(slot:TimeSlot): String{
        //Guarantee slot is only fixed or spo by GUI
        return when(slot.getType()){
            Type.SPONTANEOUS_SLOT.value-> (slot as SpontaneousTimeSlot).auxiliaryIdentifier
            else -> (slot as FixedTimeSlot).auxiliaryIdentifier
        }
    }

    override fun editTimeSlot(position: Int) {
        TODO("Not yet implemented")
    }
}
