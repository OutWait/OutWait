package elite.kit.outwait.recyclerviewScreens.managmentViewScreen

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import elite.kit.outwait.R
import elite.kit.outwait.databinding.ManagmentViewFragmentBinding
import elite.kit.outwait.recyclerviewSetUp.functionality.SlotAdapter
import elite.kit.outwait.waitingQueue.timeSlotModel.FixedTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.Pause
import elite.kit.outwait.waitingQueue.timeSlotModel.SpontaneousTimeSlot
import elite.kit.outwait.waitingQueue.timeSlotModel.TimeSlot

class managmentViewFragment : Fragment() {


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
        //slotAdapter = SlotAdapter(fakeSlotList())

        //TODO sync with data from repository
//        viewModel.slotList.observe(viewLifecycleOwner, Observer {
//                slotAdapter.slotList=it
//        })
        //slotAdapter.slotList= fakeSlotList()
        return binding.root
    }

    private fun fakeSlotList(): LiveData<MutableList<TimeSlot>> {
        var slotList = MutableLiveData<MutableList<TimeSlot>>()

        for (i in 1..1) {
            slotList.value!!.add(FixedTimeSlot())
        }

        for (i in 1..3) {
            slotList.value!!.add(Pause())
        }

        for (i in 1..1) {
            slotList.value!!.add(FixedTimeSlot())
        }

        for (i in 1..1) {
            slotList.value!!.add(SpontaneousTimeSlot())
        }
        return slotList
    }
}
