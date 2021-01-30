package elite.kit.outwait.recyclerviewSetUp.viewHolder

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<T>(private  var itemView: View):RecyclerView.ViewHolder(itemView) {
    abstract  fun bind(item:T)
}
