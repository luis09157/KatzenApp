import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import com.example.katzen.Model.ProductoModel
import com.example.katzen.R
import com.squareup.picasso.Picasso

class MenuProductosInventarioAdapter(
    context: Context,
    private var productList: List<ProductoModel>
) : ArrayAdapter<ProductoModel>(context, R.layout.producto_venta_view_fragment, productList) {

    private var originalList: List<ProductoModel> = productList.toList()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        val holder: ViewHolder

        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.producto_venta_view_fragment, parent, false)
            holder = ViewHolder()
            holder.imageView = itemView.findViewById(R.id.imagen)
            holder.nombreTextView = itemView.findViewById(R.id.textViewNombre)
            holder.precioTextView = itemView.findViewById(R.id.textViewPrecio)
            itemView.tag = holder
        } else {
            holder = itemView.tag as ViewHolder
        }

        val producto = productList[position]

        holder.nombreTextView?.text = producto.nombre
        holder.precioTextView?.text = "$${producto.precioVenta}"

        // Cargar la imagen del producto utilizando Picasso
        if (producto.rutaImagen.isNotEmpty()) {
            // Si hay una URL de imagen disponible, cargar desde la URL
            Picasso.get()
                .load(producto.rutaImagen)
                .placeholder(R.drawable.ic_imagen)
                .error(R.drawable.ic_imagen)
                .into(holder.imageView)
        } else {
            // Si no hay una URL de imagen, cargar desde el recurso drawable
            holder.imageView?.setImageResource(R.drawable.no_disponible_rosa)
        }

        return itemView!!
    }

    override fun getCount(): Int {
        return productList.size
    }

    override fun getItem(position: Int): ProductoModel? {
        return productList[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = mutableListOf<ProductoModel>()
                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(originalList)
                } else {
                    val filterPattern = constraint.toString().toLowerCase().trim()
                    for (item in originalList) {
                        if (item.nombre.toLowerCase().contains(filterPattern)) {
                            filteredList.add(item)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                productList = results?.values as List<ProductoModel>
                notifyDataSetChanged()
            }
        }
    }

    fun updateList(newList: List<ProductoModel>) {
        productList = newList
        originalList = newList.toList()
        notifyDataSetChanged()
    }

    private class ViewHolder {
        var imageView: ImageView? = null
        var nombreTextView: TextView? = null
        var precioTextView: TextView? = null
    }
}
